/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.messages;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFNiciraControllerRoleReply;
import org.projectfloodlight.openflow.protocol.OFNiciraControllerRoleRequest;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;

public class OVXVendor implements Virtualizable, Devirtualizable {
	private OFNiciraControllerRoleReply rrep;
	private OFNiciraControllerRoleRequest rreq;

	
	public OVXVendor(OFMessage m) {
		if (m instanceof OFNiciraControllerRoleReply)
			this.rrep = (OFNiciraControllerRoleReply) m;
		else if (m instanceof OFNiciraControllerRoleRequest)
			this.rreq = (OFNiciraControllerRoleRequest) m;
	}
	
	@Override
	public void devirtualize(final OVXSwitch sw) {
		OVXMessageUtil.translateXidAndSend(this.rreq, sw);
	}

	@Override
	public void virtualize(final PhysicalSwitch sw) {
		OVXMessageUtil.untranslateXidAndSend(this.rrep, sw);
	}
}
