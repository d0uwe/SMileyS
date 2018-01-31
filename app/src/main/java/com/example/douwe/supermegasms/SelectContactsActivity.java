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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.telephony.PhoneNumberUtils.formatNumber;
import static android.telephony.PhoneNumberUtils.formatNumberToE164;

public class SelectContactsActivity extends AppCompatActivity {
    ArrayList<String> groupContacts = new ArrayList<>();
    ArrayList<String> phoneNumbers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        findViewById(R.id.anotherContact).setOnClickListener(new HandlePickContactClick());

        // pick first contact
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i, 1337);

        // create the group
        Button doneButton = findViewById(R.id.finish);
        doneButton.setOnClickListener(new HandleFinishClick());
    }



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

                            // convert number to this one format, to always recognize it in the db.
                            phoneNumbers.add(formatNumberToE164(cNumber, "NL"));
                            ArrayAdapter<String> list = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupContacts);
                            ListView groupView = findViewById(R.id.groupListView);
                            groupView.setAdapter(list);

                            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            groupContacts.add(name);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "contact has no phone number", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
                break;
        }
    }

    private class HandlePickContactClick implements View.OnClickListener {
        public void onClick(View view) {
            Intent i=
                    new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(i, 1337);
        }
    }

    private class HandleFinishClick implements View.OnClickListener {
        public void onClick(View view) {
            ChatDatabase db = ChatDatabase.getInstance(getApplicationContext());
            EditText groupNameEditText = findViewById(R.id.groupName);
            String groupName = groupNameEditText.getText().toString();
            if (groupName.equals("")){
                Toast toast = Toast.makeText(getApplicationContext(), "Enter a groupname", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            int groupID = db.getNewGroup(groupName);
            for(int i = 0; i < phoneNumbers.size(); i++) {
                Helpers helper = new Helpers();
                helper.sendSMS(phoneNumbers.get(i),  Integer.toString(groupID) + "]" + "INV]" + groupName);
            }
            finish();
        }
    }
}
