package com.renker.nio.netty22;

public class NettyMessage {
	/**
	 * 消息头定义
	 */
	private Header header;
	/**
	 * 消息体
	 * 对于请求消息，它是方法的参数（作为示例，只支持携带一个参数 ）
	 * 对于响应消息，它是返回消息
	 */
	private Object body;
	public Header getHeader() {
		return header;
	}
	public void setHeader(Header header) {
		this.header = header;
	}
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}
}
