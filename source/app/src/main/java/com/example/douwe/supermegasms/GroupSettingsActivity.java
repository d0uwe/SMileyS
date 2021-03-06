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

import java.util.ArrayList;

/**
 * Created by Douwe on 1/16/18.
 *
 * This activity allows adding a user to a group and gives an overview of the members of the group.
 * By long pressing a user he can be deleted from the group.
 */
public class GroupSettingsActivity extends AppCompatActivity {
    private ArrayList<String> phoneNumbers;
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

    /**
     * Get the list of group members and display their name in the list view if the number is in
     * the contacts list, otherwise display the phone number.
     * @param groupNumber The group number of which we want to show messages
     */
    public void setListView(String groupNumber) {
        ListView listView = findViewById(R.id.groupMembers);
        ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
        ArrayList<String> memberNames = new ArrayList<>();
        phoneNumbers = new ArrayList<>();
        Helpers helper = new Helpers();

        // loop through all members
        Cursor groupMembers = db.getGroupMembers(groupNumber);
        if (groupMembers.moveToFirst()) {
            do {
                String phoneNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                phoneNumbers.add(phoneNumber);
                String contactName = helper.getContactName(getApplicationContext(), phoneNumber);
                // check if the contact name is available
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
     * Select a person to remove from the group and update the database.
     */
    public class LongHandleClick implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            String phoneNumber = phoneNumbers.get(i);
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            Helpers helper = new Helpers();
            // process delete
            helper.removeNumberFromGroup(groupNumber, phoneNumber, getApplicationContext());
            // show result in the chat
            db.insertGroup(groupNumber, phoneNumber, phoneNumber +
                    " has been removed from the group by you.", false);
            setListView(groupNumber);
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

                            Helpers helper = new Helpers();
                            helper.sendSMS(cNumber,  groupNumber + "]" + "INV]" + db.getGroupName(groupNumber));
                        }
                    }
                }
                break;
        }
    }
}
