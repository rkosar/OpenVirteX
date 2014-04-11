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
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwSrc;
import org.projectfloodlight.openflow.types.IPv4Address;

public class OVXActionNetworkLayerSource implements VirtualizableAction {

	private OFActionSetNwSrc asns;
	private final Logger log = LogManager.getLogger(OVXActionNetworkLayerSource.class.getName());
	
	public OVXActionNetworkLayerSource(OFAction action) {
		this.asns = (OFActionSetNwSrc) action;
	}

	public OVXActionNetworkLayerSource(OFVersion ofversion) {
		this.asns = OFFactories.getFactory(ofversion).actions().buildSetNwSrc().build();
	}

	@Override
	public void virtualize(final OVXSwitch sw,
			final List<OFAction> approvedActions, final OVXMatch match)
			throws ActionVirtualizationDenied {
		
		this.asns = this.asns.createBuilder().setNwAddr(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(), this.asns.getNwAddr().getInt()))).build();

		//this.networkAddress = IPMapper.getPhysicalIp(sw.getTenantId(), this.networkAddress);
		log.debug("Allocating Physical IP {}", new PhysicalIPAddress(this.asns.getNwAddr().toString()));
		approvedActions.add(this.asns);
	}

	public void setNetworkAddress(Integer physicalIp) {
		this.asns = this.asns.createBuilder().setNwAddr((IPv4Address.of(physicalIp))).build();
	}
	
	public OFActionSetNwSrc getAction() {
		return this.asns;
	}
}
