package com.example.douwe.supermegasms;

/**
 * Created by douwe on 1/16/18.
 */

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



public class ChatDatabase extends SQLiteOpenHelper {
    static ChatDatabase instance;
    public ChatDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, phoneNumber TEXT, message TEXT, inOut BOOL)");
        db.execSQL("create table conversations (_id INTEGER PRIMARY KEY AUTOINCREMENT, id TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + "messages");
        db.execSQL("DROP TABLE IF EXISTS " + "conversations");

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
        values.put("phoneNumber", phoneNumber);
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
            values2.put("id", phoneNumber);
            db.insert("conversations", null, values2);
        }
    }

    public Cursor selectAll(){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor all_messages = db.rawQuery("SELECT rowid _id,* FROM messages", null);
        return all_messages;
    }

    public Cursor selectAllConversations(){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allConvs = db.rawQuery("SELECT rowid _id,* FROM conversations", null);
        return allConvs;
    }

    public Cursor selectOneConversations(String phoneNumber){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor allConvs = db.rawQuery("SELECT * FROM messages WHERE phoneNumber = ?", new String[]{phoneNumber});;
        return allConvs;
    }


    public void clear(Context context) {
        SQLiteDatabase db =  this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);

    }

    public void update(Integer id, Integer new_status){
        System.out.println("i update with: " + Integer.toString(new_status));
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues() ;
        values.put("amount", new_status);

        db.update("orders", values,  "_id=?", new String[] { String.valueOf(id) });
    }
}

