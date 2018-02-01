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

    /**
     * Add an element to the list.
     * @param object element to be added
     */
    @Override
    public void add(ContactRow object) {
        contactList.add(object);
        super.add(object);
    }

    public ContactArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    /**
     * Get the size of the contacts list.
     * @return integer containing the size
     */
    public int getCount() {
        return this.contactList.size();
    }

    /**
     * Get an item at a certain position in the list.
     * @param index index of the item requested
     * @return the object at this index.
     */
    public ContactRow getItem(int index) {
        return this.contactList.get(index);
    }

    /**
     * Get the view for an item in the list
     * @param position position of item of which the view is needed
     * @param convertView a convertView
     * @param parent the parent
     * @return The inflated object
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactRow contactRow = getItem(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.contact_row, parent, false);

        // set information
        chatText = row.findViewById(R.id.message);
        chatText.setText(contactRow.message);
        chatText = row.findViewById(R.id.number);
        if (contactRow.name != null && !contactRow.name.isEmpty()){
            chatText.setText(contactRow.name);
        } else {
            chatText.setText(contactRow.number);
        }

        // set date
        chatText = row.findViewById(R.id.date);
        String dateText = getDate(contactRow);
        if (contactRow.date != 0){
            chatText.setText(dateText);
        } else {
            chatText.setText("");
        }
        return row;
    }

    /**
     * Get the date of the last message in a conversation. This is either the date or the time
     * the date is used when the message was received over 24 hours ago, otherwise the time is
     * shown.
     * @param contactRow contactRow object containing the information needed.
     * @return a string containing the right date format
     */
    private String getDate(ContactRow contactRow) {
        String dateString;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(contactRow.date);
        Calendar rightNow = Calendar.getInstance();

        long difference = rightNow.getTimeInMillis() - c.getTimeInMillis();
        int secondDifference = (int)(difference / 1000);

        // show the time if it's less than a day ago, otherwise the date
        if (secondDifference > (24 * 60 * 60)) {
            dateString = Integer.toString(c.get(c.MONTH) + 1) + Integer.toString(c.get(c.DAY_OF_MONTH));
        } else {
            // add a zero to prevent times like 15:6
            String minuteString = Integer.toString(c.get(c.MINUTE));
            if ((c.get(c.MINUTE)) < 10) {
                minuteString = "0" + minuteString;
            }
            dateString = c.get(c.HOUR_OF_DAY) + ":" + minuteString;
        }
        return dateString;
    }
}