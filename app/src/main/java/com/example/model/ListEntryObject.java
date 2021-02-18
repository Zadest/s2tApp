package com.example.model;

import java.time.LocalDate;

public class ListEntryObject {
    //TODO: include key from shared preferences to enable connection?
    //TODO: make it sortable by date?!
    String title;
    String text;
    boolean opened;

    public ListEntryObject(String title, String text){
        this.title = title;
        this.text = text;
        opened = false;
    }

    public String getTitle(){
        return title;
    }

    public String getText(){
        return text;
    }

    public boolean isOpenend(){
        return opened;
    }

    public void setOpened(boolean opened){
        this.opened = opened;
    }
}
