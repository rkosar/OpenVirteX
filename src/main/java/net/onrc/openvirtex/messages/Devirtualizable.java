/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.messages;


import net.onrc.openvirtex.elements.datapath.OVXSwitch;

public interface Devirtualizable {
	public void devirtualize(OVXSwitch sw);
}
