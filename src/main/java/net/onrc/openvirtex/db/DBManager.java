package net.onrc.openvirtex.db;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.Persistable;
import net.onrc.openvirtex.elements.datapath.DPIDandPort;
import net.onrc.openvirtex.elements.datapath.DPIDandPortPair;
import net.onrc.openvirtex.elements.datapath.Switch;
import net.onrc.openvirtex.elements.link.Link;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.Port;
import net.onrc.openvirtex.exceptions.DuplicateIndexException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.routing.SwitchRoute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

public class DBManager {
	public static final String DB_CONFIG = "CONFIG";
	public static final String DB_USER = "USER";
	public static final String DB_VNET = "VNET";

	private static DBManager instance;
	private DBConnection dbConnection;
	private Map<String, DBCollection> collections;
	private boolean clear;
	// Mapping between physical dpids and a list of vnet managers
	private Map<Long, List<OVXNetworkManager>> dpidToMngr; 
	// Mapping between physical links and a list of vnet managers
	private Map<DPIDandPortPair, List<OVXNetworkManager>> linkToMngr;
	// Mapping between physical ports and a list of vnet managers
	private Map<DPIDandPort, List<OVXNetworkManager>> portToMngr; 

	private static Logger log = LogManager.getLogger(DBManager.class
			.getName());

	private DBManager() {
		this.dbConnection = new MongoConnection();
		this.collections = new HashMap<String, DBCollection>();
		this.dpidToMngr = new HashMap<Long, List<OVXNetworkManager>>();
		this.linkToMngr = new HashMap<DPIDandPortPair, List<OVXNetworkManager>>();
		this.portToMngr = new HashMap<DPIDandPort, List<OVXNetworkManager>>();
	}

	public static DBManager getInstance() {
		if (DBManager.instance == null)
			DBManager.instance = new DBManager();
		return DBManager.instance;
	}

	/**
	 * Creates config, user and vnet collections
	 */
	public void init(String host, Integer port, boolean clear) {
		this.dbConnection.connect(host, port);
		// Suppress error stream when MongoDB raises java.net.ConnectException in another component (and cannot be caught)
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			// Retrieve (create if non-existing) collections from db
			// and store their handlers
			DB db = ((MongoConnection) this.dbConnection).getDB();

			DBCollection cfg = db.getCollection(DBManager.DB_CONFIG);
			this.collections.put(DBManager.DB_CONFIG, cfg);
			DBCollection user = db.getCollection(DBManager.DB_USER);
			this.collections.put(DBManager.DB_USER, user);
			DBCollection vnet = db.getCollection(DBManager.DB_VNET);
			this.collections.put(DBManager.DB_VNET, vnet);

			this.setIndex(DBManager.DB_VNET);

			this.clear = clear;
			if (this.clear)
				this.clear(DBManager.DB_VNET);
			else
				this.readOVXNetworks();

		} catch (Exception e) {
			log.error("Failed to initialize database: {}", e.getMessage());
		} finally {
			// Restore error stream
			System.setErr(ps);
		}
	}

	private void setIndex(String coll) {
		// Suppress error stream when MongoDB raises java.net.ConnectException in another component (and cannot be caught)
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			BasicDBObject options = new BasicDBObject("unique", true);
			BasicDBObject index = new BasicDBObject(TenantHandler.TENANT, 1);
			this.collections.get(coll).ensureIndex(index, options);
		} catch (Exception e) {
			log.error("Failed to set database index: {}", e.getMessage());
		} finally {
			// Restore error stream
			System.setErr(ps);
		}
	}

	private void clear(String coll) {
		// Suppress error stream when MongoDB raises java.net.ConnectException in another component (and cannot be caught)
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			this.collections.get(coll).drop();
			this.setIndex(DBManager.DB_VNET);
		} catch (Exception e) {
			log.error("Failed to clear database: {}", e.getMessage());
		} finally {
			// Restore error stream
			System.setErr(ps);
		}
	}

	public void close() {
		// Suppress error stream when MongoDB raises java.net.ConnectException in another component (and cannot be caught)
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			this.dbConnection.disconnect();
		} catch (Exception e) {
			log.error("Failed to close database connection: {}", e.getMessage());
		} finally {
			// Restore error stream
			System.setErr(ps);			
		}
	}

	/**
	 * Create document in db from persistable object obj
	 */
	public void createDoc(Persistable obj) {
		// Suppress error stream when MongoDB raises java.net.ConnectException in another component (and cannot be caught)
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			DBCollection collection = this.collections.get(obj.getDBName());
			collection.insert(new BasicDBObject(obj.getDBObject()));
		} catch (Exception e) {
			// Do not log when duplicate key
			// Virtual network was already stored and we're trying to create it again on startup
			if (e instanceof MongoException.DuplicateKey)
				log.warn("Skipped saving of virtual network with duplicate tenant id");
			else
				log.error("Failed to insert document into database: {}", e.getMessage());
		} finally {
			// Restore error stream
			System.setErr(ps);
		}
	}

	/**
	 * Remove document from db
	 */
	public void removeDoc(Persistable obj) {
		// Suppress error stream when MongoDB raises java.net.ConnectException in another component (and cannot be caught)
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			DBCollection collection = this.collections.get(obj.getDBName());
			collection.remove(new BasicDBObject(obj.getDBObject()));
		} catch (Exception e) {
			log.error("Failed to remove document from database: {}", e.getMessage());
		} finally {
			// Restore error stream
			System.setErr(ps);
		}		
	}

	/**
	 * Save persistable object obj
	 * @param obj
	 * @param coll
	 */
	public void save(Persistable obj) {
		BasicDBObject query = new BasicDBObject();
		query.putAll(obj.getDBIndex());
		BasicDBObject update = new BasicDBObject("$addToSet", new BasicDBObject(obj.getDBKey(), obj.getDBObject()));
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			DBCollection collection = this.collections.get(obj.getDBName());
			collection.update(query, update, true, false);
		} catch (Exception e) {
			//log.error("Failed to update database: {}", e.getMessage());
		} finally {
			System.setErr(ps);
		}
	}

	/**
	 * Remove persistable object obj
	 * @param obj
	 * @param coll
	 */
	public void remove(Persistable obj) {
		BasicDBObject query = new BasicDBObject();
		query.putAll(obj.getDBIndex());
		BasicDBObject update = new BasicDBObject("$pull", new BasicDBObject(obj.getDBKey(), obj.getDBObject()));
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			DBCollection collection = this.collections.get(obj.getDBName());		
			collection.update(query, update);
		} catch (Exception e) {
			log.error("Failed to remove from db: {}", e.getMessage());
		} finally {
			System.setErr(ps);
		}
	}

	/**
	 * Remove all routes of switch for specified tenant
	 */
	public void removeSwitchPath(int tenantId, long switchId) {
		BasicDBObject query = new BasicDBObject();
		query.put(TenantHandler.TENANT, tenantId);
		BasicDBObject pull = new BasicDBObject("$pull", new BasicDBObject(SwitchRoute.DB_KEY, new BasicDBObject(TenantHandler.DPID, switchId)));
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			DBCollection collection = this.collections.get(DB_VNET);
			collection.update(query, pull);
		} catch (Exception e) {
			log.error("Failed to remove from db: {}", e.getMessage());
		} finally {
			System.setErr(ps);
		}
	}

	/**
	 * Remove stored path of vlink for specified tenant
	 */
	public void removeLinkPath(int tenantId, int linkId) {
		BasicDBObject query = new BasicDBObject();
		query.put(TenantHandler.TENANT, tenantId);
		BasicDBObject pull = new BasicDBObject("$pull", new BasicDBObject(OVXLink.DB_KEY, new BasicDBObject(TenantHandler.LINK, linkId)));
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			DBCollection collection = this.collections.get(DB_VNET);
			collection.update(query, pull);
		} catch (Exception e) {
			log.error("Failed to remove from db: {}", e.getMessage());
		} finally {
			System.setErr(ps);
		}		
	}

	/**
	 * Read all virtual networks from database and spawn an OVXNetworkManager for each.
	 */
	@SuppressWarnings("unchecked")
	private void readOVXNetworks() {
		PrintStream ps = System.err;
		System.setErr(null);
		try {
			// Get a cursor over all virtual networks
			DBCollection coll = this.collections.get(DBManager.DB_VNET);
			DBCursor cursor = coll.find();
			log.info("Loading {} virtual networks from database", cursor.size());
			while (cursor.hasNext()) {
				OVXNetworkManager mngr = null;
				Map<String, Object> vnet = cursor.next().toMap();
				try {
					// Create vnet manager for each virtual network
					mngr = new OVXNetworkManager(vnet);
					OVXNetwork.reserveTenantId(mngr.getTenantId());
					// Accessing DB_KEY field through a class derived from the abstract OVXSwitch
					List<Map<String, Object>> switches = (List<Map<String, Object>>) vnet.get(Switch.DB_KEY);
					List<Map<String, Object>> links = (List<Map<String, Object>>) vnet.get(Link.DB_KEY);
					List<Map<String, Object>> ports = (List<Map<String, Object>>) vnet.get(Port.DB_KEY);
					List<Map<String, Object>> routes = (List<Map<String, Object>>) vnet.get(SwitchRoute.DB_KEY);
					this.readOVXSwitches(switches, mngr);
					this.readOVXLinks(links, mngr);
					this.readOVXPorts(ports, mngr);
					this.readOVXRoutes(routes, mngr);
					DBManager.log.info("Virtual network {} waiting for {} switches, {} links and {} ports", mngr.getTenantId(), mngr.getSwitchCount(), mngr.getLinkCount(), mngr.getPortCount());
				} catch (IndexOutOfBoundException | DuplicateIndexException e) {
					DBManager.log.error("Failed to load virtual network {}: {}", mngr.getTenantId(), e.getMessage());					
				}
			}
		} catch (Exception e) {
			log.error("Failed to load virtual networks from db: {}", e.getMessage());
		} finally {
			System.setErr(ps);
		}
	}

	/**
	 * Read OVX switches from a list of maps in db format and register them in their manager.
	 * @param switches
	 * @param mngr
	 */
	@SuppressWarnings("unchecked")
	private void readOVXSwitches(List<Map<String, Object>> switches, OVXNetworkManager mngr) {
		if (switches == null)
			return;
		// Read explicit switch mappings (virtual to physical)
		for (Map<String, Object> sw: switches) {
			List<Long> physwitches = (List<Long>) sw.get(TenantHandler.DPIDS);
			for (Long physwitch: physwitches) {
				mngr.registerSwitch(physwitch);
				List<OVXNetworkManager> mngrs = this.dpidToMngr.get(physwitch);
				if (mngrs == null)
					this.dpidToMngr.put(physwitch, new ArrayList<OVXNetworkManager>());
				this.dpidToMngr.get(physwitch).add(mngr);
			}
		}
	}

	/**
	 * Read OVX links from a list of maps in db format and register them in their manager.
	 * Also read switches that form a virtual link and register them.
	 * @param links
	 * @param mngr
	 */
	@SuppressWarnings("unchecked")
	private void readOVXLinks(List<Map<String, Object>> links, OVXNetworkManager mngr) {
		if (links == null)
			return;
		// Register links in the appropriate manager
		for (Map<String, Object> link: links) {
			List<Map<String, Object>> path = (List<Map<String, Object>>) link.get(TenantHandler.PATH);
			for (Map<String, Object> hop: path) {
				// Fetch link
				Long srcDpid = (Long) hop.get(TenantHandler.SRC_DPID);
				Integer srcPort = (Integer) hop.get(TenantHandler.SRC_PORT);
				Long dstDpid = (Long) hop.get(TenantHandler.DST_DPID);
				Integer dstPort = (Integer) hop.get(TenantHandler.DST_PORT);
				DPIDandPortPair dpp = new DPIDandPortPair(new DPIDandPort(srcDpid, srcPort),
						new DPIDandPort(dstDpid, dstPort));
				// Register link in current manager
				mngr.registerLink(dpp);
				// Update list of managers that wait for this link
				List<OVXNetworkManager> mngrs = this.linkToMngr.get(dpp);
				if (mngrs == null)
					this.linkToMngr.put(dpp, new ArrayList<OVXNetworkManager>());
				this.linkToMngr.get(dpp).add(mngr);

				// Register src/dst switches of this link
				mngr.registerSwitch(srcDpid);
				mngr.registerSwitch(dstDpid);
				// Update list of managers that wait for these switches
				mngrs = this.dpidToMngr.get(srcDpid);
				if (mngrs == null)
					this.dpidToMngr.put(srcDpid, new ArrayList<OVXNetworkManager>());
				this.dpidToMngr.get(srcDpid).add(mngr);
				mngrs = this.dpidToMngr.get(dstDpid);
				if (mngrs == null)
					this.dpidToMngr.put(dstDpid, new ArrayList<OVXNetworkManager>());
				this.dpidToMngr.get(dstDpid).add(mngr);
			}
		}
	}

	/**
	 * Read OVX links from a list of maps in db format and register them in their manager.
	 * Also read switches that form a virtual link and register them.
	 * @param links
	 * @param mngr
	 */
	private void readOVXPorts(List<Map<String, Object>> ports, OVXNetworkManager mngr) {
		if (ports == null)
			return;
		for (Map<String, Object> port: ports) {
			// Read dpid and port number
			Long dpid = (Long) port.get(TenantHandler.DPID);
			Integer portNumber = (Integer) port.get(TenantHandler.PORT);
			DPIDandPort p = new DPIDandPort(dpid, portNumber);
			// Register port in current manager
			mngr.registerPort(p);
			// Update list of managers that wait for this port
			List<OVXNetworkManager> mngrs = this.portToMngr.get(p);
			if (mngrs == null)
				this.portToMngr.put(p, new ArrayList<OVXNetworkManager>());
			this.portToMngr.get(p).add(mngr);
		}
	}	

	/**
	 * Read OVX routes from a list of maps in db format and register the switches and links in their manager.
	 * @param links
	 * @param mngr
	 */
	@SuppressWarnings({ "unchecked" })
	private void readOVXRoutes(List<Map<String, Object>> routes, OVXNetworkManager mngr) {
		if (routes == null)
			return;
		for (Map<String, Object> route: routes) {
			List<Map<String, Object>> path = (List<Map<String, Object>>) route.get(TenantHandler.PATH);
			for (Map<String, Object> hop: path) {
				Long srcDpid = (Long) hop.get(TenantHandler.SRC_DPID);
				Integer srcPort = (Integer) hop.get(TenantHandler.SRC_PORT);
				Long dstDpid = (Long) hop.get(TenantHandler.DST_DPID);
				Integer dstPort = (Integer) hop.get(TenantHandler.DST_PORT);
				DPIDandPortPair dpp = new DPIDandPortPair(new DPIDandPort(srcDpid, srcPort),
						new DPIDandPort(dstDpid, dstPort));
				// Register links in the appropriate manager
				mngr.registerLink(dpp);
				List<OVXNetworkManager> mngrs = this.linkToMngr.get(dpp);
				if (mngrs == null)
					this.linkToMngr.put(dpp, new ArrayList<OVXNetworkManager>());
				this.linkToMngr.get(dpp).add(mngr);

				// Register switches
				mngr.registerSwitch(srcDpid);
				mngr.registerSwitch(dstDpid);
				mngrs = this.dpidToMngr.get(srcDpid);
				if (mngrs == null)
					this.dpidToMngr.put(srcDpid, new ArrayList<OVXNetworkManager>());
				this.dpidToMngr.get(srcDpid).add(mngr);
				mngrs = this.dpidToMngr.get(dstDpid);
				if (mngrs == null)
					this.dpidToMngr.put(dstDpid, new ArrayList<OVXNetworkManager>());
				this.dpidToMngr.get(dstDpid).add(mngr);
			}
		}
	}

	/**
	 * Add physical switch to the OVXNetworkManagers that are waiting for this switch.
	 * Remove OVXNetworkManagers that were booted after adding this switch.  
	 * This method is called by the PhysicalSwitch.boot() method 
	 */
	public void addSwitch(final Long dpid) {
		// Disregard physical switch creation if OVX was started with --dbClear
		if (!this.clear) {
			List<OVXNetworkManager> completedMngrs = new ArrayList<OVXNetworkManager>();
			synchronized(this.dpidToMngr) {
				// Lookup virtual networks that use this physical switch
				List<OVXNetworkManager> mngrs = this.dpidToMngr.get(dpid);
				if (mngrs != null) {
					for (OVXNetworkManager mngr: mngrs) {
						mngr.setSwitch(dpid);
						if (mngr.getStatus())
							completedMngrs.add(mngr);
					}
				}
			}
			this.removeOVXNetworkManagers(completedMngrs);
		}
	}

	/**
	 * Delete physical switch from the OVXNetworkManagers that are waiting for this switch.  
	 * This method is called by PhysicalNetwork when switch has disconnected. 
	 */
	public void delSwitch(final Long dpid) {
		// Disregard physical switch deletion if OVX was started with --dbClear
		if (!this.clear) {
			synchronized(this.dpidToMngr) {
				// Lookup virtual networks that use this physical switch
				List<OVXNetworkManager> mngrs = this.dpidToMngr.get(dpid);
				if (mngrs != null) {
					for (OVXNetworkManager mngr: mngrs)
						mngr.unsetSwitch(dpid);
				}
			}
		}
	}

	/**
	 * Add physical link to the OVXNetworkManagers that are waiting for this link.  
	 * Remove OVXNetworkManagers that were booted after adding this link.  
	 */
	public void addLink(final DPIDandPortPair dpp) {
		// Disregard physical link creation if OVX was started with --dbClear
		if (!this.clear) {
			List<OVXNetworkManager> completedMngrs = new ArrayList<OVXNetworkManager>();
			synchronized(this.linkToMngr) {
				// Lookup virtual networks that use this physical link
				List<OVXNetworkManager> mngrs = this.linkToMngr.get(dpp);
				if (mngrs != null) {
					for (OVXNetworkManager mngr: mngrs) {
						mngr.setLink(dpp);
						if (mngr.getStatus())
							completedMngrs.add(mngr);
					}
				}
			}
			this.removeOVXNetworkManagers(completedMngrs);
		}
	}

	/**
	 * Delete physical link from the OVXNetworkManagers that are waiting for this switch.  
	 */
	public void delLink(final DPIDandPortPair dpp) {
		// Disregard physical link deletion if OVX was started with --dbClear
		if (!this.clear) {
			synchronized(this.linkToMngr) {
				// Lookup virtual networks that use this physical link
				List<OVXNetworkManager> mngrs = this.linkToMngr.get(dpp);
				if (mngrs != null) {
					for (OVXNetworkManager mngr: mngrs)
						mngr.unsetLink(dpp);
				}
			}
		}
	}


	/**
	 * Add physical port to the OVXNetworkManagers that are waiting for this port.
	 * Remove OVXNetworkManagers that were booted after adding this port.  
	 */
	public void addPort(final DPIDandPort port) {
		// Disregard physical port creation if OVX was started with --dbClear
		if (!this.clear) {
			List<OVXNetworkManager> completedMngrs = new ArrayList<OVXNetworkManager>();
			synchronized(this.portToMngr) {
				// Lookup virtual networks that use this physical port
				List<OVXNetworkManager> mngrs = this.portToMngr.get(port);
				if (mngrs != null) {
					for (OVXNetworkManager mngr: mngrs) {
						mngr.setPort(port);
						if (mngr.getStatus())
							completedMngrs.add(mngr);
					}
				}
			}
			this.removeOVXNetworkManagers(completedMngrs);
		}
	}

	/**
	 * Delete physical port from the OVXNetworkManagers that are waiting for this switch.  
	 */
	public void delPort(final DPIDandPort port) {
		// Disregard physical link deletion if OVX was started with --dbClear
		if (!this.clear) {
			synchronized(this.dpidToMngr) {
				// Lookup virtual networks that use this physical link
				List<OVXNetworkManager> mngrs = this.portToMngr.get(port);
				if (mngrs != null) {
					for (OVXNetworkManager mngr: mngrs)
						mngr.unsetPort(port);
				}
			}
		}
	}

	/**
	 * Remove network managers that were waiting for switches, links or ports.
	 * @param mngrs
	 */
	private void removeOVXNetworkManagers(List<OVXNetworkManager> mngrs) {
		for (OVXNetworkManager mngr: mngrs) {
			synchronized(this.dpidToMngr) {
				for (Long dpid: this.dpidToMngr.keySet())
					this.dpidToMngr.get(dpid).remove(mngr);
			}
			synchronized(this.linkToMngr) {
				for (DPIDandPortPair dpp: this.linkToMngr.keySet())
					this.linkToMngr.get(dpp).remove(mngr);
			}
			synchronized(this.portToMngr) {
				for (DPIDandPort dp: this.portToMngr.keySet())
					this.portToMngr.get(dp).remove(mngr);
			}
		}
	}
}
