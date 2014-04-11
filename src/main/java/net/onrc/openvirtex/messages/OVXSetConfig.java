/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSetConfig;

public class OVXSetConfig implements Devirtualizable {
	private final Logger log = LogManager.getLogger(OVXSetConfig.class
			.getName());
	/** miss_send_len for a full packet (-1) (short) */
	public static final int MSL_FULL = (int)0xFFFF;
	/** default miss_send_len when unspecified */
	public static final int MSL_DEFAULT = (int)0x0080;

	private OFSetConfig sc;
	
	public OVXSetConfig(OFMessage m) {
		this.sc =  ((OFSetConfig) m);
	}
	
	@Override
	public void devirtualize(final OVXSwitch sw) {
		sw.setMissSendLen(this.sc.getMissSendLen());
		this.log.info("Setting miss send length to {} for OVXSwitch {}",
					this.sc.getMissSendLen(), sw.getSwitchName());

		//sw.setMissSendLen(this.missSendLength);
		//this.log.info("Setting miss send length to {} for OVXSwitch {}",
		//		this.missSendLength, sw.getSwitchName());
	}
}
