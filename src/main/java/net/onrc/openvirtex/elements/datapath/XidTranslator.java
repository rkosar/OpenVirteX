/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.elements.datapath;

import org.projectfloodlight.openflow.util.LRULinkedHashMap;
/**
 * based on Flowvisor XidTranslator by capveg
 */
public class XidTranslator<T> {
	
	static final long MIN_XID = 256;
	static final int INIT_SIZE = 1 << 10;
	static final int MAX_SIZE = 1 << 14; // must be larger than the max lifetime
									     // of an XID * rate of
										 // mesgs/sec
	long nextID;
	LRULinkedHashMap<Long, XidPair<T>> xidMap;

	public XidTranslator() {
		this.nextID = XidTranslator.MIN_XID;
		this.xidMap = new LRULinkedHashMap<Long, XidPair<T>>(
				XidTranslator.INIT_SIZE, XidTranslator.MAX_SIZE);
	}

	/**
	 * Recovers the source of the message transaction by Xid.
	 * 
	 * @param xid
	 * @return
	 */
	public XidPair<T> untranslate(final long xid) {
		return this.xidMap.get(xid);
	}

	/**
	 * @return the new Xid for the message.
	 */
	public long translate(final long xid, final T sw) {

		final long ret = this.nextID++;
		if (this.nextID < XidTranslator.MIN_XID) {
			this.nextID = XidTranslator.MIN_XID;
		}
		this.xidMap.put(ret, new XidPair<T>(xid, sw));

		return ret;
	}
}
