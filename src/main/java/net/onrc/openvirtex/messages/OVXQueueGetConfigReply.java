/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.messages;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFQueueGetConfigReply;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;


public class OVXQueueGetConfigReply implements Virtualizable {
	OFQueueGetConfigReply qgc;
	
	public OVXQueueGetConfigReply(OFMessage m) {
		this.qgc = (OFQueueGetConfigReply) m;
	}
	
	@Override
	public void virtualize(final PhysicalSwitch sw) {
		
		final OVXSwitch vsw = OVXMessageUtil.untranslateXid(this.qgc, sw);
		if (vsw == null) {
			// log error
			return;
		}
		// re-write port mappings
	}
}
