package com.example.douwe.supermegasms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        String phoneNumber = getIntent().getExtras().getString("phoneNumber");

        setMessages(phoneNumber);
    }

    private void setMessages(String phoneNumber) {

    }
}
