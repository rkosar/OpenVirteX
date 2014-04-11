/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U64;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.cmd.CmdLineSettings;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.messages.OVXFlowMod;


public class FlowTableTest extends TestCase {
	
	OpenVirteXController ctl = null;
	private OFVersion ofversion = OFVersion.OF_10;

	public OVXFlowMod getFlowMod() {
		OVXFlowMod fm = new OVXFlowMod(ofversion);
		fm.setMatch(OFFactories.getFactory(this.ofversion).buildMatch().build());
		fm.setActions(new ArrayList<OFAction>());
		return fm;
	}
	
    public FlowTableTest(final String name) {
    	super(name);
    }

    public static Test suite() {
    	return new TestSuite(FlowTableTest.class);
    }

    public void testAddFlowMod() {
		final OVXSwitch vsw = new OVXSingleSwitch(1, 1, this.ofversion);
		final OVXFlowTable oft = new OVXFlowTable(vsw);
		final OVXFlowMod fm1 = this.getFlowMod();
	
		final U64 c1 = U64.of(vsw.getTenantId() << 32 | 1);
		final U64 c2 = oft.getCookie();
		final boolean c = oft.handleFlowMods(fm1, c2);
		Assert.assertTrue(c);
		Assert.assertEquals(c2, c1);
    }

    public void testDeleteFlowMod() {
		final OVXSwitch vsw = new OVXSingleSwitch(1, 1, this.ofversion);
		final OVXFlowTable oft = new OVXFlowTable(vsw);
		final OVXFlowMod fm1 = this.getFlowMod();
		
		final U64 c = oft.getCookie();
		oft.handleFlowMods(fm1, c);
		final OVXFlowMod fm2 = oft.deleteFlowMod(c);
	
		Assert.assertEquals(fm1, fm2);
    }

    public void testGenerateCookie() {
		final OVXSwitch vsw = new OVXSingleSwitch(1, 1, this.ofversion);
		final OVXFlowTable oft = new OVXFlowTable(vsw);
	
		final OVXFlowMod fm1 = this.getFlowMod();
		final OVXFlowMod fm2 = this.getFlowMod();
		final OVXFlowMod fm3 = this.getFlowMod();
	
		final U64 c1 = U64.of(vsw.getTenantId() << 32 | 1);
		final U64 c2 = U64.of(vsw.getTenantId() << 32 | 2);
	
		final U64 c3 = oft.getCookie();
		final U64 c4 = oft.getCookie();
		
		// generate new cookies while none in freelist
		oft.handleFlowMods(fm1, c3);
		Assert.assertEquals(c3, c1);
		oft.handleFlowMods(fm2, c4);
		Assert.assertEquals(c4, c2);
	
		// should re-use first cookie that was freed up
		oft.deleteFlowMod(c1);
		U64 c = oft.getCookie();
		oft.addFlowMod(fm3, c);
		Assert.assertEquals(c, c1);
    }

    /** test various Flow Entry match types. */
    public void testFlowEntryCompare() {
		//final Match base_m = new Match();
    	Match m = OFFactories.getFactory(this.ofversion)
    						 .buildMatch()
    						 .setExact(MatchField.ETH_TYPE, EthType.IPv4)
    			  			 .setExact(MatchField.ETH_DST, MacAddress.of(new byte[] { 0x11, 0x22, 0x33, (byte) 0xab, (byte) 0xcd, (byte) 0xef }))
    			  			 .setExact(MatchField.IN_PORT, OFPort.of(23))
    			  			 .setExact(MatchField.TCP_DST, TransportPort.of(5692))
    			  			 .build();
     
 		final OVXFlowMod base_fm = this.getFlowMod();
		base_fm.setBufferId( OFBufferId.of(1))
			   .setMatch(m)
			   .setPriority(20);
		
		final OVXFlowEntry base_fe = new OVXFlowEntry(base_fm, 11);
	
		/* a clone should be identical so be equal */
		//final OFMatch equal_m = base_m.clone();
		final Match equal_m = m.createBuilder().build();
		Assert.assertEquals(base_fe.compare(equal_m, true), OVXFlowEntry.EQUAL);
	
		/* a superset match should make base_m its subset */
		Match super_m = OFFactories.getFactory(this.ofversion)
				 				   .buildMatch()
				 				   .setExact(MatchField.IN_PORT, OFPort.of(23))
				 				   .setExact(MatchField.TCP_DST, TransportPort.of(5692))
				 				   .build();
		
		Assert.assertEquals(base_fe.compare(super_m, true), OVXFlowEntry.SUBSET);
		/* not strict - consider subset match to also be equal */
		Assert.assertEquals(base_fe.compare(super_m, false), OVXFlowEntry.EQUAL);
	
		/* a subset match should make base_m its superset */
    	Match sub_m = OFFactories.getFactory(this.ofversion)
				 				 .buildMatch()
				 				 .setExact(MatchField.ETH_TYPE, EthType.IPv4)
				 				 .setExact(MatchField.ETH_DST, MacAddress.of(new byte[] { 0x11, 0x22, 0x33, (byte) 0xab, (byte) 0xcd, (byte) 0xef }))
				 				 .setExact(MatchField.ETH_SRC, MacAddress.of(new byte[] { 0x11, 0x22, 0x33, (byte) 0xaa, (byte) 0xcc, (byte) 0xee }))
				 				 .setExact(MatchField.IN_PORT, OFPort.of(23))
				 				 .setExact(MatchField.TCP_DST, TransportPort.of(5692))
				 				 .build();
    	
		Assert.assertEquals(base_fe.compare(sub_m, true), OVXFlowEntry.SUPERSET);
	
		/* a incomparable OFMatch should return base_m to be disjoint */
		Match disj_m = OFFactories.getFactory(this.ofversion)
				 				  .buildMatch()
				 				  .setExact(MatchField.IN_PORT, OFPort.of(20))
				 				  .build();
		
		Assert.assertEquals(base_fe.compare(disj_m, true), OVXFlowEntry.DISJOINT);
    }
    
    /* main FlowTable operations */
    public void testHandleFlowMod() {
		final OVXSwitch vsw = new OVXSingleSwitch(1, 1, this.ofversion);
		final PhysicalSwitch psw = new PhysicalSwitch(0);
		ArrayList<PhysicalSwitch> l = new ArrayList<PhysicalSwitch>();
		l.add(psw);
		OVXMap.getInstance().addSwitches(l, vsw);
		final OVXFlowTable oft = new OVXFlowTable(vsw);
		
    	Match base_m = OFFactories.getFactory(this.ofversion)
    			.buildMatch()
    			.setExact(MatchField.ETH_TYPE, EthType.IPv4)
    			.setExact(MatchField.ETH_DST, MacAddress.of(new byte[] { 0x11, 0x22, 0x33, (byte) 0xab, (byte) 0xcd, (byte) 0xef }))
    			.setExact(MatchField.IN_PORT, OFPort.of(23))
    			.setExact(MatchField.TCP_DST, TransportPort.of(5692))
    			.build();
    	

		final OVXFlowMod fm = this.getFlowMod();
		fm.setBufferId(OFBufferId.of(1))
		  .setMatch(base_m)
		  .setPriority(20)      
		  .setCommand(OFFlowModCommand.MODIFY);
	
		/* add done via modify call - should work */
		Assert.assertTrue(oft.handleFlowMods(fm, oft.getCookie()));
	
		/* try strict add with superset match - should fail */
		Match super_m = OFFactories.getFactory(this.ofversion)
				.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(23))
				.setExact(MatchField.TCP_DST, TransportPort.of(5692))
				.build();

		Set<OFFlowModFlags> oflags = new HashSet<OFFlowModFlags>();
		oflags.add(OFFlowModFlags.CHECK_OVERLAP);
		
		fm.setCommand(OFFlowModCommand.ADD)
		  .setFlags(oflags)
		  .setMatch(super_m);
		
		Assert.assertFalse(oft.handleFlowMods(fm, oft.getCookie()));
	
		/* try add with overlap check off should succeed. */
		Set<OFFlowModFlags> flags = new HashSet<OFFlowModFlags>();
		flags.add(OFFlowModFlags.SEND_FLOW_REM);
		fm.setFlags(flags);
		
		Assert.assertTrue(oft.handleFlowMods(fm, oft.getCookie()));
	
		/* do a delete of one element, then a wild-card. */
		fm.setCommand(OFFlowModCommand.DELETE_STRICT);
		Assert.assertTrue(oft.handleFlowMods(fm, oft.getCookie()));
		fm.setCommand(OFFlowModCommand.ADD);
		oft.handleFlowMods(fm, oft.getCookie());
		/* OFPFW_ALL match - need to do sendSouth() */
		/*
		 * fm.setMatch(new OFMatch()).setCommand(OFFlowMod.OFPFC_DELETE);
		 * assertFalse(oft.handleFlowMods(fm));
		 * oft.dump();
		 */
    }

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	ctl = new OpenVirteXController(new CmdLineSettings());
    }

    @Override
    protected void tearDown() throws Exception {
    	super.tearDown();
    }
}
