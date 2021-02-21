package com.example.travelapp.entity;

import java.io.Serializable;

public class LocationC implements Serializable {

    private String province;
    private String city;
    private double latitude;
    private double longitude;
    private String code;

    public LocationC(String province,String city,String code){
        this.province = province;
        this.city = city;
        this.code = code;
    }

    public LocationC(String province,String city){
        this.province = province;
        this.city = city;
    }
    public LocationC(double latitude,double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getProvince(){
        return this.province;
    }

    public String getCity(){
        return this.city;
    }

    public String getCode(){
        return this.code;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public void setCity(String province,String city,String code){
        this.province = province;
        this.code = code;
        this.city = city;
    }

    public void setLocation(double latitude,double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
