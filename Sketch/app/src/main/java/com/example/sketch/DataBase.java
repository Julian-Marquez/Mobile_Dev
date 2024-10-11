package com.example.sketch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME") != null ?
            System.getenv("DATABASE_NAME") : "SketchApp.db";
    private static final int DATABASE_VERSION = 7;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Check if table exists and create if not
        String createUsersTable = "CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "username TEXT, " +
                "email TEXT, " +
                "password TEXT, " +
                "profile_pic BLOB" + //by default this will be null on first creation
                ")";

        db.execSQL("CREATE TABLE IF NOT EXISTS canvases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "canvas_name TEXT NOT NULL, " +
                "canvas_data TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS shapes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "canvas_id INTEGER, " +
                "shape_type TEXT NOT NULL, " +
                "x_position REAL NOT NULL, " +
                "y_position REAL NOT NULL, " +
                "size REAL NOT NULL, " +
                "color TEXT, " +
                "paint_style TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(canvas_id) REFERENCES canvases(id) ON DELETE CASCADE)");


        db.execSQL(createUsersTable);
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Check each field and add it to the ContentValues if it's not null
        if (user.getFirstname() != null) {
            values.put("first_name", user.getFirstname());
        }
        if (user.getLastname() != null) {
            values.put("last_name", user.getLastname());
        }
        if (user.getUserName() != null) {
            values.put("username", user.getUserName());
        }
        if (user.getEmail() != null) {
            values.put("email", user.getEmail());
        }
        if (user.getPassword() != null) {
            values.put("password", user.getPassword());
        }
        if (user.getProfilepic() != null) {
            values.put("profile_pic", user.getProfilepic());
        }

        // Update the user record in the database
        int rowsAffected = db.update("users", values, "user_id = ?", new String[]{String.valueOf(user.getid())});

        // Check if the update was successful
        if (rowsAffected > 0) {
            Log.d("DataBase", "User updated successfully with ID: " + user.getid());
        } else {
            Log.d("DataBase", "Failed to update user with ID: " + user.getid());
        }

        db.close();
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Drop existing tables if they exist
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS canvases");
            db.execSQL("DROP TABLE IF EXISTS shapes");
            // Recreate tables
            onCreate(db);
        }
    }


    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Columns to retrieve from the users table
        String[] userColumns = {"user_id", "first_name", "last_name", "username", "email", "password", "profile_pic"};

        // Query to select all users from the users table
        Cursor cursor = db.query("users", userColumns, null, null, null, null, null);

        // Iterate over the cursor and add users to the list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);  // User ID (could be useful for other operations)
                String firstName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String username = cursor.getString(3);
                String email = cursor.getString(4);
                String password = cursor.getString(5);
                byte[] profilePic = cursor.getBlob(6); // Optional field (can be null)

                // Create a new User object and add it to the list
                User user = new User(firstName, lastName, email, password, username);
                user.setId(id);
                if (profilePic != null || profilePic.length != 0) {
                    user.setProfilePic(profilePic);  // Set the profile picture if it exists
                }

                userList.add(user);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return userList;  // Return the list of users
    }

    public void clearDatabase() { // this is a debugging method only
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + "users");
        db.close();
    }


}
