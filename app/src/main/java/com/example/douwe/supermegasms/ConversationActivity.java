package com.example.douwe.supermegasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.telephony.PhoneNumberUtils.formatNumberToE164;

public class ConversationActivity extends AppCompatActivity {
    String phoneNumber = null;
    boolean inGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get who we're conversing with
        setContentView(R.layout.activity_conversation);
        phoneNumber = getIntent().getExtras().getString("phoneNumber");
        inGroup = getIntent().getExtras().getBoolean("groupBoolean");
        setMessages(phoneNumber);

        if(inGroup){
            findViewById(R.id.sendButton).setOnClickListener(new HandleSendGroupClick());
        } else{
            findViewById(R.id.sendButton).setOnClickListener(new HandleSendClick());
        }

        // register to broadcasts to refresh listview when there is a new message
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message received"));
    }

    /**
     * Receives a broadcast when an sms is received. Forces the listview to update.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (phoneNumber != null){
                setMessages(phoneNumber);
            }
        }
    };

    /**
     * Get messages send to and from this number and set them in a listview.
     * @param phoneNumber phoneNumber to show conversation with
     */
    private void setMessages(String phoneNumber) {
        // get all messages send by this phonenumber
        ChatDatabase db = ChatDatabase.getInstance(this.getApplicationContext());
        Cursor allMessages = db.selectOneConversations(phoneNumber);

        ChatArrayAdapter chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);

        // extract all resulting messages
        if(allMessages.moveToFirst()){
            do{
                String message = allMessages.getString(allMessages.getColumnIndex("message"));
                boolean in = allMessages.getInt(allMessages.getColumnIndex("inOut")) == 0;
                System.out.println(allMessages.getString(allMessages.getColumnIndex("message")));
                chatArrayAdapter.add(new ChatMessage(in, message));
            } while (allMessages.moveToNext());
        }
        allMessages.close();
        ListView messageView = findViewById(R.id.messageView);
        messageView.setAdapter(chatArrayAdapter);

        // scroll to the bottom of the list
        messageView.setSelection(chatArrayAdapter.getCount() - 1);
    }

    /**
     * Get message to be send and send it to all group members.
     */
    private class HandleSendGroupClick implements View.OnClickListener {
        public void onClick(View view) {
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            // todo: different name for phonenumber when in group?
            Cursor groupMembers = db.getGroupMembers(phoneNumber);
            // get typed text and send it to recipients
            EditText newMessageBox = findViewById(R.id.newMessage);
            String newMessage = newMessageBox.getText().toString();
            // reset edittext, since the message has been send.
            newMessageBox.setText("");

            if(groupMembers.moveToFirst()){
                do{
                    String memberPhoneNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                    String memberID = groupMembers.getString(groupMembers.getColumnIndex("groupID"));
                    String message = memberID + "]" + newMessage;
                    sendSMS(memberPhoneNumber, message);
                } while (groupMembers.moveToNext());
            }
            // not allowed to send an empty SMS.
            if (!newMessage.equals("")){
                System.out.println("inseeting with: " + phoneNumber);
                sendSMSGroup(phoneNumber, newMessage);
            }
        }
    }

    /**
     * Get message to be send and send it.
     */
    private class HandleSendClick implements View.OnClickListener {
        public void onClick(View view) {
            // get typed text and send it to recipients
            EditText newMessageBox = findViewById(R.id.newMessage);
            String newMessage = newMessageBox.getText().toString();
            // reset edittext, since the message has been send.
            newMessageBox.setText("");

            // not allowed to send an empty SMS.
            if (!newMessage.equals("")){
                sendSMS(phoneNumber, newMessage);
            }
        }
    }

    /**
     * Send an SMS.
     * @param phoneNumber number of recipient
     * @param message message to be send
     */
    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
        ChatDatabase db = ChatDatabase.getInstance(this.getApplicationContext());
        db.insert(formatNumberToE164(phoneNumber, "NL"), message, false);
        // refresh listview with the new message
        setMessages(phoneNumber);
    }

    public void sendSMSGroup(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
        ChatDatabase db = ChatDatabase.getInstance(this.getApplicationContext());
        db.insert(phoneNumber, message, false);
        // refresh listview with the new message
        setMessages(phoneNumber);
    }
}
