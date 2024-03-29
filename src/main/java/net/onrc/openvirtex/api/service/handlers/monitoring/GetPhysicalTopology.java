/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.api.service.handlers.monitoring;

import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitchSerializer;

import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.elements.port.PhysicalPortSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

/**
 * Get the physical topology in json format
 * 
 * @return Physical topology in json format
 */
public class GetPhysicalTopology extends ApiHandler<Object> {

	@SuppressWarnings("unchecked")
	@Override
	public JSONRPC2Response process(final Object params) {
		Map<String, Object> result;
		JSONRPC2Response resp = null;
		// TODO: gson objects can be shared with other methods
		final GsonBuilder gsonBuilder = new GsonBuilder();
		//gsonBuilder.setPrettyPrinting();
		gsonBuilder.excludeFieldsWithoutExposeAnnotation();
		gsonBuilder.registerTypeAdapter(PhysicalSwitch.class,
				new PhysicalSwitchSerializer());
		gsonBuilder.registerTypeAdapter(PhysicalPort.class,
				new PhysicalPortSerializer());
		/*gsonBuilder.registerTypeAdapter(PhysicalLink.class,
				new PhysicalLinkSerializer());*/
		
		final Gson gson = gsonBuilder.create();
	
		result = gson.fromJson(gson.toJson(PhysicalNetwork.getInstance()), Map.class );
		resp = new JSONRPC2Response(result, 0);
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.NO_PARAMS;
	}

}
