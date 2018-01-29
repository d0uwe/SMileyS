package com.example.douwe.supermegasms;

/**
 * Created by douwe on 1/18/18.
 */

public class ChatMessage {
    public boolean left;
    public String message;
    public String sender;
    public int date;

    public ChatMessage(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }

    public ChatMessage(boolean left, String message, String sender, int date) {
        super();
        this.left = left;
        this.message = message;
        this.sender = sender;
        this.date = date;
    }
}