package com.example.douwe.supermegasms;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    ArrayList<String> conversations;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnSendSMS = (Button) findViewById(R.id.btnsms);
        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSMS("+31640935823", "Hi You got a message!");
            }
        });

        findViewById(R.id.contact_select).setOnClickListener(new HandlePickContactClick());

        ListView convView = findViewById(R.id.contactView);
        setContactListview(convView);
        convView.setOnItemClickListener(new HandleContactClick());

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
            ListView convView = findViewById(R.id.contactView);
            setContactListview(convView);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSortNewest:
                    System.out.println("first");
                Intent i=
                        new Intent(Intent.ACTION_PICK,
                                ContactsContract.Contacts.CONTENT_URI);

                startActivityForResult(i, 2000);

                break;
            case R.id.menuSortRating:
                System.out.println("secondo");
                Intent intent = new Intent(MainActivity.this, SelectContactsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    /**
     * Get all contacts we have a conversation with and put them in a list.
     * @param convView
     */
    private void setContactListview(ListView convView) {
        ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
        Cursor all_convs = db.selectAllConversations();
        conversations = new ArrayList<>();
        if(all_convs.moveToFirst()){
            do{
                String id = all_convs.getString(all_convs.getColumnIndex("id"));
                System.out.println(id);
                conversations.add(id);
            } while (all_convs.moveToNext());
        }
        ArrayAdapter<String> list = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, conversations);
        convView.setAdapter(list);
    }

    /**
     * Send an SMS.
     * @param phoneNumber number of recipient
     * @param message message to be send
     */
    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    private class HandlePickContactClick implements View.OnClickListener {
        public void onClick(View view) {
            Intent i=
                    new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);

            startActivityForResult(i, 1337);
        }
    }

    private class HandleContactClick implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
            ListView convView = findViewById(R.id.contactView);
            intent.putExtra("phoneNumber", convView.getItemAtPosition(i).toString());
            startActivity(intent);
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
            case 2000:
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

                            Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                            intent.putExtra("phoneNumber", cNumber);
                            startActivity(intent);
                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        System.out.println("name is:" + name);


                    }
                }

                break;
        }
    }
}
