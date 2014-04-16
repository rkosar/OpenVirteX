/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.onrc.openvirtex.elements.datapath.OVXSingleSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXStatisticsRequest;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

public class OVXFlowStatisticsRequest  implements DevirtualizableStatistic {
	private OFFlowStatsRequest fsr;

	Logger log = LogManager.getLogger(OVXFlowStatisticsRequest.class.getName());
	
	public OVXFlowStatisticsRequest(OFVersion ofVersion) {
		this.fsr = OFFactories.getFactory(ofVersion).buildFlowStatsRequest().build();
	}
	
	@Override
	public void devirtualizeStatistic(final OVXSwitch sw,
			final OVXStatisticsRequest msg) {		
		//List<OVXFlowStatisticsReply> replies = new LinkedList<OVXFlowStatisticsReply>();

		List<OFFlowStatsEntry> replies = new LinkedList<OFFlowStatsEntry>();		
		HashSet<Long> uniqueCookies = new HashSet<Long>();
		int tid = sw.getTenantId();
		//int length = 0;

		//if ((this.match.getWildcardObj().isFull() || this.match.getWildcards() == -1) // the -1 is for beacon...
		if (!this.fsr.getMatch().getMatchFields().iterator().hasNext()
		  && this.fsr.getOutPort().compareTo(OFPort.ZERO) == 0) {
			for (PhysicalSwitch psw : getPhysicalSwitches(sw)) {
				List<OVXFlowStatisticsReply> reps = psw.getFlowStats(tid);
				if (reps != null) {
					for (OVXFlowStatisticsReply stat : reps) {
						if (!uniqueCookies.contains(stat.getCookie())) {
							OVXFlowMod origFM;
							try {
								origFM = sw.getFlowMod(stat.getCookie().getValue());
								uniqueCookies.add(stat.getCookie().getValue());
							} catch (MappingException e) {
								log.warn("FlowMod not found in FlowTable for cookie={}", stat.getCookie());
								continue;
							}
							stat.setCookie(origFM.getCookie())
							.setMatch(origFM.getMatch())
							.setActions(origFM.getActions());
							
							replies.add(stat.getEntry());
							//stat.setLength(U16.t(OVXFlowStatisticsReply.MINIMUM_LENGTH));
							//for (OFAction act : stat.getActions()) {
							//	stat.setLength(U16.t(stat.getLength() + act.getLength()));
							//}
							//length += stat.getLength();
						}
					}
				}
			}
	
			OFFlowStatsReply reply = OFFactories.getFactory(sw.getVersion())
					.buildFlowStatsReply()
					.setXid(msg.getXid())
					.setEntries(replies)
					.build();

			sw.sendMsg(reply, sw);
			//OVXStatisticsReply reply = new OVXStatisticsReply(OFStatsType.FLOW, sw.getVersion());
			//reply.setXid(msg.getXid())
			//	 .setStatistics(replies);

			//reply.setLengthU(OVXStatisticsReply.MINIMUM_LENGTH + length);

			//sw.sendMsg(reply.getStatsReply(), sw);
		}
	}

	private List<PhysicalSwitch> getPhysicalSwitches(OVXSwitch sw) {
		if (sw instanceof OVXSingleSwitch)
			try {
				return sw.getMap().getPhysicalSwitches(sw);
			} catch (SwitchMappingException e) {
				log.debug("OVXSwitch {} does not map to any physical switches", sw.getSwitchName());
				return new LinkedList<>();
			}
		LinkedList<PhysicalSwitch> sws = new LinkedList<PhysicalSwitch>();
		for (OVXPort p : sw.getPorts().values())
			if (!sws.contains(p.getPhysicalPort().getParentSwitch()))
				sws.add(p.getPhysicalPort().getParentSwitch());
		return sws;
	}

	public OVXFlowStatisticsRequest setOutPort(OFPort outPort) {
		this.fsr = this.fsr.createBuilder().setOutPort(outPort).build();
		return this;
	}

	public OVXFlowStatisticsRequest setMatch(OVXMatch match) {
		this.fsr = this.fsr.createBuilder().setMatch(match.getMatch()).build();
		return this;
	}

	public OVXFlowStatisticsRequest setTableId(short tableId) {
		this.fsr = this.fsr.createBuilder().setTableId(TableId.of(tableId)).build();
		return this;
	}
}
