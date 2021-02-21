package com.example.travelapp.entity;

import java.io.Serializable;
import java.util.Date;

//攻略类
public class StrategyC implements Serializable {
    private String writer;
    private float score;
    private String text;
    private String title;
    private String city;

    public StrategyC(){

    }
    public StrategyC(String writer,float score,String title,String text,String city){
        this.writer = writer;
        this.score = score;
        this.title = title;
        this.text = text;
        this.city = city;
    }


    public float getScore() {
        return score;
    }

    public String getWriter() {
        return writer;
    }

    public String getText() {
        return text;
    }


    public void setScore(float score) {
        this.score = score;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
