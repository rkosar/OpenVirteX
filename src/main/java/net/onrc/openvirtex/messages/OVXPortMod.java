/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;

import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortMod;
import org.projectfloodlight.openflow.types.OFPort;

public class OVXPortMod  implements Devirtualizable {
	private OFPortMod pm;
	
	
	public OVXPortMod(OFMessage m) {
		this.pm = (OFPortMod) m;
	}
	
	@Override
	public void devirtualize(final OVXSwitch sw) {
		// TODO Auto-generated method stub
		// assume port numbers are virtual
		
		final OVXPort p = sw.getPort(this.pm.getPortNo().getPortNumber());
		if (p == null) {
			sw.sendMsg(OVXMessageUtil.makeErrorMsg(
					OFBadRequestCode.EPERM, this.pm), sw);
			return;
		}
		// set physical port number - anything else to do?
		//this.setPortNumber(phyPort.getPortNumber());
		this.pm = this.pm.createBuilder().setPortNo(OFPort.of(p.getPhysicalPort().getPortNumber())).build();

		OVXMessageUtil.translateXid(this.pm, p);
	}
}
