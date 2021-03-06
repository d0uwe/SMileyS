package com.example.douwe.supermegasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import static android.telephony.PhoneNumberUtils.formatNumberToE164;
import static java.lang.Integer.parseInt;

/**
 * Created by Douwe on 1/10/18.
 *
 * This class is activated when an SMS comes in and processes it in the right way. It can reply
 * in the right way to headers from the app, or just place it in the database.
 */
public class SmsReceiver extends BroadcastReceiver {
    /**
     * Extract the message from incoming SMS messages and send it to the processing function
     * @param context Context.
     * @param intent Intent which contains the SMS message
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            // get messages
            Object[] sms = (Object[]) intentExtras.get("pdus");

            for (int i = 0; i < sms.length; ++i) {
                // parse each message
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                String phoneNumber = formatNumberToE164(smsMessage.getOriginatingAddress(), "NL");
                String message = smsMessage.getMessageBody().toString();

                processSMS(context, message, phoneNumber);
            }
        }
    }

    /**
     * Processes an incoming SMS, either react upon it when it contains a header or place it in the
     * right conversation in the database
     * @param context A context
     * @param message Message received
     * @param phoneNumber Phonenumber of which the message came from
     */
    private void processSMS(Context context, String message, String phoneNumber) {
        ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
        String[] contents = message.split("]", 3);

        // check if it's an header only sms or not
        if (contents.length == 3) {
            checkHeader(contents, phoneNumber, context, message);

        } else if (contents.length == 2) {
            // message belongs to a group, place it in there.
            if (db.numberInGroup(phoneNumber, contents[0])) {
                db.insertGroup(contents[0], phoneNumber, contents[1], true);
                sendBroadcast(context);
            }

        } else {
            // process as normal message.
            if (!db.userBlocked(phoneNumber)) {
                db.insert(phoneNumber, message, true);
                sendBroadcast(context);
            }
        }
    }

    /**
     * Check what kind of message the header is telling, whether it's an invite, or about adding
     * someone.
     * @param contents contents of the message
     * @param phoneNumber phone number which sent the message
     * @param context a context
     * @param message the entire message
     */
    private void checkHeader(String [] contents, String phoneNumber, Context context, String message) {
        ChatDatabase db = ChatDatabase.getInstance(context);
        Helpers helper = new Helpers();
        switch (contents[1]) {
            case "INV":
                // process invite
                if (!db.userBlocked(phoneNumber)) {
                    int groupID = db.getNewGroup(contents[2]);
                    helper.sendSMS(phoneNumber, contents[0] + "]" + "INVOK]" + groupID);
                    db.insertNumberInGroup(groupID, phoneNumber, parseInt(contents[0]));
                    sendBroadcast(context);
                }

                break;
            case "INVOK":
                // process reply on an invite
                updateMembers(contents[0], phoneNumber, contents[2], context);
                db.insertNumberInGroup(parseInt(contents[0]), phoneNumber, parseInt(contents[2]));

                break;
            case "ADD":
                // add someone to a group
                // todo: could crash
                if (db.numberInGroup(phoneNumber, contents[0])) {
                    contents = message.split("]", 4);
                    db.insertNumberInGroup(parseInt(contents[0]), contents[2], parseInt(contents[3]));
                }

                break;
            case "REMOVE":
                // remove someone from a group
                if (db.numberInGroup(phoneNumber, contents[0])) {
                    processRemoval(contents, db, phoneNumber);
                    sendBroadcast(context);
                }

                break;
            case "LEAVE":
                // someone wants to leave, remove them.
                processLeave(contents, db, phoneNumber);
                sendBroadcast(context);
                break;
        }
    }

    /**
     * A person has been removed, if this is the person all members are removed from the group in
     * the database on this phone so no messages are send out anymore. If it someone else, his
     * number is removed from the database
     * @param contents An array containing their and our id of the groupchats
     * @param db A ChatDatabase instance
     * @param phoneNumber Phone number of the person to be removed.
     */
    private void processRemoval(String [] contents, ChatDatabase db, String phoneNumber) {
        if (contents[2].equals("0")) {
            // this person has been removed from the group
            db.insertGroup(contents[0], phoneNumber,"You've been removed from this group.", true);
            db.removeMeFromGroup(contents[0]);
        } else {
            // another member of the group has been removed
            String theirID = db.getGroupMemberID(formatNumberToE164(contents[2], "NL"), contents[0]);
            db.removeNumberFromGroup2(contents[0], theirID, contents[2]);
            db.insertGroup(contents[0], phoneNumber, contents[2] +
                    " has been removed from the group", true);
        }
    }

    /**
     * A member of the group wants to leave, remove their number from the database.
     * @param contents Our and their id are stored in this array
     * @param db A ChatDatabase instance
     * @param phoneNumber The phone number of the person who wants to leave
     */
    private void processLeave(String [] contents, ChatDatabase db, String phoneNumber) {
        // another member of the group has left, remove them from the db.
        String theirID = db.getGroupMemberID(formatNumberToE164(phoneNumber, "NL"), contents[0]);
        db.removeNumberFromGroup2(contents[0], theirID, phoneNumber);
        db.insertGroup(contents[0], phoneNumber, phoneNumber + " has left the group", true);
    }

    /**
     * Update the members of a group about adding someone to a group.
     * @param myId My id for this group
     * @param phoneNumber Phone number which is added to the group
     * @param theirID The id this phone number is using as identifier for the group
     * @param context A context
     */
    private void updateMembers(String myId, String phoneNumber, String theirID, Context context) {
        ChatDatabase db = ChatDatabase.getInstance(context.getApplicationContext());
        Helpers helper = new Helpers();
        Cursor groupMembers = db.getGroupMembers(myId);
        // Go through all current members of the group and update them and the new user about their
        // existence.
        if (groupMembers.moveToFirst()) {
            do {
                // message to existing member
                String memberPhoneNumber = groupMembers.getString(groupMembers.getColumnIndex("phoneNumber"));
                String memberID = groupMembers.getString(groupMembers.getColumnIndex("groupID"));
                String message = memberID + "]ADD]" + phoneNumber + "]" + theirID;
                helper.sendSMS(memberPhoneNumber, message);

                // message to new member that the other members exist
                message = theirID + "]ADD]" + memberPhoneNumber + "]" + memberID;
                helper.sendSMS(phoneNumber, message);
            } while (groupMembers.moveToNext());
        }
    }

    /**
     * Sends a broadcast out when a message is received to refresh listviews with messages
     * @param context current context.
     */
    private void sendBroadcast(Context context) {
        Intent intent = new Intent("message received");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}