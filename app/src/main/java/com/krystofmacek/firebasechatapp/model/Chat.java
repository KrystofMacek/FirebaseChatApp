package com.krystofmacek.firebasechatapp.model;

import java.util.List;

public class Chat {
    private String uid;
    private List<String> members;

    public Chat() {
    }

    public Chat(String uid, List<String> members) {
        this.uid = uid;
        this.members = members;
    }
}
