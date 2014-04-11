// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver13;

import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.actionid.*;
import org.projectfloodlight.openflow.protocol.bsntlv.*;
import org.projectfloodlight.openflow.protocol.errormsg.*;
import org.projectfloodlight.openflow.protocol.meterband.*;
import org.projectfloodlight.openflow.protocol.instruction.*;
import org.projectfloodlight.openflow.protocol.instructionid.*;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.protocol.queueprop.*;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.util.*;
import org.projectfloodlight.openflow.exceptions.*;
import org.projectfloodlight.openflow.protocol.OFHelloFailedCode;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;

public class OFHelloFailedCodeSerializerVer13 {

    public final static short INCOMPATIBLE_VAL = (short) 0x0;
    public final static short EPERM_VAL = (short) 0x1;

    public static OFHelloFailedCode readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OFHelloFailedCode e) {
        bb.writeShort(toWireValue(e));
    }

    public static void putTo(OFHelloFailedCode e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OFHelloFailedCode ofWireValue(short val) {
        switch(val) {
            case INCOMPATIBLE_VAL:
                return OFHelloFailedCode.INCOMPATIBLE;
            case EPERM_VAL:
                return OFHelloFailedCode.EPERM;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OFHelloFailedCode in version 1.3: " + val);
        }
    }


    public static short toWireValue(OFHelloFailedCode e) {
        switch(e) {
            case INCOMPATIBLE:
                return INCOMPATIBLE_VAL;
            case EPERM:
                return EPERM_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFHelloFailedCode in version 1.3: " + e);
        }
    }

}
