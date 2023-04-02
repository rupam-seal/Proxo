package com.app.pyxller.model;

public class Users {

    private String uid, username, fullname, profileimage, bio;

    public Users(){
    }

    public Users(String uid, String username, String fullname, String profileimage, String bio) {
        this.uid = uid;
        this.username = username;
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
