package com.krystofmacek.firebasechatapp.model;

import com.google.firebase.Timestamp;

public class Message {
    private String authorId;
    private String recieverId;
    private String messageText;
    private Timestamp timestamp;
    private Boolean seen;

    public Message() {
    }

    public Message(String authorId, String recieverId, String messageText, Timestamp timestamp) {
        this.authorId = authorId;
        this.recieverId = recieverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.seen = false;
    }

    public String getRecieverId() {
        return recieverId;
    }

    public void setRecieverId(String recieverId) {
        this.recieverId = recieverId;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
