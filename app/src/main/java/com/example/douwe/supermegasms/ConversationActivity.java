package com.example.douwe.supermegasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import static android.telephony.PhoneNumberUtils.formatNumberToE164;

/**
 * Created by Douwe on 1/16/18.
 *
 * This activity gives an overview of the messages send in either an individual or group
 * conversation. It also allows sending a new message in this conversation.
 */
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

        // set what the send button should do
        if (inGroup){
            findViewById(R.id.sendButton).setOnClickListener(new HandleSendGroupClick());
        } else{
            findViewById(R.id.sendButton).setOnClickListener(new HandleSendClick());
        }

        // register to broadcasts to refresh listview when there is a new message
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message received"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(ConversationActivity.this, GroupSettingsActivity.class);
                intent.putExtra("groupID", phoneNumber);
                startActivity(intent);
                break;

            case R.id.leave:
                removeMe();
                break;
        }
        return true;
    }

    /**
     * Send a message to the other group members which indicates you leave and then remove
     * the contacts from your database.
     */
    private void removeMe() {
        ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
        // go through all group members
        Cursor groupMembers = db.getGroupMembers(phoneNumber);
        if (groupMembers.moveToFirst()) {
            do {
                String sendToNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                String theirID = Integer.toString(groupMembers.getInt(groupMembers.getColumnIndex("groupID")));
                String removeString = theirID + "]" + "LEAVE" + "]" + phoneNumber;
                sendSMSGroup(sendToNumber, removeString);
            } while (groupMembers.moveToNext());
        }
        db.insertGroup(phoneNumber, "0000", "You left this group", false);
        db.removeMeFromGroup(phoneNumber);
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
     * @param phoneNumber phone number to show conversation with
     */
    private void setMessages(String phoneNumber) {
        // get all messages send by this phone number
        ChatDatabase db = ChatDatabase.getInstance(this.getApplicationContext());
        Cursor allMessages = db.selectOneConversations(phoneNumber);

        ChatArrayAdapter chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        if (inGroup) {
            chatArrayAdapter.setGroup(true);
        }

        // extract all resulting messages
        if (allMessages.moveToFirst()) {
            do {
                String message = allMessages.getString(allMessages.getColumnIndex("message"));
                String sender = allMessages.getString(allMessages.getColumnIndex("sender"));
                boolean in = allMessages.getInt(allMessages.getColumnIndex("inOut")) == 0;
                int date = allMessages.getInt(allMessages.getColumnIndex("date"));
                chatArrayAdapter.add(new ChatMessage(in, message, sender, date));
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

            // not allowed to send an empty SMS.
            if (newMessage.equals("")){
                return;
            }
            if (groupMembers.moveToFirst()) {
                do {
                    String memberPhoneNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                    String memberID = groupMembers.getString(groupMembers.getColumnIndex("groupID"));
                    String message = memberID + "]" + newMessage;
                    sendSMSGroup(memberPhoneNumber, message);
                } while (groupMembers.moveToNext());
            }

            db.insert(phoneNumber, newMessage, false);
            setMessages(phoneNumber);
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
            // reset the text box, since the message has been send.
            newMessageBox.setText("");

            // not allowed to send an empty SMS.
            if (!newMessage.equals("")) {
                sendSMS(phoneNumber, newMessage);
            }
        }
    }

    /**
     * Send an SMS and inserts the message in the database.
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
    }
}
