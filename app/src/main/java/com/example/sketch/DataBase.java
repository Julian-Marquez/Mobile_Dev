package com.example.sketch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {

    private CanvasCreationActivity canvasContext;
    private Context context;
    private int width;
    private int height;
    private DrawingView canvas;

    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME") != null ?
            System.getenv("DATABASE_NAME") : "SketchApp.db";
    private static final int DATABASE_VERSION = 16;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "username TEXT, " +
                "email TEXT, " +
                "password TEXT, " +
                "profile_pic BLOB, " +
                "token TEXT" +
                ")";
        db.execSQL(createUsersTable);

        // Create canvases table
        String createCanvasesTable = "CREATE TABLE IF NOT EXISTS canvases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " + // Keep user_id as is
                "canvas_name TEXT NOT NULL, " +
                "canvas_data BLOB, " +
                "width INTEGER NOT NULL, " +
                "height INTEGER NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "latitude FLOAT, " +
                "longitude FLOAT, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")";
        db.execSQL(createCanvasesTable);

        // Create canvas_collaborators table to allow multiple users access to a canvas
        String createCanvasCollaboratorsTable = "CREATE TABLE IF NOT EXISTS canvas_collaborators (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "canvas_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY(canvas_id) REFERENCES canvases(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")";
        db.execSQL(createCanvasCollaboratorsTable);

        Log.d("Database", "Tables created successfully");
    }


    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Check each field and add it to the ContentValues if it's not null
        if (!user.getFirstname().isEmpty()) {
            values.put("first_name", user.getFirstname());
        }
        if (!user.getLastname().isEmpty()) {
            values.put("last_name", user.getLastname());
        }
        if (!user.getUserName().isEmpty()) {
            values.put("username", user.getUserName());
        }
        if (!user.getEmail().isEmpty()) {
            values.put("email", user.getEmail());
        }
        if (!user.getPassword().isEmpty()) {
            values.put("password", user.getPassword());
        }
        if (user.getProfilepic() != null || user.getProfilepic().length != 0) {
            values.put("profile_pic", user.getProfilepic());
        }
        try {
            if(!user.getToken().isEmpty()){
                values.put("token",user.getToken());
            }
        } catch (NullPointerException e) {

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

    public void insertLocation(float latitude,float longitude,int canvasId){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if(latitude != 0 || longitude != 0 ){
            values.put("latitude",latitude);
            values.put("longitude",longitude);
        }

        int rowsAffected = db.update("canvases", values, "id = ?", new String[]{String.valueOf(canvasId)});

        if(rowsAffected > 0){
            Log.d("Update", latitude + " " + longitude + " have been added");
        }
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

    public void setContext(CanvasCreationActivity context){
        this.canvasContext = context;
    }


    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] userColumns = {"user_id", "first_name", "last_name", "username", "email", "password", "profile_pic","token"};
        Cursor cursor = db.query("users", userColumns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int userId = cursor.getInt(0);  // User ID
                String firstName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String username = cursor.getString(3);
                String email = cursor.getString(4);
                String password = cursor.getString(5);
                byte[] profilePic = cursor.getBlob(6); // Optional field
                String token = cursor.getString(7);

                // Create a new User object
                User user = new User(firstName, lastName, email, password, username);
                user.setId(userId);
                if (profilePic != null && profilePic.length != 0) {
                    user.setProfilePic(profilePic);  // Set the profile picture if it exists
                }
                try {
                    if(!token.isEmpty()){
                        user.setToken(token);
                    }
                } catch (NullPointerException e) {
                }

                String canvasQuery =
                        "SELECT canvas_name, canvas_data, created_at, width, height, id, latitude, longitude " +
                                "FROM canvases WHERE user_id = ? " +
                                "UNION " +  // This ensures we include canvases where the user is a collaborator
                                "SELECT c.canvas_name, c.canvas_data, c.created_at, c.width, c.height, c.id, c.latitude, c.longitude " +
                                "FROM canvases c " +
                                "JOIN canvas_collaborators cc ON c.id = cc.canvas_id " +
                                "WHERE cc.user_id = ?";

                Cursor canvasCursor = db.rawQuery(canvasQuery, new String[]{String.valueOf(userId), String.valueOf(userId)});

                if (canvasCursor != null && canvasCursor.moveToFirst()) {
                    do {
                        String canvasName = canvasCursor.getString(0);
                        byte[] canvasData = canvasCursor.getBlob(1); // Assuming canvas_data is stored as a byte array
                        String createdAt = canvasCursor.getString(2);
                        int canvasWidth = canvasCursor.getInt(3);
                        int canvasHeight = canvasCursor.getInt(4);
                        int canvasId = canvasCursor.getInt(5);
                        float latitude = canvasCursor.getFloat(6);
                        float longitude = canvasCursor.getFloat(7);

                        DrawingView canvas = createDrawingView();
                        canvas.setCanvasScale(canvasWidth, canvasHeight);
                        canvas.setCanvasId(canvasId);

                        if (latitude != 0 || longitude != 0) {
                            canvas.setMapLocation(latitude, longitude);
                        }

                        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
                        android.graphics.Canvas redraw = new android.graphics.Canvas(bitmap);
                        canvas.setTitle(canvasName);
                        android.graphics.Canvas redrawCanvas = new android.graphics.Canvas(bitmap);

                        if (canvasData != null) {
                            Bitmap originalBitmap = BitmapFactory.decodeByteArray(canvasData, 0, canvasData.length);
                            redrawCanvas.drawBitmap(originalBitmap, 0, 0, null);
                            canvas.setBitmap(originalBitmap);
                        }

                        canvas.invalidate();

                        // Add the canvas to the user's list of canvases
                        user.getMyCanavas().add(canvas);

                    } while (canvasCursor.moveToNext());

                    canvasCursor.close();
                }

                userList.add(user);

            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return userList;  // Return the list of users with their canvases
    }


    public List<User> getCollaboratorsForCanvas(int canvasId) {
        List<User> collaborators = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to retrieve users who are collaborators on a specific canvas ID
        String query = "SELECT u.user_id, u.first_name, u.last_name, u.username, u.email, u.password, u.profile_pic " +
                "FROM canvas_collaborators cc " +
                "JOIN users u ON cc.user_id = u.user_id " +
                "WHERE cc.canvas_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(canvasId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Extract user details
                int userId = cursor.getInt(cursor.getColumnIndex("user_id"));
                String firstName = cursor.getString(cursor.getColumnIndex("first_name"));
                String lastName = cursor.getString(cursor.getColumnIndex("last_name"));
                String username = cursor.getString(cursor.getColumnIndex("username"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                String password = cursor.getString(cursor.getColumnIndex("password"));
                byte[] profilePic = cursor.getBlob(cursor.getColumnIndex("profile_pic"));
                int tokenColumnIndex = cursor.getColumnIndex("token");
                String token = null;
                    if(tokenColumnIndex != -1) {
                        token = cursor.getString(tokenColumnIndex);
                    }


                // Create a new User object and set its properties
                User user = new User(firstName, lastName, email, password, username);
                user.setId(userId);
                if (profilePic != null && profilePic.length > 0) {
                    user.setProfilePic(profilePic);
                }
                try {
                    if(!token.isEmpty() || token != null){
                        user.setToken(token);
                    }
                } catch (NullPointerException e) {
                }

                // Retrieve and add the canvas data for this collaborator
                Cursor canvasCursor = db.rawQuery("SELECT canvas_name, canvas_data, created_at, width, height, id, latitude, longitude FROM canvases WHERE id = ?", new String[]{String.valueOf(canvasId)});

                if (canvasCursor != null && canvasCursor.moveToFirst()) {
                    do {
                        String canvasName = canvasCursor.getString(canvasCursor.getColumnIndex("canvas_name"));
                        byte[] canvasData = canvasCursor.getBlob(canvasCursor.getColumnIndex("canvas_data"));
                        String createdAt = canvasCursor.getString(canvasCursor.getColumnIndex("created_at"));
                        int width = canvasCursor.getInt(canvasCursor.getColumnIndex("width"));
                        int height = canvasCursor.getInt(canvasCursor.getColumnIndex("height"));
                        int canvasDatabaseId = canvasCursor.getInt(canvasCursor.getColumnIndex("id"));
                        float latitude = canvasCursor.getFloat(canvasCursor.getColumnIndex("latitude"));
                        float longitude = canvasCursor.getFloat(canvasCursor.getColumnIndex("longitude"));

                        // Create a new Canvas object
                        DrawingView canvas = createDrawingView();
                        canvas.setCanvasScale(width, height);
                        canvas.setCanvasId(canvasDatabaseId);
                        canvas.setTitle(canvasName);

                        if (latitude != 0 || longitude != 0) {
                            canvas.setMapLocation(latitude, longitude);
                        }

                        if (canvasData != null) {
                            Bitmap originalBitmap = BitmapFactory.decodeByteArray(canvasData, 0, canvasData.length);
                            canvas.setBitmap(originalBitmap);
                        }

                        canvas.invalidate();
                        user.getMyCanavas().add(canvas); // Add canvas to the user's list
                    } while (canvasCursor.moveToNext());

                    canvasCursor.close();
                }

                collaborators.add(user); // Add the user with their canvases to the list

            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return collaborators;
    }




    public void updateCanvasInDatabase(int userId, int canvasId, DrawingView canvas) {
        SQLiteDatabase db = this.getWritableDatabase();

        Bitmap bitmap = canvas.getFullcanvas();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] canvasData = byteArrayOutputStream.toByteArray();

        ContentValues values = new ContentValues();
        values.put("canvas_data", canvasData);
        values.put("width", canvas.getScaledWidth());
        values.put("height", canvas.getScaledHeight());
        values.put("canvas_name", canvas.getTitle());

        // Check if the user is an owner or collaborator
        Cursor cursor = db.rawQuery(
                "SELECT id FROM canvases WHERE id = ? AND (user_id = ? OR id IN (SELECT canvas_id FROM canvas_collaborators WHERE user_id = ?))",
                new String[] { String.valueOf(canvasId), String.valueOf(userId), String.valueOf(userId) });

        boolean isAuthorized = cursor.getCount() > 0;
        cursor.close();

        if (isAuthorized) {
            // Proceed with the update
            int rowsAffected = db.update("canvases", values, "id = ? AND (user_id = ? OR id IN (SELECT canvas_id FROM canvas_collaborators WHERE user_id = ?))",
                    new String[] { String.valueOf(canvasId), String.valueOf(userId), String.valueOf(userId) });

            if (rowsAffected > 0) {
                Log.d("DataBase", "Canvas updated successfully with ID: " + canvasId + " for User ID: " + userId);
            } else {
                Log.d("DataBase", "Failed to update canvas with ID: " + canvasId + " for User ID: " + userId);
            }
        } else {
            Log.d("DataBase", "User ID: " + userId + " is not authorized to update canvas ID: " + canvasId);
        }

        db.close();
    }


    public void insertCollaborator(int canvasId, int userId) {
        // Get writable database instance
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare values to insert
        ContentValues values = new ContentValues();
        values.put("canvas_id", canvasId);  // Add the canvas ID
        values.put("user_id", userId);      // Add the user ID

        // Insert into the canvas_collaborators table
        long result = db.insert("canvas_collaborators", null, values);

        db.close();
    }


    public void clearDatabase() { // this is a debugging method only
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + "canvases");
        db.close();
    }

    public void deleteUserById(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Deleting the canvas by ID
            db.delete("users", "user_id" + " = ?", new String[]{String.valueOf(userId)});

        } catch (Exception e) {
        } finally {
            db.close();
        }
    }

    public void deleteCanvasById(int canvasId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Deleting the canvas by ID
            db.delete("canvases", "id" + " = ?", new String[]{String.valueOf(canvasId)});

        } catch (Exception e) {
        } finally {
            db.close();
        }
    }

    public DrawingView createDrawingView() {
        // Inflate the DrawingView layout
        LayoutInflater inflater = LayoutInflater.from(context);
        DrawingView canvas = (DrawingView) inflater.inflate(R.layout.load_drawing, null);

        // Set default layout parameters if needed (optional)
         width = ViewGroup.LayoutParams.MATCH_PARENT; // or a specific width
         height = ViewGroup.LayoutParams.MATCH_PARENT; // or a specific height

        // Apply layout parameters to the canvas
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        canvas.setLayoutParams(layoutParams);

        canvas.post(new Runnable() {
            @Override
            public void run() {
                // This will run after the layout is done, so width and height should be available
                Log.d("DataBase", "Canvas size: " + canvas.getWidth() + " " + canvas.getHeight());
            }
        });

        return canvas;
    }
}
