/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import java.util.Arrays;
import java.util.LinkedList;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkField;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.packet.ARP;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.util.MACAddress;
import net.onrc.openvirtex.util.OVXUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.*;

public class OVXPacketIn implements Virtualizable {

	private final Logger log      = LogManager.getLogger(
			OVXPacketIn.class.getName());
	
	private PhysicalPort port     = null;
	
	private OVXPort		 ovxPort  = null;
	
	private Integer      tenantId = null;
	
	private OFPacketIn   pi       = null;
	
	@Override
	public void virtualize(final PhysicalSwitch sw) {
		OVXSwitch vSwitch = OVXMessageUtil.untranslateXid(this.pi, sw);
		
		/*
		 * Fetching port from the physical switch
		 */
		port = sw.getPort(this.pi.getInPort().getPortNumber());
		Mappable map = sw.getMap();
		
		//match.loadFromPacket(this.pi.getData(), inport);
		Match match = OVXUtil.loadMatchFromEthPacket(this.pi.getData(), 
				port.getPortNumber(), this.pi.getVersion());
		
		/*
		 * Check whether this packet arrived on
		 * an edge port.
		 * 
		 * if it did we do not need to rewrite anything,
		 * but just find which controller this should be
		 * send to.
		 */
		if (this.port.isEdge()) {
			this.log.warn("packet is on edge");
			this.tenantId = this.fetchTenantId(match, map, true);
			if (this.tenantId == null) {
				this.log.warn(
						"PacketIn {} does not belong to any virtual network; "
								+ "dropping and installing a temporary drop rule",
								this.pi);
				this.installDropRule(sw, match);
				return;
			}
			
			/*
			 * Checks on vSwitch and the virtual port done in sendPkt.
			 */
			vSwitch = this.fetchOVXSwitch(sw, vSwitch, map);
			this.ovxPort = this.port.getOVXPort(this.tenantId, 0);
			this.sendPkt(vSwitch, match, sw);
			this.learnHostIP(match, map);
			this.learnAddresses(match, map);
			this.log.debug("Edge PacketIn {} sent to virtual network {}", this.pi,
					this.tenantId);
			return;
		}

		/*
		 * Below handles packets traveling in the core.
		 * 
		 * 
		 * The idea here is to rewrite the packets such that the controller is able to recognize them.
		 * 
		 * For IPv4 packets and ARP packets this means rewriting the IP fields and possibly the mac
		 * address fields if these packets are at the egress point of a virtual link.
		 * 
		 */

		if (match.get(MatchField.ETH_TYPE) == EthType.IPv4 
				|| match.get(MatchField.ETH_TYPE) == EthType.ARP)  {
			PhysicalIPAddress srcIP = null;
			PhysicalIPAddress dstIP = null;
			
			if (match.get(MatchField.ETH_TYPE) == EthType.IPv4) {
				srcIP = new PhysicalIPAddress(match.get(MatchField.IPV4_SRC).toString());
				dstIP = new PhysicalIPAddress(match.get(MatchField.IPV4_DST).toString());
			} else if (match.get(MatchField.ETH_TYPE) == EthType.ARP) {
				srcIP = new PhysicalIPAddress(match.get(MatchField.ARP_SPA).toString());
				dstIP = new PhysicalIPAddress(match.get(MatchField.ARP_TPA).toString());
			}

			Ethernet eth = new Ethernet();
			eth.deserialize(this.pi.getData(), 0, this.pi.getData().length);

			OVXLinkUtils lUtils = new OVXLinkUtils(eth.getSourceMAC(), eth.getDestinationMAC());
			//rewrite the Match with the values of the link
			if (lUtils.isValid()) {
				OVXPort srcPort = port.getOVXPort(lUtils.getTenantId(), lUtils.getLinkId());
				if (srcPort == null) {
					this.log.error("Virtual Src Port Unknown: {}, port {} with this match {}; dropping packet", 
							sw.getName(), port.getPortNumber(), match);
					return;
				}
				this.setInPort(srcPort.getPortNumber());
				
				OVXLink link;
				try {
					OVXPort dstPort = map.getVirtualNetwork(lUtils.getTenantId())
							.getNeighborPort(srcPort);
					
					link = map.getVirtualSwitch(sw, lUtils.getTenantId())
							.getMap()
							.getVirtualNetwork(lUtils.getTenantId())
							.getLink(dstPort, srcPort);
					
				} catch (SwitchMappingException | NetworkMappingException e) {
					return; //same as (link == null)
				}
				
				this.ovxPort = this.port.getOVXPort(lUtils.getTenantId(), link.getLinkId());
				
				OVXLinkField linkField = OpenVirteXController.getInstance().getOvxLinkField();
				//TODO: Need to check that the values in linkId and flowId don't exceed their space
				if (linkField == OVXLinkField.MAC_ADDRESS) {
					try {    
						LinkedList<MACAddress> macList = sw.getMap()
								.getVirtualNetwork(this.ovxPort.getTenantId())
								.getFlowManager()
								.getFlowValues(lUtils.getFlowId());
						
						eth.setSourceMACAddress(macList.get(0).toBytes())
						.setDestinationMACAddress(macList.get(1).toBytes());
						
						match = match.createBuilder()
								.setExact(MatchField.ETH_SRC, 
										MacAddress.of(eth.getSourceMACAddress()))
								.setExact(MatchField.ETH_DST, 
										MacAddress.of(eth.getDestinationMACAddress()))
								.build();
					} catch (NetworkMappingException e) {
						log.warn(e);    
					}
				}
				else if (linkField == OVXLinkField.VLAN) {
					// TODO
					log.warn("VLAN virtual links not yet implemented.");
					return;
				}
			}

			if (match.get(MatchField.ETH_TYPE) == EthType.ARP) {
				// ARP packet
				final ARP arp = (ARP) eth.getPayload();
				this.tenantId = this.fetchTenantId(match, map, true);
				try {
					if (map.hasVirtualIP(srcIP)) {
						arp.setSenderProtocolAddress(map.getVirtualIP(srcIP).getIp());
					}
					if (map.hasVirtualIP(dstIP)) {
						arp.setTargetProtocolAddress(map.getVirtualIP(dstIP).getIp());
					}
				} catch (AddressMappingException e) {
					log.warn("Inconsistency in OVXMap? : {}", e);   
				}
			} else if (match.get(MatchField.ETH_TYPE) == EthType.IPv4) {
				try {
					final IPv4 ip = (IPv4) eth.getPayload();
					if (map.hasVirtualIP(dstIP)) 
						ip.setDestinationAddress(map.getVirtualIP(dstIP).getIp());
					if (map.hasVirtualIP(srcIP)) 
						ip.setSourceAddress(map.getVirtualIP(srcIP).getIp());
					
					// TODO: Incorporate below into fetchTenantId
					if (this.tenantId == null)
						this.tenantId = dstIP.getTenantId();
				} catch (AddressMappingException e) {
					log.warn("Could not rewrite IP fields : {}", e);
				}
			} else {
				this.log.info("{} handling not yet implemented; dropping", 
							  match.get(MatchField.ETH_TYPE).toString());
				this.installDropRule(sw, match);
				return;
			}
			
			this.pi = this.pi.createBuilder()
					.setData(eth.serialize())
					.build();
			
			vSwitch = this.fetchOVXSwitch(sw, vSwitch, map);
			
			this.sendPkt(vSwitch, match, sw);
			this.log.debug("IPv4 or ARP PacketIn {} sent to virtual network {}", this.pi,
					this.tenantId);
			return;
		}

		this.tenantId = this.fetchTenantId(match, map, true);
		if (this.tenantId == null) {
			this.log.warn(
					"PacketIn {} does not belong to any virtual network; "
							+ "dropping and installing a temporary drop rule",
							this.pi);
			this.installDropRule(sw, match);
			return;
		}
		vSwitch = this.fetchOVXSwitch(sw, vSwitch, map);
		this.sendPkt(vSwitch, match, sw);
		this.log.debug("Layer2 PacketIn {} sent to virtual network {}", this.pi,
				this.tenantId);
	}

	private void learnHostIP(Match match, Mappable map)  {
		int ip_addr = 0;
		if (match.isExact(MatchField.IPV4_SRC)) {	
			ip_addr = match.get(MatchField.IPV4_SRC).getInt();
		} else if (match.isExact(MatchField.ARP_SPA)) {
			ip_addr = match.get(MatchField.ARP_SPA).getInt();
		}

		if (ip_addr != 0) {
			try {
				map.getVirtualNetwork(tenantId)
				.getHost(ovxPort)
				.setIPAddress(ip_addr);
			} catch (NetworkMappingException e) {
				log.warn("Failed to lookup virtual network {}", this.tenantId);
				return;
			} catch (NullPointerException npe) {
				log.warn("No host attached at {} port {}", 
						this.ovxPort.getParentSwitch().getSwitchName(), 
						this.ovxPort.getPhysicalPortNumber());
			}
		}
	}

	private void sendPkt(final OVXSwitch vSwitch, final Match match,
			final PhysicalSwitch sw) {
		if (vSwitch == null || !vSwitch.isActive()) {
			this.log.warn(
					"Controller for virtual network {} has not yet connected "
							+ "or is down", this.tenantId);
			this.installDropRule(sw, match);
			return;
		}
		this.pi = this.pi.createBuilder()
				.setBufferId(OFBufferId.of(vSwitch.addToBufferMap(this)))
				.build();
		
		if (this.port != null && this.ovxPort != null && this.ovxPort.isActive()) {
			this.setInPort(this.ovxPort.getPortNumber());
			if ((this.pi.getData() != null) && 
					(vSwitch.getMissSendLen() != OVXSetConfig.MSL_FULL)) {
				this.pi = this.pi.createBuilder()
						.setTotalLen(this.pi.getData().length)
						.setData(Arrays.copyOf(this.pi.getData(), vSwitch.getMissSendLen()))
						.build();
				//this.setLengthU(OFPacketIn.MINIMUM_LENGTH + this.packetData.length);
			}
			vSwitch.sendMsg(this.pi, sw);
		} else if (this.port == null) {
			log.error("The port {} doesn't belong to the physical switch {}", 
					this.pi.getInPort(), sw.getName());
		} else if (this.ovxPort == null || !this.ovxPort.isActive()) {
			log.error("Virtual port associated to physical port {} in physical switch {} for "
					+ "virtual network {} is not defined or inactive", 
					this.pi.getInPort(), sw.getName(), this.tenantId);
		} 
	}

	private void learnAddresses(final Match match, final Mappable map) {
		if (match.get(MatchField.ETH_TYPE) == EthType.IPv4) {
			if (match.isExact(MatchField.IPV4_SRC))
				IPMapper.getPhysicalIp(this.tenantId, match.get(MatchField.IPV4_SRC).getInt());
			if (match.isExact(MatchField.IPV4_DST))
				IPMapper.getPhysicalIp(this.tenantId, match.get(MatchField.IPV4_DST).getInt());
		} else if (match.get(MatchField.ETH_TYPE) == EthType.ARP) {
			if (match.isExact(MatchField.ARP_SPA))
				IPMapper.getPhysicalIp(this.tenantId, match.get(MatchField.ARP_SPA).getInt());
			if (match.isExact(MatchField.ARP_TPA))
				IPMapper.getPhysicalIp(this.tenantId, match.get(MatchField.ARP_TPA).getInt());
		}
	}

	private void installDropRule(final PhysicalSwitch sw, final Match match) {
		final OVXFlowMod fm = new OVXFlowMod(match.getVersion());
		
		fm.setMatch(match)
		.setBufferId(this.getBufferId())
		.setHardTimeout(1);
		
		sw.sendMsg(fm.getFlow(), sw);
	}

	private Integer fetchTenantId(final Match match, final Mappable map, final boolean useMAC) {
		MACAddress mac =  MACAddress.valueOf(match.get(MatchField.ETH_SRC).getBytes());
		if (useMAC && map.hasMAC(mac)) {
			try {
				return map.getMAC(mac);
			} catch (AddressMappingException e) {
				log.warn("Tried to return non-mapped MAC address : {}", e);
			}
		}
		return null;
	}

	private OVXSwitch fetchOVXSwitch(PhysicalSwitch psw, OVXSwitch vswitch, Mappable map) {
		if (vswitch == null) {
			try {
				vswitch = map.getVirtualSwitch(psw, this.tenantId);
			} catch (SwitchMappingException e) {
				log.warn("Cannot fetch non-mapped OVXSwitch: {}", e);
			}
		}
		return vswitch;
	}

	public OVXPacketIn(final OVXPacketIn pktIn) {
		this.pi = OFFactories.getFactory(pktIn.getPacket().getVersion())
				.buildPacketIn()
				.setBufferId(pktIn.getBufferId())
				.setInPort(pktIn.getInPort())
				.setTotalLen(pktIn.getTotalLen())
				.setData(pktIn.getData())
				.setReason(pktIn.getReason())
				.setXid(pktIn.getXid())
				.build();
	}	

	public OVXPacketIn(OFMessage m) {
		this.pi = (OFPacketIn) m;
	}

	public OVXPacketIn (final byte[] data, final int portNumber, OFVersion ofversion) {
		this.pi = OFFactories.getFactory(ofversion)
				.buildPacketIn()
				.setInPort(OFPort.of(portNumber))
				.setBufferId(OFBufferId.NO_BUFFER)
				.setReason(OFPacketInReason.NO_MATCH)
				.setData(data)
				.setTotalLen(data.length)
				.build();
	}
	
	public OVXPacketIn(OFVersion ofversion) {
		this.pi = OFFactories.getFactory(ofversion)
				.buildPacketIn()
				.setBufferId(OFBufferId.NO_BUFFER)
				.setReason(OFPacketInReason.NO_MATCH)
				.build();
	}
	     
	private long getXid() {
		return this.pi.getXid();
	}
	
	private int getTotalLen() {
		return this.pi.getTotalLen();
	}

	public byte[] getData() {
		return this.pi.getData();
	}

	public OFBufferId getBufferId() {
		return this.pi.getBufferId();
	}
	
	public OFPacketInReason getReason() {
		return this.pi.getReason();
	}

	public OFPort getInPort() {
		return this.pi.getInPort();
	}

	public OFPacketIn getPacket() {
		return this.pi;
	}
	
	public void setInPort(Integer portNumber) {
		this.pi = this.pi.createBuilder()
				.setInPort(OFPort.of((portNumber)))
				.build();
	}

	public void setBufferId(OFBufferId bufferId) {
		this.pi = this.pi.createBuilder()
				.setBufferId(bufferId)
				.build();
	}

	public void setReason(OFPacketInReason reason) {
		this.pi = this.pi.createBuilder()
				.setReason(reason)
				.build();
	} 
	
	public void setData(byte[] data) {
		this.pi = this.pi.createBuilder()
				.setData(data)
				.build();
	}
}
