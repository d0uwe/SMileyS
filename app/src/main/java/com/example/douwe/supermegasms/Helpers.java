package com.example.douwe.supermegasms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

/**
 * Created by douwe on 1/28/18.
 */

public class Helpers {
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
