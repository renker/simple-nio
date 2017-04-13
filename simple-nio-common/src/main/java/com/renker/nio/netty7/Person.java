package com.renker.nio.netty7;

import java.io.Serializable;

import org.msgpack.annotation.Message;

@Message
public class Person implements Serializable{
	private static final long serialVersionUID = 611580743369701620L;
	private int id;
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
