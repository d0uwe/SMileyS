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

                String phoneNumber = smsMessage.getOriginatingAddress();
                String message = smsMessage.getMessageBody().toString();

                ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
                db.insert(formatNumberToE164(phoneNumber, "NL"), message, true);
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

        if (contents[1].equals("INV")) {
            int groupID = db.getNewGroup(contents[2]);
            sendSMS(phoneNumber, contents[0] + "]" + "INVOK]" + groupID);
        } else if (contents[1].equals("INVOK")) {

        }
        else {
            sendMessage(context);
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