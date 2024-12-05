package com.example.sketch;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseOperations {

    private DataBase dbHelper;
    private SQLiteDatabase database;

    // Constructor
    public DatabaseOperations(Context context) {
        dbHelper = new DataBase(context);
    }

    // Open the database for writing
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // Close the database
    public void close() {
        dbHelper.close();
    }

    // Insert a new user with first name, last name, email, and password
    public long insertUser(String firstName, String lastName, String email, String password, String phone, String profilePic) {
        ContentValues values = new ContentValues();
        values.put("first_name", firstName);   // First name of the user
        values.put("last_name", lastName);     // Last name of the user
        values.put("email", email);            // Email of the user
        values.put("password", password);      // Password for the user
        values.put("phone", phone);            // Optional phone number
        values.put("profile_pic", profilePic); // Optional profile picture path

        // Insert the user into the 'users' table and return the user ID
        return database.insert("users", null, values);
    }

    // Insert a new canvas for a user
    public long insertCanvas(long userId, String canvasName, String canvasData) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);         // ID of the user who owns the canvas
        values.put("canvas_name", canvasName); // Name of the canvas
        values.put("canvas_data", canvasData); // Canvas data (e.g., serialized or JSON format)

        // Insert the canvas into the 'canvases' table and return the canvas ID
        return database.insert("canvases", null, values);
    }
}
