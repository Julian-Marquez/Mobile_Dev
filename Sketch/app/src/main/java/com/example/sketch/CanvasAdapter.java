package com.example.sketch;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CanvasAdapter extends RecyclerView.Adapter<CanvasAdapter.CanvasViewHolder> {

    private ArrayList<DrawingView> canvasList;
    MainActivity mainpage;
    private int thumbnailWidth;
    private int  thumbnailHeight;

    public CanvasAdapter(ArrayList<DrawingView> canvasList,MainActivity mainpage) {
        this.canvasList = canvasList;
        this.mainpage = mainpage;
    }

    @NonNull
    @Override
    public CanvasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for grid items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.canvas_item, parent, false);

        return new CanvasViewHolder(view);
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public void onBindViewHolder(@NonNull CanvasViewHolder holder, int position) {
        DrawingView canvas = canvasList.get(position);

        Bitmap thumbnail = canvas.captureThumbnail(400 , 400);

        holder.title.setText(canvas.getTitle());

        LayoutInflater inflater = (LayoutInflater) mainpage.getSystemService(LAYOUT_INFLATER_SERVICE);
        View optionsView = inflater.inflate(R.layout.canvas_options, null, false);

        holder.optionsButton.setOnClickListener(showOptions -> {
            PopupWindow popupWindow = new PopupWindow(optionsView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);// To enable outside touch dismissal
            popupWindow.showAsDropDown(showOptions, 0, 0, Gravity.BOTTOM);

            // Find buttons and log if they are found
            Button deleteButton = optionsView.findViewById(R.id.deleteButton);
            Button saveImage = optionsView.findViewById(R.id.save_as_image);

            // Attach delete button listener
            deleteButton.setOnClickListener(delete -> {

                DatabaseOperations operations = new DatabaseOperations(mainpage);
                operations.open();
                operations.deleteCanvas(canvas.getCanvasid());

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, canvasList.size());


                popupWindow.dismiss(); // Close the popup after deletion
                mainpage.setupmaincontent();
            });

            // Attach save image button listener
            saveImage.setOnClickListener(save -> {
                Log.d("Popup", "Save Image Button Clicked"); // Debug log to verify click is detected
                // Handle image saving logic
                popupWindow.dismiss();
            });
        });

        //draw(canvas);
        holder.image.setImageBitmap(thumbnail);
        holder.image.setClipToOutline(false);
      //  holder.image.setImageBitmap(canvas.getFullcanvas());



        holder.itemView.setOnClickListener(v -> {
            Log.d("Thumbnail", thumbnailHeight + " " + thumbnailWidth);

            // this is when the user presses to edit the canvas
            mainpage.setContentView(R.layout.canvas);

            SeekBar brushsize = mainpage.findViewById(R.id.brush_size);
            DrawingView editCanvas = mainpage.findViewById(R.id.drawing_view);

            editCanvas.setTitle(canvas.getTitle());

            // Capture the original bitmap from the canvas (use the actual bitmap from the original canvas)
            Bitmap bitmap = canvas.getFullcanvas(); // Assuming getFullcanvas() returns the full bitmap

            if (bitmap != null) {
                // Set the canvas scale and id before setting the bitmap
                editCanvas.setCanvasScale(canvas.getScaledWidth(), canvas.getScaledHeight());
                editCanvas.setCanvasId(canvas.getCanvasid());
                Log.d("Canvas Error", canvas.getScaledWidth() + " " +canvas.getScaledHeight());

                // Pass the bitmap to the new canvas for redrawing
                editCanvas.setBitmap(bitmap);  // Assuming your DrawingView has a setBitmap() method
                editCanvas.invalidate();       // Redraw the canvas
            } else {
                Log.e("Canvas Error", "Failed to get full canvas bitmap.");
            }

            brushsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // This method is called when the progress is changed.
                    // Use 'progress' as the current size of the brush.
                    // For example, set the brush size to this value:
                    //  brushsize = progress;
                    // Update your drawing tool with the new brush size
                    // Example: paint.setStrokeWidth(brushSize);
                    editCanvas.setPaintRadius(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {                }
            });




            // Clear button functionality
            ImageButton addshapebutton = mainpage.findViewById(R.id.newshapebutton);
            ImageButton redoButton = mainpage.findViewById(R.id.redobutton);
            ImageButton undoButton = mainpage.findViewById(R.id.undobutton);
            SeekBar shapesizer = mainpage.findViewById(R.id.shapesizer);
            Button clearButton = mainpage.findViewById(R.id.clear_button);
            Button colorButton = mainpage.findViewById(R.id.color_button);
            ImageButton saveButton =  mainpage.findViewById(R.id.save_button);

             View popupView = inflater.inflate(R.layout.color_select, null, false);
            View savewindow = inflater.inflate(R.layout.save_option, null, false);
            View shapeselect = inflater.inflate(R.layout.addshapemenu, null, false);

            redoButton.setOnClickListener(redo -> {

                if (editCanvas.getPaths() != null) {
                    editCanvas.reAddMostRecent();  // Remove the most recent path

                }
            });

            undoButton.setOnClickListener(undo ->{
                // Check if there are paths in the canvas and remove the most recent one
                if (editCanvas.getPaths() != null && !canvas.getPaths().isEmpty()) {
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

            AlertDialog.Builder builder = new AlertDialog.Builder(mainpage);

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


                cancelbutton.setOnClickListener(cancel -> {

                    notifyItemChanged(position);

                    saveWindow.dismiss();
                    mainpage.setupmaincontent();


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

                    DatabaseOperations operations = new DatabaseOperations(mainpage);
                    operations.open();
                    operations.updateCanvas(editCanvas.getCanvasid(),editCanvas);
                    notifyItemChanged(position);

                    saveWindow.dismiss();
                    mainpage.setupmaincontent();
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



        });



    }

    @Override
    public int getItemCount() {
        return canvasList.size();
    }

    public static class CanvasViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton optionsButton;
        ImageView image; // Use ImageView to display the thumbnail


        public CanvasViewHolder(@NonNull View itemView) {
            super(itemView);
            optionsButton = itemView.findViewById(R.id.canvasOptions);
            title = itemView.findViewById(R.id.canvas_title);
            image = itemView.findViewById(R.id.canvas_image); // Update to match your layout file

        }
    }

}
