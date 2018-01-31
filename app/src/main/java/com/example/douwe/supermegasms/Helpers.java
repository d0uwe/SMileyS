package com.example.douwe.supermegasms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

/**
 * Created by Douwe on 1/28/18.
 *
 * This helpers class contains a variety of functions which are used by multiple other files. This
 * prevents having lots of double code.
 */

public class Helpers {
    /**
     * Given a phone number, get the name which it is used with in the contacts app.
     * @param context a context
     * @param phoneNumber the phone number the name is needed of.
     * @return a string containing the name of the person, or null if the number is not used in a
     * contact.
     */
    public String getContactName(Context context, String phoneNumber) {
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

    /**
     * Sends an SMS to a phone number.
     * @param phoneNumber phone number to send the message to
     * @param message the message
     */
    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    /**
     * Remove one of the members of a group
     * @param groupNumber my group number
     * @param phoneNumber phonenumber of the person to be removed
     */
    public void removeNumberFromGroup(String groupNumber, String phoneNumber, Context context){
        ChatDatabase db = ChatDatabase.getInstance(context);
        String groupID = db.getGroupMemberID(phoneNumber, groupNumber);
        System.out.println("Groupiddd of this person is:: "+ groupID);
        String removeString = groupID + "]" + "REMOVE" + "]" + "0";
        sendSMS(phoneNumber, removeString);


        Cursor groupMembers = db.getGroupMembers(groupNumber);
        if (groupMembers.moveToFirst()) {
            do{
                String sendToNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                String theirID = Integer.toString(groupMembers.getInt(groupMembers.getColumnIndex("groupID")));

                if (sendToNumber.equals(phoneNumber) && theirID.equals(groupID)){
                    continue;
                }
                removeString = theirID + "]" + "REMOVE" + "]" + phoneNumber;
                sendSMS(sendToNumber, removeString);
            } while (groupMembers.moveToNext());
        }
    }
}
