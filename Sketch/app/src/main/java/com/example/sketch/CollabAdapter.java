package com.example.sketch;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;

//import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.*;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;

import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollabAdapter extends RecyclerView.Adapter<CollabAdapter.CollabViewHolder> {

    private ArrayList<DrawingView> canvasList;
    CollabActivity collabpage;
    private int thumbnailWidth;
    private int  thumbnailHeight;
    private List<User> collabList;

    public CollabAdapter(ArrayList<User> collabList,CollabActivity collabpage) {
        this.collabList = collabList;
        this.collabpage = collabpage;
    }

    @NonNull
    @Override
    public CollabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for grid items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collab_item, parent, false);

        return new CollabViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CollabViewHolder holder, int position) {
        User user = collabList.get(position);
        String fullname = user.getFirstname() + " " + user.getLastname();

        holder.fullName.setText(fullname);

        SharedPreferences sharedPref = collabpage.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        AlertDialog.Builder builder = new AlertDialog.Builder(collabpage);

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);
        int width = sharedPref.getInt("CollabWidth",0);
        int height = sharedPref.getInt("CollabHeight",0);
        int canvasId = sharedPref.getInt("selectedCanvas",0);

        DatabaseOperations operate = new DatabaseOperations(collabpage);

        DrawingView canvas = null;
        User loggedinUser = null;

        try {
            for(User User : operate.getDataBaseUsers()){
                if(username.equals(User.getUserName()) && User.getPassword().equals(password)){
                    loggedinUser = User;
                    Log.d("CollabAdapter","User was found");
                }

                for(DrawingView selectcanvas : User.getMyCanavas()){
                    if(selectcanvas.getCanvasid() == canvasId){

                        canvas = selectcanvas;
                        canvas.setCanvasId(selectcanvas.getCanvasid());


                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d("Error","error while trying to find canvas");
            throw new RuntimeException();
        }


        try {
            if(user.getProfilepic() != null || user.getProfilepic().length != 0){
                user.setProfilePic(user.getProfilepic());
                byte[] bytes = user.getProfilepic();

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                holder.image.setImageBitmap(bitmap);
            }
        } catch (NullPointerException e) {
            holder.image.setImageResource(R.mipmap.place_holder_profile); // set the default if the error is thrown

        }

        DrawingView finalCanvas = canvas;
        User finalLoggedinUser = loggedinUser;
        DrawingView finalCanvas1 = canvas;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Add Collaberator")
                        .setMessage("You are about to add " +  fullname + " as a collaberator to the Canvas ")
                        .setCancelable(true)
                        .setPositiveButton("Set Collaberator", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.getMyCanavas().add(finalCanvas);

                                int Canvasheight = finalCanvas.getScaledHeight();
                                int Canvaswidth = finalCanvas.getScaledWidth();

                                Bitmap bitmap = Bitmap.createBitmap(Canvaswidth, Canvasheight, Bitmap.Config.ARGB_8888);

                                Canvas drawCanvas = new Canvas(bitmap);

                                finalCanvas.draw(drawCanvas); // Use the current instance of DrawingView to draw its contents onto the bitmap

                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                                byte[] imageBytes = outputStream.toByteArray();

                                operate.open();
                                user.getMyCanavas().add(finalCanvas);

                                operate.insertCollab(user.getid(), canvasId);
                                dialogInterface.dismiss();

                                //buildNotification(finalLoggedinUser,user, finalCanvas);

                                Intent intent = new Intent(collabpage,MainActivity.class);
                                collabpage.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }

        });



//        if(width != 0 && height != 0){
//            holder.image.setImageBitmap(canvas.ScaledCanvas(width,height));
//        }else {
//            holder.image.setImageBitmap(thumbnail);
//        }


    }


    @Override
    public int getItemCount() {
        return collabList.size();
    }

    public static class CollabViewHolder extends RecyclerView.ViewHolder {
        TextView fullName;
        ImageView image; // Use ImageView to display the thumbnail


        public CollabViewHolder(@NonNull View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.FullName);
            image = itemView.findViewById(R.id.profilepic); // Update to match your layout file

        }
    }

}
