package com.example.travelapp.entity;

import java.io.Serializable;
import java.util.Date;

public class PlanRecord implements Serializable {

    Date PlanDate;
    LocationC PlanLoc;
    int plan_id;

    public PlanRecord(Date PlanDate,LocationC PlanLoc,int plan_id){
        this.PlanDate = PlanDate;
        this.PlanLoc = PlanLoc;
        this.plan_id = plan_id;
    }

    public PlanRecord(){
        ;
    }

    public PlanRecord(PlanRecord myplan){
        this.PlanLoc = myplan.getPlanLoc();
        this.PlanDate = myplan.getPlanDate();
    }

    public Date getPlanDate(){
        return this.PlanDate;
    }

    public LocationC getPlanLoc(){
        return this.PlanLoc;
    }

    public void setPlanDate(Date PlanDate){
        this.PlanDate = PlanDate;
    }

    public void setPlanLoc(LocationC PlanLoc){
        this.PlanLoc = PlanLoc;
    }

    public int getPlan_id(){
        return this.plan_id;
    }

    public void setPlan_id(int plan_id) {
        this.plan_id = plan_id;
    }
}
