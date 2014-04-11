// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_interface.java
// Do not modify

package org.projectfloodlight.openflow.protocol;

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

public interface OFPacketIn extends OFObject, OFMessage {
    OFVersion getVersion();
    OFType getType();
    long getXid();
    OFBufferId getBufferId();
    int getTotalLen();
    OFPacketInReason getReason();
    TableId getTableId() throws UnsupportedOperationException;
    Match getMatch() throws UnsupportedOperationException;
    byte[] getData();
    OFPort getInPort() throws UnsupportedOperationException;
    OFPort getInPhyPort() throws UnsupportedOperationException;
    U64 getCookie() throws UnsupportedOperationException;

    void writeTo(ChannelBuffer channelBuffer);

    Builder createBuilder();
    public interface Builder extends OFMessage.Builder {
        OFPacketIn build();
        OFVersion getVersion();
        OFType getType();
        long getXid();
        Builder setXid(long xid);
        OFBufferId getBufferId();
        Builder setBufferId(OFBufferId bufferId);
        int getTotalLen();
        Builder setTotalLen(int totalLen);
        OFPacketInReason getReason();
        Builder setReason(OFPacketInReason reason);
        TableId getTableId() throws UnsupportedOperationException;
        Builder setTableId(TableId tableId) throws UnsupportedOperationException;
        Match getMatch() throws UnsupportedOperationException;
        Builder setMatch(Match match) throws UnsupportedOperationException;
        byte[] getData();
        Builder setData(byte[] data);
        OFPort getInPort() throws UnsupportedOperationException;
        Builder setInPort(OFPort inPort) throws UnsupportedOperationException;
        OFPort getInPhyPort() throws UnsupportedOperationException;
        Builder setInPhyPort(OFPort inPhyPort) throws UnsupportedOperationException;
        U64 getCookie() throws UnsupportedOperationException;
        Builder setCookie(U64 cookie) throws UnsupportedOperationException;
    }
}
