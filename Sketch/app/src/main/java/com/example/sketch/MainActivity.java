package com.example.sketch;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sketch.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private DrawingView canvas ;
    private ArrayList<DrawingView> allcanvas;
    private View example;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    setContentView(R.layout.activity_main);
    GridLayout mainlayout = findViewById(R.id.canvas_grid);
    GridView grid = findViewById(R.id.gridview);
    FloatingActionButton newcanvas = findViewById(R.id.newcanvas);
    if (mainlayout != null && allcanvas != null && !allcanvas.isEmpty()) {
        for (DrawingView canvas : allcanvas) {
            if (canvas.getParent() != null) {
                ((ViewGroup) canvas.getParent()).removeView(canvas);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 200;
            params.height = 200;
            params.setMargins(10, 10, 10, 10); // Optional: Set margins between canvases

            // Set the size of the canvas programmatically
            canvas.setLayoutParams(params);
          //  canvas.setBackgroundColor(Color.BLUE);
            mainlayout.addView(canvas);
        }
    }



    //mainlayout.onViewAdded();

    newcanvas.setOnClickListener(v -> {
        setContentView(R.layout.canvas);

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


        canvas = findViewById(R.id.drawing_view); // This is a java class and an xml layout id

        // Clear button functionality
        Button clearButton = findViewById(R.id.clear_button);
        Button colorButton = findViewById(R.id.color_button);
        Button saveButton =  findViewById(R.id.save_button);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.color_select, null);
        View savewindow = inflater.inflate(R.layout.save_option,null);

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
            Button confrim_save = savewindow.findViewById((R.id.confirm_save));

            confrim_save.setOnClickListener(cs ->{



            if(title.getText().toString().isEmpty()){
                canvas.setTitle("Untitled");
            }
            else{
                canvas.setTitle(title.getText().toString());
            }
            builder.setTitle("Canvas Created");
            builder.setMessage("Canvas named " + canvas.getTitle() + " has been saved.");
            builder.setCancelable(true);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
            allcanvas.add(canvas);
            popupWindow.dismiss();
            this.example = canvas;
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
            public void onClick(View v) {
                canvas.clearCanvas();  // Clear the canvas
            }
        });

    });

}

}
