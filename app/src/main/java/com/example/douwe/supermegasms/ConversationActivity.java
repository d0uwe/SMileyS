package com.example.douwe.supermegasms;

import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {
    String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        phoneNumber = getIntent().getExtras().getString("phoneNumber");

        setMessages(phoneNumber);

        Button sendButt = findViewById(R.id.sendButton);
        sendButt.setOnClickListener(new HandleSendClick());
    }

    private void setMessages(String phoneNumber) {
        ArrayList<String> messages = new ArrayList<>();
        ChatDatabase db = ChatDatabase.getInstance(this.getApplicationContext());
        Cursor allMessages = db.selectOneConversations(phoneNumber);

        if(allMessages.moveToFirst()){
            do{
                messages.add(allMessages.getString(allMessages.getColumnIndex("message")));
                System.out.println(allMessages.getString(allMessages.getColumnIndex("message")));
            } while (allMessages.moveToNext());
        }
        ArrayAdapter<String> list = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        ListView messageView = findViewById(R.id.messageView);
        messageView.setAdapter(list);
    }

    private class HandleSendClick implements View.OnClickListener {
        public void onClick(View view) {
            EditText newMessageBox = findViewById(R.id.newMessage);
            String newMessage = newMessageBox.getText().toString();
            sendSMS(phoneNumber, newMessage);

        }
    }
    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

}
