package com.example.sketch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {

    private CanvasCreationActivity canvasContext;
    private Context context;
    private int width;
    private int height;
    private DrawingView canvas;

    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME") != null ?
            System.getenv("DATABASE_NAME") : "SketchApp.db";
    private static final int DATABASE_VERSION = 12;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

        Log.d("Updating", "DataBase update");
        db.execSQL("CREATE TABLE IF NOT EXISTS canvases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +  // Make sure user_id is not null
                "canvas_name TEXT NOT NULL, " +
                "canvas_data BLOB, " +
                "width INTEGER NOT NULL, " +    // Add width column
                "height INTEGER NOT NULL, " +   // Add height column
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

    public void setContext(CanvasCreationActivity context){
        this.canvasContext = context;
    }


    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        width = 1080 ;
        height = 1982;
        Log.d("Data", width + " " + height);
     //   Log.d("Data", canvas.getScaledHeight() + " " + canvas.getScaledWidth());

        // Columns to retrieve from the users table
        String[] userColumns = {"user_id", "first_name", "last_name", "username", "email", "password", "profile_pic"};
        Cursor cursor = db.query("users", userColumns, null, null, null, null, null);



        // Iterate over the cursor and add users to the list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);  // User ID
                String firstName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String username = cursor.getString(3);
                String email = cursor.getString(4);
                String password = cursor.getString(5);
                byte[] profilePic = cursor.getBlob(6); // Optional field

                // Create a new User object
                User user = new User(firstName, lastName, email, password, username);
                user.setId(id);
                if (profilePic != null || profilePic.length != 0) {
                    user.setProfilePic(profilePic);  // Set the profile picture if it exists
                }

                // Add canvases for this user
                Cursor canvasCursor = db.rawQuery("SELECT canvas_name, canvas_data, created_at, width, height, id FROM canvases WHERE user_id = ?", new String[]{String.valueOf(id)});

                if (canvasCursor != null && canvasCursor.moveToFirst()) {
                    do {
                        String canvasName = canvasCursor.getString(0);
                        byte[] canvasData = canvasCursor.getBlob(1); // Assuming canvas_data is stored as a byte array
                        String createdAt = canvasCursor.getString(2);
                        int width = canvasCursor.getInt(3);
                        int height = canvasCursor.getInt(4);
                        int canvasId =  canvasCursor.getInt(5);

                        // Create a new Canvas object and add it to the user
                        DrawingView canvas = createDrawingView(); // Move this inside the canvas loop
                        canvas.setCanvasScale(width,height);
                        canvas.setCanvasId(canvasId);
                        Log.d("Setting",canvasId + " " + width + " " + height);


                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        android.graphics.Canvas redraw = new android.graphics.Canvas(bitmap);
                        canvas.setTitle(canvasName);
                        android.graphics.Canvas redrawCanvas = new android.graphics.Canvas(bitmap);
                        // Draw the original canvas content onto the new bitmap
                        if (canvasData != null) {
                            Bitmap originalBitmap = BitmapFactory.decodeByteArray(canvasData, 0, canvasData.length);
                            // Draw the original canvas content onto the new bitmap
                            redrawCanvas.drawBitmap(originalBitmap, 0, 0, null);
                            canvas.setBitmap(originalBitmap);
                        }


                        // Pass the bitmap to the new canvas for redrawing

                        canvas.invalidate();

                        user.getMyCanavas().add(canvas);
                    } while (canvasCursor.moveToNext());

                    canvasCursor.close();
                }

                userList.add(user); // Add the user with canvases to the list

            } while (cursor.moveToNext());

            cursor.close();
        }

        return userList;  // Return the list of users with their canvases
    }

    public void updateCanvasInDatabase(int canvasId, DrawingView canvas) {
          // Assuming your DrawingView class has a method to get the Bitmap

        SQLiteDatabase db = this.getWritableDatabase();

        Bitmap bitmap = canvas.getFullcanvas();
        // Convert the bitmap to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);  // You can use PNG or JPEG
        byte[] canvasData = byteArrayOutputStream.toByteArray();

        // Prepare values to update in the database
        ContentValues values = new ContentValues();
        values.put("canvas_data", canvasData);  // The byte array representing the canvas
        values.put("width", canvas.getScaledWidth());
        values.put("height", canvas.getScaledHeight());
        values.put("canvas_name",canvas.getTitle());

        // Update the canvas record in the database
        int rowsAffected = db.update("canvases", values, "id = ?", new String[]{String.valueOf(canvasId)});

        // Check if the update was successful
        if (rowsAffected > 0) {
            Log.d("DataBase", "Canvas updated successfully with ID: " + canvasId);
        } else {
            Log.d("DataBase", "Failed to update canvas with ID: " + canvasId);
        }

        // Close the database
        db.close();
    }


    public void clearDatabase() { // this is a debugging method only
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + "canvases");
        db.close();
    }

    public void deleteCanvasById(int canvasId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Deleting the canvas by ID
            db.delete("canvases", "id" + " = ?", new String[]{String.valueOf(canvasId)});
            Log.d("Database", "Canvas with ID " + canvasId + " deleted successfully.");
        } catch (Exception e) {
            Log.e("Database", "Error deleting canvas: " + e.getMessage());
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
