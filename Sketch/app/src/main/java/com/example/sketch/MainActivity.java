package com.example.sketch;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sketch.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private ArrayList<DrawingView> allcanvas;
    private CanvasAdapter canvasAdapter;
    private RecyclerView gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode(); // method takes advantage of the full screen
        allcanvas = new ArrayList<>();
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

    canvasAdapter = new CanvasAdapter(allcanvas,this);
    setContentView(R.layout.activity_main);
    gridLayout = findViewById(R.id.grid_layout); //this is still a reycle view but named to gridlayout for the layout

    GridLayoutManager manager = new GridLayoutManager(this, 2); // 2 columns
    gridLayout.setLayoutManager(manager);
    gridLayout.setAdapter(canvasAdapter);
    // Inside your activity or fragment
    canvasAdapter.notifyDataSetChanged();
    FloatingActionButton newcanvas = findViewById(R.id.newcanvas);// 2 colums to display the canvases




    newcanvas.setOnClickListener(v -> {
        makenewCanvas();

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
            // This method is called when the progress is changed.
            // Use 'progress' as the current size of the brush.
            // For example, set the brush size to this value:
            //  brushsize = progress;
            // Update your drawing tool with the new brush size
            // Example: paint.setStrokeWidth(brushSize);
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
    SeekBar shapesizer = findViewById(R.id.shapesizer);
    Button clearButton = findViewById(R.id.clear_button);
    Button colorButton = findViewById(R.id.color_button);
    Button saveButton =  findViewById(R.id.save_button);


    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    View popupView = inflater.inflate(R.layout.color_select, null);
    View savewindow = inflater.inflate(R.layout.save_option,null);
    View shapeselect = inflater.inflate(R.layout.addshapemenu,null);

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

        recatanglebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a circle with random position and size
                canvas.addShape("rectangle", 70, 70, 100);
            }
        });

    });

    // Create the PopupWindow

    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    saveButton.setOnClickListener(s -> {
        PopupWindow popupWindow = new PopupWindow(savewindow,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true); // Dismiss popup when touching outside
        popupWindow.showAsDropDown(s, 0, 0, Gravity.BOTTOM);

        EditText title = savewindow.findViewById(R.id.canvas_title);
        Button confrim_save = savewindow.findViewById((R.id.confirm_save)); // this is the button within save button
        Button disregardbutton = savewindow.findViewById(R.id.cancel); // if the user wants to disregard the canvas
        disregardbutton.setText("Disregard");

        disregardbutton.setOnClickListener(cancel -> {


            popupWindow.dismiss();
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

            allcanvas.add(canvas); //add the new canvas

            popupWindow.dismiss();
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