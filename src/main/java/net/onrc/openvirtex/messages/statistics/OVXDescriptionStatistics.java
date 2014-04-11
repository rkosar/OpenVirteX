/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.statistics;

import org.jboss.netty.buffer.ChannelBuffer;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFDescStatsReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFVersion;

import net.onrc.openvirtex.core.OpenVirteX;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatisticsReply;
import net.onrc.openvirtex.messages.OVXStatisticsRequest;

public class OVXDescriptionStatistics  implements VirtualizableStatistic, DevirtualizableStatistic {
	private OFDescStatsReply dsr;
	
	public OVXDescriptionStatistics(OFVersion ofVersion) {
		this.dsr = OFFactories.getFactory(ofVersion).buildDescStatsReply().build();
	}
	
	/**
	 * Received a Description stats request from the controller Create a reply
	 * object populated with the virtual switch params and send it back to the
	 * controller.
	 */
	@Override
	public void devirtualizeStatistic(final OVXSwitch sw, final OVXStatisticsRequest msg) {
		OFDescStatsReply sr = OFFactories.getFactory(sw.getVersion())
				.buildDescStatsReply()
				.setDpDesc(OVXSwitch.DPDESCSTRING)
				.setHwDesc("virtual hardware")
				.setMfrDesc("Open Networking Lab")
				.setSerialNum(sw.getSwitchName())
				.setSwDesc(OpenVirteX.VERSION)
				.setXid(msg.getXid())
				.build();

		sw.sendMsg(sr, sw);
		
		/*final OVXDescriptionStatistics desc = new OVXDescriptionStatistics(sw.getVersion());
		desc.setDatapathDescription(OVXSwitch.DPDESCSTRING)
			.setHardwareDescription("virtual hardware")
			.setManufacturerDescription("Open Networking Lab")
			.setSerialNumber(sw.getSwitchName())
			.setSoftwareDescription(OpenVirteX.VERSION);

		final OVXStatisticsReply reply = new OVXStatisticsReply(OFStatsType.DESC, sw.getVersion());
		//reply.setLengthU(reply.getLength() + desc.getLength());
		reply.setXid(msg.getXid())
			 .setStatistics(Collections.singletonList(desc));
		sw.sendMsg(reply.getStatsReply(), sw);*/
	}

	@Override
	public void virtualizeStatistic(final PhysicalSwitch sw,
			final OVXStatisticsReply msg) {
		// log.error("Received illegal message form physical network; {}", msg);

	}
	
	public OVXDescriptionStatistics setDescStat(OFDescStatsReply descstats) {
		this.dsr = this.dsr.createBuilder()
				.setDpDesc(descstats.getDpDesc())
				.setFlags(descstats.getFlags())
				.setHwDesc(descstats.getHwDesc())
				.setMfrDesc(descstats.getMfrDesc())
				.setSerialNum(descstats.getSerialNum())
				.setSwDesc(descstats.getSwDesc())
				.setXid(descstats.getXid())
				.build();
		return this;
	}
	

	public String getHwDesc() {
		return this.dsr.getHwDesc();
	}

	public void readFrom(ChannelBuffer data) throws OFParseError {
		OFMessageReader<OFMessage> reader = OFFactories.getGenericReader();
		this.dsr = ((OFDescStatsReply) reader.readFrom(data));
	}
}