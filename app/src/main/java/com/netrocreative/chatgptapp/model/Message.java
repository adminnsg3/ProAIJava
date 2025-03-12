package com.netrocreative.chatgptapp.model;


public class Message {
    private String message;
    private String sentBy;

    public static final String SENT_BY_ME = "me";
    public static final String SENT_BY_BOT = "bot";

    public Message(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
}