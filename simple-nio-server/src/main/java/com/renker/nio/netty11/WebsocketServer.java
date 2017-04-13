package com.renker.nio.netty11;

import java.util.Date;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class WebsocketServer {
	public void bind(){
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						
					}
				});
			
		} catch (Exception e) {
		}
	}
	
	public class WebsocketServerHander extends SimpleChannelInboundHandler<Object>{
		
		private WebSocketServerHandshaker handshaker;
		
		@Override
		protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
			if(msg instanceof FullHttpRequest){
				handleHttpRequest(ctx, (FullHttpRequest) msg);
			}else if(msg instanceof WebSocketFrame){
				handleWebsocketFrame(ctx, (WebSocketFrame)msg);
			}
			
			
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}
		
		private void handleHttpRequest(ChannelHandlerContext ctx,FullHttpRequest req){
			// 如果http解码失败，返回http异常
			if(!req.getDecoderResult().isSuccess() || ("websocket".equals(req.headers().get("Upgrade")))){
				sendHttpRespone(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
				return;
			}
			
			// 构造握手响应返回，本机测试
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket", null, false);
			
			if(handshaker == null){
				WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
			}else{
				handshaker.handshake(ctx.channel(), req);
			}
		}
		
		private void handleWebsocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
			// 判断是否是关闭链路的指令
			if(frame instanceof CloseWebSocketFrame){
				handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
				return;
			}
			
			// 判断是滞是Ping消息
			if(frame instanceof PingWebSocketFrame){
				ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			}
			
			// 本示例只支持文本消息，不支持二进制消息
			if(!(frame instanceof TextWebSocketFrame)){
				throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
			}
			
			// 返回应答消息
			String request = ((TextWebSocketFrame)frame).text();
			
			ctx.channel().write(new TextWebSocketFrame(request+"，欢迎使用Netty Websocket 服务 ，现在时刻："+new Date().toString()));
		}
		
		private void sendHttpRespone(ChannelHandlerContext ctx,FullHttpRequest req, FullHttpResponse resp){
			// 返回应答给客户端
			if(resp.getStatus().code() != 200){
				ByteBuf buf = Unpooled.copiedBuffer(resp.getStatus().toString(),CharsetUtil.UTF_8);
				resp.content().writeBytes(buf);
				buf.release();
			}
			
			// 如果非Keep-Alive , 关闭连接
			ChannelFuture f = ctx.channel().writeAndFlush(resp);
			
			if(!isKeepAlive(req) || resp.getStatus().code() != 200){
				f.addListener(ChannelFutureListener.CLOSE);
			}
		}
		
		private boolean isKeepAlive(FullHttpRequest req){
			return false;
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
