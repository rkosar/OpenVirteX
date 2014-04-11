/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFQueueGetConfigRequest;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;

public class OVXQueueGetConfigRequest implements Devirtualizable {
	private OFQueueGetConfigRequest qgcr;
	
	public OVXQueueGetConfigRequest(OFMessage m) {
		this.qgcr = (OFQueueGetConfigRequest) m;
	}
	
	@Override
	public void devirtualize(final OVXSwitch sw) {		
		final OVXPort p = sw.getPort(this.qgcr.getPort().getPortNumber());
		if (p == null) {
			sw.sendMsg(OVXMessageUtil.makeErrorMsg(OFBadRequestCode.EPERM, this.qgcr), sw);
			return;
		}

		OVXMessageUtil.translateXid(this.qgcr, p);
	}
}
