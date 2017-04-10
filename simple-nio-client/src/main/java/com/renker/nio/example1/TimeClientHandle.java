package com.renker.nio.example1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandle implements Runnable {
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop = false;
	public TimeClientHandle(String host,int port) {
		this.host = host;
		this.port = port;
		
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		try {
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (!stop) {
			try {
				selector.select(10000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectionKeys.iterator();
				SelectionKey key = null;
				
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						e.printStackTrace();
						if(key != null){
							key.cancel();
							if(key.channel() != null){
								key.channel().close();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		if(selector != null){
//			try {
//				selector.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	private void handleInput(SelectionKey key) throws IOException{
		if(key.isValid()){
			SocketChannel sc = (SocketChannel) key.channel();
			if(sc.isConnectionPending()){
				if(sc.finishConnect()){
					if(sc.isConnected()){
						doWrite(sc);
					}
				}
			}
			
			if(key.isWritable()){
				doWrite(sc);
			}
			
			if(key.isReadable()){
				ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(writeBuffer);
				if(readBytes > 0){
					writeBuffer.flip();
					byte [] bytes = new byte[writeBuffer.remaining()];
					writeBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					
					System.out.println("[client acept msg:"+body+"]");
					
					socketChannel.register(selector, SelectionKey.OP_WRITE);
				}else if(readBytes < 0){
					key.cancel();
					sc.close();
				}else{
					// 0字节不考虑
				}
			}
		}
	}
	
	private void doConnect() throws IOException{
		if(socketChannel.connect(new InetSocketAddress(host, port))){
			socketChannel.register(selector, SelectionKey.OP_READ);
		}else{
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}
	
	private void doWrite(SocketChannel sc) throws IOException{
		byte [] req = "Hellow".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		
		writeBuffer.put(req);
		writeBuffer.flip();
		sc.write(writeBuffer);
		if(!writeBuffer.hasRemaining()){
			System.out.println("消息发送成功...");
		}
		
		sc.register(selector, SelectionKey.OP_READ);
		
	}
	
	public static void main(String[] args) {
		TimeClientHandle handle = new TimeClientHandle("127.0.0.1", 8080);
		new Thread(handle,"NIO-CLIENT-8080").start();
	}

}
