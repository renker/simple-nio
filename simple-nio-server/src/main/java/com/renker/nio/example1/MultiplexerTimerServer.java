package com.renker.nio.example1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimerServer implements Runnable {
	
	private Selector selector;
	
	private ServerSocketChannel channel;
	
	private volatile boolean stop = false;

	public MultiplexerTimerServer(int port) {
		try {
			selector = Selector.open();
			channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().bind(new InetSocketAddress(port),1024);
			channel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("服务启动...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}



	public void run() {
		while (!stop) {
			try {
				selector.select(10000);
				Set<SelectionKey> selectedKeys= selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void handleInput(SelectionKey key) throws IOException{
		if(key.isValid()){
			
			if(key.isAcceptable()){
				// 处理消息
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			
			if(key.isReadable()){
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer bf = ByteBuffer.allocate(1024);
				
				int readBytes = sc.read(bf);
				if(readBytes > 0){
					bf.flip();
					byte [] bytes = new byte[bf.remaining()];
					bf.get(bytes);
					String body = new String(bytes, "UTF-8");
					
					System.out.println("[server acept msg:"+body+"]");
					
					String response = "[server response msg:"+body+"]";
					doWrite(sc, response);
				}else if(readBytes < 0){
					key.cancel();
					sc.close();
				}else{
					// 0字节不考虑
				}
			}
		}
		
		/*if(selector != null){
			selector.close();
		}*/
	}
	
	private void doWrite(SocketChannel channel , String response) throws IOException{
		if(response != null && response.trim().length() > 0){
			byte [] bytes = response.getBytes();
			ByteBuffer bf = ByteBuffer.allocate(bytes.length);
			bf.put(bytes);
			bf.flip();
			channel.write(bf);
		}
		
	}
	
	public static void main(String[] args) {
		MultiplexerTimerServer server = new MultiplexerTimerServer(8080);
		
		new Thread(server,"NIO-SERVER-8080").start();
	}

}
