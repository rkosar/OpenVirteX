/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.elements.datapath;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

/**
 * Class representing a virtual flow entry - a wrapper for FlowMods 
 * that enables the flow table to do matching on contents.
 */
public class OVXFlowEntry implements Comparable<OVXFlowEntry>{

	/* relation of this FlowEntry to another FlowEntry during comparison */
	public static int EQUAL = 0;	//exactly same
	public static int SUPERSET = 1;	//more general
	public static int SUBSET = 2;	//more specific
	public static int INTERSECT = 3;	//mix of wildcards and matching fields
	public static int DISJOINT = 4;	//non-matching non-wildcarded fields 

	/** the FlowMod this Entry represents */
	protected OVXFlowMod flowmod;
	/** the newly generated cookie for the FlowMod */
	protected long newcookie;

	public OVXFlowEntry() {
	}
	
	public OVXFlowEntry(OVXFlowMod fm, long cookie) {
		this.flowmod = fm.clone();
		this.newcookie = cookie;
	}

	/**
	 * Compares this entry against another, and tries to determine if
	 * it is a superset, subset, or equal to it. Required for non-strict 
	 * matching and overlap checking
	 * <p>
	 * For each field, we first check wildcard equality.
	 * If both are equal, they are either 1 or 0. 
	 * If 0, we further check for field equality. If the 
	 * fields are not equal, the flow entries are considered
	 * disjoint and we exit comparison. 
	 * <p>
	 * If both wildcards are not equal, we check if one 
	 * subsumes the other. 
	 * <p>
	 * The result is tracked for each field in three ints -
	 * equality, superset, and subset. 
	 * At the end, either 1) one of the ints are 0x3fffff,
	 * or 2) none are.
	 * 
	 * @param omatch The other FlowEntry to compare this one against. 
	 * @param strict whether FlowMod from which the match came was strict or not. 
	 * @return Union enum representing the relationship 
	 */
	public int compare(Match omatch, boolean strict) {
		//to allow pass by reference...in order: equal, superset, subset
		int [] intersect = new int[] {0, 0, 0};

		Match tmatch = this.flowmod.getMatch();
		int twcard = this.convertToWcards(tmatch);
		int owcard = this.convertToWcards(omatch);
		
		/* inport */
		if (tmatch.isFullyWildcarded(MatchField.IN_PORT) == omatch.isFullyWildcarded(MatchField.IN_PORT)) {
			if (findDisjoint(twcard, 
							 MatchField.IN_PORT.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.IN_PORT).getPortNumber(), 
							 omatch.get(MatchField.IN_PORT).getPortNumber())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.IN_PORT.id.ordinal(), intersect);
		}

		/* L2 */
		if (tmatch.isFullyWildcarded(MatchField.ETH_DST)  == omatch.isFullyWildcarded(MatchField.ETH_DST)) {
			if (findDisjoint(twcard, 
							 MatchField.ETH_DST.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.ETH_DST).getLong(), 
							 omatch.get(MatchField.ETH_DST).getLong())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.ETH_DST.id.ordinal(), intersect);
		}
		
		if (tmatch.isFullyWildcarded(MatchField.ETH_SRC)  == omatch.isFullyWildcarded(MatchField.ETH_SRC)) {
			if (findDisjoint(twcard, 
							 MatchField.ETH_SRC.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.ETH_SRC).getLong(), 
							 omatch.get(MatchField.ETH_SRC).getLong())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.ETH_SRC.id.ordinal(), intersect);
		}
		
		if (tmatch.isFullyWildcarded(MatchField.ETH_TYPE)  == omatch.isFullyWildcarded(MatchField.ETH_TYPE)) {
			if (findDisjoint(twcard, 
							 MatchField.ETH_TYPE.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.ETH_TYPE).getValue(), 
							 omatch.get(MatchField.ETH_TYPE).getValue())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.ETH_TYPE.id.ordinal(), intersect);
		}
		
		
		if (tmatch.isFullyWildcarded(MatchField.VLAN_VID)  == omatch.isFullyWildcarded(MatchField.VLAN_VID)) {
			if (findDisjoint(twcard, 
							 MatchField.VLAN_VID.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.VLAN_VID).getVlan(), 
							 omatch.get(MatchField.VLAN_VID).getVlan())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.VLAN_VID.id.ordinal(), intersect);
		}
		
		if (tmatch.isFullyWildcarded(MatchField.VLAN_PCP)  == omatch.isFullyWildcarded(MatchField.VLAN_PCP)) {
			if (findDisjoint(twcard, 
							 MatchField.VLAN_PCP.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.VLAN_PCP).getValue(), 
							 omatch.get(MatchField.VLAN_PCP).getValue())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.VLAN_PCP.id.ordinal(), intersect);
		}

		/* L3 */
		if (tmatch.isFullyWildcarded(MatchField.IP_PROTO) == omatch.isFullyWildcarded(MatchField.IP_PROTO)) {
			if (findDisjoint(twcard, 
							 MatchField.IP_PROTO.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.IP_PROTO).getIpProtocolNumber(), 
							 omatch.get(MatchField.IP_PROTO).getIpProtocolNumber())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.IP_PROTO.id.ordinal(), intersect);
		}
		
		if (tmatch.isFullyWildcarded(MatchField.IP_DSCP) == omatch.isFullyWildcarded(MatchField.IP_DSCP)) {
			if (findDisjoint(twcard, 
							 MatchField.IP_DSCP.id.ordinal(), 
							 intersect, 
							 tmatch.get(MatchField.IP_DSCP).getDscpValue(), 
							 omatch.get(MatchField.IP_DSCP).getDscpValue())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.IP_DSCP.id.ordinal(), intersect);
		}
		
		// _ALL Flag erased? Normal behavior?
		if (tmatch.isFullyWildcarded(MatchField.IPV4_SRC)  == omatch.isFullyWildcarded(MatchField.IPV4_DST)) {
			if (findDisjoint(twcard,
							 MatchField.IPV4_SRC.id.ordinal(), 
							 intersect,
							 tmatch.get(MatchField.IPV4_SRC).getInt(), 
							 omatch.get(MatchField.IPV4_SRC).getInt())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.IPV4_SRC.id.ordinal(), intersect);
		}

		if (tmatch.isFullyWildcarded(MatchField.IPV4_DST)  == omatch.isFullyWildcarded(MatchField.IPV4_DST)) {
			if (findDisjoint(twcard,
							 MatchField.IPV4_DST.id.ordinal(), 
							 intersect,
							 tmatch.get(MatchField.IPV4_DST).getInt(), 
							 omatch.get(MatchField.IPV4_DST).getInt())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.IPV4_DST.id.ordinal(), intersect);
		}

		if (tmatch.isFullyWildcarded(MatchField.ARP_SPA)  == omatch.isFullyWildcarded(MatchField.ARP_SPA)) {
			if (findDisjoint(twcard,
							 MatchField.ARP_SPA.id.ordinal(), 
							 intersect,
							 tmatch.get(MatchField.ARP_SPA).getInt(), 
							 omatch.get(MatchField.ARP_SPA).getInt())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.ARP_SPA.id.ordinal(), intersect);
		}
		
		if (tmatch.isFullyWildcarded(MatchField.ARP_TPA)  == omatch.isFullyWildcarded(MatchField.ARP_TPA)) {
			if (findDisjoint(twcard,
							 MatchField.ARP_TPA.id.ordinal(), 
							 intersect,
							 tmatch.get(MatchField.ARP_TPA).getInt(), 
							 omatch.get(MatchField.ARP_TPA).getInt())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.ARP_TPA.id.ordinal(), intersect);
		}
		
		
		
		/* L4 */
		if (tmatch.isFullyWildcarded(MatchField.TCP_SRC)  == omatch.isFullyWildcarded(MatchField.TCP_SRC)) {
			if (findDisjoint(twcard,
							 MatchField.TCP_SRC.id.ordinal(), 
							 intersect,
							 tmatch.get(MatchField.TCP_SRC).getPort(), 
							 omatch.get(MatchField.TCP_SRC).getPort())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.TCP_SRC.id.ordinal(), intersect);
		}
		if (tmatch.isFullyWildcarded(MatchField.TCP_DST)  == omatch.isFullyWildcarded(MatchField.TCP_DST)) {
			if (findDisjoint(twcard,
							 MatchField.TCP_DST.id.ordinal(), 
							 intersect,
							 tmatch.get(MatchField.TCP_DST).getPort(), 
							 omatch.get(MatchField.TCP_DST).getPort())) {
				return DISJOINT;
			}
		} else { /*check if super or subset*/
			findRelation(twcard, owcard, MatchField.TCP_DST.id.ordinal(), intersect);
		}

		int equal = intersect[EQUAL];
		int superset = intersect[SUPERSET];
		int subset = intersect[SUBSET];	    	   

		if (!strict) {
			equal |= subset;
		}
		
		// Small hack to find the actual mask
		int all = 0;
		for (MatchFields val: MatchFields.values()) {
			owcard |= 1 << val.ordinal();
		}

		if (equal == all) {
			return  EQUAL;
		}
		if (superset == all) {
			return  SUPERSET;
		}
		if (subset == all) {
			return  SUBSET;
		}
		return  INTERSECT;
	}
	
	/**
	 * Checks for "ANY" values that should be wildcards but aren't, such as 
	 * NW_SRC/DST 0.0.0.0, and TCP/UDP port 0. 
	 * 
	 * @param omatch The OFMatch of the FlowMod we are comparing entries against
	 * @param owcard The wildcard field of the FlowMod. 
	 * @return the modified wildcard value (a copy).  
	 */
	private int convertToWcards(Match omatch) {
		int owcard = 0;
		for (MatchField<?> mf : omatch.getMatchFields()) {
			owcard |= 1 << mf.id.ordinal();
		}
		//ASK ABOUT THIS BEHAVIOR, IS IT CORRECT THIS WAY?
		if (omatch.isFullyWildcarded(MatchField.IPV4_DST)) {
			//owcard |= Match.OFPFW_NW_DST_ALL | Match.OFPFW_NW_DST_MASK;
			owcard |= MatchField.IPV4_DST.id.ordinal();
		}
		
		if (omatch.isFullyWildcarded(MatchField.IPV4_SRC)) {
			//owcard |= Match.OFPFW_NW_SRC_ALL | Match.OFPFW_NW_SRC_MASK;
			owcard |= MatchField.IPV4_SRC.id.ordinal();
		}
		
		if (omatch.isFullyWildcarded(MatchField.IP_PROTO)) {
			//owcard |= Match.OFPFW_NW_PROTO;
			owcard |= MatchField.IP_PROTO.id.ordinal();
		}
		
		if (omatch.isFullyWildcarded(MatchField.TCP_DST)) {
			//owcard |= Match.OFPFW_TP_DST;
			owcard |= MatchField.TCP_DST.id.ordinal();
		}
		
		if (omatch.isFullyWildcarded(MatchField.TCP_SRC)) {
			//owcard |= Match.OFPFW_TP_SRC;
			owcard |= MatchField.TCP_SRC.id.ordinal();
		}
		return owcard;
	}

	/**
	 * determine if a field is not equal-valued, for non-array fields 
	 * first checks if the OFMatch wildcard is fully wildcarded for the 
	 * field. If not, it checks the equality of the field value. 
	 *   
	 * @param match
	 * @param field
	 * @param equal
	 * @param val1
	 * @param val2
	 * @return true if disjoint FlowEntries
	 */
	private boolean findDisjoint(int wcard, int field, int [] intersect,
			Number val1, Number val2) {
		if (((wcard & field) == field) || (val1.equals(val2))) {
			updateIntersect(intersect, field);
			return false;
		}
		return true;
	}

	/**
	 * determine if fields are disjoint, for byte arrays.
	 * @param wcard
	 * @param field
	 * @param equal
	 * @param val1
	 * @param val2
	 * @return
	 */
	/*
	private boolean findDisjoint(int wcard, int field, int [] intersect, 
			byte [] val1, byte [] val2) {
		if ((wcard & field) == field) {
			updateIntersect(intersect, field);	    
			return false;
		}
		for (int i = 0; i < MACAddress.MAC_ADDRESS_LENGTH; i++) {	
			if (val1[i] != val2[i]) {
				return true;
			}
		}
		updateIntersect(intersect, field);
		return false;
	}
*/
	
	private void updateIntersect(int [] intersect, int field) {
		intersect[EQUAL] |= field;
		intersect[SUPERSET] |= field;
		intersect[SUBSET] |= field;	    	    
	}

	/**
	 * Determines if one or the other field is wildcarded. If this flow entry's
	 * field is wildcarded i.e. its wildcard value for the field is bigger, 
	 * we are superset; Else, we are subset. 
	 * 
	 * @param wcard1 our wildcard field
	 * @param wcard2 other wildcard field
	 * @param field OFMatch wildcard value 
	 * @param intersect intersection sets 
	 */
	private void findRelation(int wcard1, int wcard2, int field, int [] intersect) {
		if ((wcard1 & field) > (wcard2 & field)) {
			intersect[SUPERSET] |= field;
		} else {
			intersect[SUBSET] |= field;	    	    
		}
	}

	/** @return original OFMatch */
	public Match getMatch() {
		return this.flowmod.getMatch();
	}
	
	/** @return the virtual output port */
	public OFPort getOutport() {
		return this.flowmod.getOutPort();
	}

	public int getPriority() {
		return this.flowmod.getPriority();
	}
	
	public OVXFlowMod getFlowMod() {
		return this.flowmod;
	}
	
	public OVXFlowEntry setFlowMod(OVXFlowMod fm) {
		this.flowmod = fm;
		return this;
	}

	/**
	 * @return The new (Physical) cookie
	 */
	public long getNewCookie() {
		return this.newcookie;
	}

	/**
	 * Set the new cookie for this entry
	 */
	public OVXFlowEntry setNewCookie(Long cookie) {
		this.newcookie = cookie;
		return this;
	}
	
	/**
	 * @return The original (virtual) cookie
	 */
	public U64 getCookie() {
		return this.flowmod.getCookie();
	}
	
	public List<OFAction> getActionsList() {
		return this.flowmod.getActions();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	 @Override
	 public int hashCode() {
		 final int prime = 31;
		 int result = 1;
		 result = prime * this.flowmod.hashCode();
		 result = prime * result + (int) (newcookie ^ (newcookie >>> 32));
		 return result;
	 }

	 /*
	  * (non-Javadoc)
	  * 
	  * @see java.lang.Object#equals(java.lang.Object)
	  */
	 @Override
	 public boolean equals(final Object obj) {
		 if (this == obj) {
			 return true;
		 }
		 if (obj == null) {
			 return false;
		 }
		 if (this.getClass() != obj.getClass()) {
			 return false;
		 }
		 final OVXFlowEntry other = (OVXFlowEntry) obj;
		 if (this.newcookie != other.newcookie) {
			 return false;
		 }
		 if (this.flowmod == null) {
			 if (other.flowmod != null) {
				 return false;
			 }
		 } else if (!this.flowmod.equals(other.flowmod)) {
			 return false;
		 }
		 return true;
	 }
	 
	 /**
	  * compare this FlowEntry to another FlowMod. 
	  * @param other
	  * @return
	  */
	 public boolean equals(final OVXFlowMod other) {
		 return this.flowmod.equals(other);
	 }

	 @Override
	 public int compareTo(final OVXFlowEntry other) {
		 // sort on priority, tie break on IDs
		 if (this.flowmod.getPriority() != other.flowmod.getPriority()) {
			 return other.flowmod.getPriority() - this.flowmod.getPriority();
		 }
		 return this.hashCode() - other.hashCode();
	 }

	 public Map<String, Object> toMap() {
		 final HashMap<String, Object> map = new LinkedHashMap<String, Object>();
		 if (this.flowmod.getMatch() != null) {
			 map.put("match", ((OVXMatch)this.flowmod.getMatch()).toMap());
		 }
		 map.put("actionsList", this.flowmod.getActions());
		 map.put("priority", String.valueOf(this.flowmod.getPriority()));
		 return map;
	 }

	 @Override
	 public String toString() {
		 return "OVXFlowEntry [FlowMod=" +this.flowmod + "\n" + 
				 "newcookie=" + this.newcookie +"]";
	 }
}
