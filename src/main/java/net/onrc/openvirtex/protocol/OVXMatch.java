/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.protocol;

import java.util.HashMap;

import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.types.*;


/**
 * The Class OVXMatch. This class extends the Match class, in order to carry some useful informations for OpenVirteX, 
 * as the cookie (used by flowMods messages) and the packet data (used by packetOut messages)
 */
public class OVXMatch {

	private Match match;
	
    /** The Constant serialVersionUID. */
    //private static final long serialVersionUID = 1L;
    
    /** The cookie. */
    protected U64            cookie;
    
    /** The pkt data. */
    protected byte[]          pktData;

    /**
     * Instantiates a new void OVXatch.
     */
    public OVXMatch() {
    	super();
    	this.cookie = U64.of(0);
    	this.pktData = null;
    }

    /**
     * Instantiates a new OVXmatch from an Match instance.
     *
     * @param match the match
     */
    
    public OVXMatch(final Match match) {
    	this.match = match;
    	this.cookie = U64.of(0);
		this.pktData = null;
    
	    /*
		this.wildcards = match.getWildcards();
		this.inputPort = match.getInputPort();
		this.dataLayerSource = match.getDataLayerSource();
		this.dataLayerDestination = match.getDataLayerDestination();
		this.dataLayerVirtualLan = match.getDataLayerVirtualLan();
		this.dataLayerVirtualLanPriorityCodePoint = match
		        .getDataLayerVirtualLanPriorityCodePoint();
		this.dataLayerType = match.getDataLayerType();
		this.networkTypeOfService = match.getNetworkTypeOfService();
		this.networkProtocol = match.getNetworkProtocol();
		this.networkSource = match.getNetworkSource();
		this.networkDestination = match.getNetworkDestination();
		this.transportSource = match.getTransportSource();
		this.transportDestination = match.getTransportDestination();
		*/
    }	

    /**
     * Get cookie.
     *
     * @return the cookie
     */
    public U64 getCookie() {
	return this.cookie;
    }

    /**
     * Set cookie.
     *
     * @param cookie the cookie
     * @return the oVX match
     */
    public OVXMatch setCookie(final U64 cookie) {
	this.cookie = cookie;
	return this;
    }

    /**
     * Gets the pkt data.
     *
     * @return the pkt data
     */
    public byte[] getPktData() {
    	return this.pktData;
    }

    /**
     * Sets the pkt data.
     *
     * @param pktData the new pkt data
     */
    public void setPktData(final byte[] pktData) {
    	this.pktData = pktData;
    }

    /**
     * Checks if this match belongs to a flow mod (e.g. the cookie is not zero).
     *
     * @return true, if is flow mod
     */
    public boolean isFlowMod() {
    	return this.cookie.compareTo(U64.of(0)) != 0;
    }

    /**
     * Checks if this match belongs to a packet out (e.g. the packet data is not null).
     *
     * @return true, if is packet out
     */
    public boolean isPacketOut() {
    	return this.pktData != null;
    }

	public static class cidrToIp {
		public static String cidrToString(final int ip, final int prefix) {
			String str;
			if (prefix >= 32) {
				str = IPv4Address.of(ip).toString();
			} else {
				// use the negation of mask to fake endian magic
				final int mask = ~((1 << 32 - prefix) - 1);
				str = IPv4AddressWithMask.of(ip, mask).toString() + "/" + prefix;
			}

			return str;
		}
	}

	public HashMap<MatchFields, Object> toMap() {
		final HashMap<MatchFields, Object> ret = new HashMap<MatchFields, Object>();
		
		//ret.put("wildcards", this.match.);
		
		// l1
		//if ((this.wildcards & MatchFields.IN_PORT) == 0) {
		if (this.match.isExact(MatchField.IN_PORT)) {
			ret.put(MatchFields.IN_PORT, this.match.get(MatchField.IN_PORT).toString());
		}

		// l2
		//if ((this.wildcards & MatchFields.DL_DST) == 0) {
		if (this.match.isExact(MatchField.ETH_DST)) {
			ret.put(MatchFields.ETH_DST, this.match.get(MatchField.ETH_DST).toString());
		}

		
		//if ((this.wildcards & MatchFields.OFPFW_DL_SRC) == 0) {
		if (this.match.isExact(MatchField.ETH_SRC)) {
			ret.put(MatchFields.ETH_SRC, this.match.get(MatchField.ETH_SRC).toString());
		}

		
		//if ((this.wildcards & OFMatch.OFPFW_DL_TYPE) == 0) {
		if (this.match.isExact(MatchField.ETH_TYPE)) {
			ret.put(MatchFields.ETH_TYPE, this.match.get(MatchField.ETH_TYPE).toString());
		}

		//if ((this.wildcards & OFMatch.OFPFW_DL_VLAN) == 0) {
		if (this.match.isExact(MatchField.VLAN_VID)) {
			ret.put(MatchFields.VLAN_VID, this.match.get(MatchField.VLAN_VID).toString());
		}

		//if ((this.wildcards & OFMatch.OFPFW_DL_VLAN_PCP) == 0) {
		if (this.match.isExact(MatchField.VLAN_PCP)) {
			ret.put(MatchFields.VLAN_PCP, this.match.get(MatchField.VLAN_PCP).toString());
		}

		// l3
		if (this.match.isExact(MatchField.IPV4_DST)) {
			ret.put(MatchFields.IPV4_DST, cidrToIp.cidrToString(this.match.get(MatchField.IPV4_DST).getInt(), 
															    this.match.get(MatchField.IPV4_DST).asCidrMaskLength()));
		}
		
		if (this.match.isExact(MatchField.IPV4_SRC)) {
			ret.put(MatchFields.IPV4_SRC, cidrToIp.cidrToString(this.match.get(MatchField.IPV4_SRC).getInt(), 
															    this.match.get(MatchField.IPV4_SRC).asCidrMaskLength()));
		}
		
		if (this.match.isExact(MatchField.IP_PROTO)) {
			ret.put(MatchFields.IP_PROTO, this.match.get(MatchField.IP_PROTO).toString());
		}

		if (this.match.isExact(MatchField.IP_DSCP)) {
			ret.put(MatchFields.IP_DSCP, this.match.get(MatchField.IP_DSCP).toString());
		}

		if (this.match.isExact(MatchField.ARP_SPA)) {
			ret.put(MatchFields.ARP_SPA, cidrToIp.cidrToString(this.match.get(MatchField.ARP_SPA).getInt(), 
															    this.match.get(MatchField.ARP_SPA).asCidrMaskLength()));
		}
		
		if (this.match.isExact(MatchField.ARP_TPA)) {
			ret.put(MatchFields.ARP_TPA, cidrToIp.cidrToString(this.match.get(MatchField.ARP_TPA).getInt(), 
															    this.match.get(MatchField.ARP_TPA).asCidrMaskLength()));
		}
		
		// l4
		if (this.match.isExact(MatchField.TCP_DST)) {
			ret.put(MatchFields.TCP_DST, this.match.get(MatchField.TCP_DST).toString());
		}

		if (this.match.isExact(MatchField.TCP_SRC)) {
			ret.put(MatchFields.TCP_SRC, this.match.get(MatchField.TCP_SRC).toString());
		}
		
		return ret;
	}

	public Integer getInputPort() {
		return this.match.get(MatchField.IN_PORT).getPortNumber();
	}

	public byte[] getDataLayerSource() {
		return this.match.get(MatchField.ETH_SRC).getBytes();
	}

	public byte[] getDataLayerDestination() {
		return this.match.get(MatchField.ETH_DST).getBytes();
	}

	public Match getMatch() {
		return this.match;
	}
}
