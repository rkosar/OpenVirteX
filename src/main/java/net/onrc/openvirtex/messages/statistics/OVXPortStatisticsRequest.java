/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import java.util.LinkedList;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFPortStatsRequest;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.OFPort;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.messages.OVXStatisticsRequest;

public class OVXPortStatisticsRequest  implements DevirtualizableStatistic {
	private OFPortStatsRequest psr;
	
	public OVXPortStatisticsRequest(OFVersion ofVersion) {
		this.psr = OFFactories.getFactory(ofVersion).buildPortStatsRequest().build();
	}
	
	@Override
	public void devirtualizeStatistic(final OVXSwitch sw, final OVXStatisticsRequest msg) {
		//List<OVXPortStatisticsReply> replies = new LinkedList<OVXPortStatisticsReply>();
		//int length = 0;
		
		List<OFPortStatsEntry> entries = new LinkedList<OFPortStatsEntry>();
		
		if (this.psr.getPortNo() == OFPort.ZERO) {
			for (OVXPort p : sw.getPorts().values()) {
				OVXPortStatisticsReply reply =
						p.getPhysicalPort().getParentSwitch().getPortStat(p.getPhysicalPort().getPortNumber());
				if (reply != null) {
					/*
					 * Setting it here will also update the reference
					 * but this should not matter since we index our 
					 * port stats struct by physical port number 
					 * (so this info is not lost) and we always rewrite 
					 * the port num to the virtual port number. 
					 */
					reply.setPortNumber(p.getPortNumber());
					
					entries.add(reply.getEntry());
					//length += reply.getLength();
				}
			}
			OFPortStatsReply rep = OFFactories.getFactory(sw.getVersion())
					.buildPortStatsReply()
					.setXid(msg.getXid())
					.setEntries(entries)
					.build();
			
			sw.sendMsg(rep, sw);
			//OVXStatisticsReply rep = new OVXStatisticsReply(OFStatsType.PORT, sw.getVersion());
			//rep.setStatistics(replies)
			//   .setXid(msg.getXid());
			//rep.setLengthU(OVXStatisticsReply.MINIMUM_LENGTH + length);
			//sw.sendMsg(rep.getStatsReply(), sw);
		}	
	}

	public OVXPortStatisticsRequest setPortNumber(int portNumber) {
		this.psr = this.psr.createBuilder().setPortNo(OFPort.of(portNumber)).build();
		return this;		
	}
}
