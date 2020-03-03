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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getOtherMember(String member) {
        if(members.get(0).equals(member)) {
            return members.get(1);
        }
        return members.get(0);
    }
}
