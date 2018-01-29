package com.example.douwe.supermegasms;

/**
 * Created by douwe on 1/21/18.
 */

public class ContactRow {
    public String message;
    public String number;
    public long date;
    public String name;
    public boolean group;

    public ContactRow(String message, String number, int date, String name, boolean group) {
        super();
        this.message = message;
        this.number = number;
        this.date = ((long) date) * 1000;
        this.name = name;
        this.group = group;
    }
}
