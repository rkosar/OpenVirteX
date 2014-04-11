/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.elements.datapath;

import java.util.HashSet;
import java.util.Set;


//import org.openflow.protocol.OFFeaturesReply.OFCapabilities;
import org.projectfloodlight.openflow.protocol.OFCapabilities;

/**
 * The Class OVXSwitchCapabilities.
 */
public class OVXSwitchCapabilities {

	/** The flow stats capability. */
	protected boolean flowStatsCapability = false;

	/** The table stats capability. */
	protected boolean tableStatsCapability = false;

	/** The port stats capability. */
	protected boolean portStatsCapability = false;

	/** The stp capability. */
	protected boolean stpCapability = false;

	/** The reassemble capability. */
	protected boolean reassembleCapability = false;

	/** The queue stats capability. */
	protected boolean queueStatsCapability = false;

	/** The match ip capability. */
	protected boolean matchIpCapability = false;

	/**
	 * Instantiates a new oVX switch capabilities.
	 */
	public OVXSwitchCapabilities() {
		this.flowStatsCapability = true;
		this.tableStatsCapability = true;
		this.portStatsCapability = true;
		this.stpCapability = false;
		this.reassembleCapability = false;
		this.queueStatsCapability = false;
		this.matchIpCapability = true;
	}

	/**
	 * Sets the default capabilities.
	 */
	public void setDefaultCapabilities() {
		this.flowStatsCapability = true;
		this.tableStatsCapability = true;
		this.portStatsCapability = true;
		this.stpCapability = false;
		this.reassembleCapability = false;
		this.queueStatsCapability = false;
		this.matchIpCapability = true;
	}

	/**
	 * Gets the oVX switch capabilities.
	 * 
	 * @return the oVX switch capabilities
	 */
	public Set<OFCapabilities> getOVXSwitchCapabilities() {
		Set<OFCapabilities> capabilities = new HashSet<OFCapabilities>();
		//Integer capabilities = 0;
		if (this.flowStatsCapability) {
			capabilities.add(OFCapabilities.FLOW_STATS);
		}
		if (this.tableStatsCapability) {
			capabilities.add(OFCapabilities.TABLE_STATS);
		}
		if (this.portStatsCapability) {
			capabilities.add(OFCapabilities.PORT_STATS);
		}
		if (this.stpCapability) {
			capabilities.add(OFCapabilities.STP);
		}
		if (this.reassembleCapability) {
			capabilities.add(OFCapabilities.IP_REASM);
		}
		if (this.queueStatsCapability) {
			capabilities.add(OFCapabilities.QUEUE_STATS);
		}
		if (this.matchIpCapability) {
			capabilities.add(OFCapabilities.ARP_MATCH_IP);
		}
		return capabilities;
	}

	/**
	 * Checks if is flow stats capability.
	 * 
	 * @return true, if is flow stats capability
	 */
	public boolean isFlowStatsCapability() {
		return this.flowStatsCapability;
	}

	/**
	 * Sets the flow stats capability.
	 * 
	 * @param flowStatsCapability
	 *            the new flow stats capability
	 */
	public void setFlowStatsCapability(final boolean flowStatsCapability) {
		this.flowStatsCapability = flowStatsCapability;
	}

	/**
	 * Checks if is table stats capability.
	 * 
	 * @return true, if is table stats capability
	 */
	public boolean isTableStatsCapability() {
		return this.tableStatsCapability;
	}

	/**
	 * Sets the table stats capability.
	 * 
	 * @param tableStatsCapability
	 *            the new table stats capability
	 */
	public void setTableStatsCapability(final boolean tableStatsCapability) {
		this.tableStatsCapability = tableStatsCapability;
	}

	/**
	 * Checks if is port stats capability.
	 * 
	 * @return true, if is port stats capability
	 */
	public boolean isPortStatsCapability() {
		return this.portStatsCapability;
	}

	/**
	 * Sets the port stats capability.
	 * 
	 * @param portStatsCapability
	 *            the new port stats capability
	 */
	public void setPortStatsCapability(final boolean portStatsCapability) {
		this.portStatsCapability = portStatsCapability;
	}

	/**
	 * Checks if is stp capability.
	 * 
	 * @return true, if is stp capability
	 */
	public boolean isStpCapability() {
		return this.stpCapability;
	}

	/**
	 * Sets the stp capability.
	 * 
	 * @param stpCapability
	 *            the new stp capability
	 */
	public void setStpCapability(final boolean stpCapability) {
		this.stpCapability = stpCapability;
	}

	/**
	 * Checks if is reassemble capability.
	 * 
	 * @return true, if is reassemble capability
	 */
	public boolean isReassembleCapability() {
		return this.reassembleCapability;
	}

	/**
	 * Sets the reassemble capability.
	 * 
	 * @param reassembleCapability
	 *            the new reassemble capability
	 */
	public void setReassembleCapability(final boolean reassembleCapability) {
		this.reassembleCapability = reassembleCapability;
	}

	/**
	 * Checks if is queue stats capability.
	 * 
	 * @return true, if is queue stats capability
	 */
	public boolean isQueueStatsCapability() {
		return this.queueStatsCapability;
	}

	/**
	 * Sets the queue stats capability.
	 * 
	 * @param queueStatsCapability
	 *            the new queue stats capability
	 */
	public void setQueueStatsCapability(final boolean queueStatsCapability) {
		this.queueStatsCapability = queueStatsCapability;
	}

	/**
	 * Checks if is match ip capability.
	 * 
	 * @return true, if is match ip capability
	 */
	public boolean isMatchIpCapability() {
		return this.matchIpCapability;
	}

	/**
	 * Sets the match ip capability.
	 * 
	 * @param matchIpCapability
	 *            the new match ip capability
	 */
	public void setMatchIpCapability(final boolean matchIpCapability) {
		this.matchIpCapability = matchIpCapability;
	}

}
