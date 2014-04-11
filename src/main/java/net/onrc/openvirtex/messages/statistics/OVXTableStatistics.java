/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFTableStatsEntry;
import org.projectfloodlight.openflow.protocol.OFTableStatsReply;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.TableId;

import com.google.common.collect.ImmutableList;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatisticsReply;
import net.onrc.openvirtex.messages.OVXStatisticsRequest;

public class OVXTableStatistics  implements VirtualizableStatistic, DevirtualizableStatistic {
	private OFTableStatsEntry tse;
	
	public OVXTableStatistics(OFVersion ofVersion) {
		this.tse = OFFactories.getFactory(ofVersion).buildTableStatsEntry().build();
	}
	
	/*
	 * TODO
	 * Ideally, this would get information about the real flowtables
	 * and aggregate them in some smart way. This probably needs to 
	 * be discussed with the overall OVX team
	 */
	
	@Override
	public void devirtualizeStatistic(final OVXSwitch sw,
			final OVXStatisticsRequest msg) {
		
	    //public final static int NW_SRC_ALL_VAL = 0x2000;
	    //public final static int NW_DST_ALL_VAL = 0x80000;
	    //public final static int ALL_VAL = 0x3fffff;
	    
		this.tse = this.tse.createBuilder()
				.setActiveCount(sw.getFlowTable().getFlowTable().size())
				.setTableId(TableId.of(1))
				.setName("OVX vFlowTable (incomplete)")
				.setMaxEntries(100000)
				.setWildcards(0x3fffff & ~0x80000 & ~0x2000)
				.build(); // see above
		
		OFTableStatsReply reply = OFFactories.getFactory(sw.getVersion())
				.buildTableStatsReply()
				.setXid(msg.getXid())
				.setEntries(ImmutableList.<OFTableStatsEntry> of(this.tse))
				.build();

		sw.sendMsg(reply, sw);
		
		//this.activeCount = sw.getFlowTable().getFlowTable().size();
		//this.tableId = 1;
		/*
		 * FIXME
		 * Currently preventing controllers from wildcarding the IP
		 * field. That is if they actually look at this field.
		 */
		//this.wildcards = OFMatch.OFPFW_ALL 
		//		& ~OFMatch.OFPFW_NW_DST_ALL 
		//		& ~OFMatch.OFPFW_NW_DST_ALL;
		//this.name = "OVX vFlowTable (incomplete)";
		//this.maximumEntries = 100000;
		/*
		OVXStatisticsReply reply = new OVXStatisticsReply(OFStatsType.TABLE, sw.getVersion());
		reply.setXid(msg.getXid())
			 .setStatistics(Collections.singletonList(this.tseb.build()));
		*/
		//reply.setLengthU(OVXStatisticsReply.MINIMUM_LENGTH + this.getLength());
		//sw.sendMsg(reply.getStatsReply(), sw);
	}

	@Override
	public void virtualizeStatistic(final PhysicalSwitch sw,
			final OVXStatisticsReply msg) {
		// TODO Auto-generated method stub

	}
}
