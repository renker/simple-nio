package com.renker.nio.netty1;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class NettyClientHandle extends ChannelHandlerAdapter{
	private final ByteBuf firstMessage;
	
	public NettyClientHandle() throws UnsupportedEncodingException {
		byte[] req = "Hellow".getBytes("UTF-8");
		firstMessage = Unpooled.buffer(req.length);
		firstMessage.writeBytes(req);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(firstMessage);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		
		byte [] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		
		System.out.println("Accept server msg :"+new String(req,"UTF-8"));
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("发生异常....");
		cause.printStackTrace();
		ctx.close();
	}
}
