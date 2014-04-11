/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.messages;

import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.datapath.XidPair;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;

import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFBadActionCode;
import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowModFailedCode;
import org.projectfloodlight.openflow.protocol.OFPortModFailedCode;
import org.projectfloodlight.openflow.protocol.OFMessage;


public class OVXMessageUtil {

	public static OFMessage makeErrorMsg(final OFBadActionCode code,
			final OFMessage msg) {
		OFErrorMsg em = OFFactories.getFactory(msg.getVersion())
				.errorMsgs()
				.buildBadActionErrorMsg()
				.setCode(code)
				.setXid(msg.getXid())
				.build();
		
		return em;
	}

	public static OFMessage makeErrorMsg(final OFFlowModFailedCode code,
			final OFMessage msg) {
		OFErrorMsg em = OFFactories.getFactory(msg.getVersion())
				.errorMsgs()
				.buildFlowModFailedErrorMsg()
				.setCode(code)
				.setXid(msg.getXid())
				.build();
		
		return em;
	}

	public static OFMessage makeErrorMsg(final OFPortModFailedCode code,
			final OFMessage msg) {
		OFErrorMsg em = OFFactories.getFactory(msg.getVersion())
				.errorMsgs()
				.buildPortModFailedErrorMsg()
				.setCode(code)
				.setXid(msg.getXid())
				.build();
		
		return em;
	}

	public static OFMessage makeErrorMsg(final OFBadRequestCode code,
			final OFMessage msg) {
		OFErrorMsg em = OFFactories.getFactory(msg.getVersion())
				.errorMsgs()
				.buildBadRequestErrorMsg()
				.setCode(code)
				.setXid(msg.getXid())
				.build();
		
		return em;
	}

	/**
	 * Xid translation based on port for "accurate" translation with a specific
	 * PhysicalSwitch.
	 * 
	 * @param msg
	 * @param inPort
	 * @return
	 */
	public static OVXSwitch translateXid(OFMessage msg,
			final OVXPort inPort) {
		final OVXSwitch vsw = inPort.getParentSwitch();
		final long xid = vsw.translate(msg, inPort);
		
		msg = msg.createBuilder()
				.setXid(xid)
				.build();
		
		return vsw;
	}

	/**
	 * Xid translation based on OVXSwitch, for cases where port is
	 * indeterminable
	 * 
	 * @param msg
	 * @param vsw
	 * @return new Xid for msg
	 */
	public static Long translateXid(OFMessage msg, final OVXSwitch vsw) {
		// this returns the original XID for a BigSwitch
		final Long xid = vsw.translate(msg, null);
		
		msg = msg.createBuilder()
				.setXid(xid)
				.build();
		
		return xid;
	}

	/**
	 * translates the Xid of a PhysicalSwitch-bound message and sends it there.
	 * for when port is known.
	 * 
	 * @param msg
	 * @param inPort
	 */
	public static void translateXidAndSend(OFMessage msg,
			final OVXPort inPort) {
		final OVXSwitch vsw = OVXMessageUtil.translateXid(msg, inPort);
		vsw.sendSouth(msg, inPort);
	}

	/**
	 * translates the Xid of a PhysicalSwitch-bound message and sends it there.
	 * for when port is not known.
	 * 
	 * @param msg
	 * @param inPort
	 */
	public static void translateXidAndSend(OFMessage msg,
			final OVXSwitch vsw) {
		
		final long newXid = OVXMessageUtil.translateXid(msg, vsw);
		if (vsw instanceof OVXBigSwitch) {
			// no port info for BigSwitch, to all its PhysicalSwitches. Is this ok?
			try {
				for (final PhysicalSwitch psw : vsw.getMap().getPhysicalSwitches(vsw)) {
					final long xid = psw.translate(msg, vsw);
					msg = msg.createBuilder()
							.setXid(xid)
							.build();
					
					psw.sendMsg(msg, vsw);
					msg = msg.createBuilder()
							.setXid(newXid)
							.build();
				}
			} catch (SwitchMappingException e) {
				//log warning
			}
		} else {
			vsw.sendSouth(msg, null);
		}
	}

	public static OVXSwitch untranslateXid(OFMessage msg,
			final PhysicalSwitch psw) {
		final XidPair<OVXSwitch> pair = psw.untranslate(msg);
		if (pair == null) {
			return null;
		}
		msg = msg.createBuilder()
				.setXid(pair.getXid())
				.build();

		return pair.getSwitch();
	}

	/**
	 * undoes the Xid translation and tries to send the resulting message to the
	 * origin OVXSwitch.
	 * 
	 * @param msg
	 * @param psw
	 */
	public static void untranslateXidAndSend(final OFMessage msg,
			final PhysicalSwitch psw) {
		final OVXSwitch vsw = OVXMessageUtil.untranslateXid(msg, psw);
		if (vsw == null) {
			// log error
			return;
		}
		vsw.sendMsg(msg, psw);
	}
}
