/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package net.onrc.openvirtex.core.io;

import java.util.concurrent.ThreadPoolExecutor;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;


public class ClientChannelPipeline extends OpenflowChannelPipeline {

	private ClientBootstrap bootstrap = null;
	private OVXSwitch sw = null;
	private final ChannelGroup cg;

	public ClientChannelPipeline(
			final OpenVirteXController openVirteXController,
			final ChannelGroup cg, final ThreadPoolExecutor pipelineExecutor,
			final ClientBootstrap bootstrap, final OVXSwitch sw) {
		super();
		this.ctrl = openVirteXController;
		this.pipelineExecutor = pipelineExecutor;
		this.timer = PhysicalNetwork.getTimer();
		this.idleHandler = new IdleStateHandler(this.timer, 20, 25, 0);
		this.readTimeoutHandler = new ReadTimeoutHandler(this.timer, 30);
		this.bootstrap = bootstrap;
		this.sw = sw;
		this.cg = cg;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		final ControllerChannelHandler handler = new ControllerChannelHandler(
				this.ctrl, this.sw);

		final ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("reconnect", new ReconnectHandler(this.sw,
				this.bootstrap, this.timer, 15, this.cg));
		pipeline.addLast("ofmessagedecoder", new OVXMessageDecoder());
		pipeline.addLast("ofmessageencoder", new OVXMessageEncoder());
		pipeline.addLast("idle", this.idleHandler);
		pipeline.addLast("timeout", this.readTimeoutHandler);
		pipeline.addLast("handshaketimeout", new HandshakeTimeoutHandler(
				handler, this.timer, 15));
		
		pipeline.addLast("pipelineExecutor", new ExecutionHandler(
				this.pipelineExecutor));
		pipeline.addLast("handler", handler);
		return pipeline;
	}

}
