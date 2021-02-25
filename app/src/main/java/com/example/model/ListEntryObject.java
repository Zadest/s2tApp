package com.example.model;

import android.text.SpannableString;

public class ListEntryObject implements Comparable<ListEntryObject> {
    //TODO: make it sortable by date?!

    String key;
    String title;
    SpannableString text;
    boolean opened;

    public ListEntryObject(String key, String title, SpannableString text){
        this.key = key;
        this.title = title;
        this.text = text;
        opened = false;
    }

    public String getTitle(){
        return title;
    }

    public String getKey() { return key; }

    public SpannableString getText(){
        return text;
    }

    public boolean isOpenend(){
        return opened;
    }

    public void setOpened(boolean opened){
        this.opened = opened;
    }

    @Override
    public int compareTo(ListEntryObject o) {
        return this.getKey().compareTo(o.getKey());
    }
}
