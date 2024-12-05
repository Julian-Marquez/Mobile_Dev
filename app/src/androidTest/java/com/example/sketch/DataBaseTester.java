package com.example.sketch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataBaseTester {

    private static final String TAG = "DatabaseTest";
    private DataBase dbHelper;
    private SQLiteDatabase database;

    // Constructor to initialize the database helper
    public DataBaseTester(Context context) {
        dbHelper = new DataBase(context);
    }

    // Method to open the database
    public void open() {
        database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened.");
    }

    // Method to close the database
    public void close() {
        dbHelper.close();
        Log.d(TAG, "Database closed.");
    }

    // Method to insert a test user into the database
    public long insertUser(String name, String email) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);

        long userId = database.insert("users", null, values);
        if (userId != -1) {
            Log.d(TAG, "User inserted with ID: " + userId);
        } else {
            Log.e(TAG, "Error inserting user.");
        }
        return userId;
    }

    // Method to insert a test canvas into the database
    public long insertCanvas(long userId, String canvasName, String canvasData) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("canvas_name", canvasName);
        values.put("canvas_data", canvasData);

        long canvasId = database.insert("canvases", null, values);
        if (canvasId != -1) {
            Log.d(TAG, "Canvas inserted with ID: " + canvasId);
        } else {
            Log.e(TAG, "Error inserting canvas.");
        }
        return canvasId;
    }

    // Method to retrieve and display all users from the database
    public void getAllUsers() {
        Cursor cursor = database.query("users", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

                Log.d(TAG, "User ID: " + id + ", Name: " + name + ", Email: " + email);
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "No users found.");
        }
        cursor.close();
    }

    // Method to retrieve and display all canvases from the database
    public void getAllCanvases() {
        Cursor cursor = database.query("canvases", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String canvasName = cursor.getString(cursor.getColumnIndexOrThrow("canvas_name"));
                String canvasData = cursor.getString(cursor.getColumnIndexOrThrow("canvas_data"));

                Log.d(TAG, "Canvas ID: " + id + ", Canvas Name: " + canvasName + ", Canvas Data: " + canvasData);
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "No canvases found.");
        }
        cursor.close();
    }

}
