package com.example.douwe.supermegasms;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 * Created by Douwe on 1/16/18.
 *
 * This database contains all information about messages, which groups there are and which users
 * these groups have. There are many functions to alter this data.
 */
public class ChatDatabase extends SQLiteOpenHelper {
    static ChatDatabase instance;
    public ChatDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * Initializes all tables.
     * @param db database instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, ID TEXT, sender TEXT, message TEXT, inOut BOOL, date INT)");
        db.execSQL("create table conversations (_id INTEGER PRIMARY KEY AUTOINCREMENT, id TEXT, groupBool BOOL, lastDate INT)");
        db.execSQL("create table groupNames (_id INTEGER PRIMARY KEY AUTOINCREMENT, myID INT, groupName TEXT)");
        db.execSQL("create table groups (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, groupID INT, myID INT)");
        db.execSQL("create table blockedUsers (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT)");

    }

    /**
     * Create a fresh database instance.
     * @param db database instance
     * @param i version
     * @param i1 new version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + "messages");
        db.execSQL("DROP TABLE IF EXISTS " + "conversations");
        db.execSQL("DROP TABLE IF EXISTS " + "groupNames");
        db.execSQL("DROP TABLE IF EXISTS " + "groups");

        onCreate(db);
    }

    /**
     * Return an instance of the database if available, otherwise create one.
     * @param context a context
     * @return a ChatDatabase instance
     */
    public static ChatDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new ChatDatabase(context, "messages", null, 1);
        }
        return instance;
    }

    /**
     * Insert a message of an individual conversation into the database.
     * @param phoneNumber phone number the message was sent to / coming from
     * @param message the actual message
     * @param incoming boolean whether it's send by the user of the phone or another participant
     */
    public void insert(String phoneNumber, String message, boolean incoming) {
        // set required values
        ContentValues values = new ContentValues();
        Date date = new Date();
        values.put("ID", phoneNumber);
        values.put("message", message);
        values.put("inOut", incoming);
        values.put("date", (int)(date.getTime() / 1000));
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("messages", null, values);

        // check if we already have this conversation
        Cursor allConvs = selectAllConversations();
        boolean found = false;
        if (allConvs.moveToFirst()) {
            do {
                String id = allConvs.getString(allConvs.getColumnIndex("id"));
                if (id.equals(phoneNumber)) {
                    found = true;
                    break;
                }
            } while (allConvs.moveToNext());
        }
        // if we don't, insert it in the database
        if (!found) {
            ContentValues values2 = new ContentValues();
            values2.put("id", phoneNumber);
            // dividing by 1000 gives second accuracy, but allows storing in an int
            values2.put("lastDate", (int)(date.getTime() / 1000));
            db.insert("conversations", null, values2);
        }
        // set date of last activity in this conversation
        updateDate(phoneNumber);
    }

    /**
     * Insert a new message in a group chat into the database.
     * @param groupID the ID for this group
     * @param phoneNumber the sender of the message
     * @param message the actual message
     * @param incoming boolean whether it's send by the user of the phone or another participant
     */
    public void insertGroup(String groupID, String phoneNumber, String message, boolean incoming) {
        // store the message in the database
        ContentValues values = new ContentValues();
        Date date = new Date();
        values.put("ID", groupID);
        values.put("sender", phoneNumber);
        values.put("message", message);
        values.put("inOut", incoming);
        values.put("date", (int)(date.getTime() / 1000));
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("messages", null, values);

        // set date of last activity in this conversation
        updateDate(groupID);
    }

    /**
     * Get the group name belonging to an id.
     * @param id the id of a group
     * @return a string containing the name of the group
     */
    public String getGroupName(String id) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor groupName = db.rawQuery("SELECT * FROM groupNames WHERE myID = ?", new String[]{id});
        groupName.moveToFirst();
        return groupName.getString(groupName.getColumnIndex("groupName"));
    }

    /**
     * Get a list of all conversations stored in the database sorted by when there last was a
     * message in a conversation
     * @return Cursor containing conversation id and date of last message
     */
    public Cursor selectAllConversations() {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allConvs = db.rawQuery("SELECT * FROM conversations ORDER BY lastDate DESC", null);
        return allConvs;
    }

    /**
     * Select all messages which were posted in one conversation
     * @param phoneNumber id of the conversation, either a phonenumber or a group id
     * @return Cursor containing all the messages in the conversation.
     */
    public Cursor selectOneConversations(String phoneNumber) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allConvs = db.rawQuery("SELECT * FROM messages WHERE ID = ?", new String[]{phoneNumber});
        return allConvs;
    }

    /**
     * Get the id a certain phone number uses as identifier for a groupchat
     * @param phoneNumber the phonenumber we want the id of
     * @param myID the id this phone uses for that groupchat
     * @return the id which was found or an empty string if none was found.
     */
    public String getGroupMemberID(String phoneNumber, String myID) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allMembers = db.rawQuery("SELECT * FROM groups WHERE myID = ? AND phoneNumber = ?", new String[]{myID, phoneNumber});;
        if (allMembers.moveToFirst()){
            return Integer.toString(allMembers.getInt(allMembers.getColumnIndex("groupID")));
        } else {
            return "";
        }
    }

    /**
     * Check whether a number is actually in a group.
     * @param phoneNumber phone number which should be checked
     * @param myID my ID for the group of which we want to check
     * @return true if the number is in the group, else false
     */
    public boolean numberInGroup(String phoneNumber, String myID) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allMembers = db.rawQuery("SELECT * FROM groups WHERE myID = ? AND phoneNumber = ?", new String[]{myID, phoneNumber});;
        return allMembers.moveToFirst();
    }

    /**
     * Check how many group ID's are already taken, and return that amount + 1, since this is an
     * unused id.
     * @param groupName the name of the group, so it can be inserted in the database with the id.
     * @return the groupID as integer
     */
    public int getNewGroup(String groupName) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allGroups = db.rawQuery("SELECT * FROM groupNames", null);
        int groupID = 0;
        if (allGroups.moveToLast()) {
            groupID = allGroups.getInt(allGroups.getColumnIndex("myID")) + 1;
        }
        insertGroup(groupID, groupName);
        return groupID;
    }

    /**
     * Get all members belonging to one group conversation
     * @param myID the group ID
     * @return cursor containing all group members.
     */
    public Cursor getGroupMembers(String myID) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allMembers = db.rawQuery("SELECT * FROM groups WHERE myID = ?", new String[]{myID});
        return allMembers;
    }

    /**
     * Insert a new group in the database
     * @param myID the identifier for this new group
     * @param groupName the group name for this new group
     */
    public void insertGroup(int myID, String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // register the groupID and name
        ContentValues values = new ContentValues();
        values.put("myID", myID);
        values.put("groupName", groupName);
        db.insert("groupNames", null, values);

        // register the new group as conversation
        values.clear();
        Date date = new Date();
        values.put("id", Integer.toString(myID));
        values.put("groupBool", true);
        values.put("lastDate", (int)(date.getTime() / 1000));
        db.insert("conversations", null, values);
    }

    /**
     * Add a new number to a existing group in the database.
     * @param myID the identifier for the group
     * @param phoneNumber the phone number of the person to be added
     * @param theirID the ID the other number uses for this conversation
     */
    public void insertNumberInGroup(int myID, String phoneNumber, int theirID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("myID", myID);
        values.put("phoneNumber", phoneNumber);
        values.put("groupID", theirID);
        db.insert("groups", null, values);
    }

    /**
     * Remove one of the members of a group and broadcasts this to other group members
     * @param groupNumber my group number
     * @param phoneNumber phone number of the person to be removed
     */
    public void removeNumberFromGroup(String groupNumber, String phoneNumber, String groupID) {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.delete("groups", "phoneNumber = ? AND myID = ? AND groupID = ?", new String[] {phoneNumber, groupNumber, groupID});
    }

    /**
     * Remove a person from the group without a broadcast to other groupmembers.
     * Used when a broadcast is received from another member.
     * @param groupNumber the group number a user should be removed from
     * @param theirID their id used for this group
     * @param phoneNumber the phone number to be removed.
     */
    public void removeNumberFromGroup2(String groupNumber, String theirID, String phoneNumber) {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.delete("groups", "phoneNumber = ? AND myID = ? AND groupID = ?", new String[] {phoneNumber, groupNumber, theirID});
    }

    /**
     * Removes all members from a group in the database, so that if the user sends a message it
     * won't be send to anyone.
     * @param groupNumber the groupnumber to wipe users from.
     */
    public void removeMeFromGroup(String groupNumber) {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.delete("groups", "myID = ?", new String[] {groupNumber});
    }

    /**
     * Get all blocked users.
     * @return a cursor containing all blocked users
     */
    public Cursor getBlockedUsers() {
        SQLiteDatabase db =  this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM blockedUsers", new String[]{});
    }

    /**
     * Insert a new phonenumber in the list of blocked numbers.
     * @param phoneNumber number to be blocked
     */
    public void insertBlockUser(String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phoneNumber", phoneNumber);
        db.insert("blockedUsers", null, values);
    }

    /**
     * Unblock a phone number.
     * @param phoneNumber phone number to be unblocked
     */
    public void removeBlockUser(String phoneNumber) {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.delete("blockedUsers", "phoneNumber = ?", new String[] {phoneNumber});
    }

    /**
     * Check if a user is blocked.
     * @param phoneNumber phone number to check
     * @return true if the number is blocked, false if it's not.
     */
    public boolean userBlocked(String phoneNumber) {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor blockedUsers = db.rawQuery("SELECT * FROM blockedUsers WHERE phoneNumber = ?", new String[]{phoneNumber});
        return blockedUsers.moveToFirst();
    }

    /**
     * Clears the entire database and creates a new one.
     */
    public void clear() {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("DROP TABLE IF EXISTS conversations");
        db.execSQL("DROP TABLE IF EXISTS groupNames");
        db.execSQL("DROP TABLE IF EXISTS groups");
        db.execSQL("DROP TABLE IF EXISTS blockedUsers");

        onCreate(db);
    }

    /**
     * Update the date of the last received message for a conversation. The date is updated to the
     * current time.
     * @param id the ID of te group which date needs to be updated
     */
    public void updateDate(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues() ;
        Date date = new Date();
        // divide the timestamp by 1000 to fit it in an int. Accuracy on second is enough.
        values.put("lastDate", (int)(date.getTime() / 1000));
        db.update("conversations", values,  "id=?", new String[] { id });
    }
}

