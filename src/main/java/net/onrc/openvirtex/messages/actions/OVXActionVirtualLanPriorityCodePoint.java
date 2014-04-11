/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.actions;

import java.util.List;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetVlanPcp;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.protocol.OVXMatch;

public class OVXActionVirtualLanPriorityCodePoint implements VirtualizableAction {
	private OFActionSetVlanPcp asvp;
	
	public OVXActionVirtualLanPriorityCodePoint(OFAction action) {
		this.asvp = (OFActionSetVlanPcp) action;
	}
	
	@Override
	public void virtualize(final OVXSwitch sw,
			final List<OFAction> approvedActions, final OVXMatch match)
			throws ActionVirtualizationDenied {
		approvedActions.add(this.asvp);
	}
}
