package com.frame.handler;

import com.google.gson.JsonObject;


public interface MessageHandler
{	
	/**
	 * 消息队列用于控制SOA核心框架的附加机制
	 * @param message 消息内容
	 * @throws Exception 消息处理失败，消息通道将会择机重发消息
	 */
	public void onMessage(String topic,JsonObject content) throws Exception;
}
