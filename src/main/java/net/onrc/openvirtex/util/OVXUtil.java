/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.util;

import java.nio.ByteBuffer;
import java.util.HashMap;

import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.exceptions.UnknownActionException;
import net.onrc.openvirtex.packet.ARP;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.packet.ICMP;
import net.onrc.openvirtex.packet.IPacket;
import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.packet.TCP;
import net.onrc.openvirtex.packet.UDP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanPcp;

public class OVXUtil {
    static Logger log = LogManager.getLogger(OVXUtil.class.getName());
	public static int NUMBITSNEEDED(int x) {
		int counter = 0;
		while (x != 0) {
			x >>= 1;
			counter++;
		}
		return counter;
	}
	
	public static HashMap<String, Object> actionToMap(OFAction act) throws UnknownActionException {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		
		switch (act.getType()) {
		case OUTPUT:
			OFActionOutput out = (OFActionOutput) act;
			ret.put("type", "OUTPUT");
			ret.put("port", out.getPort());
			break;
		case SET_DL_DST:
			OFActionSetDlDst dldst = (OFActionSetDlDst) act;
			ret.put("type", "DL_DST");
			ret.put("dl_dst", new MACAddress(dldst.getDlAddr().getBytes()).toString());
			break;
		case SET_DL_SRC:
			OFActionSetDlSrc dlsrc = (OFActionSetDlSrc) act;
			ret.put("type", "DL_SRC");
			ret.put("dl_src", new MACAddress(dlsrc.getDlAddr().getBytes()).toString());
			break;
		case SET_NW_DST:
			OFActionSetNwDst nwdst = (OFActionSetNwDst) act;
			ret.put("type", "NW_DST");
			ret.put("nw_dst", new PhysicalIPAddress(nwdst.getNwAddr().toString()).toSimpleString());
			break;
		case SET_NW_SRC:
			OFActionSetNwSrc nwsrc = (OFActionSetNwSrc) act;
			ret.put("type", "NW_SRC");
			ret.put("nw_src", new PhysicalIPAddress(nwsrc.getNwAddr().toString()).toSimpleString());
			break;
		case SET_NW_TOS:
			OFActionSetNwTos nwtos = (OFActionSetNwTos) act;
			ret.put("type", "NW_TOS");
			ret.put("nw_tos", nwtos.getNwTos());
			break;
		case SET_TP_DST:
			OFActionSetTpDst tpdst = (OFActionSetTpDst) act;
			ret.put("type", "TP_DST");
			ret.put("tp_dst", tpdst.getTpPort());
			break;
		case SET_TP_SRC:
			OFActionSetTpSrc tpsrc = (OFActionSetTpSrc) act;
			ret.put("type", "TP_SRC");
			ret.put("tp_src", tpsrc.getTpPort());
			break;
		case SET_VLAN_VID:
			OFActionSetVlanVid vlan = (OFActionSetVlanVid) act;
			ret.put("type", "SET_VLAN");
			ret.put("vlan_id", vlan.getVlanVid());
			break;
		case SET_VLAN_PCP:
			OFActionSetVlanPcp pcp = (OFActionSetVlanPcp) act;
			ret.put("type", "SET_VLAN_PCP");
			ret.put("vlan_pcp", pcp.getVlanPcp());
			break;
		case STRIP_VLAN:
			ret.put("type", "STRIP_VLAN");
			break;
		case ENQUEUE:
			OFActionEnqueue enq = (OFActionEnqueue) act;
			ret.put("type", "ENQUEUE");
			ret.put("queue", enq.getQueueId());
			break;
		case EXPERIMENTER:
			ret.put("type", "VENDOR");
			break;	
		default:
			throw new UnknownActionException("Action " + act.getType() + " is unknown.");
				
		}

		return ret;
	}
	
	public static Match loadMatchFromEthPacket(final byte[] packetData, final int inputPort, final OFVersion ofversion) {
		Match.Builder mb = OFFactories.getFactory(ofversion).buildMatch();
		if (inputPort == OFPort.ANY.getPortNumber()) {
			log.warn("wildcarding port {}", inputPort);
			mb.wildcard(MatchField.IN_PORT);
		} else { 
			mb.setExact(MatchField.IN_PORT, OFPort.of(inputPort));
		}
			
		Ethernet eth = new Ethernet();
		eth.deserialize(packetData, 0, packetData.length);

		// dl type
		short dataLayerType = eth.getEtherType();
		mb.setExact(MatchField.ETH_TYPE, EthType.of(dataLayerType));
		
		mb.setExact(MatchField.ETH_DST, MacAddress.of(eth.getDestinationMACAddress()));
		
		mb.setExact(MatchField.ETH_SRC, MacAddress.of(eth.getSourceMACAddress()));

		
		if (dataLayerType != (short) 0x8100) { // need cast to avoid signed bug
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.NO_MASK);
			mb.setExact(MatchField.VLAN_PCP, VlanPcp.FULL_MASK);
			//this.setDataLayerVirtualLan((short) 0xffff);
			//this.setDataLayerVirtualLanPriorityCodePoint((byte) 0);
		} else {
			// has vlan tag
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanOF10(eth.getVlanID()));
			mb.setExact(MatchField.VLAN_PCP, VlanPcp.of(eth.getPriorityCode()));
			//this.setDataLayerVirtualLan((short) (0xfff & scratch));
			//this.setDataLayerVirtualLanPriorityCodePoint((byte) ((0xe000 & scratch) >> 13));
		}

		final IPacket pkt = eth.getPayload();
		
		if (pkt instanceof IPv4) {
			// ipv4
			final IPv4 p = (IPv4) pkt;
			// nw tos (dscp)
			mb.setExact(MatchField.IP_DSCP, IpDscp.of(p.getDiffServ()));
			//this.setNetworkTypeOfService((byte) ((0xfc & scratch) >> 2));
			
			// nw protocol
			mb.setExact(MatchField.IP_PROTO, IpProtocol.of(p.getProtocol()));
			//this.networkProtocol = packetDataBB.get();
			
			// nw src
			mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(p.getSourceAddress()));
			//this.networkSource = packetDataBB.getInt();
			
			// nw dst
			mb.setExact(MatchField.IPV4_DST, IPv4Address.of(p.getDestinationAddress()));
			//this.networkDestination = packetDataBB.getInt();
		} else if (pkt instanceof ARP) {
			// arp
			final ARP arp = (ARP) pkt;

			// opcode
			mb.setExact(MatchField.IP_PROTO, IpProtocol.of(arp.getOpCode()));
			//this.setNetworkProtocol((byte) (0xff & scratch));
			
			// if ipv4 and addr len is 4
			if (arp.getProtocolType() == 0x800 && arp.getProtocolAddressLength() == 4) {
				// nw src
				mb.setExact(MatchField.ARP_SPA, IPv4Address.of(arp.getSenderProtocolAddress()));
				//this.networkSource = packetDataBB.getInt(arpPos + 14);
				
				// nw dst
				mb.setExact(MatchField.ARP_TPA, IPv4Address.of(arp.getTargetProtocolAddress()));
				//this.networkDestination = packetDataBB.getInt(arpPos + 24);
			} else {
				//mb.wildcard(MatchField.IPV4_SRC);
				//mb.wildcard(MatchField.IPV4_DST);
				
				mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(0));
				mb.setExact(MatchField.IPV4_DST, IPv4Address.of(0));
				
				//this.setNetworkSource(0);
				//this.setNetworkDestination(0);
			}
		} else {
			// Not ARP or IP
			mb.setExact(MatchField.IP_PROTO, IpProtocol.of((byte)0));
			mb.setExact(MatchField.IP_DSCP, IpDscp.of((byte)0));
			mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(0));
			mb.setExact(MatchField.IPV4_DST, IPv4Address.of(0));
			
			//mb.wildcard(MatchField.IP_PROTO);
			//mb.wildcard(MatchField.IP_DSCP);
			//mb.wildcard(MatchField.IPV4_DST);
			//mb.wildcard(MatchField.IPV4_SRC);
			
			//this.setNetworkTypeOfService((byte) 0);
			//this.setNetworkProtocol((byte) 0);
			//this.setNetworkSource(0);
			//this.setNetworkDestination(0);
		}

		if (pkt instanceof IPv4) {
			if (pkt instanceof TCP) {
				mb.setExact(MatchField.TCP_SRC, TransportPort.of(((TCP) pkt).getSourcePort()));
				mb.setExact(MatchField.TCP_DST, TransportPort.of(((TCP) pkt).getDestinationPort()));
	
			} else if (pkt instanceof UDP) {
				mb.setExact(MatchField.TCP_SRC, TransportPort.of(((UDP) pkt).getSourcePort()));
				mb.setExact(MatchField.TCP_DST, TransportPort.of(((UDP) pkt).getDestinationPort()));
			}
			
			if (pkt instanceof ICMP) {
				mb.setExact(MatchField.TCP_SRC, TransportPort.of(((ICMP) pkt).getIcmpCode()));
				mb.setExact(MatchField.TCP_DST, TransportPort.of(((ICMP) pkt).getIcmpCode()));
			}
		}
		else {
			mb.setExact(MatchField.TCP_SRC, TransportPort.of(0));
			mb.setExact(MatchField.TCP_DST, TransportPort.of(0));
			
			//mb.wildcard(MatchField.TCP_DST);
			//mb.wildcard(MatchField.TCP_SRC);
			
			//this.wildcards |= OFMatch.OFPFW_TP_DST | OFMatch.OFPFW_TP_SRC;
			//this.setTransportDestination((short) 0);
			//this.setTransportSource((short) 0);
		}
		return mb.build();
	}

	// The one above should be used, this has some bugs
	public static Match loadFromPacket(final byte[] packetData, final int inputPort, final OFVersion ofversion) {
		short scratch;
		final ByteBuffer packetDataBB = ByteBuffer.wrap(packetData);
		final int limit = packetDataBB.limit();
		
		Match.Builder mb = OFFactories.getFactory(ofversion).buildMatch();
		if (inputPort != OFPort.ALL.getPortNumber()) {
			mb.setExact(MatchField.IN_PORT, OFPort.of(inputPort));
		} else {
			mb.wildcard(MatchField.IN_PORT);
		}
	
		assert limit >= 14;
		// dl dst
		byte [] dataLayerDestination = new byte[6];
		packetDataBB.get(dataLayerDestination);
		
		mb.setExact(MatchField.ETH_DST, MacAddress.of(dataLayerDestination));
		
		// dl src
		byte [] dataLayerSource = new byte[6];
		packetDataBB.get(dataLayerSource);
		mb.setExact(MatchField.ETH_SRC, MacAddress.of(dataLayerSource));

		// dl type
		short dataLayerType = packetDataBB.getShort();
		mb.setExact(MatchField.ETH_TYPE, EthType.of(dataLayerType));
		
		if (dataLayerType != (short) 0x8100) { // need cast to avoid signed bug
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.NO_MASK);
			mb.setExact(MatchField.VLAN_PCP, VlanPcp.FULL_MASK);
			//this.setDataLayerVirtualLan((short) 0xffff);
			//this.setDataLayerVirtualLanPriorityCodePoint((byte) 0);
		} else {
			// has vlan tag
			scratch = packetDataBB.getShort();
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanOF10((short) (0xfff & scratch)));
			mb.setExact(MatchField.VLAN_PCP, VlanPcp.of((byte) ((0xe000 & scratch) >> 13)));
			//this.setDataLayerVirtualLan((short) (0xfff & scratch));
			//this.setDataLayerVirtualLanPriorityCodePoint((byte) ((0xe000 & scratch) >> 13));
			dataLayerType = packetDataBB.getShort();
		}

		byte networkProtocol = (byte) 0;
		switch (dataLayerType) {
		case 0x0800:
			// ipv4
			// check packet length
			scratch = packetDataBB.get();
			scratch = (short) (0xf & scratch);
			int transportOffset = packetDataBB.position() - 1 + scratch * 4;
			
			// nw tos (dscp)
			scratch = packetDataBB.get();
			mb.setExact(MatchField.IP_DSCP, IpDscp.of((byte) ((0xfc & scratch) >> 2)));
			//this.setNetworkTypeOfService((byte) ((0xfc & scratch) >> 2));
			
			// nw protocol
			packetDataBB.position(packetDataBB.position() + 7);
			networkProtocol = packetDataBB.get();
			mb.setExact(MatchField.IP_PROTO, IpProtocol.of(networkProtocol));
			//this.networkProtocol = packetDataBB.get();
			
			// nw src
			packetDataBB.position(packetDataBB.position() + 2);
			mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(packetDataBB.getInt()));
			//this.networkSource = packetDataBB.getInt();
			
			// nw dst
			mb.setExact(MatchField.IPV4_DST, IPv4Address.of(packetDataBB.getInt()));
			//this.networkDestination = packetDataBB.getInt();
			packetDataBB.position(transportOffset);
			break;
		case 0x0806:
			// arp
			final int arpPos = packetDataBB.position();
			
			// opcode
			scratch = packetDataBB.getShort(arpPos + 6);
			networkProtocol = (byte) (0xff & scratch);
			mb.setExact(MatchField.IP_PROTO, IpProtocol.of(networkProtocol));
			//this.setNetworkProtocol((byte) (0xff & scratch));

			scratch = packetDataBB.getShort(arpPos + 2);
			// if ipv4 and addr len is 4
			if (scratch == 0x800 && packetDataBB.get(arpPos + 5) == 4) {
				// nw src
				mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(packetDataBB.getInt(arpPos + 14)));
				//this.networkSource = packetDataBB.getInt(arpPos + 14);
				
				// nw dst
				mb.setExact(MatchField.IPV4_DST, IPv4Address.of(packetDataBB.getInt(arpPos + 24)));
				//this.networkDestination = packetDataBB.getInt(arpPos + 24);
			} else {
				mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(0));
				mb.setExact(MatchField.IPV4_DST, IPv4Address.of(0));
				
				//this.setNetworkSource(0);
				//this.setNetworkDestination(0);
			}
			break;
		default:
			// Not ARP or IP. Wildcard NW_DST and NW_SRC
			mb.setExact(MatchField.IP_PROTO, IpProtocol.of((byte)0));
			mb.setExact(MatchField.IP_DSCP, IpDscp.of((byte)0));
			mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(0));
			mb.setExact(MatchField.IPV4_DST, IPv4Address.of(0));
			
			mb.wildcard(MatchField.IP_PROTO);
			mb.wildcard(MatchField.IP_DSCP);
			mb.wildcard(MatchField.IPV4_DST);
			mb.wildcard(MatchField.IPV4_SRC);
			//this.setNetworkTypeOfService((byte) 0);
			//this.setNetworkProtocol((byte) 0);
			//this.setNetworkSource(0);
			//this.setNetworkDestination(0);
			break;
		}

		switch (networkProtocol) {
		case 0x01:
			// icmp
			// type
			mb.setExact(MatchField.TCP_SRC, TransportPort.of(packetDataBB.get()));
			//mb.setExact(MatchField.ICMPV4_TYPE,  ICMPv4Type.of(packetDataBB.get()));
			//this.transportSource = U8.f(packetDataBB.get());
			
			// code
			mb.setExact(MatchField.TCP_DST, TransportPort.of(packetDataBB.get()));
			//mb.setExact(MatchField.ICMPV4_CODE, ICMPv4Code.of(packetDataBB.get()));
			//this.transportDestination = U8.f(packetDataBB.get());
			break;
		case 0x06:
			// tcp
			// tcp src
			mb.setExact(MatchField.TCP_SRC, TransportPort.of(packetDataBB.getShort()));
			//this.transportSource = packetDataBB.getShort();
			
			// tcp dest
			mb.setExact(MatchField.TCP_DST, TransportPort.of(packetDataBB.getShort()));
			//this.transportDestination = packetDataBB.getShort();
			break;
		case 0x11:
			// udp
			// udp src
			mb.setExact(MatchField.TCP_SRC, TransportPort.of(packetDataBB.getShort()));
			//mb.setExact(MatchField.UDP_SRC, TransportPort.of(packetDataBB.getShort()));
			//this.transportSource = packetDataBB.getShort();
			
			// udp dest
			mb.setExact(MatchField.TCP_DST, TransportPort.of(packetDataBB.getShort()));
			//mb.setExact(MatchField.UDP_DST, TransportPort.of(packetDataBB.getShort()));
			//this.transportDestination = packetDataBB.getShort();
			break;
		default:
			// Unknown network proto.
			mb.setExact(MatchField.TCP_SRC, TransportPort.of(0));
			mb.setExact(MatchField.TCP_DST, TransportPort.of(0));
			
			mb.wildcard(MatchField.TCP_DST);
			mb.wildcard(MatchField.TCP_SRC);
			log.warn("Unknown IP protocol {}", networkProtocol);
			//this.wildcards |= OFMatch.OFPFW_TP_DST | OFMatch.OFPFW_TP_SRC;
			//this.setTransportDestination((short) 0);
			//this.setTransportSource((short) 0);
			break;
		}
		return mb.build();
	}
}
