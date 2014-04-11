/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages.actions;

import java.util.List;

import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.types.IPv4Address;



public class OVXActionNetworkLayerDestination  implements VirtualizableAction {
	private OFActionSetNwDst asnd;
	private final Logger log = LogManager.getLogger(OVXActionNetworkLayerDestination.class.getName());
	
	public OVXActionNetworkLayerDestination(OFAction action) {
		this.asnd = (OFActionSetNwDst) action;
	}
	
	public OVXActionNetworkLayerDestination(OFVersion ofversion) {
		this.asnd = OFFactories.getFactory(ofversion).actions().buildSetNwDst().build();
	}

	@Override
	public void virtualize(final OVXSwitch sw,
			final List<OFAction> approvedActions, final OVXMatch match)
			throws ActionVirtualizationDenied {
		
		this.asnd = this.asnd.createBuilder()
				.setNwAddr(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(), this.asnd.getNwAddr().getInt())))
				.build();
		
		//this.networkAddress = IPMapper.getPhysicalIp(sw.getTenantId(), this.networkAddress);
		log.debug("Allocating Physical IP {}", new PhysicalIPAddress(this.asnd.getNwAddr().toString()));
		approvedActions.add(this.asnd);
	}

	public void setNetworkAddress(Integer physicalIp) {
		this.asnd = this.asnd.createBuilder().setNwAddr(IPv4Address.of(physicalIp)).build();
	}
	
	public OFActionSetNwDst getAction() {
		return this.asnd;
	}
}
