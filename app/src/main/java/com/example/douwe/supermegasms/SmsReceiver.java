package com.example.douwe.supermegasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import static android.telephony.PhoneNumberUtils.formatNumberToE164;
import static java.lang.Integer.parseInt;

/**
 * Created by Douwe on 1/10/18.
 */

public class SmsReceiver extends BroadcastReceiver {

    /**
     * Put every SMS received in the database. Then send out a broadcast to force refreshes.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            /* Get Messages */
            Object[] sms = (Object[]) intentExtras.get("pdus");

            for (int i = 0; i < sms.length; ++i) {
                /* Parse Each Message */
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String phoneNumber = formatNumberToE164(smsMessage.getOriginatingAddress(), "NL");
                String message = smsMessage.getMessageBody().toString();

                ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
                db.insert("335566", "hallooo", true);
                db.insert("4316134", "hallooo", true);

                db.insert("12341641", "hallooo", true);

                processSMS(context, message, phoneNumber);
            }
        }
    }


    private void processSMS(Context context, String message, String phoneNumber){
        ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
        String[] contents = message.split("]", 3);
        // check if it's an header only sms or not
        if(contents.length == 3){
            // check which header
            if (contents[1].equals("INV")) {
                int groupID = db.getNewGroup(contents[2]);
                sendSMS(phoneNumber, contents[0] + "]" + "INVOK]" + groupID);
                db.insertNumberInGroup(groupID, phoneNumber, parseInt(contents[0]));
            } else if (contents[1].equals("INVOK")) {
                updateMembers(contents[0], phoneNumber, contents[2], context);
                db.insertNumberInGroup(parseInt(contents[0]), phoneNumber, parseInt(contents[2]));
            } else if (contents[1].equals("ADD")){
                // todo: could crash
                contents = message.split("]", 4);

                db.insertNumberInGroup(parseInt(contents[0]), contents[2], parseInt(contents[3]));
            }
        } else if (contents.length == 2) {
            db.insert(contents[0], contents[1], true);
            sendMessage(context);
        } else {
            System.out.println("inserting: " + message);
            // process as normal message.

            System.out.println("inserting with number: " + phoneNumber + "and message: " + message);
            db.insert(phoneNumber, message, true);
            sendMessage(context);
        }
    }


    private void updateMembers(String myId, String phoneNumber, String theirID, Context context){
        ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
        Cursor groupMembers = db.getGroupMembers(myId);
        if(groupMembers.moveToFirst()){
            do{
                String memberPhoneNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                String memberID = groupMembers.getString(groupMembers.getColumnIndex("groupID"));
                String message = memberID + "]ADD]" + phoneNumber + "]" + theirID;
                sendSMS(memberPhoneNumber, message);
            } while (groupMembers.moveToNext());
        }

    }


    /**
     * Sends a broadcast out when a message is received to refresh listviews with messages
     * @param context current context.
     */
    private void sendMessage(Context context) {
        Intent intent = new Intent("message received");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Send an SMS.
     * @param phoneNumber number of recipient
     * @param message message to be send
     */
    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
}