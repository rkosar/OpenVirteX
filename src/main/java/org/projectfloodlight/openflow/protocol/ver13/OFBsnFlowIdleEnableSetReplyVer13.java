// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
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
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFBsnFlowIdleEnableSetReplyVer13 implements OFBsnFlowIdleEnableSetReply {
    private static final Logger logger = LoggerFactory.getLogger(OFBsnFlowIdleEnableSetReplyVer13.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int LENGTH = 24;

        private final static long DEFAULT_XID = 0x0L;
        private final static long DEFAULT_ENABLE = 0x0L;
        private final static long DEFAULT_STATUS = 0x0L;

    // OF message fields
    private final long xid;
    private final long enable;
    private final long status;
//
    // Immutable default instance
    final static OFBsnFlowIdleEnableSetReplyVer13 DEFAULT = new OFBsnFlowIdleEnableSetReplyVer13(
        DEFAULT_XID, DEFAULT_ENABLE, DEFAULT_STATUS
    );

    // package private constructor - used by readers, builders, and factory
    OFBsnFlowIdleEnableSetReplyVer13(long xid, long enable, long status) {
        this.xid = xid;
        this.enable = enable;
        this.status = status;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x25L;
    }

    @Override
    public long getEnable() {
        return enable;
    }

    @Override
    public long getStatus() {
        return status;
    }



    public OFBsnFlowIdleEnableSetReply.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFBsnFlowIdleEnableSetReply.Builder {
        final OFBsnFlowIdleEnableSetReplyVer13 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean enableSet;
        private long enable;
        private boolean statusSet;
        private long status;

        BuilderWithParent(OFBsnFlowIdleEnableSetReplyVer13 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBsnFlowIdleEnableSetReply.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x25L;
    }

    @Override
    public long getEnable() {
        return enable;
    }

    @Override
    public OFBsnFlowIdleEnableSetReply.Builder setEnable(long enable) {
        this.enable = enable;
        this.enableSet = true;
        return this;
    }
    @Override
    public long getStatus() {
        return status;
    }

    @Override
    public OFBsnFlowIdleEnableSetReply.Builder setStatus(long status) {
        this.status = status;
        this.statusSet = true;
        return this;
    }


        @Override
        public OFBsnFlowIdleEnableSetReply build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                long enable = this.enableSet ? this.enable : parentMessage.enable;
                long status = this.statusSet ? this.status : parentMessage.status;

                //
                return new OFBsnFlowIdleEnableSetReplyVer13(
                    xid,
                    enable,
                    status
                );
        }

    }

    static class Builder implements OFBsnFlowIdleEnableSetReply.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean enableSet;
        private long enable;
        private boolean statusSet;
        private long status;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBsnFlowIdleEnableSetReply.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x25L;
    }

    @Override
    public long getEnable() {
        return enable;
    }

    @Override
    public OFBsnFlowIdleEnableSetReply.Builder setEnable(long enable) {
        this.enable = enable;
        this.enableSet = true;
        return this;
    }
    @Override
    public long getStatus() {
        return status;
    }

    @Override
    public OFBsnFlowIdleEnableSetReply.Builder setStatus(long status) {
        this.status = status;
        this.statusSet = true;
        return this;
    }
//
        @Override
        public OFBsnFlowIdleEnableSetReply build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            long enable = this.enableSet ? this.enable : DEFAULT_ENABLE;
            long status = this.statusSet ? this.status : DEFAULT_STATUS;


            return new OFBsnFlowIdleEnableSetReplyVer13(
                    xid,
                    enable,
                    status
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFBsnFlowIdleEnableSetReply> {
        @Override
        public OFBsnFlowIdleEnableSetReply readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 4
            byte version = bb.readByte();
            if(version != (byte) 0x4)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_13(4), got="+version);
            // fixed value property type == 4
            byte type = bb.readByte();
            if(type != (byte) 0x4)
                throw new OFParseError("Wrong type: Expected=OFType.EXPERIMENTER(4), got="+type);
            int length = U16.f(bb.readShort());
            if(length != 24)
                throw new OFParseError("Wrong length: Expected=24(24), got="+length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            long xid = U32.f(bb.readInt());
            // fixed value property experimenter == 0x5c16c7L
            int experimenter = bb.readInt();
            if(experimenter != 0x5c16c7)
                throw new OFParseError("Wrong experimenter: Expected=0x5c16c7L(0x5c16c7L), got="+experimenter);
            // fixed value property subtype == 0x25L
            int subtype = bb.readInt();
            if(subtype != 0x25)
                throw new OFParseError("Wrong subtype: Expected=0x25L(0x25L), got="+subtype);
            long enable = U32.f(bb.readInt());
            long status = U32.f(bb.readInt());

            OFBsnFlowIdleEnableSetReplyVer13 bsnFlowIdleEnableSetReplyVer13 = new OFBsnFlowIdleEnableSetReplyVer13(
                    xid,
                      enable,
                      status
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", bsnFlowIdleEnableSetReplyVer13);
            return bsnFlowIdleEnableSetReplyVer13;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFBsnFlowIdleEnableSetReplyVer13Funnel FUNNEL = new OFBsnFlowIdleEnableSetReplyVer13Funnel();
    static class OFBsnFlowIdleEnableSetReplyVer13Funnel implements Funnel<OFBsnFlowIdleEnableSetReplyVer13> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFBsnFlowIdleEnableSetReplyVer13 message, PrimitiveSink sink) {
            // fixed value property version = 4
            sink.putByte((byte) 0x4);
            // fixed value property type = 4
            sink.putByte((byte) 0x4);
            // fixed value property length = 24
            sink.putShort((short) 0x18);
            sink.putLong(message.xid);
            // fixed value property experimenter = 0x5c16c7L
            sink.putInt(0x5c16c7);
            // fixed value property subtype = 0x25L
            sink.putInt(0x25);
            sink.putLong(message.enable);
            sink.putLong(message.status);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFBsnFlowIdleEnableSetReplyVer13> {
        @Override
        public void write(ChannelBuffer bb, OFBsnFlowIdleEnableSetReplyVer13 message) {
            // fixed value property version = 4
            bb.writeByte((byte) 0x4);
            // fixed value property type = 4
            bb.writeByte((byte) 0x4);
            // fixed value property length = 24
            bb.writeShort((short) 0x18);
            bb.writeInt(U32.t(message.xid));
            // fixed value property experimenter = 0x5c16c7L
            bb.writeInt(0x5c16c7);
            // fixed value property subtype = 0x25L
            bb.writeInt(0x25);
            bb.writeInt(U32.t(message.enable));
            bb.writeInt(U32.t(message.status));


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFBsnFlowIdleEnableSetReplyVer13(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("enable=").append(enable);
        b.append(", ");
        b.append("status=").append(status);
        b.append(")");
        return b.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFBsnFlowIdleEnableSetReplyVer13 other = (OFBsnFlowIdleEnableSetReplyVer13) obj;

        if( xid != other.xid)
            return false;
        if( enable != other.enable)
            return false;
        if( status != other.status)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime *  (int) (enable ^ (enable >>> 32));
        result = prime *  (int) (status ^ (status >>> 32));
        return result;
    }

}
