package com.krystofmacek.firebasechatapp.model;

import com.google.firebase.Timestamp;

public class Message {
        private String author;
    private String messageText;
    private Timestamp timestamp;

    public Message() {
    }

    public Message(String author, String messageText, Timestamp timestamp) {
        this.author = author;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
