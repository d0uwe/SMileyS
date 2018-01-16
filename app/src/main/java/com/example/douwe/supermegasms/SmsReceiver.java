package com.example.douwe.supermegasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by douwe on 1/10/18.
 */

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            /* Get Messages */
            Object[] sms = (Object[]) intentExtras.get("pdus");

            for (int i = 0; i < sms.length; ++i) {
                /* Parse Each Message */
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String phone = smsMessage.getOriginatingAddress();
                String message = smsMessage.getMessageBody().toString();

                Toast.makeText(context, phone + ": " + message, Toast.LENGTH_SHORT).show();
                ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
                db.insert(phone, message);
                db.insert("335566", "hallooo");
                db.insert("4316134", "hallooo");

                db.insert("12341641", "hallooo");

                Cursor all_messages = db.selectAll();
                if(all_messages.moveToFirst()){
                    do{
                        System.out.println(all_messages.getString(all_messages.getColumnIndex("message")));
                    } while (all_messages.moveToNext());
                }
                all_messages = db.selectAllConversations();
                if(all_messages.moveToFirst()){
                    do{
                        System.out.println(all_messages.getString(all_messages.getColumnIndex("id")));
                    } while (all_messages.moveToNext());
                }

                System.out.println(db.selectAll().toString());
            }
        }
    }
}