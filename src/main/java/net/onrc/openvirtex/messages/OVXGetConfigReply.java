/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFGetConfigReply;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;


public class OVXGetConfigReply implements Virtualizable {
	private OFGetConfigReply gcr;
	
	public OVXGetConfigReply(OFMessage m) {
		this.gcr = ((OFGetConfigReply) m);
	}
	
	public OVXGetConfigReply(OFVersion ofversion) {
		this.gcr = OFFactories.getFactory(ofversion).buildGetConfigReply().build();
	}
	
	@Override
	public void virtualize(final PhysicalSwitch sw) {
		// TODO Auto-generated method stub
	}
	
	public OFGetConfigReply getConfigReply() {
		return this.gcr;
	}
	
	public OVXGetConfigReply setMissSendLen(int missSendLen) {
		this.gcr = this.gcr.createBuilder().setMissSendLen(missSendLen).build();
		return this;
	}

	public OVXGetConfigReply setXid(long xid) {
		this.gcr = this.gcr.createBuilder().setXid(xid).build();
		return this;
	}
}
