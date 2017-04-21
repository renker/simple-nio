package com.renker.nio.netty10_2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private final String url;
	
	private final String basePath = "D:/mybatisGeneratorFile";
	
	public HttpFileServerHandler(String url) {
		this.url =url;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		
		// 解码异常 返回异常
		if(!(request.getDecoderResult().isSuccess())){
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		
		// 不是GET请求 返回异常
		if(request.getMethod() != HttpMethod.GET){
			sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		
		String uri = request.getUri();
		String path = sanitizeUri(uri);
		
		// 路径为空 返回异常
		if(path == null){
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}
		
		File file = new File(path);
		//if(file.isHidden() || !file.exists()){
		
		// 文件不存在，返回异常
		if(!file.exists()){
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		// 如果是文件夹，显示列表
		if(file.isDirectory()){
			if(uri.endsWith("/")){
				
			}else{
				
			}
			// 显示列表
			sendList(ctx,file);
		}
		
		// 文件不是文件，返回异常
		if(!file.isFile()){
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}
		
		RandomAccessFile randomAccessFile = null;
		
		try {
			// 以只读模式打开文件夹
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		long fileLength = randomAccessFile.length();
		
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		
		HttpHeaders.setContentLength(response, fileLength);
		
		setContentTypeHeader(response, file);
		
		if(HttpHeaders.isKeepAlive(request)){
			response.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
		}
		
		ctx.write(response);
		
		ChannelFuture sendFileFuture = null;
		sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile,0,fileLength,8192), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			
			@Override
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				System.out.println("Transfer complete...");
			}
			
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
				if(total < 0){
					System.err.println("Transfer progress: " + progress);
				}else{
					System.out.println("Transfer progress: " + progress + "/" + total);
				}
			}
		});
		
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		
		if(!HttpHeaders.isKeepAlive(request)){
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if(ctx.channel().isActive()){
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void sendError(ChannelHandlerContext ctx , HttpResponseStatus status){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,Unpooled.copiedBuffer("Failure: "+status.toString()+"\r\n", CharsetUtil.UTF_8));
		response.headers().set("Content-Type", "text/html;charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	
	}
	
	private void sendList(ChannelHandlerContext ctx, File dir){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set("Content-Type","text/html;charset=UTF-8");
		String dirPath = dir.getPath();
		dirPath = dirPath.replaceAll("\\\\", "/");
		dirPath = dirPath.replace(basePath, url);
		StringBuffer buf = new StringBuffer();
		
		buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append("目录:");
        buf.append("</title></head><body>\r\n");
        
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\" ../\">...</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if(f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            
            String childPath = f.getPath();
            childPath = childPath.replaceAll("\\\\", "/");
            childPath = childPath.replace(basePath, url);
            
            buf.append("<li>链接：<a href=\"");
            buf.append(childPath);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        
        buf.append("</ul></body></html>\r\n");
        
        ByteBuf buffer = Unpooled.copiedBuffer(buf,CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	private void setContentTypeHeader(HttpResponse response,File file){
		MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
		response.headers().set("",mimetypesFileTypeMap.getContentType(file.getPath()));
	}
	
	private String sanitizeUri(String uri){
		try {
			uri = URLDecoder.decode(uri,"UTF-8");
		} catch (UnsupportedEncodingException  e) {
			e.printStackTrace();
			try {
				uri = URLDecoder.decode(uri,"ISO-8859-1");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}
		}
		
		if(!uri.startsWith(url)){
			return null;
		}
		
		if(!uri.startsWith("/")){
			return null;
		}
		
		uri = uri.replace(url, basePath);
		
		return uri;
	}

}
