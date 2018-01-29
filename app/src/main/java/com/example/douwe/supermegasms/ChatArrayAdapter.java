package com.example.douwe.supermegasms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
// borrowed from: https://trinitytuts.com/simple-chat-application-using-listview-in-android/
class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;
    private boolean group = false;
    private Helpers helper = new Helpers();

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row;
        if (!group) {
            row = inflateIndividual(chatMessageObj, parent);
        } else {
            row = inflateGroup(chatMessageObj, parent);
        }
        ((TextView) row.findViewById(R.id.message)).setText(chatMessageObj.message);
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