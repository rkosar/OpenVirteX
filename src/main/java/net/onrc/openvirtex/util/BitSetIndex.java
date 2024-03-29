/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.util;

import java.util.BitSet;

import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.link.OVXLinkField;
import net.onrc.openvirtex.exceptions.DuplicateIndexException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;

public class BitSetIndex {
	private BitSet set;
	private IndexType type;

	public enum IndexType {
		/*
		 * Each index type is associated with the biggest index it can accept. When the user request a new id, the class check if an 
		 * id in the range from 1 to the biggest index is available.
		 */

		TENANT_ID((int)Math.pow(2,OpenVirteXController.getInstance().getNumberVirtualNets())), 
		SWITCH_ID((int)Math.pow(2,32)),
		LINK_ID(getLinkMaxValue()),
		ROUTE_ID((int)Math.pow(2,24)),
		PORT_ID(U16.f(OFPort.MAX.getShortPortNumber())),
		FLOW_ID((int)Math.pow(2,24)),
		HOST_ID((int)Math.pow(2,32)),
		FLOW_COUNTER(getLinkMaxValue()),
		IP_ID((int)Math.pow(2,(32-OpenVirteXController.getInstance().getNumberVirtualNets()))),
		DEFAULT(1000);

		protected Integer value;

		private static Integer getLinkMaxValue() {
			if (OpenVirteXController.getInstance().getOvxLinkField().getValue() == OVXLinkField.MAC_ADDRESS.getValue())
				return (int) Math.pow(2,((48-OpenVirteXController.getInstance().getNumberVirtualNets())/2));
			else if (OpenVirteXController.getInstance().getOvxLinkField().getValue() == OVXLinkField.VLAN.getValue())
				return (int) Math.pow(2,((12-OpenVirteXController.getInstance().getNumberVirtualNets())/2));
			else 
				return 1000;
		}

		private IndexType(final Integer value) {
			this.value = value;
		}

		private Integer getValue() {
			return this.value;
		}

		public static String allMaxToString() {
			return "TENANT_ID: " + TENANT_ID.getValue() + "\n" + 
					"SWITCH_ID: " + SWITCH_ID.getValue() + "\n" +
					"LINK_ID: " + LINK_ID.getValue() + "\n" +
					"ROUTE_ID: " + ROUTE_ID.getValue() + "\n" +
					"PORT_ID: " + PORT_ID.getValue() + "\n" +
					"FLOW_ID: " + FLOW_ID.getValue() + "\n" +
					"HOST_ID: " + HOST_ID.getValue() + "\n" +
					"FLOW_COUNTER: " + FLOW_COUNTER.getValue() + "\n" +
					"IP_ID: " + IP_ID.getValue() + "\n" +
					"DEFAULT: " + DEFAULT.getValue();
		}
	}

	public BitSetIndex(IndexType type) {
		this.set = new BitSet();
		this.type = type;
		//Set the first bit to true, in order to start each index from 1
		this.set.flip(0);
	}

	public synchronized Integer getNewIndex()
			throws IndexOutOfBoundException {
		Integer index = this.set.nextClearBit(0);
		try {
			this.getNewIndex(index);
		} catch (DuplicateIndexException e) {
			// Will never happen as we obtained the next index through nextClearBit()
		}
		return index; 
	}
	
	public synchronized Integer getNewIndex(Integer index)
			throws IndexOutOfBoundException, DuplicateIndexException {
		if (index < type.getValue()) {
			if (!this.set.get(index)) {
				this.set.flip(index);
				return index;
			} else 
				throw new DuplicateIndexException("Index " + index + " already used");
		} else
			throw new IndexOutOfBoundException("No id available in range [0," + type.getValue().toString() + "]");
	}

	public synchronized boolean releaseIndex(Integer index) {
		if (this.set.get(index)) {
			this.set.flip(index);
			return true;
		}
		else return false;
	}

	public void reset() {
		this.set.clear();
		this.set.flip(0);
	}
	
}
