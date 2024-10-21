package com.example.sketch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<DrawingView> allcanvas;
    private CanvasAdapter canvasAdapter;
    private RecyclerView gridLayout;
    private DatabaseOperations operate = new DatabaseOperations(this);
    private int profilePicWidth;
    private User currentuser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

       // operate.cleanDataBase();
        setupmaincontent();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


public void setupmaincontent(){
    setContentView(R.layout.activity_main);


    boolean loggedin = false;

    allcanvas = new ArrayList<>();

    ImageButton profileButton = findViewById(R.id.profilepicButton);

    SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();

    String username = sharedPref.getString("username", null);
    String password = sharedPref.getString("password", null);

    List<User> allUsers = operate.getDataBaseUsers();

    if (username != null && password != null) {
        loggedin = true;
        for (User user : allUsers) {
            if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {
                currentuser = user;
                Log.d("User Id"," " + user.getid());
                allcanvas = user.getMyCanavas();
                for(DrawingView view : allcanvas){
                    Log.d("count",view.getTitle() + " " + view.getCanvasid());
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
    canvasAdapter = new CanvasAdapter(allcanvas,this);


    gridLayout = findViewById(R.id.grid_layout); //this is still a reycle view but named to gridlayout for the layout


    gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            // This gets called once the layout is complete and the view has its final dimensions

            // Remove the listener to avoid multiple calls
            gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            // Now you can safely access the width and height
            Log.d("Layout Width", gridLayout.getWidth() + "");
            Log.d("Layout Height", gridLayout.getHeight() + "");
        }
    });
    GridLayoutManager manager = new GridLayoutManager(this, 2); // 2 columns
    gridLayout.setLayoutManager(manager);
    gridLayout.setAdapter(canvasAdapter);
    // Inside your activity or fragment
    canvasAdapter.notifyDataSetChanged();
    FloatingActionButton newcanvas = findViewById(R.id.newcanvas);// 2 colums to display the canvases


    boolean finalLoggedin = loggedin;
    profileButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            profilePicWidth = v.getWidth();
            
            if(!finalLoggedin) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
            
               
        }
    });
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    newcanvas.setOnClickListener(v -> {
        if(finalLoggedin) {
            Intent intent = new Intent(MainActivity.this,CanvasCreationActivity.class);
            startActivity(intent);
        }else {
            builder.setTitle("Login Needed").setMessage("Please Login to Continue");
            builder.setCancelable(true);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
        }

    });


}

}