/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.elements.port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortReason;
import org.projectfloodlight.openflow.protocol.OFPortState;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.db.DBManager;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.link.PhysicalLink;
import net.onrc.openvirtex.messages.OVXPortStatus;


public class PhysicalPort extends Port<PhysicalSwitch, PhysicalLink> {

	private final Map<Integer, HashMap<Integer, OVXPort>> ovxPortMap;

	/**
	 * Instantiate PhysicalPort based on an OpenFlow physical port
	 * 
	 * @param port
	 * @param sw
	 */
	public PhysicalPort(final OFPortDesc portdesc, final PhysicalSwitch sw,
			final boolean isEdge) {
		this.pd = OFFactories.getFactory(portdesc.getVersion())
				.buildPortDesc()
				.setAdvertised(portdesc.getAdvertised())
				.setConfig(portdesc.getConfig())
				.setCurr(portdesc.getCurr())
				.setHwAddr(portdesc.getHwAddr())
				.setName(portdesc.getName())
				.setPeer(portdesc.getPeer())
				.setPortNo(portdesc.getPortNo())
				.setState(portdesc.getState())
				.setSupported(portdesc.getSupported())
				.build();
		this.mac = this.pd.getHwAddr();
		
		this.parentSwitch = sw;
		this.isEdge = isEdge;
		this.ovxPortMap = new HashMap<Integer, HashMap<Integer, OVXPort>>();
	}

	public OVXPort getOVXPort(final Integer tenantId, final Integer vLinkId) {
		if (this.ovxPortMap.get(tenantId) == null) {
			return null;
		}
		
		OVXPort p = this.ovxPortMap.get(tenantId).get(vLinkId);
		
		if (p != null && !p.isActive())
			return null;
		return p;
	}

	public void setOVXPort(final OVXPort ovxPort) {
		if (this.ovxPortMap.get(ovxPort.getTenantId()) != null) {
		    if (ovxPort.getLink() != null)
		    	this.ovxPortMap.get(ovxPort.getTenantId())
		    		.put(ovxPort.getLink().getInLink().getLinkId(), ovxPort);
		    else 
		    	this.ovxPortMap.get(ovxPort.getTenantId())
		    		.put(0, ovxPort);
		} else {
			final HashMap<Integer, OVXPort> portMap = new HashMap<Integer, OVXPort>();
			if (ovxPort.getLink() != null)
			    portMap.put(ovxPort.getLink().getOutLink().getLinkId(), ovxPort);
			else 
			    portMap.put(0, ovxPort);
			this.ovxPortMap.put(ovxPort.getTenantId(), portMap);
		}
	}

	@Override
	public Map<String, Object> getDBIndex() {
		return null;
	}

	@Override
	public String getDBKey() {
		return null;
	}

	@Override
	public String getDBName() {
		return DBManager.DB_VNET;
	}

	@Override
	public Map<String, Object> getDBObject() {
		Map<String, Object> dbObject = new HashMap<String, Object>();
		dbObject.put(TenantHandler.DPID, this.getParentSwitch().getSwitchId());
		dbObject.put(TenantHandler.PORT, this.pd.getPortNo());
		return dbObject;
	}
	
	public void removeOVXPort(OVXPort ovxPort) {
	    if (this.ovxPortMap.containsKey(ovxPort.getTenantId())) {
		this.ovxPortMap.remove(ovxPort.getTenantId());
	    }
	}
	
	public OFPortDesc getPortDesc()
	{
		return pd;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (this == that)
			return true;
		if (!(that instanceof PhysicalPort))
			return false;
		
		PhysicalPort port = (PhysicalPort) that;
	    return this.pd.getPortNo() == port.pd.getPortNo() 
	    		&& this.parentSwitch.getSwitchId() == port.getParentSwitch().getSwitchId();
	}
	    
	/**
	 * @param tenant The ID of the tenant of interest
	 * @return The OVXPorts that map to this PhysicalPort for a given tenant ID, if 
	 * tenant is null all of the OVXPorts mapping to this port
	 */
	public List<Map<Integer, OVXPort>> getOVXPorts(Integer tenant) {
	    	List<Map<Integer, OVXPort>> ports = new ArrayList<Map<Integer,OVXPort>>();
		if (tenant == null) {    	
		    	ports.addAll(this.ovxPortMap.values());
	    	} else {
			ports.add(this.ovxPortMap.get(tenant));
		}
	    	return Collections.unmodifiableList(ports);
	}
	
	/**
	 * Changes the attribute of this port according to a MODIFY PortStatus
	 * @param portstat
	 */
	public void applyPortStatus(OVXPortStatus portstat) {
		if (!portstat.isReason(OFPortReason.MODIFY)) {    	
			return;    
		}
		OFPortDesc psport = portstat.getDesc();
		
		this.pd = this.pd.createBuilder()
				.setPortNo(psport.getPortNo())
				.setHwAddr(psport.getHwAddr())
				.setName(psport.getName())
				.setConfig(psport.getConfig())
				.setState(psport.getState())
				.setCurr(psport.getCurr())
				.setAdvertised(psport.getAdvertised())
				.setSupported(psport.getSupported())
				.setPeer(psport.getPeer())
				.build();
		
		this.mac = this.pd.getHwAddr();
	}

	/**
	 * unmaps this port from the global mapping and its parent switch. 
	 */
	public void unregister() {
		/* remove links, if any */
		if ((this.portLink != null) && (this.portLink.exists())) {
			this.portLink.egressLink.unregister();
			this.portLink.ingressLink.unregister();
		}
	}

	public void setHardwareAddress(byte[] address) {
		this.mac = MacAddress.of(address);
	}

	public void setPortNumber(int portNo) {
		this.pd = this.pd
				.createBuilder()
				.setPortNo(OFPort.of(portNo))
				.build();
	}
	
	public byte [] getHardwareAddress() {
		return this.mac.getBytes();
	}

	public Set<OFPortState> getState() {
		return this.pd.getState();
	}
}
