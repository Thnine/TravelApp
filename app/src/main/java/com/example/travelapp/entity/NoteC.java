package com.example.travelapp.entity;

public class NoteC {
    String note_text;
    boolean note_isEx;
    int note_id;

    public NoteC(String note_text,boolean note_isEx,int note_id){
        this.note_text = note_text;
        this.note_isEx = note_isEx;
        this.note_id = note_id;
    }

    public boolean isNote_isEx() {
        return note_isEx;
    }

    public String getNote_text() {
        return note_text;
    }

    public void setNote_isEx(boolean note_isEx) {
        this.note_isEx = note_isEx;
    }

    public void setNote_text(String note_text) {
        this.note_text = note_text;
    }

    public int getNote_id() {
        return note_id;
    }

    public void setNote_id(int note_id) {
        this.note_id = note_id;
    }
}
