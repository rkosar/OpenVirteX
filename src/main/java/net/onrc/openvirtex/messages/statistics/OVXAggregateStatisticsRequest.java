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

import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSingleSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.OVXStatisticsRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFAggregateStatsReply;
import org.projectfloodlight.openflow.protocol.OFAggregateStatsRequest;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

public class OVXAggregateStatisticsRequest implements DevirtualizableStatistic {
	private OFAggregateStatsRequest asr; 
	Logger log = LogManager.getLogger(OVXAggregateStatisticsRequest.class.getName());
	
	public OVXAggregateStatisticsRequest(OFVersion ofVersion) {
		this.asr = OFFactories.getFactory(ofVersion).buildAggregateStatsRequest().build();
	}
	
	@Override
	public void devirtualizeStatistic(final OVXSwitch sw, final OVXStatisticsRequest msg) {
		//OVXAggregateStatisticsReply stat = new OVXAggregateStatisticsReply(sw.getVersion());
		int tid = sw.getTenantId();
		HashSet<Long> uniqueCookies = new HashSet<Long>();

		OFAggregateStatsReply.Builder statb = OFFactories.getFactory(sw.getVersion()).buildAggregateStatsReply();

		//if ((this.match.getWildcardObj().isFull() || this.match.getWildcards() == -1) // the -1 is for beacon...
		if (!this.asr.getMatch().getMatchFields().iterator().hasNext()
				&& this.asr.getOutPort() == OFPort.ZERO) {
			FlowTable ft = sw.getFlowTable();
			statb.setFlowCount(ft.getFlowTable().size())
			.setByteCount(U64.ZERO)
			.setPacketCount(U64.ZERO);
			
			for (PhysicalSwitch psw : getPhysicalSwitches(sw)) {
				List<OVXFlowStatisticsReply> reps = psw.getFlowStats(tid);
				if (reps != null) {
					for (OVXFlowStatisticsReply s : reps) {
						if (!uniqueCookies.contains(s.getCookie())) {
							statb.setByteCount(U64.of(statb.getByteCount().getValue() + s.getEntry().getByteCount().getValue()));
							statb.setByteCount(U64.of(statb.getPacketCount().getValue() + s.getEntry().getPacketCount().getValue()));
							//stat.setByteCount(stat.getByteCount().getValue() + s.getByteCount());
							//stat.setByteCount(stat.getPacketCount().getValue() + s.getPacketCount());
							uniqueCookies.add(s.getCookie().getValue());
						}
					}
				}
			}
		}
		
		statb.setXid(msg.getXid());
		sw.sendMsg(statb.build(), sw);
		
		/*OVXStatisticsReply reply = new OVXStatisticsReply(OFStatsType.AGGREGATE, sw.getVersion());
		
		reply.setXid(msg.getXid())
			 .setStatistics(stat);
			 //reply.setLengthU(OVXStatisticsReply.MINIMUM_LENGTH + stat.getLength());
		sw.sendMsg(reply.getStatsReply(), sw);*/
	}	
	
	private List<PhysicalSwitch> getPhysicalSwitches(OVXSwitch sw) {
		if (sw instanceof OVXSingleSwitch) {
			try {
				return sw.getMap().getPhysicalSwitches(sw);
			} catch (SwitchMappingException e) {
				log.debug("OVXSwitch {} does not map to any physical switches", sw.getSwitchName());
				return new LinkedList<>();
			}
		}
		LinkedList<PhysicalSwitch> sws = new LinkedList<PhysicalSwitch>();
		for (OVXPort p : sw.getPorts().values())
			if (!sws.contains(p.getPhysicalPort().getParentSwitch()))
				sws.add(p.getPhysicalPort().getParentSwitch());
		return sws;
	}
}
