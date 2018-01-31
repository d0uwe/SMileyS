package com.example.douwe.supermegasms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Douwe on 1/16/18.
 *
 * This adapter fills the conversation list view. It places messages right or left and sets all
 * information such as the message, date, contact who send them.
 *
 * borrowed from: https://trinitytuts.com/simple-chat-application-using-listview-in-android/
 * but has been quite extensively altered
 */
class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private Context context;
    private boolean group = false;
    private Helpers helper = new Helpers();

    /**
     * Add an element to the list.
     * @param object element to be added
     */
    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    /**
     * Get the size of the contacts list.
     * @return integer containing the size
     */
    public int getCount() {
        return this.chatMessageList.size();
    }

    /**
     * Set a boolean to indicate whether this is a group or not
     * @param group The boolean
     */
    public void setGroup(boolean group) {
        this.group = group;
    }

    /**
     * Get the item at a certain position in the list.
     * @param index Index of the item that is requested
     * @return the item at the index.
     */
    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    /**
     * Get the view for an item in the list
     * @param position position of item of which the view is needed
     * @param convertView a convertView
     * @param parent the parent
     * @return The inflated object
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row;
        if (!group) {
            row = inflateIndividual(chatMessageObj, parent);
        } else {
            row = inflateGroup(chatMessageObj, parent);
        }
        ((TextView) row.findViewById(R.id.message)).setText(chatMessageObj.message);
        long date = ((long)chatMessageObj.date) * 1000;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        String dateString = c.get(c.MONTH) + 1 + "-" + c.get(c.DAY_OF_MONTH) + " " +
                c.get(c.HOUR_OF_DAY) + "." + c.get(c.MINUTE) + " " + c.get(c.YEAR);
        ((TextView)row.findViewById(R.id.date)).setText(dateString);
        return row;
    }

    /**
     * Inflate a listview row for an individual chat
     * @param chatMessageObj object containing the message, sender and other specifics
     * @param parent a ViewGroup
     * @return inflated row for in the ListView.
     */
    public View inflateIndividual(ChatMessage chatMessageObj, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;
        if (chatMessageObj.left) {
            row = inflater.inflate(R.layout.right, parent, false);
        } else {
            row = inflater.inflate(R.layout.left, parent, false);
        }
        return row;
    }

    /**
     * Inflate a listview row for a group chat
     * @param chatMessageObj object containing the message, sender and other specifics
     * @param parent a ViewGroup
     * @return inflated row for in the ListView.
     */
    public View inflateGroup(ChatMessage chatMessageObj, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;
        if (chatMessageObj.left) {
            row = inflater.inflate(R.layout.right_group, parent, false);
        } else {
            row = inflater.inflate(R.layout.left_group, parent, false);
            String contactName = helper.getContactName(getContext(), chatMessageObj.sender);
            if (contactName != null && !contactName.isEmpty()){
                ((TextView)row.findViewById(R.id.sender)).setText(contactName);
            } else {
                ((TextView)row.findViewById(R.id.sender)).setText(chatMessageObj.sender);
            }
        }
        return row;
    }
}