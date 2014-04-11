/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.OFPort;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatisticsReply;

public class OVXPortStatisticsReply implements VirtualizableStatistic {	
	private Map<Integer, OVXPortStatisticsReply> stats = null;
	private OFPortStatsEntry pse;
	
	public OVXPortStatisticsReply(OFVersion ofVersion) {
		this.pse = OFFactories.getFactory(ofVersion).buildPortStatsEntry().build();
	}
	
	@Override
	public void virtualizeStatistic(final PhysicalSwitch sw,
			final OVXStatisticsReply msg) {
		OFPortStatsReply ofstatmsg = (OFPortStatsReply) msg;
		
		stats = new HashMap<Integer, OVXPortStatisticsReply>();
		//List<?> statList = msg.getStatistics();
		for (OFPortStatsEntry entry : ofstatmsg.getEntries()) {
			//OVXPortStatisticsReply pStat = (OVXPortStatisticsReply) stat;
			OVXPortStatisticsReply pStat = new OVXPortStatisticsReply(entry.getVersion());
			pStat.setEntry(entry);
			stats.put(pStat.getPortNumber(), pStat);
		}
		sw.setPortStatistics(stats);
	}

	public OVXPortStatisticsReply setPortNumber(Integer portNumber) {
		this.pse = this.pse.createBuilder().setPortNo(OFPort.of(portNumber)).build();
		return this;
	}

	public OVXPortStatisticsReply setEntry(OFPortStatsEntry entry) {
		this.pse = entry;
		return this;
	}
	
	public Integer getPortNumber() {
		return this.pse.getPortNo().getPortNumber();
	}
	
	public OFPortStatsEntry getEntry() {
		return this.pse;
	}
}

