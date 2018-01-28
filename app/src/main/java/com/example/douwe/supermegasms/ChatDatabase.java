package com.example.douwe.supermegasms;

/**
 * Created by Douwe on 1/16/18.
 */

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;


public class ChatDatabase extends SQLiteOpenHelper {
    static ChatDatabase instance;
    public ChatDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, ID TEXT, sender TEXT, message TEXT, inOut BOOL)");
        db.execSQL("create table conversations (_id INTEGER PRIMARY KEY AUTOINCREMENT, id TEXT, groupBool BOOL, lastDate INT)");
        db.execSQL("create table groupNames (_id INTEGER PRIMARY KEY AUTOINCREMENT, myID INT, groupName TEXT)");
        db.execSQL("create table groups (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, groupID INT, myID INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + "messages");
        db.execSQL("DROP TABLE IF EXISTS " + "conversations");
        db.execSQL("DROP TABLE IF EXISTS " + "groupNames");
        db.execSQL("DROP TABLE IF EXISTS " + "groups");

        onCreate(db);
    }

    public static ChatDatabase getInstance(Context context) {
        if (instance == null){
            instance = new ChatDatabase(context, "messages", null, 1);
        }
        return instance;
    }

    public void insert(String phoneNumber, String message, boolean incoming){
        ContentValues values = new ContentValues();
        values.put("ID", phoneNumber);
        values.put("message", message);
        values.put("inOut", incoming);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("messages", null, values);

        Cursor all_convs = selectAllConversations();
        boolean found = false;
        if(all_convs.moveToFirst()){
            do{
                String id = all_convs.getString(all_convs.getColumnIndex("id"));
                if (id.equals(phoneNumber)) {
                    found = true;
                    break;
                }
            } while (all_convs.moveToNext());
        }
        if (!found) {
            ContentValues values2 = new ContentValues();
            Date date = new Date();
            values2.put("id", phoneNumber);
            values2.put("lastDate", (int)(date.getTime() / 1000));
            db.insert("conversations", null, values2);
        }
        updateDate(phoneNumber);

    }

    public void insertGroup(String groupID, String phoneNumber, String message, boolean incoming){
        ContentValues values = new ContentValues();
        values.put("ID", groupID);
        values.put("sender", phoneNumber);
        values.put("message", message);
        values.put("inOut", incoming);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("messages", null, values);

        Cursor all_convs = selectAllConversations();
        boolean found = false;
        if(all_convs.moveToFirst()){
            do{
                String id = all_convs.getString(all_convs.getColumnIndex("id"));
                if (id.equals(phoneNumber)) {
                    found = true;
                    break;
                }
            } while (all_convs.moveToNext());
        }
        updateDate(groupID);
    }

    public Cursor selectAll(){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor all_messages = db.rawQuery("SELECT rowid _id,* FROM messages", null);
        return all_messages;
    }

    public String getGroupName(String id){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor groupName = db.rawQuery("SELECT * FROM groupNames WHERE myID = ?", new String[]{id});
        groupName.moveToFirst();
        return groupName.getString(groupName.getColumnIndex("groupName"));
    }

    public Cursor selectAllConversations(){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allConvs = db.rawQuery("SELECT * FROM conversations ORDER BY lastDate DESC", null);
        return allConvs;
    }

    public Cursor selectAllGroupConversations(){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allGroups = db.rawQuery("SELECT rowid _id,* FROM groupNames", null);
        return allGroups;
    }

    public Cursor selectOneConversations(String phoneNumber){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allConvs = db.rawQuery("SELECT * FROM messages WHERE ID = ?", new String[]{phoneNumber});
        return allConvs;
    }

    public int getNewGroup(String groupName){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allGroups = db.rawQuery("SELECT * FROM groupNames", null);
        int groupID = 0;
        if(allGroups.moveToLast()) {
            groupID = allGroups.getInt(allGroups.getColumnIndex("myID")) + 1;
        }
        insertGroup(groupID, groupName);
        return groupID;
    }

    public Cursor getGroupMembers(String myID){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allMembers = db.rawQuery("SELECT * FROM groups WHERE myID = ?", new String[]{myID});;
        return allMembers;
    }

    public void insertGroup(int myID, String groupName){
        ContentValues values = new ContentValues();
        values.put("myID", myID);
        values.put("groupName", groupName);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("groupNames", null, values);
        values.clear();
        Date date = new Date();
        values.put("id", Integer.toString(myID));
        values.put("groupBool", true);
        values.put("lastDate", (int)(date.getTime() / 1000));
        System.out.println("i just inserted : "+ myID);
        db.insert("conversations", null, values);
    }

    public void insertNumberInGroup(int myID, String phoneNumber, int theirID){
        ContentValues values = new ContentValues();
        values.put("myID", myID);
        values.put("phoneNumber", phoneNumber);
        values.put("groupID", theirID);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("groups", null, values);
    }

    public void clear(Context context) {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);

    }

    public void updateDate(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues() ;
        Date date = new Date();
        values.put("lastDate", (int)(date.getTime() / 1000));
        System.out.println("date is now: " + ((int)(date.getTime() / 1000)));
        db.update("conversations", values,  "id=?", new String[] { id });
    }
}

