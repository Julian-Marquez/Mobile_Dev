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
    @SuppressLint("WrongViewCast")
    public void makenewCanvas( ){

    setContentView(R.layout.canvas);
     DrawingView canvas = findViewById(R.id.drawing_view); // This is a java class and an xml layout id

    SeekBar brushsize = findViewById(R.id.brush_size);


    brushsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            canvas.setPaintRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {                }
    });




    // Canvas button functionality
    ImageButton addshapebutton = findViewById(R.id.newshapebutton);
    ImageButton eraserButton = findViewById(R.id.eraserButton);
    ImageButton undoButton =  findViewById(R.id.undobutton);
    ImageButton redoButton = findViewById(R.id.redobutton);
    SeekBar shapesizer = findViewById(R.id.shapesizer);
    Button clearButton = findViewById(R.id.clear_button);
    Button colorButton = findViewById(R.id.color_button);
    ImageButton saveButton =  findViewById(R.id.save_button);

    //windows for tools
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    View popupView = inflater.inflate(R.layout.color_select, null);
    View savewindow = inflater.inflate(R.layout.save_option,null);
    View shapeselect = inflater.inflate(R.layout.addshapemenu,null);

    eraserButton.setOnClickListener(erase ->{
    //todo fix the eraser mode
        canvas.enableEraserMode();
    });

    redoButton.setOnClickListener(redo -> {

        if (canvas.getPaths() != null) {
            canvas.reAddMostRecent();  // Remove the most recent path

        }


    });

        undoButton.setOnClickListener(undo ->{

                // Check if there are paths in the canvas and remove the most recent one
                if (canvas.getPaths() != null && !canvas.getPaths().isEmpty()) {
                    canvas.removeMostRecent();  // Remove the most recent path

            }
        });



        addshapebutton.setOnClickListener(shape -> {
        PopupWindow popupWindow = new PopupWindow(shapeselect,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true); // Dismiss popup when touching outside
        popupWindow.showAsDropDown(shape, 0, 0, Gravity.BOTTOM);

        ImageButton circlebutton = shapeselect.findViewById(R.id.circlebutton);
        ImageButton recatanglebutton = shapeselect.findViewById(R.id.rectanglebutton);
        ImageButton starbutton = shapeselect.findViewById(R.id.starbutton);
        ImageButton trianglebutton = shapeselect.findViewById(R.id.trainglebutton);

        circlebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                canvas.addShape("circle", 70, 70, 100);
            }

        });
        starbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                canvas.addShape("star", 70, 70, 100);
            }

        });
        trianglebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                canvas.addShape("triangle", 70, 70, 100);
            }

        });


        recatanglebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                canvas.addShape("rectangle", 70, 70, 100);
            }
        });

    });

    shapesizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            canvas.setCurrentShapeSize(progress*6);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(canvas.isSelectedShape()){
                seekBar.setProgress((int)(canvas.getCurrentShapeSize())/5);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    );

    // Create the PopupWindow

    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    saveButton.setOnClickListener(s -> {
        PopupWindow popupSaveWindow = new PopupWindow(savewindow,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        popupSaveWindow.setFocusable(true);
        popupSaveWindow.setOutsideTouchable(true); // Dismiss popup when touching outside
        popupSaveWindow.showAsDropDown(s, 0, 0, Gravity.BOTTOM);

        //save menu attributes
        EditText title = savewindow.findViewById(R.id.canvas_title);
        Button confrim_save = savewindow.findViewById((R.id.confirm_save)); // this is the button within save button
        Button disregardbutton = savewindow.findViewById(R.id.cancel); // if the user wants to disregard the canvas

        disregardbutton.setText("Disregard");

        disregardbutton.setOnClickListener(cancel -> {


            popupSaveWindow.dismiss();
            setupmaincontent();


        });
        confrim_save.setOnClickListener(cs ->{



            if(title.getText().toString().isEmpty()){
                canvas.setTitle("Untitled"); // by default
            }
            else{
                canvas.setTitle(title.getText().toString());
            }
            builder.setTitle("Canvas Created");
            builder.setMessage("Canvas named " + canvas.getTitle() + " has been saved.");
            builder.setCancelable(true);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();




            currentuser.getMyCanavas().add(canvas);
            allcanvas.add(canvas); //add the new canvas

            //capturing the via Bitmap then converting to bytes to insert into database
            Bitmap bitmap = canvas.getFullcanvas();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] imageBytes = outputStream.toByteArray();


            //operate.insertCanvas(currentuser.getid(),canvas.getTitle(),imageBytes);

            popupSaveWindow.dismiss();
            setupmaincontent();
        });
    });


    colorButton.setOnClickListener(c -> {
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true); // Dismiss popup when touching outside

        popupWindow.showAsDropDown(c, 0, 0, Gravity.BOTTOM);


        //all color buttons
        Button darkblue =  popupView.findViewById(R.id.color1);
        Button blue =  popupView.findViewById(R.id.color2);
        Button darkgreen =  popupView.findViewById(R.id.color3);
        Button green =  popupView.findViewById(R.id.color4);
        Button white =  popupView.findViewById(R.id.color5);
        Button yellow =  popupView.findViewById(R.id.color6);
        Button orange =  popupView.findViewById(R.id.color7);
        Button orangelight =  popupView.findViewById(R.id.color8);
        Button red =  popupView.findViewById(R.id.color9);
        Button black =  popupView.findViewById(R.id.color10);
        darkblue.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#0099CC"));
            popupWindow.dismiss();
        });
        blue.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#00DDFF"));
            popupWindow.dismiss();
        });
        darkgreen.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#669900"));
            popupWindow.dismiss();
        });
        green.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#99CC00"));
            popupWindow.dismiss();
        });
        white.setOnClickListener(db ->{
            canvas.setPaint(Color.WHITE);
            popupWindow.dismiss();
        });
        yellow.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#FFFF00"));
            popupWindow.dismiss();
        });
        orange.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#FF8800"));
            popupWindow.dismiss();

        });
        orangelight.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#FFBB33"));
            popupWindow.dismiss();
        });
        red.setOnClickListener(db ->{
            canvas.setPaint(Color.parseColor("#CC0000"));
            popupWindow.dismiss();
        });
        black.setOnClickListener(db ->{
            canvas.setPaint(Color.BLACK);
            popupWindow.dismiss();
        });



    });
    clearButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
            canvas.clearCanvas();  // Clear the canvas
        }
    });


}
public ArrayList<DrawingView> getAllCanvas(){
        return this.allcanvas;
}

    private void enableImmersiveMode() {
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat insetsController = ViewCompat.getWindowInsetsController(decorView);

        if (insetsController != null) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
            insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

}