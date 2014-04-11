// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_factory_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver12;

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


public class OFActionsVer12 implements OFActions {
    public final static OFActionsVer12 INSTANCE = new OFActionsVer12();




    public OFActionBsnMirror.Builder buildBsnMirror() {
        return new OFActionBsnMirrorVer12.Builder();
    }

    public OFActionBsnSetTunnelDst.Builder buildBsnSetTunnelDst() {
        return new OFActionBsnSetTunnelDstVer12.Builder();
    }
    public OFActionBsnSetTunnelDst bsnSetTunnelDst(long dst) {
        return new OFActionBsnSetTunnelDstVer12(
                dst
                    );
    }

    public OFActionEnqueue.Builder buildEnqueue() {
        throw new UnsupportedOperationException("OFActionEnqueue not supported in version 1.2");
    }
    public OFActionEnqueue enqueue(OFPort port, long queueId) {
        throw new UnsupportedOperationException("OFActionEnqueue not supported in version 1.2");
    }

    public OFActionNiciraDecTtl niciraDecTtl() {
        return OFActionNiciraDecTtlVer12.INSTANCE;
    }

    public OFActionOutput.Builder buildOutput() {
        return new OFActionOutputVer12.Builder();
    }
    public OFActionOutput output(OFPort port, int maxLen) {
        return new OFActionOutputVer12(
                port,
                      maxLen
                    );
    }

    public OFActionSetDlDst.Builder buildSetDlDst() {
        throw new UnsupportedOperationException("OFActionSetDlDst not supported in version 1.2");
    }
    public OFActionSetDlDst setDlDst(MacAddress dlAddr) {
        throw new UnsupportedOperationException("OFActionSetDlDst not supported in version 1.2");
    }

    public OFActionSetDlSrc.Builder buildSetDlSrc() {
        throw new UnsupportedOperationException("OFActionSetDlSrc not supported in version 1.2");
    }
    public OFActionSetDlSrc setDlSrc(MacAddress dlAddr) {
        throw new UnsupportedOperationException("OFActionSetDlSrc not supported in version 1.2");
    }

    public OFActionSetNwDst.Builder buildSetNwDst() {
        throw new UnsupportedOperationException("OFActionSetNwDst not supported in version 1.2");
    }
    public OFActionSetNwDst setNwDst(IPv4Address nwAddr) {
        throw new UnsupportedOperationException("OFActionSetNwDst not supported in version 1.2");
    }

    public OFActionSetNwSrc.Builder buildSetNwSrc() {
        throw new UnsupportedOperationException("OFActionSetNwSrc not supported in version 1.2");
    }
    public OFActionSetNwSrc setNwSrc(IPv4Address nwAddr) {
        throw new UnsupportedOperationException("OFActionSetNwSrc not supported in version 1.2");
    }

    public OFActionSetNwTos.Builder buildSetNwTos() {
        throw new UnsupportedOperationException("OFActionSetNwTos not supported in version 1.2");
    }
    public OFActionSetNwTos setNwTos(short nwTos) {
        throw new UnsupportedOperationException("OFActionSetNwTos not supported in version 1.2");
    }

    public OFActionSetTpDst.Builder buildSetTpDst() {
        throw new UnsupportedOperationException("OFActionSetTpDst not supported in version 1.2");
    }
    public OFActionSetTpDst setTpDst(TransportPort tpPort) {
        throw new UnsupportedOperationException("OFActionSetTpDst not supported in version 1.2");
    }

    public OFActionSetTpSrc.Builder buildSetTpSrc() {
        throw new UnsupportedOperationException("OFActionSetTpSrc not supported in version 1.2");
    }
    public OFActionSetTpSrc setTpSrc(TransportPort tpPort) {
        throw new UnsupportedOperationException("OFActionSetTpSrc not supported in version 1.2");
    }

    public OFActionSetVlanPcp.Builder buildSetVlanPcp() {
        throw new UnsupportedOperationException("OFActionSetVlanPcp not supported in version 1.2");
    }
    public OFActionSetVlanPcp setVlanPcp(VlanPcp vlanPcp) {
        throw new UnsupportedOperationException("OFActionSetVlanPcp not supported in version 1.2");
    }

    public OFActionSetVlanVid.Builder buildSetVlanVid() {
        throw new UnsupportedOperationException("OFActionSetVlanVid not supported in version 1.2");
    }
    public OFActionSetVlanVid setVlanVid(VlanVid vlanVid) {
        throw new UnsupportedOperationException("OFActionSetVlanVid not supported in version 1.2");
    }

    public OFActionStripVlan stripVlan() {
        throw new UnsupportedOperationException("OFActionStripVlan not supported in version 1.2");
    }

    public OFActionCopyTtlIn copyTtlIn() {
        return OFActionCopyTtlInVer12.INSTANCE;
    }

    public OFActionCopyTtlOut copyTtlOut() {
        return OFActionCopyTtlOutVer12.INSTANCE;
    }

    public OFActionDecMplsTtl decMplsTtl() {
        return OFActionDecMplsTtlVer12.INSTANCE;
    }

    public OFActionDecNwTtl decNwTtl() {
        return OFActionDecNwTtlVer12.INSTANCE;
    }

    public OFActionGroup.Builder buildGroup() {
        return new OFActionGroupVer12.Builder();
    }
    public OFActionGroup group(OFGroup group) {
        return new OFActionGroupVer12(
                group
                    );
    }

    public OFActionPopMpls.Builder buildPopMpls() {
        return new OFActionPopMplsVer12.Builder();
    }
    public OFActionPopMpls popMpls(EthType ethertype) {
        return new OFActionPopMplsVer12(
                ethertype
                    );
    }

    public OFActionPopVlan popVlan() {
        return OFActionPopVlanVer12.INSTANCE;
    }

    public OFActionPushMpls.Builder buildPushMpls() {
        return new OFActionPushMplsVer12.Builder();
    }
    public OFActionPushMpls pushMpls(EthType ethertype) {
        return new OFActionPushMplsVer12(
                ethertype
                    );
    }

    public OFActionPushVlan.Builder buildPushVlan() {
        return new OFActionPushVlanVer12.Builder();
    }
    public OFActionPushVlan pushVlan(EthType ethertype) {
        return new OFActionPushVlanVer12(
                ethertype
                    );
    }

    public OFActionSetMplsLabel.Builder buildSetMplsLabel() {
        throw new UnsupportedOperationException("OFActionSetMplsLabel not supported in version 1.2");
    }
    public OFActionSetMplsLabel setMplsLabel(long mplsLabel) {
        throw new UnsupportedOperationException("OFActionSetMplsLabel not supported in version 1.2");
    }

    public OFActionSetMplsTc.Builder buildSetMplsTc() {
        throw new UnsupportedOperationException("OFActionSetMplsTc not supported in version 1.2");
    }
    public OFActionSetMplsTc setMplsTc(short mplsTc) {
        throw new UnsupportedOperationException("OFActionSetMplsTc not supported in version 1.2");
    }

    public OFActionSetMplsTtl.Builder buildSetMplsTtl() {
        return new OFActionSetMplsTtlVer12.Builder();
    }
    public OFActionSetMplsTtl setMplsTtl(short mplsTtl) {
        return new OFActionSetMplsTtlVer12(
                mplsTtl
                    );
    }

    public OFActionSetNwEcn.Builder buildSetNwEcn() {
        throw new UnsupportedOperationException("OFActionSetNwEcn not supported in version 1.2");
    }
    public OFActionSetNwEcn setNwEcn(IpEcn nwEcn) {
        throw new UnsupportedOperationException("OFActionSetNwEcn not supported in version 1.2");
    }

    public OFActionSetNwTtl.Builder buildSetNwTtl() {
        return new OFActionSetNwTtlVer12.Builder();
    }
    public OFActionSetNwTtl setNwTtl(short nwTtl) {
        return new OFActionSetNwTtlVer12(
                nwTtl
                    );
    }

    public OFActionSetQueue.Builder buildSetQueue() {
        return new OFActionSetQueueVer12.Builder();
    }
    public OFActionSetQueue setQueue(long queueId) {
        return new OFActionSetQueueVer12(
                queueId
                    );
    }

    public OFActionSetField.Builder buildSetField() {
        return new OFActionSetFieldVer12.Builder();
    }
    public OFActionSetField setField(OFOxm<?> field) {
        return new OFActionSetFieldVer12(
                field
                    );
    }

    public OFActionPopPbb popPbb() {
        throw new UnsupportedOperationException("OFActionPopPbb not supported in version 1.2");
    }

    public OFActionPushPbb.Builder buildPushPbb() {
        throw new UnsupportedOperationException("OFActionPushPbb not supported in version 1.2");
    }
    public OFActionPushPbb pushPbb(EthType ethertype) {
        throw new UnsupportedOperationException("OFActionPushPbb not supported in version 1.2");
    }

    public OFMessageReader<OFAction> getReader() {
        return OFActionVer12.READER;
    }


    public OFVersion getVersion() {
            return OFVersion.OF_12;
    }
}
