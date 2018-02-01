package com.example.douwe.supermegasms;

/**
 * Created by Douwe on 1/18/18.
 *
 * This object contains all information needed to make fill a chat message layout.
 */

public class ChatMessage {
    public boolean left;
    public String message;
    public String sender;
    public int date;

    /**
     * The constructor.
     * @param left is the message send my the user or a contact?
     * @param message the message
     * @param sender who sent it
     * @param date when was the message received
     */
    public ChatMessage(boolean left, String message, String sender, int date) {
        super();
        this.left = left;
        this.message = message;
        this.sender = sender;
        this.date = date;
    }
}