package com.renker.nio.netty22;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;

public class HeartBeatReqHandler extends ChannelHandlerAdapter {
	private volatile ScheduledFuture<?> heartBeat;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyMessage message = (NettyMessage) msg;
		if(message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP.value()){
			heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
			
//			NettyMessage heatBeat = new NettyMessage();
//			Header header = new Header();
//			header.setType(MessageType.HEARTBEAT_REQ.value());
//			heatBeat.setHeader(header);
//			System.out.println("--> [client]客户端发送心跳请求消息到服务端 : "+heartBeat);
//			ctx.writeAndFlush(heartBeat);
			
			
		}else if(message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_RESP.value()){
			System.out.println("<-- [client]客户端接收服务端心跳响应消息  : "+message);
		}else{
			ctx.fireChannelRead(msg);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if(heartBeat != null){
			heartBeat.cancel(true);
			heartBeat = null;
		}
		
		ctx.fireExceptionCaught(cause);
	}
	
	private class HeartBeatTask implements Runnable{
		private final ChannelHandlerContext ctx;
		public HeartBeatTask(final ChannelHandlerContext context) {
			this.ctx = context;
		}
		
		@Override
		public void run() {
			NettyMessage heatBeat = buildHeadBeat();
			System.out.println("--> [client]客户端发送心跳请求消息到服务端 : "+heartBeat);
			ctx.writeAndFlush(heatBeat);
		}
		
		private NettyMessage buildHeadBeat(){
			NettyMessage message = new NettyMessage();
			Header header = new Header();
			header.setType(MessageType.HEARTBEAT_REQ.value());
			message.setHeader(header);
			return message;
		}
	}
}
