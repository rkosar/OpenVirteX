/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import java.util.List;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.messages.statistics.DevirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.OVXDescriptionStatistics;
import net.onrc.openvirtex.messages.statistics.OVXTableStatistics;
import net.onrc.openvirtex.messages.statistics.OVXVendorStatistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFVersion;

public class OVXStatisticsRequest  implements Devirtualizable { 
	private OFStatsRequest<?> sr; 
	
	private final Logger log = LogManager.getLogger(OVXStatisticsRequest.class.getName());
	private List<?> statistics;

	public OVXStatisticsRequest(OFMessage m) {
		this.sr = (OFStatsRequest<?>) m;
	}
	
	public OVXStatisticsRequest(OFStatsType type, OFVersion ofversion) {
		OFFactory factory = OFFactories.getFactory(ofversion);
		switch (type)
		{
		case FLOW: 	
			this.sr = factory.buildFlowStatsRequest().build();
			break;
		case AGGREGATE:
			this.sr = factory.buildAggregateStatsRequest().build();
			break;
		case DESC: 
			this.sr = factory.buildDescStatsRequest().build();
			break;
		case EXPERIMENTER:
			//this.srb =  null; // did on purpose, not sure what to map to 
			break;
		case GROUP:
			this.sr = factory.buildGroupStatsRequest().build();
			break;
		case GROUP_DESC:
			this.sr = factory.buildGroupDescStatsRequest().build();
			break;
		case GROUP_FEATURES:
			this.sr = factory.buildGroupFeaturesStatsRequest().build();
			break;
		case METER:
			this.sr = factory.buildMeterStatsRequest().build();
			break;
		case METER_CONFIG:
			this.sr = factory.buildMeterConfigStatsRequest().build();
			break;
		case METER_FEATURES:
			this.sr = factory.buildMeterFeaturesStatsRequest().build();
			break;
		case PORT:
			this.sr = factory.buildPortStatsRequest().build();
			break;
		case PORT_DESC:
			this.sr = factory.buildPortDescStatsRequest().build();
			break;
		case QUEUE:
			this.sr = factory.buildQueueStatsRequest().build();
			break;
		case TABLE:
			this.sr = factory.buildTableStatsRequest().build();
			break;
		case TABLE_FEATURES:
			this.sr = factory.buildTableFeaturesStatsRequest().build();
			break;
		default:
			this.sr = null;
			break;
		}
	}

	@Override
	public void devirtualize(final OVXSwitch sw) {
		switch (this.sr.getStatsType()) {
		// Desc, vendor, table stats have no body. fuckers.
		case DESC:
			new OVXDescriptionStatistics(sw.getVersion()).devirtualizeStatistic(sw, this);
			break;
		case TABLE:
			new OVXTableStatistics(sw.getVersion()).devirtualizeStatistic(sw, this);
			break;
		case EXPERIMENTER:
			new OVXVendorStatistics(sw.getVersion()).devirtualizeStatistic(sw, this);
			break;
		default:
			try {
				final Object stat = this.statistics.get(0);
				((DevirtualizableStatistic) stat).devirtualizeStatistic(sw, this);
			} catch (final ClassCastException e) {
				this.log.error("Statistic received is not devirtualizable {}", this);
			}
		}
	}
	
	public long getXid() {
		return this.sr.getXid();
	}

	public OFStatsRequest<?> getStats() {
		return this.sr;
	}

	public OVXStatisticsRequest setXid(long xid) {
		this.sr = this.sr.createBuilder().setXid(xid).build();
		return this;
	}
	
	public OVXStatisticsRequest setStatistics(List<?> statistics) {
		this.statistics = statistics;
		return this;
	}
}
