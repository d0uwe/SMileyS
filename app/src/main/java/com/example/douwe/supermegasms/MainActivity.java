package com.example.douwe.supermegasms;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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

import java.lang.reflect.Array;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    ContactArrayAdapter contactArrayAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Intent i= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

                startActivityForResult(i, 2000);

                break;
            case R.id.menuSortRating:
                Intent intent = new Intent(MainActivity.this, SelectContactsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    /**
     * Get all contacts we have a conversation with and put them in a list.
     * @param convView the listview in which the adapter should be set.
     */
    private void setContactListview(ListView convView) {
        ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
        Cursor all_convs = db.selectAllConversations();
        contactArrayAdapter = new ContactArrayAdapter(getApplicationContext(), R.layout.contact_row);

        if(all_convs.moveToFirst()){
            do{
                String id = all_convs.getString(all_convs.getColumnIndex("id"));
                String lastMessage = getLastMessage(id, db);
                System.out.println(id);
                contactArrayAdapter.add(new ContactRow(lastMessage, id, "10.42", getContactName(getApplicationContext(), id)));

            } while (all_convs.moveToNext());
        }
        all_convs.close();
        convView.setAdapter(contactArrayAdapter);
    }

    /**
     * Get the latest message in a conversation
     * @param id phoneNumber of which we would like to ge the message
     * @param db ChatDataBase containing the messages
     * @return the last message in this conversation.
     */
    private String getLastMessage(String id, ChatDatabase db){
        Cursor allMessages = db.selectOneConversations(id);
        allMessages.moveToLast();
        return allMessages.getString(allMessages.getColumnIndex("message"));
    }

    private class HandleContactClick implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
            ListView convView = findViewById(R.id.contactView);
            ContactRow clickedRow = (ContactRow)convView.getItemAtPosition(i);
            intent.putExtra("phoneNumber", clickedRow.number);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
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
