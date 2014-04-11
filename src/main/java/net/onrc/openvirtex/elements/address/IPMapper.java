/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.elements.address;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;

public class IPMapper {
    static Logger log = LogManager.getLogger(IPMapper.class.getName());

    public static Integer getPhysicalIp(Integer tenantId, Integer virtualIP) {
    	final Mappable map = OVXMap.getInstance();
    	final OVXIPAddress vip = new OVXIPAddress(tenantId, virtualIP);
    	try {
    		PhysicalIPAddress pip;
    		if (map.hasPhysicalIP(vip, tenantId)) {
    			pip = map.getPhysicalIP(vip, tenantId);
    		} else {
    			pip = new PhysicalIPAddress(map.getVirtualNetwork(tenantId).nextIP());
    			log.debug("Adding IP mapping {} -> {} for tenant {}", vip, pip, tenantId);
    			
    			map.addIP(pip, vip);
    		}
    		return pip.getIp();
    	} catch (IndexOutOfBoundException e) {
    		log.error("No available physical IPs for virtual ip {} in tenant {}",vip, tenantId);
    	} catch (NetworkMappingException e) {
    		log.error(e);
    	} catch (AddressMappingException e) {
    		log.error("Inconsistency in Physical-Virtual mapping : {}", e);
    	}
    	return 0;
    }

    public static void rewriteMatch(final Integer tenantId, Match m) {
    	if (m.get(MatchField.ETH_TYPE) == EthType.IPv4) {
    		m = m.createBuilder().setExact(MatchField.IPV4_SRC, 
    				IPv4Address.of(getPhysicalIp(tenantId, m.get(MatchField.IPV4_SRC).getInt()))).build();
    		m = m.createBuilder().setExact(MatchField.IPV4_DST, 
    				IPv4Address.of(getPhysicalIp(tenantId, m.get(MatchField.IPV4_DST).getInt()))).build();
    	} else if (m.get(MatchField.ETH_TYPE) == EthType.ARP) {
    		m = m.createBuilder().setExact(MatchField.ARP_SPA, 
    				IPv4Address.of(getPhysicalIp(tenantId, m.get(MatchField.ARP_SPA).getInt()))).build();
    		m = m.createBuilder().setExact(MatchField.ARP_TPA, 
    				IPv4Address.of(getPhysicalIp(tenantId, m.get(MatchField.ARP_TPA).getInt()))).build();
    	}
    	
    	//match.setNetworkSource(getPhysicalIp(tenantId, match.getNetworkSource()));
    	//match.setNetworkDestination(getPhysicalIp(tenantId, match.getNetworkDestination()));
    }

    public static List<OFAction> prependRewriteActions(final Integer tenantId, final Match match) {
    	final List<OFAction> actions = new LinkedList<OFAction>();
    	
    	if (match.get(MatchField.ETH_TYPE) == EthType.IPv4) {
    		if(match.isExact(MatchField.IPV4_SRC)) {
    			final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource(match.getVersion());
    			//srcAct.setNetworkAddress(getPhysicalIp(tenantId, match.getNetworkSource()));
    			srcAct.setNetworkAddress(getPhysicalIp(tenantId, match.get(MatchField.IPV4_SRC).getInt()));
    			actions.add(srcAct.getAction());
    		}
    		
    		if (match.isExact(MatchField.IPV4_DST)) {
    			final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination(match.getVersion());
    			//dstAct.setNetworkAddress(getPhysicalIp(tenantId, match.getNetworkDestination()));
    			dstAct.setNetworkAddress(getPhysicalIp(tenantId, match.get(MatchField.IPV4_DST).getInt()));    		
    			actions.add(dstAct.getAction());
    		}
    	} else if (match.get(MatchField.ETH_TYPE) == EthType.ARP) {
    		if (match.isExact(MatchField.ARP_SPA)) {
    			final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource(match.getVersion());
    			srcAct.setNetworkAddress(getPhysicalIp(tenantId, match.get(MatchField.ARP_SPA).getInt()));
    			actions.add(srcAct.getAction());
    		}
    		
    		if (match.isExact(MatchField.ARP_TPA)) {
    			final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination(match.getVersion());
    			dstAct.setNetworkAddress(getPhysicalIp(tenantId, match.get(MatchField.ARP_TPA).getInt()));    		
    			actions.add(dstAct.getAction());
    		}
    	}
    	return actions;
    }
    
    public static List<OFAction> prependUnRewriteActions(final Match match) {
    	final List<OFAction> actions = new LinkedList<OFAction>();
    	
    	if (match.get(MatchField.ETH_TYPE) == EthType.IPv4) {
    		if (match.isExact(MatchField.IPV4_SRC)) {
    			final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource(match.getVersion());
    			srcAct.setNetworkAddress(match.get(MatchField.IPV4_SRC).getInt());
    			actions.add(srcAct.getAction());
    		}
    		
        	if (match.isExact(MatchField.IPV4_DST)) {
        		final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination(match.getVersion());
        		dstAct.setNetworkAddress(match.get(MatchField.IPV4_DST).getInt());
        		actions.add(dstAct.getAction());
        	}
    	} else if (match.get(MatchField.ETH_TYPE) == EthType.ARP) {
    		if (match.isExact(MatchField.ARP_SPA)) {
    			final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource(match.getVersion());
    			srcAct.setNetworkAddress(match.get(MatchField.ARP_SPA).getInt());
    			actions.add(srcAct.getAction());
    		}
    		
    		if (match.isExact(MatchField.ARP_TPA)) {
    			final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination(match.getVersion());
    			dstAct.setNetworkAddress(match.get(MatchField.ARP_TPA).getInt());
    			actions.add(dstAct.getAction());
    		}
    	}
    	return actions;
    }
}
