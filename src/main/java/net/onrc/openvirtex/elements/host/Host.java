/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.elements.host;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.db.DBManager;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.Persistable;
import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.address.OVXIPAddress;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.util.MACAddress;

public class Host implements Persistable {
	
	static Logger log = LogManager.getLogger(Host.class.getName());
	
	public static final String DB_KEY = "hosts"; 

	private final Integer hostId;
	private final MACAddress mac;
	private final OVXPort port;

	private OVXIPAddress ipAddress = new OVXIPAddress(0, 0);

	public Host(final MACAddress mac, final OVXPort port, final Integer hostId) {
		this.mac = mac;
		this.port = port;
		this.hostId = hostId;
	}

	public void setIPAddress(int ip) {
		this.ipAddress  = new OVXIPAddress(this.port.getTenantId(), ip);
	}
	
	public OVXIPAddress getIp() {
		return this.ipAddress;
	}
	
	public MACAddress getMac() {
		return mac;
	}

	public OVXPort getPort() {
		return port;
	}

	public void register() {
		DBManager.getInstance().save(this);
	}

	@Override
	public Map<String, Object> getDBIndex() {
		Map<String, Object> index = new HashMap<String, Object>();
		index.put(TenantHandler.TENANT, this.port.getTenantId());
		return index;
	}

	@Override
	public String getDBKey() {
		return Host.DB_KEY;
	}

	@Override
	public String getDBName() {
		return DBManager.DB_VNET;
	}

	@Override
	public Map<String, Object> getDBObject() {
		Map<String, Object> dbObject = new HashMap<String, Object>();
		dbObject.put(TenantHandler.VDPID, this.port.getParentSwitch().getSwitchId());
		dbObject.put(TenantHandler.VPORT, this.port.getPortNumber());
		dbObject.put(TenantHandler.MAC, this.mac.toLong());
		dbObject.put(TenantHandler.HOST, this.hostId);
		return dbObject;
	}

	public Integer getHostId() {
		return hostId;
	}

	public void unregister() {
		try {
			DBManager.getInstance().remove(this);
			this.tearDown();
			Mappable map = this.port.getParentSwitch().getMap();
			map.removeMAC(this.mac);
			map.getVirtualNetwork(port.getTenantId()).removeHost(this);
		} catch (NetworkMappingException e) {
			//log object?
		}

	}
	
	public void tearDown() {
		this.port.tearDown();	
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Host other = (Host) obj;
		if (mac == null) {
			if (other.mac != null)
				return false;
		} else if (!mac.equals(other.mac))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}

	
	/*
	 * Super ugly method to convert virtual elements of 
	 * a host to physical data
	 */
	public HashMap<String, Object> convertToPhysical() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("hostId", this.hostId);
		map.put("dpid", this.port.getPhysicalPort().getParentSwitch().getSwitchName());
		map.put("port", port.getPhysicalPortNumber());
		map.put("mac", this.mac.toString());
		
		if (this.ipAddress.getIp() != 0)
			try {
				map.put("ipAddress", OVXMap.getInstance().getPhysicalIP(this.ipAddress, this.port.getTenantId()).toSimpleString());
			} catch (AddressMappingException e) {
				log.warn("Unable to fetch physical IP for host");
			}
		return map;
	}
}
