/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import org.projectfloodlight.openflow.protocol.OFAggregateStatsReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.U64;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatisticsReply;

public class OVXAggregateStatisticsReply implements VirtualizableStatistic {
	private OFAggregateStatsReply asr;
	
	public OVXAggregateStatisticsReply(OFVersion ofVersion) {
		this.asr = OFFactories.getFactory(ofVersion).buildAggregateStatsReply().build();
	}
	
	@Override
	public void virtualizeStatistic(final PhysicalSwitch sw,
			final OVXStatisticsReply msg) {
		
	}

	public U64 getByteCount() {
		return this.asr.getByteCount();
	}
	
	public U64 getPacketCount() {
		return this.asr.getPacketCount();
	}
	
	public long getFlowCount(){
		return this.asr.getFlowCount();
	}
	
	public OFAggregateStatsReply getReply(){
		return this.asr;
	}
	
	public OVXAggregateStatisticsReply setFlowCount(long flowCount) {
		this.asr = this.asr.createBuilder().setFlowCount(flowCount).build();
		return this;
	}

	public OVXAggregateStatisticsReply setByteCount(long byteCount) {
		this.asr = this.asr.createBuilder().setByteCount(U64.of(byteCount)).build();
		return this;
	}

	public OVXAggregateStatisticsReply setPacketCount(long packetCount) {
		this.asr = this.asr.createBuilder().setPacketCount(U64.of(packetCount)).build();
		return this;
	}
}
