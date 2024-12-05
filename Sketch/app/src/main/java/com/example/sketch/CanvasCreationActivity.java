package com.example.sketch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CanvasCreationActivity extends AppCompatActivity {
    private DatabaseOperations operate = new DatabaseOperations(this);
    private int width = 0;
    private int height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        makenewCanvas();


    }

    @SuppressLint("WrongViewCast")
    public void makenewCanvas() {

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);

        List<User> allUsers = operate.getDataBaseUsers();
        User currentuser = null;
        List<DrawingView> allcanvas = new ArrayList<>();

        for (User user : allUsers) {
            if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {
                currentuser = user;
                allcanvas = user.getMyCanavas();
            }

            setContentView(R.layout.canvas);
            DrawingView canvas = findViewById(R.id.drawing_view); // This is a java class and an xml layout id



            canvas.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // This gets called once the layout is complete and the view has its final dimensions

                    // Remove the listener to avoid multiple calls
                    canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Now you can safely access the width and height
                    width = canvas.getWidth();
                    height = canvas.getHeight();
                    canvas.setCanvasScale(width,height);
                }
            });

           // canvas.getFullcanvas();


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

                disregardbutton.setText("Disregard");

                disregardbutton.setOnClickListener(cancel -> {


                    popupSaveWindow.dismiss();
                    Intent intent = new Intent(CanvasCreationActivity.this, MainActivity.class);
                    startActivity(intent);


                });
                confrim_save.setOnClickListener(cs -> {


                    if (title.getText().toString().isEmpty()) {
                        canvas.setTitle("Untitled"); // by default
                    } else {
                        canvas.setTitle(title.getText().toString());
                    }
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
                    Intent intent = new Intent(CanvasCreationActivity.this, MainActivity.class);
                    startActivity(intent);
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
    }
}
