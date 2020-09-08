package com.example.myapplication;

import java.util.Date;

public class Message {
    private String user;
    private String text;
    private long time;

    public Message(String text, String user){
        this.user = user;
        this.text = text;

        time = new Date().getTime();
    }

    public Message(){
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
