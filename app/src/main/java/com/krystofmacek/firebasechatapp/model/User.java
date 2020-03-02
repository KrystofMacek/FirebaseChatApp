package com.krystofmacek.firebasechatapp.model;

import java.util.List;
import java.util.Map;

// Model class uzivatele
public class User {

    private String uid;
    private String displayName;
    private Map<String, String> location;
    private List<String> tags;
    private List<String> friends;

    public User() {
    }

    public User(String uid, String displayName, Map<String, String> location, List<String> tags, List<String> friends) {
        this.uid = uid;
        this.displayName = displayName;
        this.location = location;
        this.tags = tags;
        this.friends = friends;
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
}
