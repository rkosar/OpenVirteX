/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFVersion;

import junit.framework.Assert;
import junit.framework.TestSuite;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.cmd.CmdLineSettings;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.elements.port.PhysicalPort;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class PassingAPITest extends AbstractAPICalls {
	static final OFVersion ofversion = OFVersion.OF_10;
	static final int MAX_TENANTS = 4;
	static final OpenVirteXController ctrl = 
			new OpenVirteXController(new CmdLineSettings());

	/**
	 * @return the suite of tests being tested
	 */
	public static TestSuite suite() {
		return new TestSuite(PassingAPITest.class);
	}

	/**
	 * Test whether a create network call succeeds
	 */
	@SuppressWarnings("unchecked")
	public void testCreateNetPass() {

		final JSONRPC2Response resp = super.createNetwork();

		Assert.assertNull("CreateOVXNetwork should not return null",
				resp.getError());

		Assert.assertTrue("CreateOVXNetwork has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals(1, result.get(TenantHandler.TENANT));
	}

	/**
	 * Test whether the creation of a host actually succeeds.
	 */
	@SuppressWarnings("unchecked")
	public void testCreateSingleSwitch() {
		final PhysicalSwitch sw = new PhysicalSwitch(1);
		PhysicalNetwork.getInstance().addSwitch(sw);

		super.createNetwork();
		final JSONRPC2Response resp = super.createSwitch(1,
				Collections.singletonList(1));

		Assert.assertNull("CreateOVXSwitch should not return null",
				resp.getError());

		Assert.assertTrue("CreateOVXSwitch has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals((long) 46200400562356225L, result.get(TenantHandler.VDPID));
	}

	/**
	 * Test whether the creation of a host actually succeeds.
	 */
	@SuppressWarnings("unchecked")
	public void testCreateBigSwitch() {
		final PhysicalSwitch sw1 = new PhysicalSwitch(1);
		final PhysicalSwitch sw2 = new PhysicalSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();		
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw2, false);
		PhysicalNetwork.getInstance().createLink(p1, p2);
		PhysicalNetwork.getInstance().createLink(p2, p1);
		super.createNetwork();
		final List<Integer> l = new LinkedList<>();
		l.add(1);
		l.add(2);
		final JSONRPC2Response resp = super.createSwitch(1, l);

		Assert.assertNull("CreateOVXSwitch should not return null",
				resp.getError());

		Assert.assertTrue("CreateOVXSwitch has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals((long) 46200400562356225L, result.get(TenantHandler.VDPID));
	}

	@SuppressWarnings("unchecked")
	public void testCreatePort() {
		final TestSwitch sw = new TestSwitch(1);
		PhysicalNetwork.getInstance().addSwitch(sw);
		final OFPortDesc portdesc = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final PhysicalPort port = new PhysicalPort(portdesc, sw, true);
		port.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		port.setPortNumber(1);
		sw.addPort(port);

		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));

		final JSONRPC2Response resp = super.createPort(1, (long) 1, 1);

		Assert.assertNull("CreateOVXPort should not return null",
				resp.getError());

		Assert.assertTrue("CreateOVXPort has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals((short) 1, result.get(TenantHandler.VPORT));
	}

	@SuppressWarnings("unchecked")
	public void testConnectHost() {
		final TestSwitch sw1 = new TestSwitch(1);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final PhysicalPort port = new PhysicalPort(portdesc1, sw1, true);
		port.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		port.setPortNumber(1);
		sw1.addPort(port);

		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createPort(1, (long) 1, 1);
		final JSONRPC2Response resp = super.connectHost(1, (long) 46200400562356225L, (short) 1,
				"00:00:00:00:00:01");

		Assert.assertNull(
				resp.getError() == null ? "ConnectHost should not return null"
						: resp.getError().getMessage(), resp.getError());

		Assert.assertTrue("ConnectHost has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals(1, result.get(TenantHandler.HOST));
	}

	@SuppressWarnings("unchecked")
	public void testConnectLink() {
		super.createNetwork();
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber(1);
		sw1.addPort(p1);
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw2, false);
		p2.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p2.setPortNumber((short)1);
		sw2.addPort(p2);
		PhysicalNetwork.getInstance().createLink(p1, p2);
		PhysicalNetwork.getInstance().createLink(p2, p1);

		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);

		JSONRPC2Response resp = super.connectLink(1, (long) 46200400562356225L, (short)1, (long) 46200400562356226L, 
				(short)1, "manual" , (byte)0);

		Assert.assertNull(
				resp.getError() == null ? "ConnectLink should not return null"
						: resp.getError().getMessage(), resp.getError());

		Assert.assertTrue("ConnectLink has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals(1, result.get(TenantHandler.LINK));

		// TODO: should this not be its own separate test?

		resp = super.setLinkPath(1, 1, "1/1-2/1" , (byte)100);

		Assert.assertNull(
				resp.getError() == null ? "SetLinkPath should not return null"
						: resp.getError().getMessage(), resp.getError());

		Assert.assertTrue("SetLinkPath has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		result = (Map<String, Object>) resp.getResult();

		// TODO: we should check if the path really has been set
		// can't do this now as map is not working properly in the tests

		Assert.assertEquals(1, result.get(TenantHandler.LINK));
	}

	/**
	 * Test whether a create switch route call succeeds
	 */
	@SuppressWarnings("unchecked")
	public void testConnectRoutePass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();

		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		final List<Integer> l = new LinkedList<>();
		l.add(1);
		l.add(2);
		super.createSwitch(1, l);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)1, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:02");

		final JSONRPC2Response resp = super.connectRoute(1, 46200400562356225L, (short)1, (short)2, "1/1-2/1", (byte) 100);

		Assert.assertNull("ConnectOVXRoute should not return null",
				resp.getError());

		Assert.assertTrue("ConnectOVXRoute has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals(1, result.get(TenantHandler.ROUTE));
	}

	/**
	 * Test whether a remove network call succeeds
	 */
	public void testRemoveNetPass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();
	
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356226L, (short)2, "00:00:00:00:00:02");
		super.connectLink(1, (long)46200400562356225L, (short) 1, (long)46200400562356226L, (short) 1, "manual", (byte) 0);
		super.setLinkPath(1, 1, "1/1-2/1", (byte) 100);
		final JSONRPC2Response resp = super.removeNetwork(1);

		Assert.assertNull(resp.getResult());
	}

	/**
	 * Test whether a remove switch call succeeds
	 */
	public void testRemoveSwitchPass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();

		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356226L, (short)2, "00:00:00:00:00:02");
		super.connectLink(1, (long)46200400562356225L, (short) 1, (long)46200400562356226L, (short) 1, "manual", (byte) 0);
		super.setLinkPath(1, 1, "1/1-2/1", (byte) 100);
		final JSONRPC2Response resp = super.removeSwitch(1, 46200400562356225L);

		Assert.assertNull(resp.getError());
	}

	/**
	 * Test whether a remove port call succeeds
	 */
	public void testRemovePortPass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();
	
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356226L, (short)2, "00:00:00:00:00:02");
		super.connectLink(1, (long)46200400562356225L, (short) 1, (long)46200400562356226L, (short) 1, "manual", (byte) 0);
		super.setLinkPath(1, 1, "1/1-2/1", (byte) 100);
		final JSONRPC2Response resp = super.removePort(1, 46200400562356225L, (short)1);

		Assert.assertNull(resp.getError());
	}

	/**
	 * Test whether a remove link call succeeds
	 */
	public void testDisconnectLinkPass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();
	
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356226L, (short)2, "00:00:00:00:00:02");
		super.connectLink(1, (long)46200400562356225L, (short) 1, (long)46200400562356226L, (short) 1, "manual", (byte) 0);
		super.setLinkPath(1, 1, "1/1-2/1", (byte) 100);

		final JSONRPC2Response resp = super.disconnectLink(1, 1);

		Assert.assertNull(resp.getError());
	}

	/**
	 * Test whether a disconnect host call succeeds
	 */
	public void testDisconnectHostPass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();	
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356226L, (short)2, "00:00:00:00:00:02");
		super.connectLink(1, (long)46200400562356225L, (short) 1, (long)46200400562356226L, (short) 1, "manual", (byte) 0);
		super.setLinkPath(1, 1, "1/1-2/1", (byte) 100);

		final JSONRPC2Response resp = super.disconnectHost(1, 1);

		Assert.assertNull("Disconnect Host should have not returned null",
				resp.getError());

		Assert.assertNull(resp.getResult());
	}

	/**
	 * Test whether a disconnect host call succeeds
	 */
	public void testDisconnectRoutePass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		
		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		final List<Integer> l = new LinkedList<>();
		l.add(1);
		l.add(2);
		super.createSwitch(1, l);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)1, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:02");
		super.connectRoute(1, 46200400562356225L, (short)1, (short)2, "1/1-2/1", (byte) 100);

		final JSONRPC2Response resp = super.disconnectRoute(1, (long)46200400562356225L, 1);

		Assert.assertNull("Remove switch route should not return null",
				resp.getError());

		Assert.assertNull(resp.getResult());
	}

	/**
	 * Test whether a disconnect host call succeeds
	 */
	@SuppressWarnings("unchecked")
	public void testStopNetPass() {
		// set the physical network (linear 2 sws with 1 host x sw)
		final TestSwitch sw1 = new TestSwitch(1);
		final TestSwitch sw2 = new TestSwitch(2);
		PhysicalNetwork.getInstance().addSwitch(sw1);
		PhysicalNetwork.getInstance().addSwitch(sw2);
		final OFPortDesc portdesc1 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc2 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc3 = OFFactories.getFactory(ofversion).buildPortDesc().build();
		final OFPortDesc portdesc4 = OFFactories.getFactory(ofversion).buildPortDesc().build();

		final PhysicalPort p1 = new PhysicalPort(portdesc1, sw1, false);
		p1.setHardwareAddress(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
		p1.setPortNumber((short)1);
		sw1.addPort(p1);
		final PhysicalPort p2 = new PhysicalPort(portdesc2, sw1, true);
		p2.setHardwareAddress(new byte[] { 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c });
		p2.setPortNumber((short)2);
		sw1.addPort(p2);
		final PhysicalPort p3 = new PhysicalPort(portdesc3, sw2, false);
		p3.setHardwareAddress(new byte[] { 0x11, 0x12, 0x13, 0x14, 0x15, 0x16 });
		p3.setPortNumber((short)1);
		sw2.addPort(p3);
		final PhysicalPort p4 = new PhysicalPort(portdesc4, sw2, true);
		p4.setHardwareAddress(new byte[] { 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c });
		p4.setPortNumber((short)2);
		sw2.addPort(p4);
		PhysicalNetwork.getInstance().createLink(p1, p3);
		PhysicalNetwork.getInstance().createLink(p3, p1);

		//set the virtual network (copy of the phy network)
		super.createNetwork();
		super.createSwitch(1, Collections.singletonList(1));
		super.createSwitch(1, Collections.singletonList(2));
		super.createPort(1, (long) 1, 1);
		super.createPort(1, (long) 2, 1);
		super.createPort(1, (long) 1, 2);
		super.createPort(1, (long) 2, 2);
		super.connectHost(1, (long)46200400562356225L, (short)2, "00:00:00:00:00:01");
		super.connectHost(1, (long)46200400562356226L, (short)2, "00:00:00:00:00:02");
		super.connectLink(1, (long)46200400562356225L, (short) 1, (long)46200400562356226L, (short) 1, "manual", (byte) 0);
		super.setLinkPath(1, 1, "1/1-2/1", (byte) 100);
		final JSONRPC2Response resp = super.stopNetwork(1);

		Assert.assertNull("Stop network should not return null", resp.getError());

		Assert.assertTrue("Stop network has incorrect return type",
				resp.getResult() instanceof Map<?, ?>);

		Map<String, Object> result = (Map<String, Object>) resp.getResult();

		Assert.assertEquals(1, result.get(TenantHandler.TENANT));

		Assert.assertEquals(false, result.get(TenantHandler.IS_BOOTED));
	}

	@Override
	protected void tearDown() throws Exception {
		OVXMap.reset();
		PhysicalNetwork.reset();
		OVXNetwork.reset();
	}

}
