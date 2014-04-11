/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatisticsReply;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.U64;

public class OVXFlowStatisticsReply implements VirtualizableStatistic {
	private OFFlowStatsEntry.Builder replyb;
	
	public OVXFlowStatisticsReply(OFVersion ofVersion) {
		this.replyb = OFFactories.getFactory(ofVersion).buildFlowStatsEntry();
	}
	@Override
	public void virtualizeStatistic(final PhysicalSwitch sw, final OVXStatisticsReply msg) {
		OFFlowStatsReply ofstatmsg = (OFFlowStatsReply) msg;
		if (ofstatmsg.getXid() != 0) {
			sw.removeFlowMods(msg);
			return;
		}
		HashMap<Integer, List<OVXFlowStatisticsReply>> stats = new HashMap<Integer, List<OVXFlowStatisticsReply>>();
		
		for (OFFlowStatsEntry entry : ofstatmsg.getEntries()) {
			//OVXFlowStatisticsReply reply = (OVXFlowStatisticsReply) stat;
			OVXFlowStatisticsReply reply = new OVXFlowStatisticsReply(entry.getVersion());
			reply.setEntry(entry);
			int tid = getTidFromCookie(reply.getCookie());
			addToStats(tid, reply, stats);
		}
		sw.setFlowStatistics(stats);
	}

	private void addToStats(int tid, OVXFlowStatisticsReply reply, HashMap<Integer, List<OVXFlowStatisticsReply>> stats) {
		List<OVXFlowStatisticsReply> statsList = stats.get(tid);
		if (statsList == null) 
			statsList = new LinkedList<OVXFlowStatisticsReply>();
		statsList.add(reply);
		stats.put(tid, statsList);
	}

	private int getTidFromCookie(U64 cookie) {
		return (int) (cookie.getValue() >> 32);
	}

	public OVXFlowStatisticsReply setCookie(U64 cookie) {
		this.replyb.setCookie(cookie);
		return this;
	}
	
	public OVXFlowStatisticsReply setMatch(Match match) {
		this.replyb.setMatch(match);
		return this;
	}

	public OVXFlowStatisticsReply setActions(List<OFAction> actions) {
		this.replyb.setActions(actions);
		return this;
	}
		
	public OVXFlowStatisticsReply setEntry(OFFlowStatsEntry entry) {
		this.replyb = entry.createBuilder();
		return this;
	}

	
	public U64 getCookie() {
		return this.replyb.getCookie();
	}
	
	public Match getMatch() {
		return this.replyb.getMatch();
	}

	public List<OFAction> getActions() {
		return this.replyb.getActions();
	}
	
	public OFFlowStatsEntry getEntry() {
		return this.replyb.build();
	}
	public OFVersion getVersion() {
		return this.replyb.getVersion();
	}
}
