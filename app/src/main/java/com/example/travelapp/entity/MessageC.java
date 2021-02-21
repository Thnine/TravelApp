package com.example.travelapp.entity;

import java.util.List;

//消息结构体
public class MessageC {
    private String message;
    private String fromuser;
    private String touser;

    public MessageC(String message,String fromuser,String touser){
        this.message = message;
        this.fromuser = fromuser;
        this.touser = touser;
    }

    public String getMessage(){
        return this.message;
    }

    public String getFromuser(){
        return this.fromuser;
    }

    public String getTouser(){
        return this.touser;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setFromuser(String fromuser){
        this.fromuser = fromuser;
    }

    public void setTouser(String touser){
        this.touser = touser;
    }

}
