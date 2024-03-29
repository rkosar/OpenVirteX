package net.onrc.openvirtex.elements.datapath.statistics;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.messages.OVXStatisticsRequest;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatisticsRequest;
import net.onrc.openvirtex.messages.statistics.OVXPortStatisticsRequest;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.OFPort;

public class StatisticsManager implements TimerTask, OVXSendMsg {

	private HashedWheelTimer timer = null;
	private PhysicalSwitch sw;
	
	Logger log = LogManager.getLogger(StatisticsManager.class.getName());
	
	private Integer refreshInterval = 30;
	private boolean stopTimer = false;

	public StatisticsManager(PhysicalSwitch sw) {
		/*
		 * Get the timer from the PhysicalNetwork
		 * class. 
		 */
		this.timer = PhysicalNetwork.getTimer();
		this.sw = sw;
		this.refreshInterval = OpenVirteXController.getInstance().getStatsRefresh();
	}

	@Override
	public void run(Timeout timeout) throws Exception {
		log.debug("Collecting stats for {}", this.sw.getSwitchName());
		sendPortStatistics();
		sendFlowStatistics(0, 0);
		
		if (!this.stopTimer) {
			log.debug("Scheduling stats collection in {} seconds for {}", 
					this.refreshInterval, this.sw.getSwitchName());
			timeout.getTimer().newTimeout(this, refreshInterval, TimeUnit.SECONDS);	
		}
	}

	private void sendFlowStatistics(int tid, int port) {
		OFVersion ofversion = this.sw.getFeaturesReply().getVersion();
		OVXStatisticsRequest req = new OVXStatisticsRequest(OFStatsType.FLOW, ofversion);
		// TODO: stuff like below should be wrapped into an XIDUtil class
		int xid = (tid << 16) | port; 
		req.setXid(xid);
		
		OVXFlowStatisticsRequest freq = new OVXFlowStatisticsRequest(ofversion);
		OVXMatch match = new OVXMatch(ofversion);
		//match.setWildcards(OFFlowWildcards.ALL);
		
		//OFPort.ANY ?
		freq.setMatch(match)
			.setOutPort(OFPort.ZERO)
			.setTableId((short)0x00FF);
			//.setTableId((byte)0xFF);

		req.setStatistics(Collections.singletonList(freq));
		//req.setLengthU(req.getLengthU() + freq.getLength());
		sendMsg(req.getStats(), this);
	}
 
	private void sendPortStatistics() {
		OFVersion ofversion = this.sw.getFeaturesReply().getVersion();
		OVXStatisticsRequest req = new OVXStatisticsRequest(OFStatsType.PORT, ofversion); 
		OVXPortStatisticsRequest preq = new OVXPortStatisticsRequest(ofversion);
		
		preq.setPortNumber(OFPort.ZERO.getPortNumber());
		req.setStatistics(Collections.singletonList(preq));
		//req.setLengthU(req.getLengthU() + preq.getLength());
		sendMsg(req.getStats(), this);
	}
	
	public void start() {	
		/*
		 * Initially start polling quickly.
		 * Then drop down to configured value
		 */
		log.info("Starting Stats collection thread for {}", this.sw.getSwitchName());
		timer.newTimeout(this, 1, TimeUnit.SECONDS);
	}
	
	public void stop() {
		log.info("Stopping Stats collection thread for {}", this.sw.getSwitchName());
		this.stopTimer  = true;
	}

	@Override
	public void sendMsg(OFMessage msg, OVXSendMsg from) {
		sw.sendMsg(msg, from);
	}

	@Override
	public String getName() {
		return "Statistics Manager (" + sw.getName() + ")";
	}

	public void cleanUpTenant(Integer tenantId, Integer port) {
		sendFlowStatistics(tenantId, port);
	}
}
