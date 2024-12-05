package com.example.sketch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CanvasGameActivity extends AppCompatActivity {
    private DatabaseOperations operate = new DatabaseOperations(this);
    private int width = 0;
    private int height = 0;
    User currentuser = null;
    String finalOption;
    DrawingView canvas; // This is a java class and an xml layout id
    List<DrawingView> allcanvas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.canvas);
        canvas = findViewById(R.id.drawing_view);



        String[] options = {
                "A happy sun",
                "A dancing robot",
                "A spooky ghost",
                "A flying spaceship",
                "A cute puppy",
                "A superhero",
                "A delicious ice cream cone",
                "A roaring dragon",
                "A bustling city skyline",
                "A peaceful beach",
                "A magical unicorn",
                "A fluffy cloud",
                "A cheerful rainbow",
                "A fast car",
                "A wise owl",
                "A blooming flower",
                "A friendly alien",
                "A tropical island",
                "A spooky monster",
                "A cozy campfire"
        };



        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);
        boolean tutorial = sharedPref.getBoolean("tutorial",false);

        List<User> allUsers = operate.getDataBaseUsers();


        for (User user : allUsers) {
            if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {
                currentuser = user;
                allcanvas = user.getMyCanavas();
            }
        }



            canvas.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Now you can safely access the width and height
                    width = canvas.getWidth();
                    height = canvas.getHeight();
                    canvas.setCanvasScale(width,height);
                }
            });

            canvas.setCanvasScale(width,height); // very important this will be very specific for each mobile device

            try {
                Random rand = new Random();
                int index = rand.nextInt(19);
                finalOption = options[index];

            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException(e);
            }
            canvas.setTitle(finalOption);

            AlertDialog.Builder showOption = new AlertDialog.Builder(this);
            boolean isEmpty = false;

            //instead of crashing the Whole class this will handle that
            try{

                if(currentuser.getMyCanavas().isEmpty()){
                    isEmpty = false;
                }

            }catch(NullPointerException e){
                isEmpty = true;
            }

            if(isEmpty || !tutorial){ // prompt the tutorial
                showOption.setTitle("The Drawing Game").setMessage("You will be given a Minute and a half to draw a random option." +
                        " When Time is up you wont be able to draw no more." +
                        " You have a Minute and a half to draw " + finalOption);
            }else {
                showOption.setTitle("Drawing Time").setMessage("You have a Minute and a half to draw " + finalOption);
            }
                   showOption.setPositiveButton("Start", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            editor.putBoolean("tutorial", true);
                            editor.apply();
                            countdown(130);
                        }
                    })
                           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(CanvasGameActivity.this,MainActivity.class);
                    startActivity(intent);
                    dialogInterface.dismiss();
                }
            }).setCancelable(true).show();

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
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });


            // Canvas button functionality
            ImageButton addshapebutton = findViewById(R.id.newshapebutton);
            ImageButton mirrorButton = findViewById(R.id.mirrorButton);
            ImageButton eraserButton = findViewById(R.id.eraserButton);
            ImageButton undoButton = findViewById(R.id.undobutton);
            ImageButton redoButton = findViewById(R.id.redobutton);
            SeekBar shapesizer = findViewById(R.id.shapesizer);
            Button clearButton = findViewById(R.id.clear_button);
            Button colorButton = findViewById(R.id.color_button);
            ImageButton saveButton = findViewById(R.id.save_button);

            //windows for tools
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.color_select, null);
            View savewindow = inflater.inflate(R.layout.save_option, null);
            View shapeselect = inflater.inflate(R.layout.addshapemenu, null);

            mirrorButton.setOnClickListener(mirror -> {
                canvas.getFullcanvas();
                // canvas.setFillMode(true);
                canvas.mirrorCanvas(true);
            });
            eraserButton.setOnClickListener(erase -> {

                canvas.enableEraserMode();
            });

            redoButton.setOnClickListener(redo -> {

                if (canvas.getPaths() != null) {
                    canvas.reAddMostRecent();  // Remove the most recent path

                }

            });

            undoButton.setOnClickListener(undo -> {

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

                circlebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Add a circle with random position and size
                        canvas.addShape("circle", 70, 70, 100);
                    }

                });
                starbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Add a circle with random position and size
                        canvas.addShape("star", 70, 70, 100);
                    }

                });
                trianglebutton.setOnClickListener(new View.OnClickListener() {
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

                                                          canvas.setCurrentShapeSize(progress * 6);
                                                      }

                                                      @Override
                                                      public void onStartTrackingTouch(SeekBar seekBar) {
                                                          if (canvas.isSelectedShape()) {
                                                              seekBar.setProgress((int) (canvas.getCurrentShapeSize()) / 5);
                                                          }
                                                      }

                                                      @Override
                                                      public void onStopTrackingTouch(SeekBar seekBar) {

                                                      }
                                                  }
            );

            // Create the PopupWindow

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            User finalCurrentuser = currentuser;
            List<DrawingView> finalAllcanvas = allcanvas;
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

                confrim_save.setVisibility(View.GONE);
                title.setVisibility(View.GONE);

                disregardbutton.setText("Disregard");

                disregardbutton.setOnClickListener(cancel -> {


                    popupSaveWindow.dismiss();
                    Intent intent = new Intent(CanvasGameActivity.this, MainActivity.class);
                    startActivity(intent);


                });
                confrim_save.setOnClickListener(cs -> {


                    canvas.setTitle(finalOption);
                    builder.setTitle("Canvas Created");
                    builder.setMessage("Canvas named " + canvas.getTitle() + " has been saved.");
                    builder.setCancelable(true);
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    builder.show();


                    finalCurrentuser.getMyCanavas().add(canvas);
                    finalAllcanvas.add(canvas); //add the new canvas



                    // Create a Bitmap with the same dimensions as the DrawingView
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    Canvas drawCanvas = new Canvas(bitmap);
                    canvas.setCanvasScale(width,height);

                    canvas.draw(drawCanvas); // Use the current instance of DrawingView to draw its contents onto the bitmap

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                    byte[] imageBytes = outputStream.toByteArray();

                    if (imageBytes.length > 0) {
                        operate.open();
                        operate.insertCanvas(finalCurrentuser.getid(), canvas.getTitle(), imageBytes, canvas.getScaledWidth(),canvas.getScaledHeight());
                    } else {
                        Log.e("DatabaseError", "Bitmap is empty or not captured correctly!");
                    }



                    popupSaveWindow.dismiss();
//                    Intent intent = new Intent(CanvasCreationActivity.this, MainActivity.class);
//                    startActivity(intent);
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
                Button darkblue = popupView.findViewById(R.id.color1);
                Button blue = popupView.findViewById(R.id.color2);
                Button darkgreen = popupView.findViewById(R.id.color3);
                Button green = popupView.findViewById(R.id.color4);
                Button white = popupView.findViewById(R.id.color5);
                Button yellow = popupView.findViewById(R.id.color6);
                Button orange = popupView.findViewById(R.id.color7);
                Button orangelight = popupView.findViewById(R.id.color8);
                Button red = popupView.findViewById(R.id.color9);
                Button black = popupView.findViewById(R.id.color10);
                darkblue.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#0099CC"));
                    popupWindow.dismiss();
                });
                blue.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#00DDFF"));
                    popupWindow.dismiss();
                });
                darkgreen.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#669900"));
                    popupWindow.dismiss();
                });
                green.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#99CC00"));
                    popupWindow.dismiss();
                });
                white.setOnClickListener(db -> {
                    canvas.setPaint(Color.WHITE);
                    popupWindow.dismiss();
                });
                yellow.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#FFFF00"));
                    popupWindow.dismiss();
                });
                orange.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#FF8800"));
                    popupWindow.dismiss();

                });
                orangelight.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#FFBB33"));
                    popupWindow.dismiss();
                });
                red.setOnClickListener(db -> {
                    canvas.setPaint(Color.parseColor("#CC0000"));
                    popupWindow.dismiss();
                });
                black.setOnClickListener(db -> {
                    canvas.setPaint(Color.BLACK);
                    popupWindow.dismiss();
                });


            });
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    canvas.clearCanvas();  // Clear the canvas
                }
            });



    }
    public void countdown(int seconds) {
        Timer timer = new Timer();
        final boolean[] stop = {false};
        TimerTask task = new TimerTask() {
            int time = seconds;

            @Override
            public void run() {
                if (time >= 0) {
                    Log.d("Time left: ",  time + " seconds");
                    time--;
                } else {
                    timer.cancel();
                    runOnUiThread(new Runnable() { // Ensure display is called on the UI thread
                        @Override
                        public void run() {
                            display(); // Call the dialog when the countdown ends
                        }
                    });
                }

            }

        };
        // Schedule the task to run every 1 second (1000 ms)
        timer.scheduleAtFixedRate(task, 0, 1000);

    }
    public void display(){
        AlertDialog.Builder finish = new AlertDialog.Builder(CanvasGameActivity.this);
        finish.setTitle("Times Up").setMessage("You your time to draw " + finalOption + " is up.")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        canvas.setTitle(finalOption);


                        currentuser.getMyCanavas().add(canvas);
                        allcanvas.add(canvas); //add the new canvas



                        // Create a Bitmap with the same dimensions as the DrawingView
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                        Canvas drawCanvas = new Canvas(bitmap);
                        canvas.setCanvasScale(width,height);

                        canvas.draw(drawCanvas); // Use the current instance of DrawingView to draw its contents onto the bitmap

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                        byte[] imageBytes = outputStream.toByteArray();

                        if (imageBytes.length > 0) {
                            operate.open();
                            operate.insertCanvas(currentuser.getid(), canvas.getTitle(), imageBytes, canvas.getScaledWidth(),canvas.getScaledHeight());
                        } else {
                            Log.e("DatabaseError", "Bitmap is empty or not captured correctly!");
                        }
                        Intent intent = new Intent(CanvasGameActivity.this,MainActivity.class);
                        startActivity(intent);

                    }
                })
                .setNegativeButton("Don't Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(CanvasGameActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                }).show().setCancelable(false);
    }

}
