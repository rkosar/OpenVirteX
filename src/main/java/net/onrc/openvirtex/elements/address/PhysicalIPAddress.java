/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.elements.address;

import net.onrc.openvirtex.core.OpenVirteXController;

public class PhysicalIPAddress extends IPAddress {

	public PhysicalIPAddress(final Integer ip) {
		this.ip = ip;
	}

	public PhysicalIPAddress(final String ipAddress) {
		super(ipAddress);
	}
	
	public Integer getTenantId() {
		return ip >> (32 - OpenVirteXController.getInstance().getNumberVirtualNets());
	}
}
