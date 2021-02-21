package com.example.travelapp.entity;

import java.io.Serializable;
import java.security.acl.Group;
import java.util.List;

public class GroupC implements Serializable {

    private String Groupname;
    private FriendC Owner;
    private List<FriendC> Members;
    private List<PlanRecord> PlanRecords;
    private int group_id;

    public GroupC(String Groupname,FriendC Owner,List<FriendC> Members,List<PlanRecord> PlanRecords){
        this.Groupname = Groupname;
        this.Owner = Owner;
        this.Members = Members;
        this.PlanRecords = PlanRecords;
    }

    public String getGroupname(){
        return this.Groupname;
    }

    public FriendC getOwner(){
        return this.Owner;
    }

    public List<FriendC> getMembers(){
        return this.Members;
    }

    public List<PlanRecord> getPlanRecords(){
        return this.PlanRecords;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }
}
