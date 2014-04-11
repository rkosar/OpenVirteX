/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.messages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.PhysicalLink;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.LinkPair;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.LinkMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.routing.SwitchRoute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortState;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortReason;
import org.projectfloodlight.openflow.protocol.OFPortStatus;

public class OVXPortStatus implements Virtualizable {
	private OFPortStatus ps;
	private final Logger log      = LogManager.getLogger(OVXPortStatus.class);

	public OVXPortStatus(OFMessage m) {
		this.ps = (OFPortStatus) m;
	}
	
	@Override
	public void virtualize(final PhysicalSwitch sw) {
		Mappable map = sw.getMap();
		
		PhysicalPort p = sw.getPort(this.ps.getDesc().getPortNo().getPortNumber());
		if (p == null) {
			handlePortAdd(sw, p); 
			return;
		}

		log.info("Received {} from switch {}", this.toString(), sw.getSwitchId());		
		LinkPair<PhysicalLink> pair = p.getLink();
		try {
			Set<Integer> vnets = map.listVirtualNetworks().keySet();
			for ( Integer tenantId : vnets) {
				/* handle vLinks/routes containing phyLink to/from this port. */    
				if ((pair != null) && (pair.exists())) {
					handleLinkChange(sw, map, pair, tenantId);
				}
				List<Map<Integer, OVXPort>> vports = p.getOVXPorts(tenantId);
				/* cycle through all OVXPorts for this port. */
				Iterator<Map<Integer, OVXPort>> p_itr = vports.iterator();
				while(p_itr.hasNext()) {
					Map<Integer, OVXPort> mp = p_itr.next();
					if (mp == null) {
						continue;
					}
					for (Map.Entry<Integer, OVXPort> p_map : mp.entrySet()) {
						OVXPort vport = p_map.getValue();
						if (vport == null) {
							continue;
						}
						if (isReason(OFPortReason.DELETE)) {
							/* try to remove OVXPort, vLinks, routes */
							vport.unMapHost();
							vport.handlePortDelete(this);
							sw.removePort(p);
						} else if (isReason(OFPortReason.MODIFY)) {
							if (isState(OFPortState.LINK_DOWN)) {
								/* set ports as edge, but don't remove vLinks */
								vport.handlePortDisable(this);
							} else if (!isState(OFPortState.LINK_DOWN) &&
									   !p.getState().contains(OFPortState.LINK_DOWN)) {
								/* set links to non-edge, if it was previously disabled */
								vport.handlePortEnable(this);
							} 
						}
					}
				}
			}
		} catch (NetworkMappingException | LinkMappingException e) {
			log.warn("Couldn't process reason={} for PortStatus for port {}", 
					this.ps.getReason(), p.getPortNumber());
			e.printStackTrace();
		}	
	} 

	private void handlePortAdd(PhysicalSwitch sw, PhysicalPort p) {
		/* add a new port to PhySwitch if add message, quit otherwise */
		if (isReason(OFPortReason.ADD)) {
			p = new PhysicalPort(this.ps.getDesc(), sw, false);
			if (!sw.addPort(p)) {
				log.warn("Could not add new port {} to physical switch {}", 
						p.getPortNumber(), sw.getSwitchId());
			}
			log.info("Added port {} to switch {}", p.getPortNumber(), sw.getSwitchId());		
		}
	}

	/**
	 * Handles change in internal link state, e.g a PhysicalPort in, but not at 
	 * edges of, a OVXLink or SwitchRoute
	 * 
	 * @param map Mappable containing global information
	 * @param pair the LinkPair associated with the PhysicalPort
	 * @param tid the tenant ID
	 * @throws LinkMappingException
	 * @throws NetworkMappingException
	 */
	private void handleLinkChange(PhysicalSwitch sw, Mappable map, 
			LinkPair<PhysicalLink> pair, int tid) 
					throws LinkMappingException, NetworkMappingException {
		PhysicalLink plink = pair.getOutLink();
		
		if (!isState(OFPortState.LINK_DOWN) && 
			!plink.getSrcPort().getState().contains(OFPortState.LINK_DOWN)) {
			OVXNetwork net = map.getVirtualNetwork(tid);
			for (OVXLink link : net.getLinks())
				link.tryRevert(plink);
			for (OVXSwitch ovxSw : net.getSwitches()) {
				if (ovxSw instanceof OVXBigSwitch) {
					for (Map<OVXPort, SwitchRoute> routeMap : ((OVXBigSwitch) ovxSw).getRouteMap().values()) {
						for (SwitchRoute route : routeMap.values())
							route.tryRevert(plink);
					}
				}
			}
		}
		
		if (map.hasOVXLinks(plink, tid)) {
			List<OVXLink> vlinks = map.getVirtualLinks(plink, tid);
			for (OVXLink vlink : vlinks) {
				if (isReason(OFPortReason.DELETE)) {
					/* couldn't recover, remove link */
					if (!vlink.tryRecovery(plink)) {
						OVXPort vport = vlink.getSrcPort();
						vport.unMapHost();
						vport.handlePortDelete(this);
						sw.removePort(plink.getSrcPort());
					}
				}
				if (isReason(OFPortReason.MODIFY)) {
					if (isState(OFPortState.LINK_DOWN)) { 
						/* couldn't recover, remove link */
						if (!vlink.tryRecovery(plink)) {
							vlink.getSrcPort().handlePortDisable(this);
						}    
					} else if (!isState(OFPortState.LINK_DOWN) && 
							   !plink.getSrcPort().getState().contains(OFPortState.LINK_DOWN)) {
						log.debug("enabling OVXLink mapped to port {}");
						/* try to switch back to original path, 
						 * if not just bring up and hope it's working */
						if (!vlink.tryRevert(plink)) {
							vlink.getSrcPort().handlePortEnable(this);
						}
					} 
				}
			}
		}
		if (map.hasSwitchRoutes(plink, tid)) {
			Set<SwitchRoute> routes = new HashSet<SwitchRoute>(map.getSwitchRoutes(plink, tid));
			for (SwitchRoute route : routes) {
				/* try to recover, remove route if we fail, but don't send any stat up */
				if ((isReason(OFPortReason.DELETE)) ||
						(isReason(OFPortReason.MODIFY) & isState(OFPortState.LINK_DOWN))) {
					if (!route.tryRecovery(plink)) {
						route.getSrcPort().handleRouteDisable(this);
					}
				} 
			}
		}
	}

	public boolean isReason(OFPortReason reason) {
		return this.ps.getReason().compareTo(reason) == 0;
	}

	public boolean isState(OFPortState state) {
		return this.ps.getDesc().getState().contains(state);   
	}

	@Override
	public String toString() {
		return "OVXPortStatus: reason[" 
				+ OFPortReason.valueOf(this.ps.getReason().name()) + "]"
				+ " port[" + this.ps.getDesc().getPortNo() +"]";
	}
	
	public OFPortReason getReason(){
		return this.ps.getReason();
	}
	
	public OFPortDesc getDesc(){
		return this.ps.getDesc();
	}
	
	public OVXPortStatus setStatus(OFPortStatus status){
		this.ps = status;
		return this;
	}
	
	public OVXPortStatus setDesc(OFPortDesc desc){
		this.ps = this.ps.createBuilder().setDesc(desc).build();
		return this;
	}
}
