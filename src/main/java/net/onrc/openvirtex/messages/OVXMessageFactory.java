/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;

import net.onrc.openvirtex.messages.actions.*;
import net.onrc.openvirtex.messages.statistics.*;

public class OVXMessageFactory  {
	private static OVXMessageFactory                    instance = null;

	private static final Map<OFType, Class<?>>          convertMap;

	private static final Map<OFActionType, Class<?>>    convertActionsMap;

	private static final Map<OFStatsType, Class<?>>     convertStatsRequestMap;

	private static final Map<OFStatsType, Class<?>>     convertStatsReplyMap;
	
	static {
		Map<OFType, Class<?>> tmpMap = new HashMap<OFType, Class<?>>();
		tmpMap.put(OFType.HELLO, OVXHello.class);
		tmpMap.put(OFType.ERROR, OVXError.class);
		tmpMap.put(OFType.ECHO_REQUEST, OVXEchoRequest.class); 
		tmpMap.put(OFType.ECHO_REPLY, OVXEchoReply.class);
		tmpMap.put(OFType.EXPERIMENTER, OVXVendor.class);
		tmpMap.put(OFType.FEATURES_REQUEST, OVXFeaturesRequest.class); 
		tmpMap.put(OFType.FEATURES_REPLY, OVXFeaturesReply.class);
		tmpMap.put(OFType.GET_CONFIG_REQUEST, OVXGetConfigRequest.class);
		tmpMap.put(OFType.GET_CONFIG_REPLY, OVXGetConfigReply.class);
		tmpMap.put(OFType.SET_CONFIG, OVXSetConfig.class);
		tmpMap.put(OFType.PACKET_IN, OVXPacketIn.class);
		tmpMap.put(OFType.FLOW_REMOVED, OVXFlowRemoved.class);
		tmpMap.put(OFType.PORT_STATUS, OVXPortStatus.class);
		tmpMap.put(OFType.PACKET_OUT, OVXPacketOut.class);
		tmpMap.put(OFType.FLOW_MOD, OVXFlowMod.class);		
		tmpMap.put(OFType.PORT_MOD, OVXPortMod.class);
		tmpMap.put(OFType.STATS_REQUEST, OVXStatisticsRequest.class);
		tmpMap.put(OFType.STATS_REPLY, OVXStatisticsReply.class);
		tmpMap.put(OFType.BARRIER_REQUEST, OVXBarrierRequest.class);
		tmpMap.put(OFType.BARRIER_REPLY, OVXBarrierReply.class);
		tmpMap.put(OFType.QUEUE_GET_CONFIG_REQUEST, OVXQueueGetConfigRequest.class);
		tmpMap.put(OFType.QUEUE_GET_CONFIG_REPLY, OVXQueueGetConfigReply.class);
		convertMap = Collections.unmodifiableMap(tmpMap);
		
		Map<OFActionType, Class<?>> tmpActionsMap = new HashMap<OFActionType, Class<?>>();
		tmpActionsMap.put(OFActionType.OUTPUT, OVXActionOutput.class);
		tmpActionsMap.put(OFActionType.SET_VLAN_VID, OVXActionVirtualLanIdentifier.class);
		tmpActionsMap.put(OFActionType.SET_VLAN_PCP, OVXActionVirtualLanPriorityCodePoint.class);
		tmpActionsMap.put(OFActionType.STRIP_VLAN, OVXActionStripVirtualLan.class);
		tmpActionsMap.put(OFActionType.SET_DL_SRC, OVXActionDataLayerSource.class);
		tmpActionsMap.put(OFActionType.SET_DL_DST, OVXActionDataLayerDestination.class);
		tmpActionsMap.put(OFActionType.SET_NW_SRC, OVXActionNetworkLayerSource.class);
		tmpActionsMap.put(OFActionType.SET_NW_DST , OVXActionNetworkLayerDestination.class);
		tmpActionsMap.put(OFActionType.SET_NW_TOS, OVXActionNetworkTypeOfService.class);
		tmpActionsMap.put(OFActionType.SET_TP_SRC, OVXActionTransportLayerSource.class);
		tmpActionsMap.put(OFActionType.SET_TP_DST, OVXActionTransportLayerDestination.class); 
		tmpActionsMap.put(OFActionType.ENQUEUE, OVXActionEnqueue.class);
		tmpActionsMap.put(OFActionType.EXPERIMENTER, OVXActionVendor.class);
		convertActionsMap = Collections.unmodifiableMap(tmpActionsMap);
		
		Map<OFStatsType, Class<?>> tmpStatsRequestMap = new HashMap<OFStatsType, Class<?>>();
		tmpStatsRequestMap.put(OFStatsType.DESC, OVXDescriptionStatistics.class);
		tmpStatsRequestMap.put(OFStatsType.FLOW, OVXFlowStatisticsRequest.class);
		tmpStatsRequestMap.put(OFStatsType.AGGREGATE, OVXAggregateStatisticsRequest.class);
		tmpStatsRequestMap.put(OFStatsType.TABLE, OVXTableStatistics.class);
		tmpStatsRequestMap.put(OFStatsType.PORT, OVXPortStatisticsRequest.class);
		tmpStatsRequestMap.put(OFStatsType.QUEUE, OVXQueueStatisticsRequest.class);
		tmpStatsRequestMap.put(OFStatsType.EXPERIMENTER, OVXVendorStatistics.class);
		convertStatsRequestMap = Collections.unmodifiableMap(tmpStatsRequestMap);
		
		
		Map<OFStatsType, Class<?>> tmpStatsReplyMap = new HashMap<OFStatsType, Class<?>>();
		tmpStatsReplyMap.put(OFStatsType.DESC, OVXDescriptionStatistics.class);
		tmpStatsReplyMap.put(OFStatsType.FLOW, OVXFlowStatisticsRequest.class);
		tmpStatsReplyMap.put(OFStatsType.AGGREGATE, OVXAggregateStatisticsRequest.class);
		tmpStatsReplyMap.put(OFStatsType.TABLE, OVXTableStatistics.class);
		tmpStatsReplyMap.put(OFStatsType.PORT, OVXPortStatisticsRequest.class);
		tmpStatsReplyMap.put(OFStatsType.QUEUE, OVXQueueStatisticsRequest.class);
		tmpStatsReplyMap.put(OFStatsType.EXPERIMENTER, OVXVendorStatistics.class);
		convertStatsReplyMap = Collections.unmodifiableMap(tmpStatsReplyMap);
	}
	
	protected OVXMessageFactory() {
		super();
	}

	public static OVXMessageFactory getInstance() {
		if (OVXMessageFactory.instance == null) {
			OVXMessageFactory.instance = new OVXMessageFactory();
		}
		return OVXMessageFactory.instance;
	}

	public static Object getMessage(final OFMessage m) {
		if (m == null || m.getType() == null) {
			return new OVXUnknownMessage();
		}

		if (OVXMessageFactory.convertMap.get(m.getType()) == null) {
			throw new IllegalArgumentException("OFMessage type " + m.getType().toString()
					+ " unknown to OVX");
		}
		 
		final Class<?> c = OVXMessageFactory.convertMap.get(m.getType());
		try {
			return c.getConstructor(OFMessage.class).newInstance(m);
			
			//final OFMessage m = (OFMessage) c.getConstructor(new Class[] {}).newInstance();
			
			//if (m instanceof OFMessageFactoryAware) {
			//	((OFMessageFactoryAware) m).setMessageFactory(this);
			//}
			//if (m instanceof OFActionFactoryAware) {
			//	((OFActionFactoryAware) m).setActionFactory(this);
			//}
			//if (m instanceof OFStatisticsFactoryAware) {
			//	((OFStatisticsFactoryAware) m).setStatisticsFactory(this);
			//}
			//if (m instanceof OFVendorDataFactoryAware) {
			//	((OFVendorDataFactoryAware)m).setVendorDataFactory(this);
			//}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object getAction(final OFAction a) {
		final Class<?> c =  OVXMessageFactory.convertActionsMap.get(a.getType());
		try {
			return c.getConstructor(OFAction.class).newInstance(a);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	// big hack; need to fix	
	public static OFStatsType getStatistics(final OFType t, final OFStatsType st, final OFVersion ofversion) {
		Class<?> c;
		if (t == OFType.STATS_REPLY) {
			if (OVXMessageFactory.convertStatsReplyMap.get(st.getClass()) == null){ 
				c = OVXVendorStatistics.class;
			} else {
				c = OVXMessageFactory.convertStatsReplyMap.get(st.getClass());
			}
		} else if (t == OFType.STATS_REQUEST) {
			if (OVXMessageFactory.convertStatsRequestMap.get(st.getClass()) == null) {
				c = OVXVendorStatistics.class;
			} else {
				c = OVXMessageFactory.convertStatsRequestMap.get(st.getClass());
			}
		} else {
			throw new RuntimeException("non-stats type in stats factory: " + t);
		}
		try {
			return (OFStatsType) c.getConstructor(OFVersion.class).newInstance(ofversion);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
