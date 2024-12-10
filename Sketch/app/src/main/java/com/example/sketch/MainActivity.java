package com.example.sketch;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ArrayList<DrawingView> allcanvas;
    private CanvasAdapter canvasAdapter;
    private RecyclerView gridLayout;
    private DatabaseOperations operate = new DatabaseOperations(this);
    private int profilePicWidth;
    private User currentuser = null;
    private static String channel = "simple_channel";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

       // operate.cleanDataBase();
        setupmaincontent();
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_CODE);

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

    List<String> suggestions = new ArrayList<>();

    User Collab =  null;

    try {
        try {
            List<User> collabTest = operate.getCollab(1);

            for(User collab : collabTest){
                Log.d("User Info",collab.getEmail() + " " + collab.getMyCanavas().get(0).getCanvasid());

            }
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
    } catch (IndexOutOfBoundsException e) {

    }


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

    //testing notification
    //sendNotification();

    canvasAdapter = new CanvasAdapter(allcanvas,this);

    gridLayout = findViewById(R.id.grid_layout); //this is still a reycle view but named to gridlayout for the layout

    gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {

            gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        }
    });
    GridLayoutManager manager = new GridLayoutManager(this, 2); // 2 columns
    gridLayout.setLayoutManager(manager);
    gridLayout.setAdapter(canvasAdapter);
    // Inside your activity or fragment
    canvasAdapter.notifyDataSetChanged();

    FloatingActionButton newcanvas = findViewById(R.id.newcanvas);
    FloatingActionButton OpenMaps = findViewById(R.id.ViewLocations);
    FloatingActionButton newCanvasGame = findViewById(R.id.canvasGameButton);
    SearchView searchBar = findViewById(R.id.searchBar);


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
            // get the suggestion linked to the canvas
            DrawingView canvas  = allcanvas.get(i); // direct them to the location

            if(finalLoggedin) {

                editCanavs(canvas);

            }else {
                builder.setTitle("Login Needed").setMessage("Please Login to Continue");
                builder.setCancelable(true);
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
            return true;
        }
    });

   // testFirebaseService();

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
    newCanvasGame.setOnClickListener(v -> {
        if(finalLoggedin) {
            Intent intent = new Intent(MainActivity.this,CanvasGameActivity.class);
            startActivity(intent);
        }else {
            builder.setTitle("Login Needed").setMessage("Please Login to Continue");
            builder.setCancelable(true);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    });

    OpenMaps.setOnClickListener(maps -> {
                if (finalLoggedin) {
                    Intent intent = new Intent(MainActivity.this, LocationView.class);
                    startActivity(intent);
                } else {
                    builder.setTitle("Login Needed").setMessage("Please Login to Continue");
                    builder.setCancelable(true);
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    builder.show();
                }
            }
    );

}

public void editCanavs(DrawingView canvas){
    setContentView(R.layout.canvas);

    SeekBar brushsize = findViewById(R.id.brush_size);
    DrawingView editCanvas = findViewById(R.id.drawing_view);

    editCanvas.setTitle(canvas.getTitle());

    // Capture the original bitmap from the canvas (use the actual bitmap from the original canvas)
    Bitmap bitmap = canvas.getFullcanvas();

    if (bitmap != null) {
        // Set the canvas scale and id before setting the bitmap
        editCanvas.setCanvasScale(canvas.getScaledWidth(), canvas.getScaledHeight());
        editCanvas.setCanvasId(canvas.getCanvasid());
        // Pass the bitmap to the new canvas for redrawing
        editCanvas.setBitmap(bitmap);
        editCanvas.invalidate();
    }

    brushsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            editCanvas.setPaintRadius(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {                }
    });

    // Clear button functionality
    ImageButton addshapebutton = findViewById(R.id.newshapebutton);
    ImageButton mirrorButton = findViewById(R.id.mirrorButton);
    ImageButton eraserButton = findViewById(R.id.eraserButton);
    ImageButton redoButton = findViewById(R.id.redobutton);
    ImageButton undoButton = findViewById(R.id.undobutton);
    SeekBar shapesizer = findViewById(R.id.shapesizer);
    ImageButton clearButton = findViewById(R.id.clear_button);
    Button colorButton = findViewById(R.id.color_button);
    ImageButton saveButton =  this.findViewById(R.id.save_button);

    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

    View popupView = inflater.inflate(R.layout.color_select, null, false);
    View savewindow = inflater.inflate(R.layout.save_option, null, false);
    View shapeselect = inflater.inflate(R.layout.addshapemenu, null, false);

    mirrorButton.setOnClickListener(mirror -> {
        editCanvas.getFullcanvas();
        // canvas.setFillMode(true);
        editCanvas.mirrorCanvas(true);
    });
    eraserButton.setOnClickListener(erase -> {

        editCanvas.enableEraserMode();
    });

    redoButton.setOnClickListener(redo -> {

        if (editCanvas.getPaths() != null) {
            editCanvas.reAddMostRecent();  // Remove the most recent path
        }
    });

    undoButton.setOnClickListener(undo ->{
        // Check if there are paths in the canvas and remove the most recent one
        if (editCanvas.getPaths() != null && !editCanvas.getPaths().isEmpty()) {
           
            editCanvas.removeMostRecent();  // Remove the most recent path

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
                editCanvas.addShape("circle", 70, 70, 100);
            }

        });
        starbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                editCanvas.addShape("star", 70, 70, 100);
            }

        });
        trianglebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                editCanvas.addShape("triangle", 70, 70, 100);
            }

        });


        recatanglebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                editCanvas.addShape("rectangle", 70, 70, 100);
            }
        });

    });

    shapesizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                              @Override
                                              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                  // This method is called when the progress is changed.
                                                  // Use 'progress' as the current size of the brush.
                                                  // For example, set the brush size to this value:
                                                  //  brushsize = progress;
                                                  // Update your drawing tool with the new brush size
                                                  // Example: paint.setStrokeWidth(brushSize);
                                                  editCanvas.setCurrentShapeSize(progress*6);
                                              }

                                              @Override
                                              public void onStartTrackingTouch(SeekBar seekBar) {
                                                  if(editCanvas.isSelectedShape()){
                                                      seekBar.setProgress((int)(editCanvas.getCurrentShapeSize())/5);
                                                  }
                                              }

                                              @Override
                                              public void onStopTrackingTouch(SeekBar seekBar) {

                                              }
                                          }
    );

    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    saveButton.setOnClickListener(s -> {
        PopupWindow saveWindow = new PopupWindow(savewindow,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        saveWindow.setFocusable(true);
        saveWindow.setOutsideTouchable(true); // Dismiss popup when touching outside
        saveWindow.showAsDropDown(s, 0, 0, Gravity.BOTTOM);

        EditText titleText = savewindow.findViewById(R.id.canvas_title);
        Button confrim_save = savewindow.findViewById((R.id.confirm_save)); // this is the button within save button
        Button cancelbutton = savewindow.findViewById(R.id.cancel); // if the user wants to cancel there changes

        titleText.setText(editCanvas.getTitle());

        cancelbutton.setOnClickListener(cancel -> {


            saveWindow.dismiss();
            setupmaincontent();


        });

        confrim_save.setOnClickListener(cs ->{

            String title = titleText.getText().toString();

            if(!title.isEmpty()){
                editCanvas.setTitle(title); //
            }
            builder.setTitle("Canvas Updated");
            builder.setMessage("Canvas named " + editCanvas.getTitle() + " has been Updated.");
            builder.setCancelable(true);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();

            DatabaseOperations operations = new DatabaseOperations(this);
            operations.open();
            operations.updateCanvas(currentuser.getid(),editCanvas.getCanvasid(),editCanvas);


            saveWindow.dismiss();
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);
        });
    });


    colorButton.setOnClickListener(c -> {
        PopupWindow colorWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        colorWindow.setFocusable(true);
        colorWindow.setOutsideTouchable(true); // Dismiss popup when touching outside

        colorWindow.showAsDropDown(c, 0, 0, Gravity.BOTTOM);


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
            editCanvas.setPaint(Color.parseColor("#0099CC"));
            colorWindow.dismiss();
        });
        blue.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#00DDFF"));
            colorWindow.dismiss();
        });
        darkgreen.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#669900"));
            colorWindow.dismiss();
        });
        green.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#99CC00"));
            colorWindow.dismiss();
        });
        white.setOnClickListener(db ->{
            editCanvas.setPaint(Color.WHITE);
            colorWindow.dismiss();
        });
        yellow.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#FFFF00"));
            colorWindow.dismiss();
        });
        orange.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#FF8800"));
            colorWindow.dismiss();

        });
        orangelight.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#FFBB33"));
            colorWindow.dismiss();
        });
        red.setOnClickListener(db ->{
            editCanvas.setPaint(Color.parseColor("#CC0000"));
            colorWindow.dismiss();
        });
        black.setOnClickListener(db ->{
            editCanvas.setPaint(Color.BLACK);
            colorWindow.dismiss();
        });



    });
    clearButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editCanvas.clearCanvas();  // Clear the canvas
        }
    });
}

    private void testFirebaseService() {
        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                accessFireBaseToken();
            } else {
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void accessFireBaseToken(){

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM Token", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    try{
                        if(currentuser.getToken().isEmpty()) {
                            currentuser.setToken(token);
                            operate.updateUser(currentuser);
                        }
                    }catch(NullPointerException e){

                    }

                    try {
                        // first check the user's Canvases
                        for(DrawingView userCanvas : currentuser.getMyCanavas()) {
                            //then check all users that are collaberators
                            for (User collab : operate.getCollab(userCanvas.getCanvasid())) {
                                //check if the current user is  a collaberator not the owner
                                if(collab.getid() == currentuser.getid()) {
                                    // now find the owner
                                    for(User owner : operate.getDataBaseUsers()){
                                        // check owners canvases
                                        for(DrawingView canvas : owner.getMyCanavas()){
                                            //if the owner's canvas match send the notification to the collaberotor
                                            if(canvas.getCanvasid() == userCanvas.getCanvasid() && owner.getid() != currentuser.getid()){

                                                buildNotification(owner,currentuser,canvas);
                                            }
                                        }
                                    }
                                 }
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                    } catch (NullPointerException e){

                    }

                    Log.d("FCM Token", "Token: " + token);

                });
    }
    private void buildNotification(User sendUser,User receiver,DrawingView canvas) {

        int notificationId = sendUser.getid();
        String title = "You are now a Collaborator!";
        String body = "Hello " + receiver.getFirstname() + ", " + sendUser.getFirstname() + " " + sendUser.getLastname() + " has set you as a \n" +
                "collaborator for a Canvas named " + canvas.getTitle();





        MyFirebaseMessagingService service = new MyFirebaseMessagingService();

        try {
            if(!receiver.getToken().isEmpty()){
                Log.d("Token", "Token has been recieved");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Perform network operation here (e.g., sending the notification)
                        try {
                            //    service.sendPushNotification2(receiver.getToken(),title,body);

                        } catch (Exception e) {
                            Log.d("Error","Error when trying to send");
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        } catch (NullPointerException e) {
        } catch (Exception e) {

        }

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Set<String> notifications = sharedPref.getStringSet("notifications", new HashSet<>());
        Set<String> canvasIds = new HashSet<>(notifications);

        if (canvas.getCanvasid() != 0) {
            String canvasIdStr = String.valueOf(canvas.getCanvasid());

            if (canvasIds.add(canvasIdStr)) {
                Log.d("Item", canvasIdStr);

                editor.putStringSet("notifications", canvasIds);
                editor.apply();

                service.sendNotification(title, body, notificationId, this);
            }
        }



    }
    public void createMockRemoteMessage(Context context, String title, String body) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.collab)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

}