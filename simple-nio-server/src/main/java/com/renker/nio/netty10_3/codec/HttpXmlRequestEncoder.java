package com.renker.nio.netty10_3.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HttpXmlRequestEncoder extends SimpleChannelInboundHandler<HttpXmlRequest> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, HttpXmlRequest msg) throws Exception {
		
	}
}
