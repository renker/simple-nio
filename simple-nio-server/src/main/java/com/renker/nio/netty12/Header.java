package com.renker.nio.netty12;

import java.util.HashMap;
import java.util.Map;

public class Header {
	/**
	 * Netty消息检验码，它由三部分组成
	 * 1. 0xABEF:固定值，表明该消息是Netty协议消息，2个字节
	 * 2. 主版本号：1~255， 1 个字节
	 * 3. 次版本号：1~255， 1个字节
	 * crcCode = 0xABEF+主版本号+次版本号
	 */
	private int crcCode = 0xabef0101 ;
	
	/**
	 * 消息长度，整个消息，包含消息头与消息体
	 */
	private int length;
	
	/**
	 * 集群节点内全局唯一，由ID生成器生成
	 */
	private long sessionId;
	
	/**
	 * 0：业务请求消息
	 * 1：业务响应消息
	 * 2：业务ONE WAY 消息（即时请求也是响应消息）
	 * 3：握手请求消息
	 * 4：握手应答消息
	 * 5：心跳请求消息
	 * 6：心跳应答消息
	 */
	private Byte type;
	
	/**
	 * 消息优先级：0~255
	 */
	private Byte priority;
	
	/**
	 * 可选字段，用于扩展消息头
	 */
	private Map<String, Object> attachment;

	public int getCrcCode() {
		return crcCode;
	}

	public void setCrcCode(int crcCode) {
		this.crcCode = crcCode;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	public Byte getPriority() {
		return priority == null ? (byte)0:priority;
	}

	public void setPriority(Byte priority) {
		this.priority = priority;
	}

	public Map<String, Object> getAttachment() {
		return attachment == null ? new HashMap<String, Object>():attachment;
	}

	public void setAttachment(Map<String, Object> attachment) {
		this.attachment = attachment;
	}
	
}
