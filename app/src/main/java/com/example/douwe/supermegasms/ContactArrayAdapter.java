package com.example.douwe.supermegasms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
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
        ContactRow chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.contact_row, parent, false);

        chatText = row.findViewById(R.id.message);
        chatText.setText(chatMessageObj.message);
        chatText = row.findViewById(R.id.number);
        if (chatMessageObj.name != null && !chatMessageObj.name.isEmpty()){
            chatText.setText(chatMessageObj.name);
        } else {
            chatText.setText(chatMessageObj.number);
        }

        chatText = (TextView) row.findViewById(R.id.date);
        chatText.setText("10:42");
        // TODO: set date.

        return row;
    }
}