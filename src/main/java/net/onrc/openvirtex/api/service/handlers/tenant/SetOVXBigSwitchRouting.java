/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.HashMap;
import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.RoutingAlgorithmException;
import net.onrc.openvirtex.routing.RoutingAlgorithms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class SetOVXBigSwitchRouting extends ApiHandler<Map<String, Object>> {

	Logger log = LogManager.getLogger(SetOVXBigSwitchRouting.class.getName());

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;

		try {
			final Number tenantId = HandlerUtils.<Number> fetchField(
					TenantHandler.TENANT, params, true, null);
			final Number dpid = HandlerUtils.<Number> fetchField(
					TenantHandler.VDPID, params, true, null);
			final String alg = HandlerUtils.<String> fetchField(
					TenantHandler.ALGORITHM, params, true, null);
			final Number backupNumber = HandlerUtils.<Number> fetchField(
					TenantHandler.BACKUPS, params, true, null);

			HandlerUtils.isValidTenantId(tenantId.intValue());
			HandlerUtils.isValidOVXBigSwitch(tenantId.intValue(),
					dpid.longValue());

			final OVXMap map = OVXMap.getInstance();
			final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId
					.intValue());

			final RoutingAlgorithms algorithm = virtualNetwork
					.setOVXBigSwitchRouting(dpid.longValue(), alg,
							backupNumber.byteValue());

			if (algorithm == null) {
				resp = new JSONRPC2Response(false, 0);
			} else {
				this.log.info(
						"Set routing algorithm {} for big-switch {} in virtual network {}",
						algorithm.getRoutingType().getValue(), virtualNetwork
						.getSwitch(dpid.longValue()).getSwitchName(),
						virtualNetwork.getTenantId());
				OVXBigSwitch ovxSwitch = (OVXBigSwitch) virtualNetwork.getSwitch(dpid.longValue());
				Map<String, Object> reply = new HashMap<String, Object>(ovxSwitch.getDBObject());
				reply.put(TenantHandler.TENANT, ovxSwitch.getTenantId());
				resp = new JSONRPC2Response(reply, 0);
			}

		} catch (final MissingRequiredField e) {
			resp = new JSONRPC2Response(
					new JSONRPC2Error(
							JSONRPC2Error.INVALID_PARAMS.getCode(),
							this.cmdName()
							+ ": Unable to set routing mode for big-switch : "
							+ e.getMessage()), 0);
		} catch (final InvalidDPIDException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": Invalid DPID : " + e.getMessage()), 0);
		} catch (final InvalidTenantIdException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": Invalid tenant id : " + e.getMessage()), 0);
		} catch (final RoutingAlgorithmException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": Invalid algorithm : " + e.getMessage()), 0);
		} catch (final NetworkMappingException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
					+ ": " + e.getMessage()), 0);
		}
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
