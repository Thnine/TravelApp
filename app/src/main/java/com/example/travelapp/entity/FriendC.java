package com.example.travelapp.entity;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//好友结构体
public class FriendC implements Serializable {
    private String username;
    private String signature;
    private List<MessageC> messages;
    private Boolean isSelectedInGroupMemberAdd;//判定是否被选中


    public FriendC(String username,List<MessageC> messages){
        this.username = username;
        this.messages = messages;
        signature = "";
        this.isSelectedInGroupMemberAdd = false;
    }

    public FriendC(String username){
        this.username = username;
        this.messages = new ArrayList<>();
        signature = "";
        this.isSelectedInGroupMemberAdd = false;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public List<MessageC> getMessages(){
        return this.messages;
    }

    public void setMessages(List<MessageC> messages){
        this.messages = messages;
    }

    public String getLastMessage(){
        return this.messages.get(this.messages.size()-1).getMessage();
    }

    public String getSignature(){
        return this.signature;
    }

    public void setSignature(String signature){
        this.signature = signature;
    }

    public boolean getisSelectedInGroupMemberAdd(){
        return this.isSelectedInGroupMemberAdd;
    }

    public void setIsSelectedInGroupMemberAdd(Boolean isSelectedInGroupMemberAdd){
        this.isSelectedInGroupMemberAdd = isSelectedInGroupMemberAdd;
    }

}
