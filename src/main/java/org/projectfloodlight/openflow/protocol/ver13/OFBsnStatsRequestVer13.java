// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_virtual_class.java
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
import org.jboss.netty.buffer.ChannelBuffer;

abstract class OFBsnStatsRequestVer13 {
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int MINIMUM_LENGTH = 24;


    public final static OFBsnStatsRequestVer13.Reader READER = new Reader();

    static class Reader implements OFMessageReader<OFBsnStatsRequest<?>> {
        @Override
        public OFBsnStatsRequest<?> readFrom(ChannelBuffer bb) throws OFParseError {
            if(bb.readableBytes() < MINIMUM_LENGTH)
                return null;
            int start = bb.readerIndex();
            // fixed value property version == 4
            byte version = bb.readByte();
            if(version != (byte) 0x4)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_13(4), got="+version);
            // fixed value property type == 18
            byte type = bb.readByte();
            if(type != (byte) 0x12)
                throw new OFParseError("Wrong type: Expected=OFType.STATS_REQUEST(18), got="+type);
            int length = U16.f(bb.readShort());
            if(length < MINIMUM_LENGTH)
                throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);
            U32.f(bb.readInt());
            // fixed value property statsType == 65535
            short statsType = bb.readShort();
            if(statsType != (short) 0xffff)
                throw new OFParseError("Wrong statsType: Expected=OFStatsType.EXPERIMENTER(65535), got="+statsType);
            OFStatsRequestFlagsSerializerVer13.readFrom(bb);
            // pad: 4 bytes
            bb.skipBytes(4);
            // fixed value property experimenter == 0x5c16c7L
            int experimenter = bb.readInt();
            if(experimenter != 0x5c16c7)
                throw new OFParseError("Wrong experimenter: Expected=0x5c16c7L(0x5c16c7L), got="+experimenter);
            int subtype = bb.readInt();
            bb.readerIndex(start);
            switch(subtype) {
               case 0xa:
                   // discriminator value 0xaL=0xaL for class OFBsnFlowChecksumBucketStatsRequestVer13
                   return OFBsnFlowChecksumBucketStatsRequestVer13.READER.readFrom(bb);
               case 0x5:
                   // discriminator value 0x5L=0x5L for class OFBsnGentableBucketStatsRequestVer13
                   return OFBsnGentableBucketStatsRequestVer13.READER.readFrom(bb);
               case 0x4:
                   // discriminator value 0x4L=0x4L for class OFBsnGentableDescStatsRequestVer13
                   return OFBsnGentableDescStatsRequestVer13.READER.readFrom(bb);
               case 0x2:
                   // discriminator value 0x2L=0x2L for class OFBsnGentableEntryDescStatsRequestVer13
                   return OFBsnGentableEntryDescStatsRequestVer13.READER.readFrom(bb);
               case 0x3:
                   // discriminator value 0x3L=0x3L for class OFBsnGentableEntryStatsRequestVer13
                   return OFBsnGentableEntryStatsRequestVer13.READER.readFrom(bb);
               case 0x7:
                   // discriminator value 0x7L=0x7L for class OFBsnGentableStatsRequestVer13
                   return OFBsnGentableStatsRequestVer13.READER.readFrom(bb);
               case 0x1:
                   // discriminator value 0x1L=0x1L for class OFBsnLacpStatsRequestVer13
                   return OFBsnLacpStatsRequestVer13.READER.readFrom(bb);
               case 0x8:
                   // discriminator value 0x8L=0x8L for class OFBsnPortCounterStatsRequestVer13
                   return OFBsnPortCounterStatsRequestVer13.READER.readFrom(bb);
               case 0x6:
                   // discriminator value 0x6L=0x6L for class OFBsnSwitchPipelineStatsRequestVer13
                   return OFBsnSwitchPipelineStatsRequestVer13.READER.readFrom(bb);
               case 0xb:
                   // discriminator value 0xbL=0xbL for class OFBsnTableChecksumStatsRequestVer13
                   return OFBsnTableChecksumStatsRequestVer13.READER.readFrom(bb);
               case 0x9:
                   // discriminator value 0x9L=0x9L for class OFBsnVlanCounterStatsRequestVer13
                   return OFBsnVlanCounterStatsRequestVer13.READER.readFrom(bb);
               default:
                   throw new OFParseError("Unknown value for discriminator subtype of class OFBsnStatsRequestVer13: " + subtype);
            }
        }
    }
}
