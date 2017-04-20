package com.renker.nio.netty22;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatRespHandler extends ChannelHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyMessage message = (NettyMessage) msg;
		
		// 返回心跳应答消息
		if(message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_REQ.value()){
			System.out.println("<-- [server]接收到客户端心跳请求消息 : "+message);
			NettyMessage hearBeat = bulidHeartBeat(); 
			System.out.println("--> [server]发送心跳响应消息到客户端 : "+message);
			ctx.writeAndFlush(hearBeat);
		}else{
			ctx.fireChannelRead(msg);
		}
	}
	
	private NettyMessage bulidHeartBeat(){
		NettyMessage message = new NettyMessage();
		Header header = new Header();
		header.setType(MessageType.HEARTBEAT_RESP.value());
		message.setHeader(header);
		return message;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
}
