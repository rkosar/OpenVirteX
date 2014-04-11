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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.errormsg.OFErrorMsgs;

public class OVXError implements Virtualizable, Devirtualizable {
	private final Logger log = LogManager.getLogger(OVXError.class
             .getName());
	
	OFErrorMsg emsg;
	
	@Override
	public void devirtualize(final OVXSwitch sw) {
		// TODO Auto-generated method stub
	}

	@Override
	public void virtualize(final PhysicalSwitch sw) {
		/* TODO: For now, just report the error. 
		 * In the future parse them and forward to 
		 * controller if need be.
		 */
		log.error(getErrorString(emsg));
	}
	
	public OVXError(final OFErrorMsg emsg) {
		this.emsg = emsg;
	}
	
	public OFErrorMsg getErrorMsg() {
		return emsg;
	}
	
	 /**
     * Get a useable error string from the OFErrorMsg.
     * @param error
     * @return
     */
    private static String getErrorString(OFErrorMsg error) {
        // TODO: this really should be OFError.toString. Sigh.
    	OFErrorMsgs err_msg = OFFactories.getFactory(error.getVersion()).errorMsgs();
    	
    	String error_name = error.getErrType().name();
    	
        switch (error.getErrType()) {
            case HELLO_FAILED:
            	return String.format("Error %s %s", error_name, err_msg.buildHelloFailedErrorMsg());
            case BAD_REQUEST:
            	return String.format("Error %s %s", error_name, err_msg.buildBadRequestErrorMsg());
            case BAD_ACTION:
            	return String.format("Error %s %s", error_name, err_msg.buildBadActionErrorMsg());
            case FLOW_MOD_FAILED:
            	return String.format("Error %s %s", error_name, err_msg.buildFlowModFailedErrorMsg());
            case PORT_MOD_FAILED:
            	return String.format("Error %s %s", error_name, err_msg.buildPortModFailedErrorMsg());
            case QUEUE_OP_FAILED:
            	return String.format("Error %s %s", error_name, err_msg.buildQueueOpFailedErrorMsg());
            case EXPERIMENTER:
                return String.format("Error %s %s", error_name, err_msg.buildExperimenterErrorMsg());
            case BAD_INSTRUCTION:
                return String.format("Error %s %s", error_name, err_msg.buildBadInstructionErrorMsg());
            case BAD_MATCH:
            	return String.format("Error %s %s", error_name, err_msg.buildBadActionErrorMsg());
            case SWITCH_CONFIG_FAILED:
            	return String.format("Error %s %s", error_name, err_msg.buildSwitchConfigFailedErrorMsg());
            case ROLE_REQUEST_FAILED:
            	return String.format("Error %s %s", error_name, err_msg.buildRoleRequestFailedErrorMsg());	
            default:
            	return String.format("Unknown error type");
        }
    }
}