package io.text.models;

import java.util.Date;

public class Message {

    private String uid;
    private String name;
    private String text;
    private String contextName;
    private String contextText;
    private long time;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String uid, String name, String text, String contextName, String contextText ) {
        this.uid = uid;
        this.name = name;
        this.text = text;
        this.time = new Date().getTime();
        this.contextName = contextName;
        this.contextText = contextText;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getContextName() {
        return contextName;
    }

    public String getContextText() {
        return contextText;
    }

    public long getTime() {
        return time;
    }
}
