package com.example.sketch;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
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
    private OnItemClickListener onItemClickListener;
    MainActivity mainpage;

    public CanvasAdapter(ArrayList<DrawingView> canvasList,MainActivity mainpage) {
        this.canvasList = canvasList;
        this.mainpage = mainpage;
       // this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CanvasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for grid items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.canvas_item, parent, false);
        return new CanvasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CanvasViewHolder holder, int position) {
        DrawingView canvas = canvasList.get(position);


        // Capture a thumbnail
        Bitmap thumbnail = canvas.captureThumbnail(400, 400);
        holder.image.setImageBitmap(thumbnail);
        holder.title.setText(canvas.getTitle());

        holder.itemView.setOnClickListener(v -> { // this is when th user presses to edit the canvas
            mainpage.setContentView(R.layout.canvas);

            SeekBar brushsize = mainpage.findViewById(R.id.brush_size);
            DrawingView editCanvas  = mainpage.findViewById(R.id.drawing_view);

            // Capture the bitmap from the original canvas
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            android.graphics.Canvas redraw = new android.graphics.Canvas(bitmap);

            // Draw the original canvas content onto the new bitmap
            editCanvas.draw(redraw);

            Bitmap bit = canvas.getFullcanvas();

            // Pass the bitmap to the new canvas for redrawing
            editCanvas.setBitmap(bit);
            editCanvas.invalidate();


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
            Button clearButton = mainpage.findViewById(R.id.clear_button);
            Button colorButton = mainpage.findViewById(R.id.color_button);
            Button saveButton =  mainpage.findViewById(R.id.save_button);
            LayoutInflater inflater = (LayoutInflater) mainpage.getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.color_select, null);
            View savewindow = inflater.inflate(R.layout.save_option,null);


            // Create the PopupWindow

            AlertDialog.Builder builder = new AlertDialog.Builder(mainpage);

            saveButton.setOnClickListener(s -> {
                PopupWindow popupWindow = new PopupWindow(savewindow,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true); // Dismiss popup when touching outside
                popupWindow.showAsDropDown(s, 0, 0, Gravity.BOTTOM);

                EditText title = savewindow.findViewById(R.id.canvas_title);
                Button confrim_save = savewindow.findViewById((R.id.confirm_save)); // this is the button within save button
                Button cancelbutton = savewindow.findViewById(R.id.cancel); // if the user wants to cancel there changes


                cancelbutton.setOnClickListener(cancel -> {

                    mainpage.getAllCanvas().set(position,canvas); // pass the orignal canvas object
                    notifyItemChanged(position);

                    popupWindow.dismiss();
                    mainpage.setupmaincontent();


                });

                confrim_save.setOnClickListener(cs ->{



                    if(!title.getText().toString().isEmpty()){
                        editCanvas.setTitle(title.getText().toString()); //
                    }
                    builder.setTitle("Canvas Created");
                    builder.setMessage("Canvas named " + editCanvas.getTitle() + " has been Updated.");
                    builder.setCancelable(true);
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    builder.show();

                    mainpage.getAllCanvas().set(position,editCanvas);
                    notifyItemChanged(position);

                    popupWindow.dismiss();
                    mainpage.setupmaincontent();
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
                    editCanvas.setPaint(Color.parseColor("#0099CC"));
                });
                blue.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#00DDFF"));
                    popupWindow.dismiss();
                });
                darkgreen.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#669900"));
                    popupWindow.dismiss();
                });
                green.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#99CC00"));
                    popupWindow.dismiss();
                });
                white.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.WHITE);
                    popupWindow.dismiss();
                });
                yellow.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#FFFF00"));
                    popupWindow.dismiss();
                });
                orange.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#FF8800"));
                    popupWindow.dismiss();

                });
                orangelight.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#FFBB33"));
                    popupWindow.dismiss();
                });
                red.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.parseColor("#CC0000"));
                });
                black.setOnClickListener(db ->{
                    editCanvas.setPaint(Color.BLACK);
                    popupWindow.dismiss();
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
        ImageView image; // Use ImageView to display the thumbnail

        public CanvasViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.canvas_title);
            image = itemView.findViewById(R.id.canvas_image); // Update to match your layout file
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DrawingView canvas);
    }
}
