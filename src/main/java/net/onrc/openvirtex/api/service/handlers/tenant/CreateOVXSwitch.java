/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.exceptions.NetworkMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class CreateOVXSwitch extends ApiHandler<Map<String, Object>> {

	Logger log = LogManager.getLogger(CreateOVXSwitch.class.getName());

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;

		try {
			final Number tenantId = HandlerUtils.<Number> fetchField(
					TenantHandler.TENANT, params, true, null);
			final List<Number> dpids = HandlerUtils.<List<Number>> fetchField(
					TenantHandler.DPIDS, params, true, null);
			final Long dp = HandlerUtils.<Number> fetchField(
					TenantHandler.DPID, params, false, 0).longValue();

			HandlerUtils.isValidTenantId(tenantId.intValue());
			
			final OVXMap map = OVXMap.getInstance();
			final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId
					.intValue());
			final List<Long> longDpids = new ArrayList<Long>();
			for (final Number dpid : dpids) {
				longDpids.add(dpid.longValue());
			}
			
			HandlerUtils.isValidDPID(tenantId.intValue(), longDpids);
			final OVXSwitch ovxSwitch;
			if (dp == 0)
				ovxSwitch = virtualNetwork.createSwitch(longDpids);
			else
				ovxSwitch = virtualNetwork.createSwitch(longDpids, dp);	
			
			if (ovxSwitch == null) {
				resp = new JSONRPC2Response(new JSONRPC2Error(JSONRPC2Error.INTERNAL_ERROR.getCode(), this.cmdName()), 0);
			} else {
				this.log.info(
						"Created virtual switch {} in virtual network {}",
						ovxSwitch.getSwitchName(), virtualNetwork.getTenantId());
				Map<String, Object> reply = new HashMap<String, Object>(ovxSwitch.getDBObject());
				reply.put(TenantHandler.TENANT,  ovxSwitch.getTenantId());
				resp = new JSONRPC2Response(reply, 0);
			}

		} catch (final MissingRequiredField e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": Unable to create virtual switch : "
					+ e.getMessage()), 0);
		} catch (final InvalidDPIDException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": Invalid DPID : " + e.getMessage()), 0);
		} catch (final InvalidTenantIdException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": Invalid tenant id : " + e.getMessage()), 0);
		} catch (final NetworkMappingException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": " + e.getMessage()), 0);
		} catch (final IndexOutOfBoundException e) {
			resp = new JSONRPC2Response(
					new JSONRPC2Error(
							JSONRPC2Error.INVALID_PARAMS.getCode(),
							this.cmdName()
							+ ": Impossible to create the virtual switch, too many switches in this virtual network : "
							+ e.getMessage()), 0);
		}
		return resp;

	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
