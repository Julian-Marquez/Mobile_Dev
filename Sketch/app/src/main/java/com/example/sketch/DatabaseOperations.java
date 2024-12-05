package com.example.sketch;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {

    private DataBase dbHelper;
    private SQLiteDatabase database;

    // Constructor
    public DatabaseOperations(Context context) {
        dbHelper = new DataBase(context);
    }

    // Open the database for writing
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertCollab(int userId,int canvasId){
        dbHelper.insertCollaborator(canvasId,userId);
    }

    public List<User> getCollab(int canvasId){

        return dbHelper.getCollaboratorsForCanvas(canvasId);
    }

    public void insertToken(String token){
       // dbHelper.addToken(token);
    }

    public void addLocation(float latitude, float longitude,int canvasId){
        dbHelper.insertLocation(latitude,longitude,canvasId);
    }

    // Close the database
    public void close() {
        dbHelper.close();
    }

    public void cleanDataBase(){
        dbHelper.clearDatabase();
    }

    public void updateUser(User user){
        dbHelper.updateUser(user);
    }

    public void updateCanvas(int userId,int id,DrawingView drawingView){
        dbHelper.updateCanvasInDatabase(userId,id,drawingView);
    }

    // Get all users from the database
    public List<User> getDataBaseUsers() {
        return dbHelper.getAllUsers();
    }

    public void deleteCanvas(int canvasID){
        dbHelper.deleteCanvasById(canvasID);
    }

    public void deleteUser(int userId){
        dbHelper.deleteUserById(userId);
    }

    // Insert a new user
    public long insertUser(String firstName, String lastName, String username, String email, String password, String profilePic) {
        ContentValues values = new ContentValues();
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        values.put("profile_pic", profilePic);

        // Error handling for insert
        try {
            return database.insertOrThrow("users", null, values);
        } catch (SQLException e) {
            // Log the error (optional) and return -1 to indicate failure
            e.printStackTrace();
            return -1;
        }
    }

    // Insert a new canvas for a user
    public long insertCanvas(int userId, String canvasName, byte[] canvasData,int width,int height) {
        // Ensure database is open
        if (database == null || !database.isOpen()) {
            Log.e("DatabaseError", "Database is not open.");
            return -1; // or handle the error appropriately
        }

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("canvas_name", canvasName);
        values.put("canvas_data", canvasData); // Store binary data as a byte array
        values.put("width", width);
        values.put("height", height);
        Log.d("Inserting", "canvases " + userId + " " + canvasName + " " + canvasData + width + " " + height);

        // Attempt to insert canvas data into the database
        try {
            return database.insertOrThrow("canvases", null, values);
        } catch (SQLException e) {
            Log.e("DatabaseError", "Insert failed: " + e.getMessage());
            e.printStackTrace();
            return -1; // Indicate failure
        }
    }


    // Insert a new shape for a canvas
    public long insertShape(long canvasId, String shapeType, float xPosition, float yPosition, float size, String color, String paintStyle) {
        ContentValues values = new ContentValues();
        values.put("canvas_id", canvasId);
        values.put("shape_type", shapeType);
        values.put("x_position", xPosition);
        values.put("y_position", yPosition);
        values.put("size", size);
        values.put("color", color);
        values.put("paint_style", paintStyle);

        // Error handling for insert
        try {
            return database.insertOrThrow("shapes", null, values);
        } catch (SQLException e) {
            // Log the error (optional) and return -1 to indicate failure
            e.printStackTrace();
            return -1;
        }
    }
}
