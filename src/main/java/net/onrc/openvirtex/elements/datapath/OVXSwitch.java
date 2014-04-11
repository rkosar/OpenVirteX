/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.db.DBManager;
import net.onrc.openvirtex.elements.Persistable;

import java.util.Set;
import java.util.TreeSet;

import net.onrc.openvirtex.elements.datapath.role.RoleManager;
import net.onrc.openvirtex.elements.datapath.role.RoleManager.Role;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ControllerStateException;
import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.UnknownRoleException;
import net.onrc.openvirtex.messages.Devirtualizable;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageFactory;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPacketIn;
import net.onrc.openvirtex.util.BitSetIndex;
import net.onrc.openvirtex.util.BitSetIndex.IndexType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.util.LRULinkedHashMap;
import org.projectfloodlight.openflow.protocol.OFNiciraControllerRoleRequest;
import org.projectfloodlight.openflow.protocol.OFNiciraControllerRoleReply;

/**
 * The Class OVXSwitch.
 */
public abstract class OVXSwitch extends Switch<OVXPort> implements Persistable {
	private Logger log = LogManager.getLogger(OVXSwitch.class
			.getName());

	/**
	 * Datapath description string should this be made specific per type of
	 * virtual switch
	 */
	public static final String                          DPDESCSTRING     = "OpenVirteX Virtual Switch";

	/** The buffer dimension. */
	protected static int                                bufferDimension  = 4096;

	/** The supported actions. */
	protected Set<OFActionType>                         supportedActions = null;

	/** The tenant id. */
	protected Integer                                   tenantId         = 0;

	/** The miss send len. Default in spec is 128 */
	protected Integer                                   missSendLen      = 128; 

	/** The is active. */
	protected boolean                                   isActive         = false;

	/** The capabilities. */
	protected OVXSwitchCapabilities                     capabilities;

	/** The backoff counter for this switch when unconnected */
	private AtomicInteger                               backOffCounter   = null;

	/**
	 * The buffer map
	 */
	protected LRULinkedHashMap<Integer, OVXPacketIn>    bufferMap;

	private AtomicInteger                               bufferId         = null;

	private final BitSetIndex                           portCounter;

	/**
	 * The virtual flow table
	 */
	protected FlowTable                                 flowTable;
	
	private OFVersion                                   ofversion;	
	/**
	 * Used to save which channel the message came in on.
	 */
	private final XidTranslator<Channel>                channelMux;

	/**
	 * Role Manager. Saves all role requests coming 
	 * from each controller. It is also responsible 
	 * for permitting or denying certain operations
	 * based on the current role of a controller.
	 */
	private final RoleManager                           roleMan;

	/**
	 * Instantiates a new OVX switch.
	 * 
	 * @param switchId
	 *            the switch id
	 * @param tenantId
	 *            the tenant id
	 */
	protected OVXSwitch(final Long switchId, final Integer tenantId, final OFVersion ofversion) {
		super(switchId);
		this.tenantId = tenantId;
		this.missSendLen = 0;
		this.isActive = false;
		this.capabilities = new OVXSwitchCapabilities();
		this.backOffCounter = new AtomicInteger();
		this.resetBackOff();
		this.bufferMap = new LRULinkedHashMap<Integer, OVXPacketIn>(
				OVXSwitch.bufferDimension);
		this.portCounter = new BitSetIndex(IndexType.PORT_ID);
		this.bufferId = new AtomicInteger(1);
		this.flowTable = new OVXFlowTable(this);
		this.roleMan = new RoleManager();
		this.channelMux = new XidTranslator<Channel>();
		this.ofversion = ofversion;
		// Supporting all the actions 
		this.supportedActions = new HashSet<OFActionType>();
		for(OFActionType action : OFActionType.values()){
			this.supportedActions.add(action);
		}	
	}
	

	/**
	 * Gets the tenant id.
	 * 
	 * @return the tenant id
	 */
	public Integer getTenantId() {
		return this.tenantId;
	}

	/**
	 * Gets the miss send len.
	 * 
	 * @return the miss send len
	 */
	public int getMissSendLen() {
		return this.missSendLen;
	}

	/**
	 * Sets the miss send len.
	 * 
	 * @param missSendLen
	 *            the miss send len
	 * @return true, if successful
	 */
	public boolean setMissSendLen(final Integer missSendLen) {
		this.missSendLen = missSendLen;
		return true;
	}

	/**
	 * Checks if is active.
	 * 
	 * @return true, if is active
	 */
	public boolean isActive() {
		return this.isActive;
	}

	/**
	 * Sets the active.
	 * 
	 * @param isActive
	 *            the new active
	 */
	public void setActive(final boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Gets the physical port number.
	 * 
	 * @param ovxPortNumber
	 *            the ovx port number
	 * @return the physical port number
	 */
	public Integer getPhysicalPortNumber(final OFPort ovxPortNumber) {
		return this.portMap.get(ovxPortNumber).getPhysicalPortNumber();
	}
	
	public OFVersion getVersion() {
		return ofversion;
	}
	
	public void resetBackOff() {
		this.backOffCounter.set(-1);
	}

	public int incrementBackOff() {
		return this.backOffCounter.incrementAndGet();
	}

	public int getNextPortNumber() throws IndexOutOfBoundException {
		return this.portCounter.getNewIndex();
	}

	public void relesePortNumber(OFPort portNumber) {
		this.portCounter.releaseIndex(portNumber.getPortNumber());
	}

	protected void addDefaultPort(final LinkedList<OFPortDesc> ports) {
	
		Set<OFPortConfig> portconfig = new HashSet<OFPortConfig>();
		portconfig.add(OFPortConfig.PORT_DOWN);
		
		Set<OFPortState> portstate = new HashSet<OFPortState>();
		portstate.add(OFPortState.LINK_DOWN);
		
		Set<OFPortFeatures> portfeatures = new HashSet<OFPortFeatures>();
		portfeatures.add(OFPortFeatures.PF_COPPER);
		
		final byte[] addr = { (byte) 0xA4, (byte) 0x23, (byte) 0x05,
				(byte) 0x00, (byte) 0x00, (byte) 0x00 };
		
		OFPortDesc pd = OFFactories.getFactory(ofversion)
				.buildPortDesc()
				.setPortNo(OFPort.LOCAL)
				.setName("OF Local Port")
				.setHwAddr(MacAddress.of(addr))
				.setConfig(portconfig)
				.setState(portstate)
				.setAdvertised(portfeatures)
				.setCurr(portfeatures)
				.setSupported(portfeatures)
				.build();
		ports.add(pd);
	}

	public void register(final List<PhysicalSwitch> physicalSwitches) {
		this.map.addSwitches(physicalSwitches, this);
		DBManager.getInstance().save(this);
	}

	public void unregister() {
		DBManager.getInstance().remove(this);
		this.isActive = false;
		if (this.getPorts() != null) {
			OVXNetwork net;
			try {
				net = this.getMap().getVirtualNetwork(this.tenantId);
			} catch (NetworkMappingException e) {
				log.error("Error retrieving the network with id {}. Unregister for OVXSwitch {} not fully done!", 
						this.getTenantId(), this.getSwitchName());
				return;
			}
			final Set<Integer> portSet = new TreeSet<Integer>(this.getPorts().keySet());
			
			for (final Integer set_port : portSet) {
				final OVXPort port = this.getPort(set_port);
				if (port.isEdge()) {
					Host h = net.getHost(port);
					if (h != null) 
						net.getHostCounter().releaseIndex(h.getHostId());
				} else {
					net.getLinkCounter().releaseIndex(port.getLink().getInLink().getLinkId());
				}
				port.unregister();
			}
		}
		// remove the switch from the map
		try {
			this.map.getVirtualNetwork(this.tenantId).removeSwitch(this);
		} catch (NetworkMappingException e) {
			log.warn(e.getMessage());
		}

		cleanUpFlowMods(false);

		this.map.removeVirtualSwitch(this);
		this.tearDown();
	}

	private void cleanUpFlowMods(boolean isOk) {
		log.info("Cleaning up flowmods");
		List<PhysicalSwitch> physicalSwitches;
		try {
			physicalSwitches = this.map.getPhysicalSwitches(this);
		} catch (SwitchMappingException e) {
			if (!isOk)
				log.warn("Failed to cleanUp flowmods for tenant {} on switch {}", 
						this.tenantId, this.getSwitchName());
			return;
		}
		for (PhysicalSwitch sw : physicalSwitches) 
			sw.cleanUpTenant(this.tenantId, 0);
	}

	@Override
	public Map<String, Object> getDBIndex() {
		Map<String, Object> index = new HashMap<String, Object>();
		index.put(TenantHandler.TENANT, this.tenantId);
		return index;
	}

	@Override
	public String getDBKey() {
		return Switch.DB_KEY;
	}

	@Override
	public String getDBName() {
		return DBManager.DB_VNET;
	}

	@Override
	public Map<String, Object> getDBObject() {
		Map<String, Object> dbObject = new HashMap<String, Object>();
		dbObject.put(TenantHandler.VDPID, this.switchId);
		List<Long> switches = new ArrayList<Long>();
		try {
			for (PhysicalSwitch sw: this.map.getPhysicalSwitches(this)) {
				switches.add(sw.getSwitchId());
			}
		} catch (SwitchMappingException e) {
			return null;
		}
		dbObject.put(TenantHandler.DPIDS, switches);
		return dbObject;
	}	

	@Override
	public void tearDown() {
		this.isActive = false;
		 
		roleMan.shutDown();

		cleanUpFlowMods(true);
		for (OVXPort p : getPorts().values()) {
			if (p.isLink()) 
				p.tearDown();
		}
	}

	/**
	 * Generate features reply.
	 */
	public void generateFeaturesReply() {
		final LinkedList<OFPortDesc> portList = new LinkedList<OFPortDesc>();
		for (final OVXPort ovxPort : this.portMap.values()) {
			portList.add(ovxPort.getDesc());
		}
		
		/*
		 * Giving the switch a port (the local port) which is set
		 * administratively down.
		 * 
		 * Perhaps this can be used to send the packets to somewhere
		 * interesting.
		 */
		this.addDefaultPort(portList);
		
		OFFeaturesReply fr = OFFactories.getFactory(ofversion)
				.buildFeaturesReply()
				.setDatapathId(DatapathId.of(this.switchId))
				.setPorts(portList)
				.setNBuffers(OVXSwitch.bufferDimension)
				.setNTables((short)1)
				.setCapabilities(this.capabilities.getOVXSwitchCapabilities())
				.setActions(this.supportedActions)
				.setXid(0)
				.build();
		
		//ofReply.setLengthU(OFFeaturesReply.MINIMUM_LENGTH
		//		+ OFPhysicalPort.MINIMUM_LENGTH * portList.size());

		this.setFeaturesReply(fr);
	}

	/**
	 * Boots virtual switch by connecting it to the controller TODO: should
	 * 
	 * @return True if successful, false otherwise
	 */
	@Override
	public boolean boot() {
		this.generateFeaturesReply();
		final OpenVirteXController ovxController = OpenVirteXController
				.getInstance();
		ovxController.registerOVXSwitch(this);
		this.setActive(true);
		for (OVXPort p : getPorts().values()) {
			if (p.isLink()) {
				p.boot();
			}	
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.onrc.openvirtex.elements.datapath.Switch#toString()
	 */
	@Override
	public String toString() {
		return "SWITCH: switchId: " + this.switchId 
					+ " - switchName: " + this.switchName 
					+ " - isConnected: " + this.isConnected
					+ " - tenantId: " + this.tenantId 
					+ " - missSendLength: " + this.missSendLen 
					+ " - isActive: " + this.isActive
					+ " - capabilities: " + this.capabilities.getOVXSwitchCapabilities();
	}

	public synchronized int addToBufferMap(final OVXPacketIn pktIn) {
		// TODO: this isn't thread safe... fix it
		this.bufferId.compareAndSet(OVXSwitch.bufferDimension, 0);
		this.bufferMap.put(this.bufferId.get(), new OVXPacketIn(pktIn));
		return this.bufferId.getAndIncrement();
	}

	public OVXPacketIn getFromBufferMap(final Integer bufId) {
		return this.bufferMap.get(bufId);
	}

	public FlowTable getFlowTable() {
		return this.flowTable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OVXSwitch))
			return false;
		OVXSwitch other = (OVXSwitch) obj;
		if (tenantId == null) {
			if (other.tenantId != null)
				return false;
		} 
		return this.switchId == other.switchId
				&& this.tenantId == other.tenantId;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.onrc.openvirtex.core.io.OVXSendMsg#sendMsg(org.openflow.protocol.
	 * OFMessage, net.onrc.openvirtex.core.io.OVXSendMsg)
	 */
	@Override
	public void sendMsg(OFMessage msg, final OVXSendMsg from) {
		XidPair<Channel> pair = channelMux.untranslate(msg.getXid());
		
		Channel c = null;
		if (pair != null) {
			msg = msg.createBuilder()
					.setXid(pair.getXid())
					.build();
			c = pair.getSwitch();
		}
		
		if (this.isConnected && this.isActive) {
			roleMan.sendMsg(msg, c);
		} else {
			// TODO: we probably should install a drop rule here.
			log.warn("Virtual switch {} is not active or is not connected to a controller", switchName);
		}	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
	 * .OFMessage)
	 */
	@Override
	public void handleIO(OFMessage msg, Channel channel) {
		/*
		 * Save the channel the msg came in on
		 */
		//msg.setXid(channelMux.translate(msg.getXid(), channel));
		long newxid = channelMux.translate(msg.getXid(), channel);
		msg = msg.createBuilder()
				.setXid(newxid)
				.build();
		
		try {
			/*
			 * Check whether this channel (ie. controller) is permitted 
			 * to send this msg to the dataplane
			 */
			if (this.roleMan.canSend(channel, msg)) {
				((Devirtualizable) OVXMessageFactory.getMessage(msg)).devirtualize(this);
			}
			else
				denyAccess(channel, msg, this.roleMan.getRole(channel));
		} catch (final ClassCastException e) {
			this.log.error("Received illegal message: {} error {}", msg, e);
		}
	}
	
	@Override
	public void handleRoleIO(OFExperimenter msg, Channel channel) {
		Role role = extractNiciraRoleRequest(channel, msg);
		try {
			this.roleMan.setRole(channel, role);
			sendRoleReply(role, msg.getXid(), channel);
			log.info("Finished handling role for {}", channel.getRemoteAddress() );
		} catch (IllegalArgumentException | UnknownRoleException ex) {
			log.warn(ex.getMessage());
		}
	}

	/**
	 * get a OVXFlowMod out of the map
	 * 
	 * @param cookie
	 *            the physical cookie
	 * @return
	 * @throws MappingException 
	 */
	public OVXFlowMod getFlowMod(final Long cookie) throws MappingException {
		return this.flowTable.getFlowMod(U64.of(cookie));
	}
	
	public void setChannel(Channel channel) {
		this.roleMan.addController(channel);
	}
	
	public void removeChannel(Channel channel) {
		this.roleMan.removeChannel(channel);
	}
	
	/**
	 * Remove an entry in the mapping
	 * 
	 * @param cookie
	 * @return The deleted FlowMod
	 */
	public OVXFlowMod deleteFlowMod(final Long cookie) {
		return this.flowTable.deleteFlowMod(U64.of(cookie));
	}
	
	private Role extractNiciraRoleRequest(Channel chan, OFExperimenter experimenterMessage) {		
		//long experimenter = experimenterMessage.getExperimenter();
		//if (experimenter != 0x2320L || !(experimenterMessage instanceof OFNiciraControllerRoleRequest))	
		//	return null;
		
		if (!(experimenterMessage instanceof OFNiciraControllerRoleRequest))
			return null;		
		
		OFNiciraControllerRoleRequest rr = (OFNiciraControllerRoleRequest) experimenterMessage;
		
		Role role = Role.fromNxRole(rr.getRole().ordinal());
		
		if (role == null) {
			String msg = String.format("Controller: [%s], State: [%s], "
					+ "received ROLE_REPLY with invalid role "
					+ "value %d",
					chan.getRemoteAddress(),
					this.toString(),
					rr.getRole());
			throw new ControllerStateException(msg);
		}
		return role;
	}
	
	private void denyAccess(Channel channel, OFMessage m, Role role) {
		log.warn("Controller {} may not send message {} because role state is {}", 
				channel.getRemoteAddress(), m, role);
		OFMessage e = OVXMessageUtil.makeErrorMsg(OFBadRequestCode.EPERM, m);
		channel.write(Collections.singletonList(e));
	}

	private void sendRoleReply(Role role, long xid, Channel channel) {
		OFNiciraControllerRoleReply rr = OFFactories.getFactory(this.ofversion)
				.buildNiciraControllerRoleReply()
				.setXid(xid)
				.setRole(OFNiciraControllerRole.values()[role.ordinal()])
				.build();
		
		//vendor.setXid(xid);
		//vendor.setVendor(OFNiciraVendorData.NX_VENDOR_ID);
		//OFRoleReplyVendorData reply = new OFRoleReplyVendorData(role.toNxRole());
		//vendor.setVendorData(reply);
		//vendor.setLengthU(OFVendor.MINIMUM_LENGTH + reply.getLength());
		
		channel.write(Collections.singletonList(rr));
	}

	/**
	 * Generates a new XID for messages destined for the physical network.
	 * 
	 * @param msg The OFMessage being translated
	 * @param inPort The ingress port 
	 * @return the new message XID
	 * @throws SwitchMappingException 
	 */
	public abstract long translate(OFMessage msg, OVXPort inPort);

	/**
	 * Sends a message towards the physical network, via the PhysicalSwitch mapped to this OVXSwitch. 
	 * 
	 * @param msg The OFMessage being translated
	 * @param inPort The ingress port, used to identify the PhysicalSwitch underlying an OVXBigSwitch. May be null. 
	 * Sends a message towards the physical network
	 * 
	 * @param msg The OFMessage being translated
	 * @param inPort The ingress port
	 */
	public abstract void sendSouth(OFMessage msg, OVXPort inPort);

}
