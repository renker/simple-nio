package com.renker.nio.netty12;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.renker.nio.netty12.marshalling.MarshallingEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Netty 消息编码
 * @author Tao
 *
 */
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage>{
	
	MarshallingEncoder marshallingEncoder;
	
	public NettyMessageEncoder() throws IOException {
		this.marshallingEncoder = new MarshallingEncoder();
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf sendBuf) throws Exception {
		if(msg == null || msg.getHeader() == null){
			throw new Exception("The encode message is null");
		}
		
		sendBuf.writeInt(msg.getHeader().getCrcCode());
		sendBuf.writeInt(msg.getHeader().getLength());
		sendBuf.writeLong(msg.getHeader().getSessionId());
		sendBuf.writeByte(msg.getHeader().getType());
		sendBuf.writeByte(msg.getHeader().getPriority());
		sendBuf.writeInt(msg.getHeader().getAttachment().size());
		
		String key = null;
		byte [] keyArray = null;
		Object value = null;
		for (Map.Entry<String, Object> param : msg.getHeader().getAttachment().entrySet()) {
			key = param.getKey();
			keyArray = key.getBytes("UTF-8");
			sendBuf.writeInt(keyArray.length);
			sendBuf.writeBytes(keyArray);
			
			value = param.getValue();
			marshallingEncoder.encode(value, sendBuf);
		}
		
		key = null;
		keyArray = null;
		value = null;
		
		if(msg.getBody() != null){
			marshallingEncoder.encode(msg.getBody(), sendBuf);
			
			sendBuf.writeInt(0);
			sendBuf.setInt(4, sendBuf.readableBytes() - 8);
		}else{
			sendBuf.writeInt(0);
			sendBuf.setInt(4, sendBuf.readableBytes() - 8);
		}
	}

}
