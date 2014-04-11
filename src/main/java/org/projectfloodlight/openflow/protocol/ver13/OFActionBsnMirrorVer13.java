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
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFActionBsnMirrorVer13 implements OFActionBsnMirror {
    private static final Logger logger = LoggerFactory.getLogger(OFActionBsnMirrorVer13.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int LENGTH = 24;

        private final static OFPort DEFAULT_DEST_PORT = OFPort.ANY;
        private final static long DEFAULT_VLAN_TAG = 0x0L;
        private final static short DEFAULT_COPY_STAGE = (short) 0x0;

    // OF message fields
    private final OFPort destPort;
    private final long vlanTag;
    private final short copyStage;
//
    // Immutable default instance
    final static OFActionBsnMirrorVer13 DEFAULT = new OFActionBsnMirrorVer13(
        DEFAULT_DEST_PORT, DEFAULT_VLAN_TAG, DEFAULT_COPY_STAGE
    );

    // package private constructor - used by readers, builders, and factory
    OFActionBsnMirrorVer13(OFPort destPort, long vlanTag, short copyStage) {
        this.destPort = destPort;
        this.vlanTag = vlanTag;
        this.copyStage = copyStage;
    }

    // Accessors for OF message fields
    @Override
    public OFActionType getType() {
        return OFActionType.EXPERIMENTER;
    }

    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x1L;
    }

    @Override
    public OFPort getDestPort() {
        return destPort;
    }

    @Override
    public long getVlanTag() {
        return vlanTag;
    }

    @Override
    public short getCopyStage() {
        return copyStage;
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



    public OFActionBsnMirror.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFActionBsnMirror.Builder {
        final OFActionBsnMirrorVer13 parentMessage;

        // OF message fields
        private boolean destPortSet;
        private OFPort destPort;
        private boolean vlanTagSet;
        private long vlanTag;
        private boolean copyStageSet;
        private short copyStage;

        BuilderWithParent(OFActionBsnMirrorVer13 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFActionType getType() {
        return OFActionType.EXPERIMENTER;
    }

    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x1L;
    }

    @Override
    public OFPort getDestPort() {
        return destPort;
    }

    @Override
    public OFActionBsnMirror.Builder setDestPort(OFPort destPort) {
        this.destPort = destPort;
        this.destPortSet = true;
        return this;
    }
    @Override
    public long getVlanTag() {
        return vlanTag;
    }

    @Override
    public OFActionBsnMirror.Builder setVlanTag(long vlanTag) {
        this.vlanTag = vlanTag;
        this.vlanTagSet = true;
        return this;
    }
    @Override
    public short getCopyStage() {
        return copyStage;
    }

    @Override
    public OFActionBsnMirror.Builder setCopyStage(short copyStage) {
        this.copyStage = copyStage;
        this.copyStageSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



        @Override
        public OFActionBsnMirror build() {
                OFPort destPort = this.destPortSet ? this.destPort : parentMessage.destPort;
                if(destPort == null)
                    throw new NullPointerException("Property destPort must not be null");
                long vlanTag = this.vlanTagSet ? this.vlanTag : parentMessage.vlanTag;
                short copyStage = this.copyStageSet ? this.copyStage : parentMessage.copyStage;

                //
                return new OFActionBsnMirrorVer13(
                    destPort,
                    vlanTag,
                    copyStage
                );
        }

    }

    static class Builder implements OFActionBsnMirror.Builder {
        // OF message fields
        private boolean destPortSet;
        private OFPort destPort;
        private boolean vlanTagSet;
        private long vlanTag;
        private boolean copyStageSet;
        private short copyStage;

    @Override
    public OFActionType getType() {
        return OFActionType.EXPERIMENTER;
    }

    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x1L;
    }

    @Override
    public OFPort getDestPort() {
        return destPort;
    }

    @Override
    public OFActionBsnMirror.Builder setDestPort(OFPort destPort) {
        this.destPort = destPort;
        this.destPortSet = true;
        return this;
    }
    @Override
    public long getVlanTag() {
        return vlanTag;
    }

    @Override
    public OFActionBsnMirror.Builder setVlanTag(long vlanTag) {
        this.vlanTag = vlanTag;
        this.vlanTagSet = true;
        return this;
    }
    @Override
    public short getCopyStage() {
        return copyStage;
    }

    @Override
    public OFActionBsnMirror.Builder setCopyStage(short copyStage) {
        this.copyStage = copyStage;
        this.copyStageSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

//
        @Override
        public OFActionBsnMirror build() {
            OFPort destPort = this.destPortSet ? this.destPort : DEFAULT_DEST_PORT;
            if(destPort == null)
                throw new NullPointerException("Property destPort must not be null");
            long vlanTag = this.vlanTagSet ? this.vlanTag : DEFAULT_VLAN_TAG;
            short copyStage = this.copyStageSet ? this.copyStage : DEFAULT_COPY_STAGE;


            return new OFActionBsnMirrorVer13(
                    destPort,
                    vlanTag,
                    copyStage
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFActionBsnMirror> {
        @Override
        public OFActionBsnMirror readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property type == 65535
            short type = bb.readShort();
            if(type != (short) 0xffff)
                throw new OFParseError("Wrong type: Expected=OFActionType.EXPERIMENTER(65535), got="+type);
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
            // fixed value property experimenter == 0x5c16c7L
            int experimenter = bb.readInt();
            if(experimenter != 0x5c16c7)
                throw new OFParseError("Wrong experimenter: Expected=0x5c16c7L(0x5c16c7L), got="+experimenter);
            // fixed value property subtype == 0x1L
            int subtype = bb.readInt();
            if(subtype != 0x1)
                throw new OFParseError("Wrong subtype: Expected=0x1L(0x1L), got="+subtype);
            OFPort destPort = OFPort.read4Bytes(bb);
            long vlanTag = U32.f(bb.readInt());
            short copyStage = U8.f(bb.readByte());
            // pad: 3 bytes
            bb.skipBytes(3);

            OFActionBsnMirrorVer13 actionBsnMirrorVer13 = new OFActionBsnMirrorVer13(
                    destPort,
                      vlanTag,
                      copyStage
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", actionBsnMirrorVer13);
            return actionBsnMirrorVer13;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFActionBsnMirrorVer13Funnel FUNNEL = new OFActionBsnMirrorVer13Funnel();
    static class OFActionBsnMirrorVer13Funnel implements Funnel<OFActionBsnMirrorVer13> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFActionBsnMirrorVer13 message, PrimitiveSink sink) {
            // fixed value property type = 65535
            sink.putShort((short) 0xffff);
            // fixed value property length = 24
            sink.putShort((short) 0x18);
            // fixed value property experimenter = 0x5c16c7L
            sink.putInt(0x5c16c7);
            // fixed value property subtype = 0x1L
            sink.putInt(0x1);
            message.destPort.putTo(sink);
            sink.putLong(message.vlanTag);
            sink.putShort(message.copyStage);
            // skip pad (3 bytes)
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFActionBsnMirrorVer13> {
        @Override
        public void write(ChannelBuffer bb, OFActionBsnMirrorVer13 message) {
            // fixed value property type = 65535
            bb.writeShort((short) 0xffff);
            // fixed value property length = 24
            bb.writeShort((short) 0x18);
            // fixed value property experimenter = 0x5c16c7L
            bb.writeInt(0x5c16c7);
            // fixed value property subtype = 0x1L
            bb.writeInt(0x1);
            message.destPort.write4Bytes(bb);
            bb.writeInt(U32.t(message.vlanTag));
            bb.writeByte(U8.t(message.copyStage));
            // pad: 3 bytes
            bb.writeZero(3);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFActionBsnMirrorVer13(");
        b.append("destPort=").append(destPort);
        b.append(", ");
        b.append("vlanTag=").append(vlanTag);
        b.append(", ");
        b.append("copyStage=").append(copyStage);
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
        OFActionBsnMirrorVer13 other = (OFActionBsnMirrorVer13) obj;

        if (destPort == null) {
            if (other.destPort != null)
                return false;
        } else if (!destPort.equals(other.destPort))
            return false;
        if( vlanTag != other.vlanTag)
            return false;
        if( copyStage != other.copyStage)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((destPort == null) ? 0 : destPort.hashCode());
        result = prime *  (int) (vlanTag ^ (vlanTag >>> 32));
        result = prime * result + copyStage;
        return result;
    }

}
