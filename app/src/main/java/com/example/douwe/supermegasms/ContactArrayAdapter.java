package com.example.douwe.supermegasms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Douwe.
 *
 * This array adapter sets the listview containing all conversations which are in the database.
 * It shows the contact / groupname, last message and time.
 */
class ContactArrayAdapter extends ArrayAdapter<ContactRow> {

    private TextView chatText;
    private List<ContactRow> contactList = new ArrayList<>();
    private Context context;

    @Override
    public void add(ContactRow object) {
        contactList.add(object);
        super.add(object);
    }

    public ContactArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.contactList.size();
    }

    public ContactRow getItem(int index) {
        return this.contactList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ContactRow contactRow = getItem(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.contact_row, parent, false);

        chatText = row.findViewById(R.id.message);
        chatText.setText(contactRow.message);
        chatText = row.findViewById(R.id.number);
        if (contactRow.name != null && !contactRow.name.isEmpty()){
            chatText.setText(contactRow.name);
        } else {
            chatText.setText(contactRow.number);
        }

        chatText = row.findViewById(R.id.date);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(contactRow.date);
        int minutes = c.get(c.MINUTE);
        String minuteString = Integer.toString(minutes);
        if (minutes < 10) {
            minuteString = "0" + Integer.toString(minutes);
        }
        if (contactRow.date != 0){
            // TODO move string making to helper.
            chatText.setText(c.get(c.HOUR) + ":" + minuteString);
        } else {
            chatText.setText("");
        }

        return row;
    }
}