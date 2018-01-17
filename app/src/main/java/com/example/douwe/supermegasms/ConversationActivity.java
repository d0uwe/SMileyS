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

public class ConversationActivity extends AppCompatActivity {
    String phoneNumber = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get who we're conversing with
        setContentView(R.layout.activity_conversation);
        phoneNumber = getIntent().getExtras().getString("phoneNumber");
        setMessages(phoneNumber);

        findViewById(R.id.sendButton).setOnClickListener(new HandleSendClick());

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
        ArrayList<String> messages = new ArrayList<>();
        // get all messages send by this phonenumber
        ChatDatabase db = ChatDatabase.getInstance(this.getApplicationContext());
        Cursor allMessages = db.selectOneConversations(phoneNumber);

        // extract all resulting messages
        if(allMessages.moveToFirst()){
            do{
                messages.add(allMessages.getString(allMessages.getColumnIndex("message")));
                System.out.println(allMessages.getString(allMessages.getColumnIndex("message")));
            } while (allMessages.moveToNext());
        }
        // convert arraylist to arrayadapter and set it
        ArrayAdapter<String> messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        ListView messageView = findViewById(R.id.messageView);
        messageView.setAdapter(messageAdapter);

        // scroll to the bottom of the list
        messageView.setSelection(messageAdapter.getCount() - 1);
    }

    /**
     * Get message to be send and send it.
     */
    private class HandleSendClick implements View.OnClickListener {
        public void onClick(View view) {
            // get typed text and send it to recipients
            EditText newMessageBox = findViewById(R.id.newMessage);
            String newMessage = newMessageBox.getText().toString();

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
        db.insert(phoneNumber, message, false);
    }
}
