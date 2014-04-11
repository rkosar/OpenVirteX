/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import java.util.Set;
import java.util.TreeSet;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;

import org.projectfloodlight.openflow.protocol.OFConfigFlags;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFGetConfigReply;
import org.projectfloodlight.openflow.protocol.OFGetConfigRequest;
import org.projectfloodlight.openflow.protocol.OFMessage;

public class OVXGetConfigRequest implements Devirtualizable {
	private OFGetConfigRequest gcr;
	
	public OVXGetConfigRequest(OFMessage m) {
		this.gcr = (OFGetConfigRequest) m;
	}
	
	@Override
	public void devirtualize(final OVXSwitch sw) {
		//final OVXGetConfigReply reply = new OVXGetConfigReply(sw.getVersion());
		
		//reply.setMissSendLen(sw.getMissSendLen());
		//reply.setXid(this.gcr.getXid());

		Set<OFConfigFlags> flags = new TreeSet<OFConfigFlags>();
		flags.add(OFConfigFlags.FRAG_NORMAL);
		OFGetConfigReply rb = OFFactories.getFactory(sw.getVersion())
				.buildGetConfigReply()
				.setMissSendLen(sw.getMissSendLen())
				.setFlags(flags)
				.setXid(this.gcr.getXid())
				.build();
		
		sw.sendMsg(rb, sw);
		//sw.sendMsg(reply.getConfigReply(), sw);
	}
}
