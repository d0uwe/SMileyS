package com.example.douwe.supermegasms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
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
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (!group) {
            if (chatMessageObj.left) {
                row = inflater.inflate(R.layout.right, parent, false);
            } else {
                row = inflater.inflate(R.layout.left, parent, false);
            }
        } else {
            if (chatMessageObj.left) {
                row = inflater.inflate(R.layout.right_group, parent, false);
            } else {
                row = inflater.inflate(R.layout.left_group, parent, false);
                String contactName = getContactName(getContext(), chatMessageObj.sender);
                if (contactName != null && !contactName.isEmpty()){
                    ((TextView)row.findViewById(R.id.sender)).setText(contactName);
                } else {
                    ((TextView)row.findViewById(R.id.sender)).setText(chatMessageObj.sender);
                }
            }
        }
        ((TextView) row.findViewById(R.id.message)).setText(chatMessageObj.message);
        chatText = (TextView) row.findViewById(R.id.message);
        chatText.setText(chatMessageObj.message);
        return row;
    }

    // todo: duplicate with mainactivity
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}