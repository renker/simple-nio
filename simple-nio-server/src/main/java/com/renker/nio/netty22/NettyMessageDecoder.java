package com.renker.nio.netty22;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.renker.nio.netty22.marshalling.MarshallingDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Netty 消息解码器
 * @author Tao
 *
 */
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder{
	
	MarshallingDecoder marshallingDecoder;

	public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) throws IOException {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
		marshallingDecoder = new MarshallingDecoder();
	}
	
	public Object decode (ChannelHandlerContext ctx,ByteBuf in) throws Exception{
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		
		if(frame == null){
			return null;
		}
		
		NettyMessage message = new NettyMessage();
		
		Header header = new Header();
		
		header.setCrcCode(frame.readInt());
		header.setLength(frame.readInt());
		header.setSessionId(frame.readLong());
		header.setType(frame.readByte());
		header.setPriority(frame.readByte());
		
		int size = frame.readInt();
		
		if(size > 0){
			Map<String, Object> attch = new HashMap<String,Object>(size);
			int keySize = 0;
			byte[] keyArray = null;
			String key = null;
			
			for (int i = 0; i < size; i++) {
				keySize = frame.readInt();
				keyArray = new byte[keySize];
				
				frame.readBytes(keyArray);
				
				key = new String(keyArray, "UTF-8");
				
				attch.put(key, marshallingDecoder.decode(frame));
				
				keyArray = null;
				key = null;
				header.setAttachment(attch);
			}
			
			
		}
		
		if(frame.readableBytes() > 4){
			message.setBody(marshallingDecoder.decode(frame));
		}
		
		message.setHeader(header);
		
		return message;
	}

}
