package com.example.douwe.supermegasms;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.telephony.PhoneNumberUtils.formatNumberToE164;

public class BlockActivity extends AppCompatActivity {
    ArrayList<String> phoneNumbers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        setListView();
        ((Button) findViewById(R.id.addMember)).setOnClickListener(new HandleAddClick());
        ((ListView) findViewById(R.id.blockedUsers)).setOnItemLongClickListener(new LongHandleClick());
    }

    /**
     * Get all blocked numbers from the database and display the contact name if available,
     * otherwise display the phone number
     */
    public void setListView() {
        ListView listView = findViewById(R.id.blockedUsers);
        ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
        Cursor blockedUsers = db.getBlockedUsers();
        phoneNumbers = new ArrayList<>();
        ArrayList<String> memberNames = new ArrayList<>();
        Helpers helper = new Helpers();
        if (blockedUsers.moveToFirst()) {
            do {
                String phoneNumber = blockedUsers.getString(blockedUsers.getColumnIndex("phoneNumber"));
                phoneNumbers.add(phoneNumber);
                String contactName = helper.getContactName(getApplicationContext(), phoneNumber);
                if (contactName != null && !contactName.isEmpty()) {
                    memberNames.add(contactName);
                } else {
                    memberNames.add(phoneNumber);
                }
            } while (blockedUsers.moveToNext());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.my_simple_list_item, R.id.text, memberNames);
        listView.setAdapter(adapter);
    }

    /**
     * Select person to block
     */
    private class HandleAddClick implements View.OnClickListener {
        public void onClick(View view) {
            Intent i= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(i, 2000);
        }
    }

    /**
     * Select a person to remove from the group and update the database.
     */
    public class LongHandleClick implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            String phoneNumber = phoneNumbers.get(i);
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            db.removeBlockUser(phoneNumber);
            setListView();
            return true;
        }
    }


    /**
     * This function is called when an contact has been selected.
     * @param reqCode Request code used
     * @param resultCode Did the request succeed?
     * @param data data which came with the result
     */
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
                            db.insertBlockUser(formatNumberToE164(cNumber, "NL"));
                            setListView();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "contact has no phone number", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
                break;
        }
    }
}
