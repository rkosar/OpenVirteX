/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.messages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.DroppedMessageException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.util.OVXUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;


public class OVXPacketOut implements Devirtualizable {
	private final Logger log = LogManager.getLogger(OVXPacketOut.class.getName());
	private final List<OFAction> approvedActions = new LinkedList<OFAction>();
	private OFPacketOut po;
	private Match match;
	
	@Override
	public void devirtualize(final OVXSwitch sw) {
		final OVXPort inport = sw.getPort(this.po.getInPort().getPortNumber());
		OVXMatch ovxMatch = null;

		if (this.po.getBufferId() == OFBufferId.NO_BUFFER) {
			if (this.po.getData().length <= 14) {
				this.log.error("PacketOut has no buffer or data {}; dropping", this.po);
				sw.sendMsg(OVXMessageUtil.makeErrorMsg(
						OFBadRequestCode.BAD_LEN, this.po), sw);
				return;
			}

			this.match = OVXUtil.loadMatchFromEthPacket(this.po.getData(), 
					this.po.getInPort().getPortNumber(), this.po.getVersion());
	    	
			ovxMatch = new OVXMatch(this.match);
			ovxMatch.setPktData(this.po.getData());
		} else {
			final OVXPacketIn cause = sw.getFromBufferMap(this.po.getBufferId().getInt());
			if (cause == null) {
				this.log.error(
						"Unknown buffer id {} for virtual switch {}; dropping",
						this.po.getBufferId().getInt(), sw);
				return;
			}

	    	this.match = OVXUtil.loadMatchFromEthPacket(cause.getData(), 
	    			this.po.getInPort().getPortNumber(), this.po.getVersion());
	    	
			this.po = this.po.createBuilder()
					.setBufferId(cause.getBufferId())
					.build();
			
			ovxMatch = new OVXMatch(this.match);
			ovxMatch.setPktData(cause.getData());
			if (cause.getBufferId() == OFBufferId.NO_BUFFER) {
				this.po = this.po.createBuilder()
						.setData(cause.getData())
						.build();
			}
		}

		for (final OFAction act : this.getActions()) {
			try {
				((VirtualizableAction) OVXMessageFactory.getAction(act)).virtualize(sw, 
						this.approvedActions, ovxMatch);
			} catch (final ActionVirtualizationDenied e) {
				this.log.warn("Action {} could not be virtualized; error: {}",
						act, e.getMessage());
				sw.sendMsg(OVXMessageUtil.makeErrorMsg(e.getErrorCode(), this.po), sw);
				return;
			} catch (final DroppedMessageException e) {
				this.log.debug("Dropping packetOut {}", this.po);
				return;
			}
		}
		
		if (this.getInPort().getShortPortNumber() < OFPort.MAX.getShortPortNumber())
			this.po = this.po.createBuilder()
			.setInPort(OFPort.of(inport.getPhysicalPortNumber()))
			.build();
		
		this.prependRewriteActions(sw);
		this.po = this.po.createBuilder()
				.setActions(this.approvedActions)
				.build();

		//TODO: Beacon sometimes send msg with inPort == controller, check with Ayaka if it's ok
		if (this.getInPort().getShortPortNumber() < OFPort.MAX.getShortPortNumber())
			OVXMessageUtil.translateXid(this.po, inport);
		this.log.debug("Sending packet-out to sw {}: {}", sw.getName(), this.po);
		sw.sendSouth(this.po, inport);
	}

	private void prependRewriteActions(final OVXSwitch sw) {
		if (this.match.get(MatchField.ETH_TYPE) == EthType.ARP) {
			if (this.match.isExact(MatchField.ARP_SPA)) {
				final OVXActionNetworkLayerSource srcAct = 
						new OVXActionNetworkLayerSource(this.getVersion());
				srcAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), 
						this.match.get(MatchField.ARP_SPA).getInt()));
				this.approvedActions.add(0, srcAct.getAction());
			}
			
			if (this.match.isExact(MatchField.ARP_TPA)) {
				final OVXActionNetworkLayerDestination dstAct = 
						new OVXActionNetworkLayerDestination(this.getVersion());
				dstAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), 
						this.match.get(MatchField.ARP_TPA).getInt()));
				this.approvedActions.add(0, dstAct.getAction());
			}
		} else if (this.match.get(MatchField.ETH_TYPE) == EthType.IPv4) {
			if (this.match.isExact(MatchField.IPV4_SRC)) {
				final OVXActionNetworkLayerSource srcAct = 
						new OVXActionNetworkLayerSource(this.getVersion());
				srcAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), 
						this.match.get(MatchField.IPV4_SRC).getInt()));
				this.approvedActions.add(0, srcAct.getAction());
			}

			if (this.match.isExact(MatchField.IPV4_DST)) {
				final OVXActionNetworkLayerDestination dstAct = 
						new OVXActionNetworkLayerDestination(this.getVersion());
				dstAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), 
						this.match.get(MatchField.IPV4_DST).getInt()));
				this.approvedActions.add(0, dstAct.getAction());
			}
		}
	}

	public OVXPacketOut(final OVXPacketOut pktOut) {
		this.po = OFFactories.getFactory(pktOut.getVersion())
				.buildPacketOut()
				.setBufferId(pktOut.getBufferId())
				.setInPort(pktOut.getInPort())
				.setData(pktOut.getData())
				.setXid(pktOut.getXid())
				.setActions(pktOut.getActions())
				.build();
	}
		
	public OVXPacketOut(OFMessage m) {
		this.po = (OFPacketOut) m;
	}
	
	public OVXPacketOut(final byte[] pktData, final int inPort, final int outPort, 
			final OFVersion ofversion) {
		final OFActionOutput outAction = OFFactories.getFactory(ofversion)
				.actions()
				.buildOutput()
				.setPort(OFPort.of(outPort))
				.build();

		final ArrayList<OFAction> actions = new ArrayList<OFAction>();
		actions.add(outAction);

		this.po = OFFactories.getFactory(ofversion)
				.buildPacketOut()
				.setInPort(OFPort.of(inPort))
				.setBufferId(OFBufferId.NO_BUFFER)
				.setActions(actions)
				.setData(pktData)
				.build();
	}
	
	public OFVersion getVersion() {
		return this.po.getVersion();
	}
	
	public OFBufferId getBufferId() {
		return this.po.getBufferId();
	}

	public OFPort getInPort() {
		return this.po.getInPort();
	}
	
	public byte[] getData() {
		return this.po.getData();
	}

	public long getXid() {
		return this.po.getXid();
	}

	public List<OFAction> getActions() {
		return this.po.getActions();
	}

	public OFPacketOut getPacket() {
		return this.po;
	}
}
