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
import net.onrc.openvirtex.elements.network.PhysicalNetwork;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;

public class SwitchChannelPipeline extends OpenflowChannelPipeline {

	private ExecutionHandler eh = null;
	
	public SwitchChannelPipeline(
			final OpenVirteXController openVirteXController,
			final ThreadPoolExecutor pipelineExecutor) {
		super();
		this.ctrl = openVirteXController;
		this.pipelineExecutor = pipelineExecutor;
		this.timer = PhysicalNetwork.getTimer();
		this.idleHandler = new IdleStateHandler(this.timer, 20, 25, 0);
		this.readTimeoutHandler = new ReadTimeoutHandler(this.timer, 30);
		this.eh = new ExecutionHandler(this.pipelineExecutor);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		final SwitchChannelHandler handler = new SwitchChannelHandler(this.ctrl);

		final ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("ofmessagedecoder", new OVXMessageDecoder());
		pipeline.addLast("ofmessageencoder", new OVXMessageEncoder());
		pipeline.addLast("idle", this.idleHandler);
		pipeline.addLast("timeout", this.readTimeoutHandler);
		pipeline.addLast("handshaketimeout", new HandshakeTimeoutHandler(
				handler, this.timer, 15));
		
		pipeline.addLast("pipelineExecutor", eh);
		pipeline.addLast("handler", handler);
		return pipeline;
	}

}
