package com.example.sketch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "SketchApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CANVASES = "canvases";

    // Table Create Statements
    // User Table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "email TEXT);";

    // Canvas Table
    private static final String CREATE_TABLE_CANVASES = "CREATE TABLE " + TABLE_CANVASES + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER, " +
            "canvas_name TEXT, " +
            "canvas_data TEXT, " +
            "FOREIGN KEY(user_id) REFERENCES users(id));";

    // Constructor
    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables when the database is first created
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CANVASES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CANVASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate tables
        onCreate(db);
    }
}
