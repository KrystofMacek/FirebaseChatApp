package com.krystofmacek.firebasechatapp.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class Chat {
    private String uid;
    private List<String> members;
    private Timestamp lastMessageTime;

    // prázdný konstruktor pro Firestore
    public Chat() {
    }
    public Chat(String uid, List<String> members) {
        this.uid = uid;
        this.members = members;
    }
    // get a set metody pro UID reprezentující chat
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    // get a set metody pro seznam clenů
    public List<String> getMembers() {
        return members;
    }
    public void setMembers(List<String> members) {
        this.members = members;
    }
    // get a set metody pro záznam času poslední zprávy v chatu
    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }
    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    // pomocná metoda pro jednoho z clenu
    public String getOtherMember(String member) {
        if(members.get(0).equals(member)) {
            return members.get(1);
        }
        return members.get(0);
    }
}
