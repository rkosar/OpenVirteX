/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
/**
 * Copyright 2011, Big Switch Networks, Inc.
 * Originally created by David Erickson, Stanford University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 **/

package net.onrc.openvirtex.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 
 * 
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class LLDPTLV {
	protected byte type;
	protected short length;
	protected byte[] value;

	/**
	 * @return the type
	 */
	public byte getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public LLDPTLV setType(final byte type) {
		this.type = type;
		return this;
	}

	/**
	 * @return the length
	 */
	public short getLength() {
		return this.length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public LLDPTLV setLength(final short length) {
		this.length = length;
		return this;
	}

	/**
	 * @return the value
	 */
	public byte[] getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public LLDPTLV setValue(final byte[] value) {
		this.value = value;
		return this;
	}

	public byte[] serialize() {
		// type = 7 bits
		// info string length 9 bits, each value == byte
		// info string
		final short scratch = (short) ((0x7f & this.type) << 9 | 0x1ff & this.length);
		final byte[] data = new byte[2 + this.length];
		final ByteBuffer bb = ByteBuffer.wrap(data);
		bb.putShort(scratch);
		if (this.value != null) {
			bb.put(this.value);
		}
		return data;
	}

	public LLDPTLV deserialize(final ByteBuffer bb) {
		short sscratch;
		sscratch = bb.getShort();
		this.type = (byte) (sscratch >> 9 & 0x7f);
		this.length = (short) (sscratch & 0x1ff);
		if (this.length > 0) {
			this.value = new byte[this.length];

			// if there is an underrun just toss the TLV
			if (bb.remaining() < this.length) {
				return null;
			}
			bb.get(this.value);
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 1423;
		int result = 1;
		result = prime * result + this.length;
		result = prime * result + this.type;
		result = prime * result + Arrays.hashCode(this.value);
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
		if (!(obj instanceof LLDPTLV)) {
			return false;
		}
		final LLDPTLV other = (LLDPTLV) obj;
		if (this.length != other.length) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		if (!Arrays.equals(this.value, other.value)) {
			return false;
		}
		return true;
	}
}
