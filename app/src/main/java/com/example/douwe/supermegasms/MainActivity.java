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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static android.telephony.PhoneNumberUtils.formatNumberToE164;


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

    @Override
    public void onResume(){
        super.onResume();
        ListView convView = findViewById(R.id.contactView);
        setContactListview(convView);
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
            case R.id.solo_chat:
                Intent i= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, 2000);
                break;
            case R.id.group_chat:
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
        Cursor allConvs = db.selectAllConversations();
        contactArrayAdapter = new ContactArrayAdapter(getApplicationContext(), R.layout.contact_row);

        if(allConvs.moveToFirst()){
            do{
                String id = allConvs.getString(allConvs.getColumnIndex("id"));
                boolean group = allConvs.getInt(allConvs.getColumnIndex("groupBool")) != 0;

                String lastMessage = getLastMessage(id, db);
                int lastDate = getLastDate(id, db);
                if (group){
                    String groupName = db.getGroupName(id);
                    contactArrayAdapter.add(new ContactRow(lastMessage, id, lastDate, groupName, true));
                } else {
                    Helpers helpers = new Helpers();
                    contactArrayAdapter.add(new ContactRow(lastMessage, id, lastDate, helpers.getContactName(getApplicationContext(), id), false));
                }

            } while (allConvs.moveToNext());
        }
        allConvs.close();

//        Cursor allGroups = db.selectAllGroupConversations();
//        if(allGroups.moveToFirst()){
//            do{
//                String id = allGroups.getString(allGroups.getColumnIndex("myID"));
//                String lastMessage = getLastMessage(id, db);
//
//                System.out.println("id iss: " + id);
//                contactArrayAdapter.add(new ContactRow(lastMessage, id, "10.42", allGroups.getString(allGroups.getColumnIndex("groupName")), true));
//
//            } while (allGroups.moveToNext());
//        }
//        allGroups.close();


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
        if(allMessages.moveToLast()) {
            return allMessages.getString(allMessages.getColumnIndex("message"));
        } else {
            return "";
        }
    }

    /**
     * Get the date of the latest message in a conversation
     * @param id phoneNumber of which we would like to ge the message
     * @param db ChatDataBase containing the messages
     * @return the last message in this conversation.
     */
    private int getLastDate(String id, ChatDatabase db){
        Cursor allMessages = db.selectOneConversations(id);
        if(allMessages.moveToLast()) {
            return allMessages.getInt(allMessages.getColumnIndex("date"));
        } else {
            return 0;
        }
    }

    private class HandleContactClick implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
            ListView convView = findViewById(R.id.contactView);
            ContactRow clickedRow = (ContactRow)convView.getItemAtPosition(i);
            intent.putExtra("phoneNumber", clickedRow.number);
            intent.putExtra("groupBoolean", clickedRow.group);
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

                            Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                            intent.putExtra("phoneNumber", formatNumberToE164(cNumber, "NL"));
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
