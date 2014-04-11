/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.messages;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.elements.datapath.OVXFlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.DroppedMessageException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.UnknownActionException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.util.OVXUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFailedCode;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.*;


public class OVXFlowMod implements Devirtualizable {

	private final Logger                      log             = LogManager.getLogger(OVXFlowMod.class.getName());

	private final List<OFAction>              approvedActions = new LinkedList<OFAction>();
	
	private OVXSwitch                         sw              = null;
	
	private U64                               ovxCookie       = U64.of(-1);
	
	private OFFlowMod                         flow            = null;
	
	public OVXFlowMod(OFMessage m) {
		this.flow = (OFFlowMod) m;
	}
	 
	public OVXFlowMod(OFVersion ofversion) {
		this.flow = OFFactories.getFactory(ofversion).buildFlowModify().build();
	}

	@Override
	public void devirtualize(final OVXSwitch sw) {
		/* Drop LLDP-matching messages sent by some applications */
		
		Match m = this.flow.getMatch();
		
		if (m.get(MatchField.ETH_TYPE) == EthType.LLDP) {
			return;
		}

		this.sw = sw;
		FlowTable ft = this.sw.getFlowTable();
		
		int bufferId = OFBufferId.NO_BUFFER.getInt();
		if (sw.getFromBufferMap(this.flow.getBufferId().getInt() ) != null) {
			bufferId = sw.getFromBufferMap(this.flow.getBufferId().getInt()).getBufferId().getInt();
		}

		OVXMatch ovxMatch = new OVXMatch(m);
		ovxCookie = ((OVXFlowTable) ft).getCookie();
		//Store the virtual flowMod and obtain the physical cookie
		ovxMatch.setCookie(ovxCookie);
		boolean pflag = ft.handleFlowMods(this, ovxCookie);
		this.setCookie(ovxMatch.getCookie());
		
		for (final OFAction act : this.flow.getActions()) {
			try {
				((VirtualizableAction) OVXMessageFactory.getAction(act)).virtualize(sw, this.approvedActions, ovxMatch);
			} catch (final ActionVirtualizationDenied e) {
				this.log.warn("Action {} could not be virtualized; error: {}",
						act, e.getMessage());
				ft.deleteFlowMod(ovxCookie);
				sw.sendMsg(OVXMessageUtil.makeErrorMsg(e.getErrorCode(), this.flow), sw);
				return;
			} catch (final DroppedMessageException e) {
				this.log.warn("Dropping flowmod {}", this);
				ft.deleteFlowMod(ovxCookie);
				//TODO perhaps send error message to controller
				return;
			}
		}

		final OFPort inport = m.get(MatchField.IN_PORT);
		
		this.setBufferId(OFBufferId.of(bufferId));

		if (inport == null) {
			if (!this.flow.getMatch().isExact(MatchField.IN_PORT)) {
				/* expand match to all ports */
				for (OVXPort iport : sw.getPorts().values()) {
					//int wcard = this.match.getWildcards() & (~OFMatch.OFPFW_IN_PORT); 
					//this.match.setWildcards(wcard);
					prepAndSendSouth(iport, pflag);
				}
			} else {	
				this.log.error("Unknown virtual port id {}; dropping flowmod {}", inport, this);
				sw.sendMsg(OVXMessageUtil.makeErrorMsg(OFFlowModFailedCode.EPERM, this.flow), sw);
				return;
			}
		} else {
			final OVXPort ovxInPort = sw.getPort(inport.getPortNumber());
			prepAndSendSouth(ovxInPort, pflag);
		}
	}
	
	private void prepAndSendSouth(OVXPort inPort, boolean pflag) {
		if (!inPort.isActive()) {
			log.warn("Virtual network {}: port {} on switch {} is down.", sw.getTenantId(),
					inPort.getPortNumber(), sw.getSwitchName());
			return;
		}
		// updating match
		Match m = this.flow.getMatch()
				.createBuilder()
				.setExact(MatchField.IN_PORT, OFPort.of(inPort.getPhysicalPortNumber()))
				.build();
		
		this.flow = this.flow.createBuilder().setMatch(m).build();
		
		OVXMessageUtil.translateXid(this.flow, inPort);

		try {
		    if (inPort.isEdge()) {
				this.prependRewriteActions();
		    } else {
		    	IPMapper.rewriteMatch(sw.getTenantId(), m);
				//TODO: Verify why we have two send points... and if this is the right place for the match rewriting
				if (inPort != null && inPort.isLink() && 
					(m.isExact(MatchField.ETH_DST) || m.isExact(MatchField.ETH_SRC))) {
				    //rewrite the OFMatch with the values of the link
				    OVXPort dstPort = sw.getMap().getVirtualNetwork(sw.getTenantId()).getNeighborPort(inPort);
				    OVXLink link = sw.getMap().getVirtualNetwork(sw.getTenantId()).getLink(dstPort, inPort);
				    if (inPort != null && link != null) {
						Integer flowId = sw.getMap()
								.getVirtualNetwork(sw.getTenantId())
								.getFlowManager()
								.getFlowId(m.get(MatchField.ETH_SRC).getBytes(), m.get(MatchField.ETH_DST).getBytes());
						OVXLinkUtils lUtils = new OVXLinkUtils(sw.getTenantId(), link.getLinkId(), flowId);
						lUtils.rewriteMatch(m);
				    }
				}
		    }
		} catch (NetworkMappingException e) {
			log.warn("OVXFlowMod. Error retrieving the network with id {} for flowMod {}. Dropping packet...", 
					this.sw.getTenantId(), this);                    
		} catch (DroppedMessageException e) {
			log.warn("OVXFlowMod. Error retrieving flowId in network with id {} for flowMod {}. Dropping packet...", 
					this.sw.getTenantId(), this);
		}
		this.flow = this.flow.createBuilder().setMatch(m).build();
		
		this.computeLength();
		
		if (pflag) {
			//this.flags |= OFFlowModFlags.SEND_FLOW_REM;
			Set<OFFlowModFlags> flags = new TreeSet<OFFlowModFlags>();
			flags.addAll(this.flow.getFlags());
			flags.add(OFFlowModFlags.SEND_FLOW_REM);
			this.flow = this.flow.createBuilder().setFlags(flags).build();
			sw.sendSouth(this.flow, inPort);
		}
	}

	private void computeLength() {
		this.flow = this.flow.createBuilder()
				.setActions(this.approvedActions)
				.build();
		/*
		this.flowmod.setActions(this.approvedActions);
		this.setLengthU(OVXFlowMod.MINIMUM_LENGTH);
		for (final OFAction act : this.approvedActions) {
			this.setLengthU(this.getLengthU() + act.getLengthU());
		}
		*/
	}

	private void prependRewriteActions() {
		Match m = this.flow.getMatch();
		
		if (m.get(MatchField.ETH_TYPE) == EthType.IPv4) {
			if(m.isExact(MatchField.IPV4_SRC)) {
				final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource(this.getVersion());
				srcAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), m.get(MatchField.IPV4_SRC).getInt()));
				this.approvedActions.add(0, (OFAction) srcAct);
			}
			
			if (m.isExact(MatchField.IPV4_DST)) {
				final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination(this.getVersion());
				dstAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), m.get(MatchField.IPV4_DST).getInt()));
				this.approvedActions.add(0, (OFAction)dstAct);
			}
		} else if (m.get(MatchField.ETH_TYPE) == EthType.ARP) {
			if (m.isExact(MatchField.ARP_SPA)) {
				final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource(this.getVersion());
				srcAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), m.get(MatchField.ARP_SPA).getInt()));
				this.approvedActions.add(0, (OFAction) srcAct);
			}
		
			if (m.isExact(MatchField.ARP_TPA)) {
				final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination(this.getVersion());
				dstAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(), m.get(MatchField.ARP_TPA).getInt()));
				this.approvedActions.add(0, (OFAction)dstAct);
			}
		}
	}

	/**
	 * @param flagbit The OFFlowMod flag 
	 * @return true if the flag is set 
	 */
	public boolean hasFlag(OFFlowModFlags flag) {
		return (this.flow.getFlags().contains(flag));
	}
	
	public OVXFlowMod clone() {
	    OVXFlowMod flowMod = new OVXFlowMod(this.flow);
	    flowMod.approvedActions.addAll(this.approvedActions);
	    flowMod.ovxCookie = this.ovxCookie;
	    flowMod.sw = this.sw;
	    
	    /*try {
	    	//flowMod = (OVXFlowMod) super.clone();
	    } catch (CloneNotSupportedException e) {
	    	log.error("Error cloning flowMod: {}" , this);
	    }*/
	    return flowMod;
	}
	
	public Map<String, Object> toMap() {
		 final HashMap<String, Object> map = new LinkedHashMap<String, Object>();
		 if (this.flow.getMatch() != null) {
			 map.put("match", new OVXMatch(this.flow.getMatch()).toMap());
		 }
		 LinkedList<HashMap<String,Object>> actions = new LinkedList<HashMap<String,Object>>();
		 for (OFAction act : this.flow.getActions()) {
			 try {
				actions.add(OVXUtil.actionToMap(act));
			} catch (UnknownActionException e) {
				log.warn("Ignoring action {} because {}", act, e.getMessage());
			}
		 }
		 map.put("actionsList", actions);
		 map.put("priority", String.valueOf(this.flow.getPriority()));
		 return map;
	 }
	
	public void setPhysicalCookie() {
		U64 tmp = this.flow.getCookie();
		this.flow = this.flow.createBuilder().setCookie(this.ovxCookie).build();
		this.ovxCookie = tmp;
	}
	
	public void setVirtualCookie() {
		U64 tmp = this.ovxCookie;
		this.ovxCookie = this.flow.getCookie();
		this.flow = this.flow.createBuilder().setCookie(tmp).build();
	}

	public OFFlowModCommand getCommand() {
		return this.flow.getCommand();
	}

	public Set<OFFlowModFlags> getFlags() {
		return this.flow.getFlags();
	}

	public int getPriority() {
		return this.flow.getPriority();
	}

	public OFPort getOutPort() {
		return this.flow.getOutPort();
	}

	public U64 getCookie() {
		return this.flow.getCookie();
	}
	
	public Match getMatch() {
		return this.flow.getMatch();
	}

	public List<OFAction> getActions() {
		return this.flow.getActions();
	}
	
	public OFVersion getVersion() {
		return this.flow.getVersion();
	}
	
	public OFFlowMod getFlow() {
		return this.flow;
	}


	public OVXFlowMod setFlags(Set<OFFlowModFlags> flags) {
		this.flow = this.flow.createBuilder().setFlags(flags).build();
		return this;
	}
	
	public OVXFlowMod setCookie(U64 cookie) {
		this.flow = this.flow.createBuilder().setCookie(cookie).build();
		return this;
	}

	public OVXFlowMod setBufferId(OFBufferId bufferId) {
		this.flow = this.flow.createBuilder().setBufferId(bufferId).build();
		return this;
	}
	
	public OVXFlowMod setMatch(Match match) {
		this.flow = this.flow.createBuilder().setMatch(match).build();
		return this;
	}
	
	public OVXFlowMod setActions(List<OFAction> actions) {
		this.flow = this.flow.createBuilder().setActions(actions).build();
		return this;
	}
	
	public OVXFlowMod setHardTimeout(int hardTimeout) {
		this.flow = this.flow.createBuilder().setHardTimeout(hardTimeout).build();
		return this;
	}
	
	public OVXFlowMod setOutPort(OFPort outPort) {
		this.flow = this.flow.createBuilder().setOutPort(outPort).build();
		return this;
	}
	
	public OVXFlowMod setPriority(int priority) {
		this.flow = this.flow.createBuilder().setPriority(priority).build();
		return this;
	}
	
	public OVXFlowMod setFlow(OFFlowMod fm) {
		this.flow = fm;
		return this;
	}
	
	public OVXFlowMod setCommand(OFFlowModCommand command) {
		OFFactory factory = OFFactories.getFactory(this.flow.getVersion());
		OFFlowMod.Builder fmb = null;
		
		switch (command)
		{
		case ADD: fmb = factory.buildFlowAdd(); break; 
		case DELETE: fmb = factory.buildFlowDelete(); break;
		case DELETE_STRICT: fmb = factory.buildFlowDeleteStrict(); break;
		case MODIFY: fmb = factory.buildFlowModify(); break;
		case MODIFY_STRICT: fmb = factory.buildFlowModifyStrict(); break;
		default: this.log.error("Unknown FlowModCommand {}", command); return null;
		}
		
		// any shorter way??
		this.flow = fmb.setActions(this.flow.getActions())
				 	 .setBufferId(this.flow.getBufferId())
				 	 .setCookie(this.flow.getCookie())
				 	 .setFlags(this.flow.getFlags())
				 	 .setHardTimeout(this.flow.getHardTimeout())
				 	 .setIdleTimeout(this.flow.getIdleTimeout())
				 	 .setMatch(this.flow.getMatch())
				 	 .setOutPort(this.flow.getOutPort())
				 	 .setPriority(this.flow.getPriority())
				 	 .setXid(this.flow.getXid())
				 	 .build();
		return this;
	}
}
