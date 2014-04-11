/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.elements.port;

import java.util.Map;
import java.util.HashMap;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.Persistable;
import net.onrc.openvirtex.elements.datapath.DPIDandPort;
import net.onrc.openvirtex.elements.datapath.Switch;
import net.onrc.openvirtex.elements.link.Link;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.MacAddress;
/**
 * The Class Port.
 * 
 * @param <T1>
 * 		The Generic Switch type
 * @param <T2>
 * 		The Generic Link type
 */

@SuppressWarnings("rawtypes")
public class Port<T1 extends Switch, T2 extends Link> implements Persistable {
	public static final String DB_KEY = "ports";
	
	protected OFPortDesc pd;
	protected MacAddress mac;
	protected Boolean isEdge;
	protected T1 parentSwitch;
	protected LinkPair<T2> portLink;

	// TODO: duplexing/speed on port/link???
	/**
	 * Instantiates a new port.
	 */
	protected Port()
	{
	//default initilization should be done here
	}
	protected Port(final OFPortDesc ofPortdesc) {
		//super();
		this.pd = ofPortdesc;
		/*
				OFFactories.getFactory(ofPortdesc.getVersion())
				.buildPortDesc()
				.setHwAddr(ofPortdesc.getHwAddr())
				.setName(ofPortdesc.getName())
				.setConfig(ofPortdesc.getConfig())
				.setState(ofPortdesc.getState())
				.setCurr(ofPortdesc.getCurr())
				.setAdvertised(ofPortdesc.getAdvertised())
				.setSupported(ofPortdesc.getSupported())
				.setPeer(ofPortdesc.getPeer());
		*/
		if (ofPortdesc.getHwAddr() == null)
			this.pd = this.pd.createBuilder()
			.setHwAddr(MacAddress.of(new byte[] { (byte) 0xDE,(byte) 0xAD, (byte) 0xBE,(byte) 0xEF,(byte) 0xCA,(byte) 0xFE }))
			.build();
		
		this.mac = this.pd.getHwAddr();
		
		/*
		this.hardwareAddress = ofPortdesc.getHwAddr();
		this.name = ofPortdesc.getName();
		this.config = ofPortdesc.getConfig();
		this.state = ofPortdesc.getState();
		this.currentFeatures = ofPortdesc.getCurr();
		this.advertisedFeatures = ofPortdesc.getAdvertised();
		this.supportedFeatures = ofPortdesc.getSupported();
		this.peerFeatures = ofPortdesc.getPeer();
		
		if (this.hardwareAddress == null)
			this.hardwareAddress =  new byte[] { (byte) 0xDE,(byte) 0xAD, 
				(byte) 0xBE,(byte) 0xEF,(byte) 0xCA,(byte) 0xFE };
				this.mac = new MACAddress(this.hardwareAddress);
		*/
		
		this.isEdge = false;
		this.parentSwitch = null;
		this.portLink = null;
	}

	/*
	@Override
	public void setHardwareAddress(final byte[] hardwareAddress) {
		super.setHardwareAddress(hardwareAddress);
		// no way to update MACAddress instances
		this.mac = new MACAddress(hardwareAddress);
	}
	*/

	/**
	 * Gets the checks if is edge.
	 * 
	 * @return the checks if is edge
	 */
	public Boolean isEdge() {
		return this.isEdge;
	}

	/**
	 * Sets the checks if is edge.
	 * 
	 * @param isEdge
	 *            the new checks if is edge
	 */
	public void setEdge(final Boolean isEdge) {
		this.isEdge = isEdge;
	}

	public T1 getParentSwitch() {
		return this.parentSwitch;
	}

	/**
	 * Set the link connected to this port.
	 * @param link
	 */
	public void setInLink(T2 link) {
	    	if (this.portLink == null) {
			this.portLink = new LinkPair<T2>();
	    	}
	    	this.portLink.setInLink(link);
	}
	
	/**
	 * Set the link connected to this port.
	 * @param link
	 */
	public void setOutLink(T2 link) {
	    	if (this.portLink == null) {
			this.portLink = new LinkPair<T2>();
	    	}
	    	this.portLink.setOutLink(link);
	}
	
	/**
	 * @return The physical link connected to this port
	 */
	public LinkPair<T2> getLink() {
	    return this.portLink;
	}
	
	public Integer getPortNumber(){
		return this.pd.getPortNo().getPortNumber();
	}
	/**
	 * 
	 * @return the highest nominal throughput currently exposed by the port
	 */
	public Integer getCurrentThroughput() {
		//portdesc.getCurr().contains(CONTROLLER)
		PortFeatures feature = new PortFeatures();
	    return feature.getHighestThroughput();
	}
	
	@Override
	public String toString() {
		return "PORT:\n- portNumber: " + this.pd.getPortNo()
				+ "\n- parentSwitch: " + this.getParentSwitch().getSwitchName()
				+ "\n- hardwareAddress: " + this.pd.getHwAddr().toString()
				+ "\n- config: " + this.pd.getConfig().toArray().toString() //does this produce a meaningful list? Check! 
				+ "\n- state: " + this.pd.getState().toArray().toString() 
				+ "\n- currentFeatures: " + this.pd.getCurr().toString()
				+ "\n- advertisedFeatures: " + this.pd.getAdvertised().toString()
				+ "\n- supportedFeatures: " + this.pd.getSupported().toString()
				+ "\n- peerFeatures: " + this.pd.getPeer().toString()
				+ "\n- isEdge: " + this.isEdge;
	}
	
	/* should transient features of the Port be taken into account by hashCode()? They 
	 * prevent ports from being fetched from maps once their status changes. */
  	@Override
	public int hashCode() {
		final int prime = 307;
		int result = 1;
		result = prime * result + this.pd.getAdvertised().hashCode();
		result = prime * result + this.pd.getConfig().hashCode();
		result = prime * result + this.pd.getHwAddr().hashCode();
		//result = prime * result + Arrays.hashCode(portdesc.getHwAddr().hashCode());
		result = prime * result + this.pd.getName().hashCode();
		//		+ (portdesc.getName().hashCode() == null ? 0 : this.name.hashCode());
		result = prime * result + this.pd.getPortNo().getPortNumber();
		result = prime * result + this.parentSwitch.hashCode();
		return result;
	} 

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Port))
			return false;
		Port other = (Port) obj;
		if (parentSwitch == null) {
			if (other.parentSwitch != null)
				return false;
		} else if (!parentSwitch.equals(other.parentSwitch))
			return false;
		return true;
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
		return null;
	}

	@Override
	public Map<String, Object> getDBObject() {
		Map<String, Object> dbObject = new HashMap<String, Object>();
		dbObject.put(TenantHandler.DPID, this.parentSwitch.getSwitchId()); 
		dbObject.put(TenantHandler.PORT, this.pd.getPortNo().getPortNumber());
		return dbObject;
	}
	
	public DPIDandPort toDPIDandPort() {
		return new DPIDandPort(this.parentSwitch.getSwitchId(), this.pd.getPortNo().getPortNumber());
	}
}
