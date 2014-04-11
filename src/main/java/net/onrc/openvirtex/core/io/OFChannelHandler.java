/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.core.io;

import java.io.IOException;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.datapath.Switch;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;

public abstract class OFChannelHandler extends IdleStateAwareChannelHandler {

	@SuppressWarnings("rawtypes")
	protected Switch sw;
	protected Channel channel;
	protected OpenVirteXController ctrl;
	protected OFVersion ofversion;
	protected OFFactory offactory;

	public abstract boolean isHandShakeComplete();

	protected abstract String getSwitchInfoString();

	protected abstract void sendHandShakeMessage(OFType type)
			throws IOException;

}
