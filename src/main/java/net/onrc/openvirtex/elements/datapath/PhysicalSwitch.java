/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.elements.datapath;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.statistics.StatisticsManager;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageFactory;
import net.onrc.openvirtex.messages.OVXStatisticsReply;
import net.onrc.openvirtex.messages.Virtualizable;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatisticsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortStatisticsReply;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;

/**
 * The Class PhysicalSwitch.
 */
public class PhysicalSwitch extends Switch<PhysicalPort> {

	/** The log. */
	Logger log = LogManager.getLogger(PhysicalSwitch.class.getName());
	
	/** The Xid mapper */
	private final XidTranslator<OVXSwitch> translator;


	private StatisticsManager statsMan = null;
	private AtomicReference<Map<Integer, OVXPortStatisticsReply>> portStats;
	private AtomicReference<Map<Integer, List<OVXFlowStatisticsReply>>> flowStats;
	/**
	 * Unregisters OVXSwitches and associated virtual elements mapped to
	 * this PhysicalSwitch. Called by unregister() when the PhysicalSwitch 
	 * is torn down.  
	 */
	class DeregAction implements Runnable {
		PhysicalSwitch psw;   
		int tid;
		DeregAction(PhysicalSwitch s, int t) {
			this.psw = s;
			this.tid = t;
		}

		@Override
		public void run() {
			OVXSwitch vsw;
			try {
				if (psw.map.hasVirtualSwitch(psw, tid)) {
					vsw = psw.map.getVirtualSwitch(psw, tid);
					/* save = don't destroy the switch, it can be saved */    
					boolean save = false;
					if (vsw instanceof OVXBigSwitch) {    
						save = ((OVXBigSwitch) vsw).tryRecovery(psw);    	    
					} 
					if (!save) {
						vsw.unregister();
					}
				}
			} catch (SwitchMappingException e) {
				log.warn("Inconsistency in OVXMap: {}", e.getMessage());
			}
		}
	}

	/**
	 * Instantiates a new physical switch.
	 * 
	 * @param switchId
	 *            the switch id
	 */
	public PhysicalSwitch(final long switchId) {
		super(switchId);
		this.translator = new XidTranslator<OVXSwitch>();
		this.portStats = new AtomicReference<Map<Integer, OVXPortStatisticsReply>>();
		this.flowStats = new AtomicReference<Map<Integer, List<OVXFlowStatisticsReply>>>();
		this.statsMan = new StatisticsManager(this);
	}

	/**
	 * Gets the OVX port number.
	 * 
	 * @param physicalPortNumber
	 *            the physical port number
	 * @param tenantId
	 *            the tenant id
	 * @return the oVX port number
	 */
	public Integer getOVXPortNumber(final Integer physicalPortNumber,
			final Integer tenantId, final Integer vLinkId) {
		return this.portMap.get(physicalPortNumber)
				.getOVXPort(tenantId, vLinkId).getPortNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
	 * .OFMessage)
	 */
	@Override
	public void handleIO(final OFMessage msg, Channel channel) {
		try {
			Object o = OVXMessageFactory.getMessage(msg);
			((Virtualizable) o).virtualize(this);
			
		} catch (final ClassCastException e) {
			this.log.error("Received illegal message : " + msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.onrc.openvirtex.elements.datapath.Switch#tearDown()
	 */
	@Override
	public void tearDown() {
		this.log.info("Switch disconnected {} ",
				this.featuresReply.getDatapathId());
		this.statsMan.stop();
		this.channel.disconnect();
		this.map.removePhysicalSwitch(this);
	}

	/**
	 * Fill port map. Assume all ports are edges until discovery says otherwise.
	 */
	protected void fillPortMap() {
		for (final OFPortDesc port : this.featuresReply.getPorts()) {
			final PhysicalPort physicalPort = new PhysicalPort(port, this, true);
			this.addPort(physicalPort);
		}
	}

	@Override
	public boolean addPort(final PhysicalPort port) {
		final boolean result = super.addPort(port);
		if (result) {
			PhysicalNetwork.getInstance().addPort(port);
		}
		return result;
	}
	
	/**
	 * removes the specified port from this PhysicalSwitch. This includes
	 * removal from the switch's port map, topology discovery, and the 
	 * PhysicalNetwork topology. 
	 *  
	 * @param port
	 * @return
	 */
	public boolean removePort(final PhysicalPort port) {
		final boolean result = super.removePort(port.getPortNumber());
		if (result) {
			PhysicalNetwork pnet = PhysicalNetwork.getInstance();
			pnet.removePort(pnet.getDiscoveryManager(this.getSwitchId()), port);    
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.onrc.openvirtex.elements.datapath.Switch#init()
	 */
	@Override
	public boolean boot() {
		this.log.info("Switch connected with dpid {}, name {} and type {}",
				this.featuresReply.getDatapathId(), 
				this.getSwitchName(),
				this.desc.getHwDesc());
		PhysicalNetwork.getInstance().addSwitch(this);
		this.fillPortMap();
		this.statsMan.start();
		return true;
	}

	/**
	 * Removes this PhysicalSwitch from the network. Also removes associated
	 * ports, links, and virtual elements mapped to it (OVX*Switch, etc.).
	 */
	@Override
	public void unregister() {
		/* tear down OVXSingleSwitches mapped to this PhysialSwitch */
		for (Integer tid : this.map.listVirtualNetworks().keySet()) {   
			DeregAction dereg = new DeregAction(this, tid);    
			new Thread(dereg).start();
		}
		/* try to remove from network and disconnect */
		PhysicalNetwork.getInstance().removeSwitch(this);
		this.portMap.clear();
		this.tearDown();
	}

	@Override
	public void sendMsg(final OFMessage msg, final OVXSendMsg from) {
		if ((this.channel.isOpen()) && (this.isConnected)) {
			this.channel.write(Collections.singletonList(msg));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.onrc.openvirtex.elements.datapath.Switch#toString()
	 */
	@Override
	public String toString() {
		return "DPID : " + this.switchId 
				+ ", remoteAddr : "
				+ ((this.channel == null) ? "None" : this.channel.getRemoteAddress().toString());
	}

	/**
	 * Gets the port.
	 * 
	 * @param portNumber
	 *            the port number
	 * @return the port instance
	 */

	public PhysicalPort getPort(final Integer portNumber) {
		return this.portMap.get(portNumber);
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof PhysicalSwitch) {
			return this.switchId == ((PhysicalSwitch) other).switchId;
		}
		return false;
	}

	public long translate(final OFMessage ofm, final OVXSwitch sw) {
		return this.translator.translate(ofm.getXid(), sw);
	}

	public XidPair<OVXSwitch> untranslate(final OFMessage m) {
		final XidPair<OVXSwitch> pair = this.translator.untranslate(m.getXid());
		if (pair == null) {
			return null;
		}
		return pair;
	}

	public void setPortStatistics(Map<Integer, OVXPortStatisticsReply> stats) {
		this.portStats.set(stats);
	}

	public void setFlowStatistics(Map<Integer, List<OVXFlowStatisticsReply>> stats) {
		this.flowStats.set(stats);
	}

	public List<OVXFlowStatisticsReply> getFlowStats(int tid) {
		Map<Integer, List<OVXFlowStatisticsReply>> stats = this.flowStats.get();
		if (stats != null && stats.containsKey(tid))
			return Collections.unmodifiableList(stats.get(tid));
		return null;
	}

	public OVXPortStatisticsReply getPortStat(int portNumber) {
		Map<Integer, OVXPortStatisticsReply> stats = this.portStats.get();
		if (stats != null) {
			return stats.get(portNumber);
		}
		return null;
	}

	public void cleanUpTenant(Integer tenantId, Integer port) {
		this.statsMan.cleanUpTenant(tenantId, port);
	}
	
	public void removeFlowMods(OVXStatisticsReply msg) {
		OFFlowStatsReply ofstatmsg = (OFFlowStatsReply) msg;

		long tid = ofstatmsg.getXid() >> 16;
		int port = (int) ofstatmsg.getXid() & 0xFFFF;
		
		for (OFFlowStatsEntry entry : ofstatmsg.getEntries()) {
			//OVXFlowStatisticsReply reply = (OVXFlowStatisticsReply) stat;
			OVXFlowStatisticsReply reply = new OVXFlowStatisticsReply(entry.getVersion());
			reply.setEntry(entry);
			
			if (tid != this.getTidFromCookie(reply.getCookie().getValue()))
				continue;
			if (port != 0) {
				sendDeleteFlowMod(reply, port);
				if (reply.getMatch().get(MatchField.IN_PORT).getPortNumber() == port) 
					sendDeleteFlowMod(reply, OFPort.ANY.getPortNumber());
			} else
				sendDeleteFlowMod(reply, OFPort.ANY.getPortNumber());
		}
	}

	private void sendDeleteFlowMod(OVXFlowStatisticsReply reply, int port) {
		OVXFlowMod dFm = new OVXFlowMod(reply.getVersion());
		dFm.setCommand(OFFlowModCommand.DELETE_STRICT)
		.setMatch(reply.getMatch())
		.setOutPort(OFPort.of(port));
		//dFm.setLengthU(OVXFlowMod.MINIMUM_LENGTH);
		this.sendMsg(dFm.getFlow(), this);
	}

	private int getTidFromCookie(long cookie) {
		return (int) (cookie >> 32);
	}

	@Override
	public void handleRoleIO(OFExperimenter msg, Channel channel) {
		log.warn("Received Role message {} from switch {}, but no role was requested", msg, this.switchName);
	}

	@Override
	public void removeChannel(Channel channel) {
		
	}
}
