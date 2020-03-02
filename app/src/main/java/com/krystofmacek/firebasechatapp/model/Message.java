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
}
