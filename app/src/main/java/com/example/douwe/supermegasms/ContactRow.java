package com.example.douwe.supermegasms;

/**
 * Created by Douwe on 1/21/18.
 *
 * This object contains all information to fill a row in the conversation overview.
 */

public class ContactRow {
    public String message;
    public String number;
    public long date;
    public String name;
    public boolean group;

    /**
     * Constructor of the object.
     * @param message the message
     * @param number the number which send it
     * @param date the date
     * @param name the name of the contact
     * @param group is it a group, or not?
     */
    public ContactRow(String message, String number, int date, String name, boolean group) {
        super();
        this.message = message;
        this.number = number;
        this.date = ((long) date) * 1000;
        this.name = name;
        this.group = group;
    }
}
