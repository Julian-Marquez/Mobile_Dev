import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.sketch.DataBase;

public class DatabaseRetrieval {

    private SQLiteDatabase database;
    private DataBase dbHelper;

    // Constructor
    public DatabaseRetrieval(Context context) {
        dbHelper = new DataBase(context);
    }

    // Open the database
    public void open() {
        database = dbHelper.getReadableDatabase();
    }

    // Close the database
    public void close() {
        dbHelper.close();
    }

    // Get all users
    public Cursor getAllUsers() {
        String[] columns = {"id", "name", "email"};
        return database.query("users", columns, null, null, null, null, null);
    }

    // Get all canvases for a specific user
    public Cursor getCanvasesByUser(long userId) {
        String[] columns = {"id", "canvas_name", "canvas_data"};
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        return database.query("canvases", columns, selection, selectionArgs, null, null, null);
    }
}
