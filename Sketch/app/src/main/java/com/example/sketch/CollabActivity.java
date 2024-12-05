package com.example.sketch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class CollabActivity extends AppCompatActivity {
    private int profilePicWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.collabpage);


        boolean loggedin = false;

        DatabaseOperations operate  = new DatabaseOperations(this);
        User currentuser = null;
        ArrayList<DrawingView> allcanvas = new ArrayList<>();


        ImageButton profileButton = findViewById(R.id.profilepicButton);
        SearchView searchBar = findViewById(R.id.searchBar);
        Button mainPage = findViewById(R.id.main_page_button);

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);
        int canvasId = sharedPref.getInt("selectedCanvas",0);

        List<User> allUsers;
         allUsers = operate.getDataBaseUsers();

        List<String> suggestions = new ArrayList<>();

        if (username != null && password != null) {
            loggedin = true;
            for (User user : allUsers) {

                Log.d("Users",user.getEmail());

                if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {
                    currentuser = user;
                    allcanvas = user.getMyCanavas();

                    if(!allcanvas.isEmpty()){
                        for(DrawingView canvas : user.getMyCanavas()){
                            suggestions.add(canvas.getTitle());
                        }

                    }

                    // Check if there is a profile picture
                    if (user.getProfilepic() != null && user.getProfilepic().length > 0) {
                        // Use ViewTreeObserver to ensure the layout is done before getting width/height
                        profileButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                // Remove the listener to avoid repeated calls
                                profileButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                                // Get the default width and height of the profile button after layout
                                int defaultWidth = profileButton.getWidth();
                                int defaultHeight = profileButton.getHeight();

                                // Get the byte array of the user's image
                                byte[] bytes = user.getProfilepic();

                                // Decode the byte array into a Bitmap
                                Bitmap originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                if (originalBitmap != null) {
                                    // Scale the Bitmap to the default width and height of the profile button
                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, defaultWidth, defaultHeight, true);

                                    // Set the scaled bitmap as the image for the profile button
                                    profileButton.setImageBitmap(scaledBitmap);
                                } else {
                                    Log.e("Error", "Could not decode byte array into a Bitmap.");
                                }
                            }
                        });
                    }
                }
            }
        }

        ArrayList<User> convertList = (ArrayList<User>) (allUsers);
        convertList.remove(currentuser); // user can not choose themselves

        try {
            try{
                try{

                    List<User> collabs = operate.getCollab(canvasId);
                   for(User user: convertList){
                       for(DrawingView canvas : user.getMyCanavas()){
                           if(canvas.getCanvasid() == canvasId | user.getMyCanavas().contains(canvas)){
                               convertList.remove(user);
                           }
                       }
                   }


                }catch(NullPointerException e){

                }

            }catch(ConcurrentModificationException e){

            }
        } catch (IndexOutOfBoundsException e) {
        }
        CollabAdapter adapter = new CollabAdapter(convertList,this);


        RecyclerView gridLayout = findViewById(R.id.grid_layout); //this is still a reycle view but named to gridlayout for the layout

        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // This gets called once the layout is complete and the view has its final dimensions

                // Remove the listener to avoid multiple calls
                gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
        GridLayoutManager manager = new GridLayoutManager(this, 2); // 2 columns
        gridLayout.setLayoutManager(manager);
        gridLayout.setAdapter(adapter);
//        // Inside your activity or fragment
        adapter.notifyDataSetChanged();




        MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "suggestion"});
        for (int i = 0; i < suggestions.size(); i++) {
            cursor.addRow(new Object[]{i, suggestions.get(i)});
        }

        SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{"suggestion"},
                new int[]{android.R.id.text1},
                0
        );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        boolean finalLoggedin = loggedin;

        searchBar.setSuggestionsAdapter(suggestionAdapter);
        searchBar.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {

                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor cursor = (Cursor) suggestionAdapter.getItem(i);
                String suggestion = cursor.getString(cursor.getColumnIndex("suggestion"));
                searchBar.setQuery(suggestion, true); // Set selected suggestion to the query

                return true;
            }
        });


        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollabActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });


        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePicWidth = v.getWidth();

                if(!finalLoggedin) {
                    Intent intent = new Intent(CollabActivity.this, LoginActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(CollabActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }


            }
        });

    }
}
