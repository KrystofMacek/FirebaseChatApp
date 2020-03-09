package com.krystofmacek.firebasechatapp.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Model class uzivatele
public class User {

    private String uid;
    private String displayName;
    private Map<String, String> location;
    private List<String> tags = new ArrayList<>();
    private List<String> friends = new ArrayList<>();
    private List<String> activeChats = new ArrayList<>();
    private String registrationToken;

    public User() {
    }

    public User(String uid, String displayName, Map<String, String> location, List<String> tags, List<String> friends, List<String> activeChats, String registrationToken) {
        this.uid = uid;
        this.displayName = displayName;
        this.location = location;
        this.tags = tags;
        this.friends = friends;
        this.activeChats = activeChats;
        this.registrationToken = registrationToken;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getLocation() {
        return location;
    }

    public void setLocation(Map<String, String> location) {
        this.location = location;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getActiveChats() {
        return activeChats;
    }

    public void setActiveChats(List<String> activeChats) {
        this.activeChats = activeChats;
    }
}
