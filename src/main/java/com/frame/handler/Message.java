package com.frame.handler;

import java.io.Serializable;

import com.google.gson.JsonObject;

public class Message implements Serializable
{
    private static final long serialVersionUID = 2017081417099017L;
    private String topic = null;
    private JsonObject content = null;
    private MessageHandler handler = null;

    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    public void setConten(JsonObject content)
    {
        this.content = content;
    }

    public void setHandler(MessageHandler handler)
    {
        this.handler = handler;
    }

    public MessageHandler getHandler()
    {
        return this.handler;
    }

    public JsonObject getConten()
    {
        return this.content;
    }

    public String getTopic()
    {
        return this.topic;
    }

}
