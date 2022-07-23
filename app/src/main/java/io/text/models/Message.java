package io.text.models;

import java.util.Date;

public class Message {

    private String uid;
    private String name;
    private String text;
    private long time;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String uid, String name, String text) {
        this.uid = uid;
        this.name = name;
        this.text = text;
        this.time = new Date().getTime();
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

    public long getTime() {
        return time;
    }
}
