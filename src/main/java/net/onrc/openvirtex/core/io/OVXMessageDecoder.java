/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.core.io;


import net.onrc.openvirtex.messages.OVXMessageFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;

/**
 * Decode an openflow message from a netty Channel.
 * 
 * @author alshabib
 */
public class OVXMessageDecoder extends FrameDecoder {
	
	OVXMessageFactory factory = OVXMessageFactory.getInstance();

	@Override
	protected Object decode(final ChannelHandlerContext ctx,
			final Channel channel, final ChannelBuffer buffer) throws Exception {
		if (!channel.isConnected()) {
			// if the channel is closed, there will be nothing to read.
			return null;
		}
		
		OFMessageReader<OFMessage> reader = OFFactories.getGenericReader();
	
		OFMessage message = reader.readFrom(buffer);
		//final List<OFMessage> message = this.factory.parseMessage(buffer);
		
		return message;
	}
}
