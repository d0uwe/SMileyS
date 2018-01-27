package com.example.douwe.supermegasms;

/**
 * Created by douwe on 1/21/18.
 */

public class ContactRow {
    public String message;
    public String number;
    public String date;
    public String name;
    public boolean group;

    public ContactRow(String message, String number, String date, String name, boolean group) {
        super();
        this.message = message;
        this.number = number;
        this.date = date;
        this.name = name;
        this.group = group;
    }
}
