package com.example.sketch;

import java.util.ArrayList;

public class User {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String username;
    private ArrayList<DrawingView> mycanavas;
    private int id;
    private byte[] profilepic; // using byte data for photos
    private String Token;

    public User(String firstname,String lastname,String email,String password,String username){
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.username = username;
        this.mycanavas = new ArrayList<>();
    }

    public String getUserName() {
        return this.username;
    }

    public void setUserName(String username){
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname){
        this.firstname = firstname;
    }
    public String getLastname(){
        return this.lastname;
    }

    public void setLastname(String lastname){
        this.lastname = lastname;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password  = password;
    }

    public ArrayList<DrawingView> getMyCanavas(){
        return this.mycanavas;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getid(){
        return this.id;
    }

    public void setProfilePic(byte[] pic){
        this.profilepic = pic;
    }
    public byte[] getProfilepic(){
        return this.profilepic;
    }
    public void setToken(String token){
        this.Token = token;
    }
    public String getToken(){
        return this.Token;
    }

}
