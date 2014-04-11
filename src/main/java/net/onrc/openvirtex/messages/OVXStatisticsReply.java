/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.messages;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFAggregateStatsReply;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;

public class OVXStatisticsReply implements Virtualizable {
	private OFStatsReply sr;
	private final Logger log = LogManager.getLogger(OVXStatisticsReply.class.getName());
	
	public OVXStatisticsReply(OFMessage m) {
		this.sr = (OFStatsReply) m;
	}

	@Override
	public void virtualize(final PhysicalSwitch sw) {
		/*
		 * The entire stat message will be handled in the 
		 * specific stattype handler. 
		 * 
		 * This means that for stattypes that have a list
		 * of replies the handles will have to call 
		 * getStatistics to handle them all.
		 */
		try {
			if (this.sr.getStatsType() == OFStatsType.AGGREGATE && ((OFAggregateStatsReply)this.sr).getPacketCount().getValue() > 0 ) 
			{
				VirtualizableStatistic stat = (VirtualizableStatistic) this.sr;
				stat.virtualizeStatistic(sw, this);
			} else if (this.sr.getStatsType() == OFStatsType.FLOW) {
				sw.setFlowStatistics(null);
			}
		} catch (final ClassCastException e) {
			this.log.error("Statistic received is not virtualizable {}", this);
		}
	}
}