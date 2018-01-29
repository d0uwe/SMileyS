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

        chatText = (TextView) row.findViewById(R.id.date);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(contactRow.date);
        if (contactRow.date != 0){
            // TODO move string making to helper.
            chatText.setText(c.get(c.HOUR) + ":" + c.get(c.MINUTE));
        } else {
            chatText.setText("");
        }

        return row;
    }
}