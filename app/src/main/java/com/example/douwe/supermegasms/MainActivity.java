package com.example.douwe.supermegasms;

import android.app.Activity;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private AppDatabase appDatabase;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnSendSMS = (Button) findViewById(R.id.btnsms);
        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSMS("+31640935823", "Hi You got a message!");
           /*here i can send message to emulator 5556. In Real device
            *you can change number*/
            }
        });

        findViewById(R.id.contact_select).setOnClickListener(new HandleClick());

        appDatabase = AppDatabase.getDatabase(this.getApplication());


    }

//Sends an SMS message to another device

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
    private void pickContact(View v) {
        Intent i=
                new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);

        startActivityForResult(i, 1337);
    }

    private class HandleClick implements View.OnClickListener {
        public void onClick(View view) {
            Intent i=
                    new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);

            startActivityForResult(i, 1337);
            System.out.println(i.getData());

        }
    }

    //code
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (1337):
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {


                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            System.out.println("number is:" + cNumber);
                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        System.out.println("name is:" + name);

                        TextView textview = findViewById(R.id.textView);
                        textview.setText(name);


                    }
                }
                break;
        }
    }

    @Entity(tableName = "users")
    public class User {

        @PrimaryKey
        @ColumnInfo(name = "userid")
        private String mId;

        @ColumnInfo(name = "username")
        private String mUserName;



        @Ignore
        public User(String userName) {
            mId = UUID.randomUUID().toString();
            mUserName = userName;
        }

        public User(String id, String userName, Date date) {
            this.mId = id;
            this.mUserName = userName;
        }
    }
}
