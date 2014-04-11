/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.exceptions.MappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFMessage;

public class OVXFlowRemoved implements Virtualizable {
	
	private OFFlowRemoved fr;
	Logger log = LogManager.getLogger(OVXFlowRemoved.class.getName());

	public OVXFlowRemoved(OFMessage m) {
		fr = (OFFlowRemoved) m;
	}
	@Override
	public void virtualize(final PhysicalSwitch sw) {
		
		int tid = (int) (this.fr.getCookie().getValue() >> 32);
		
		/* a PhysSwitch can be a OVXLink */
		if (!(sw.getMap().hasVirtualSwitch(sw, tid))) {
			return;
		}
		try {
			OVXSwitch vsw = sw.getMap().getVirtualSwitch(sw, tid);
			/* If we are a Big Switch we might receive multiple same-cookie FR's
			 * from multiple PhysicalSwitches. Only handle if the FR's newly seen */
			if (vsw.getFlowTable().hasFlowMod(this.fr.getCookie())) {
				OVXFlowMod fm = vsw.getFlowMod(this.fr.getCookie().getValue());
				/* send north ONLY if tenant controller wanted a FlowRemoved for the FlowMod*/
				vsw.deleteFlowMod(this.fr.getCookie().getValue());
				if (fm.hasFlag(OFFlowModFlags.SEND_FLOW_REM)) {
					writeFields(fm);
					vsw.sendMsg(this.fr, sw);
				}
			}
		} catch (MappingException e) {
			log.warn("Exception fetching FlowMod from FlowTable: {}", e);
		}
	}

	/**
	 * rewrites the fields of this message using values from the supplied FlowMod.  
	 * 
	 * @param fm the original FlowMod associated with this FlowRemoved
	 * @return the physical cookie 
	 */
	private void writeFields(OVXFlowMod fm) {
		fr = OFFactories.getFactory(fm.getVersion())
						.buildFlowRemoved()
						.setCookie(fm.getCookie())
						.setMatch(fm.getMatch())
						.setPriority(fm.getPriority())
						.build();
				
		//this.cookie = fm.getCookie();
		//this.match = fm.getMatch();
		//this.priority = fm.getPriority();
		//this.idleTimeout = fm.getIdleTimeout();
	}
	
	@Override
	public String toString() {
		return "OVXFlowRemoved: cookie=" + this.fr.getCookie()
				+ " priority=" + this.fr.getPriority()
				+ " match=" + this.fr.getMatch().toString()
				+ " reason=" + this.fr.getReason();
	}
}
