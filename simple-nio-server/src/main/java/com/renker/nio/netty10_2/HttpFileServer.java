package com.renker.nio.netty10_2;

import java.io.File;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {
	public static void main(String[] args) {
		
		File file = new File("D:/");
		
		System.out.println(file.exists());
		
		new HttpFileServer().bind();
		
	}
	public void bind(){
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		String url = "/file";
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// HTTP请求消息解码器
						ch.pipeline().addLast(new HttpRequestDecoder())
							// 将多个消息转换为单一的FullHttpRequest 或者 FullHttpResponse,原因是HTTP解码器在第个HTTP消息中会生成多个消息对象
							.addLast(new HttpObjectAggregator(65536))
							// HTTP响应消息编码器
							.addLast(new HttpResponseEncoder())
							// 支持异步发送大的码流（例如大的文件传输）但不占用过多内存，防止发生Java内存溢出
							.addLast(new ChunkedWriteHandler())
							.addLast(new HttpFileServerHandler(url));
					}
				});
			
			ChannelFuture f = b.bind(8080).sync();
			System.out.println("HTTP 文件目录服务器启动，网址是："+"http://127.0.0.1:8080"+url);
			
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
