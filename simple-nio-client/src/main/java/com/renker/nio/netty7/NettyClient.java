package com.renker.nio.netty7;

import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class NettyClient {
	public static void main(String[] args) {
		new NettyClient().connect("127.0.01", 8080);
	}
	
	public void connect(String host,int port){
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap b = new Bootstrap();
			b.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast("frame decoder",new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
						ch.pipeline().addLast("msgpack decoder",new MsgpackDecoder());
						ch.pipeline().addLast("frame encoder",new LengthFieldPrepender(2));
						ch.pipeline().addLast("msgpack encoder",new MsgpackEncoder());
						ch.pipeline().addLast(new NettyClientHandler());
					}
				});
			
			ChannelFuture f = b.connect(host, port).sync();
			
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}
	
	
	
	public class NettyClientHandler extends ChannelHandlerAdapter{
		private int count;
		public NettyClientHandler() {
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			for (int i = 0; i < 10000; i++) {
				Person p = new Person();
				p.setId(i);
				p.setName("Tom_"+i);
				ctx.writeAndFlush(p);
			}
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			Person p = (Person) msg;
			System.out.println("Server response msg :[id:"+p.getId()+",name:"+p.getName()+"]"+" the count is :"+ ++count);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
