package com.example.douwe.supermegasms;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class GroupSettingsActivity extends AppCompatActivity {
    private ArrayList<String> phoneNumbers;
    private ArrayList<String> memberNames;
    private String groupNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);
        groupNumber = getIntent().getExtras().getString("groupID");

        setListView(groupNumber);
        ((Button) findViewById(R.id.addMember)).setOnClickListener(new HandleAddClick());
        ((ListView) findViewById(R.id.groupMembers)).setOnItemLongClickListener(new LongHandleClick());
    }

    public void setListView(String groupNumber) {
        ListView listView = findViewById(R.id.groupMembers);
        ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
        Cursor groupMembers = db.getGroupMembers(groupNumber);
        phoneNumbers = new ArrayList<>();
        memberNames = new ArrayList<>();
        Helpers helper = new Helpers();
        if(groupMembers.moveToFirst()) {
            do {
                String phoneNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                phoneNumbers.add(phoneNumber);
                String contactName = helper.getContactName(getApplicationContext(), phoneNumber);
                if (contactName != null && !contactName.isEmpty()) {
                    memberNames.add(contactName);
                } else {
                    memberNames.add(phoneNumber);
                }
            } while (groupMembers.moveToNext());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.my_simple_list_item, R.id.text, memberNames);
        listView.setAdapter(adapter);
    }

    /**
     * Select person to add to the group
     */
    private class HandleAddClick implements View.OnClickListener {
        public void onClick(View view) {
            Intent i= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(i, 2000);
        }
    }

    /**
     * Select a person to remove from the group
     */
    public class LongHandleClick implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            String phoneNumber = phoneNumbers.get(i);
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            db.removeNumberFromGroup(groupNumber, phoneNumber);

            return true;
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
                            ChatDatabase db= ChatDatabase.getInstance(getApplicationContext());

                            sendSMS(cNumber,  groupNumber + "]" + "INV]" + db.getGroupName(groupNumber));
                        }
                    }
                }
                break;
        }
    }

    /**
     * Send an SMS out.
     * @param phoneNumber Number to send the SMS to.
     * @param message Message to be send to the phone number.
     */
    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
