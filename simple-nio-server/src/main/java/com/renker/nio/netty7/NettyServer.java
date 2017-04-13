package com.renker.nio.netty7;


import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class NettyServer {
	public static void main(String[] args) {
		new NettyServer().bind();
	}
	
	public void bind(){
		System.out.println("服务启动...");
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.childHandler(new ChildChannelHandler());
			
			ChannelFuture f = b.bind(8080).sync();
			
			f.channel().closeFuture().sync();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast("frame decoder",new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
			ch.pipeline().addLast("msgpack decoder",new MsgpackDecoder());
			ch.pipeline().addLast("frame encoder",new LengthFieldPrepender(2));
			ch.pipeline().addLast("msgpack encoder",new MsgpackEncoder());
			ch.pipeline().addLast(new NettyServerHandle());
		}
	}
	
	public class NettyServerHandle extends ChannelHandlerAdapter{
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			Person p = (Person) msg;
			System.out.println("Server receive msg : [id:"+p.getId()+",name:"+p.getName()+"]");
			ctx.writeAndFlush(p);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
