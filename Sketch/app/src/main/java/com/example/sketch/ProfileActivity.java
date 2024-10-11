package com.example.sketch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    private User currentUser;
    private DatabaseOperations operate;
    private int deafultheight;
    private int deafultwidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_page);

        TextView displayname = findViewById(R.id.FullName);
        TextView displayusername = findViewById(R.id.username);
        TextView displayemail = findViewById(R.id.email);
        TextView displaypassword = findViewById(R.id.password);
        ImageView editPicture = findViewById(R.id.profilepic);

        deafultwidth = editPicture.getWidth();
        deafultheight = editPicture.getHeight();


        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);

        operate  = new DatabaseOperations(this);

        operate.open();

        List<User> allUsers = operate.getDataBaseUsers();
        StringBuilder build = new StringBuilder();
        for(User user : allUsers){

            if (username.equalsIgnoreCase(user.getUserName()) && password.equalsIgnoreCase(user.getPassword())) {


                currentUser = user;
                currentUser.setId(user.getid());
                displayemail.setText(currentUser.getEmail());
                displayname.setText(currentUser.getFirstname() + " " + user.getLastname());
                displayusername.setText(currentUser.getUserName());
                for(int i = 0; i < currentUser.getPassword().length(); i++){
                    build.append("*");
                }
                displaypassword.setText(build); // just show the user the lenght of there password
                if(user.getProfilepic() != null){
                    currentUser.setProfilePic(user.getProfilepic());
                    byte[] bytes = user.getProfilepic();

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    editPicture.setImageBitmap(bitmap);
                }

            }

        }

        editPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deafultwidth = v.getWidth();
                    deafultheight = v.getHeight();
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_PICTURE);
                }

        });




    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {
                // Get the original Bitmap
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);


                ImageView editPicture = findViewById(R.id.profilepic);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, deafultwidth, deafultwidth, true);

                // Set the scaled Bitmap to the ImageView
                editPicture.setImageBitmap(scaledBitmap);

                // Convert the scaled Bitmap to a byte array for the database
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // Update the current user with the new profile picture
                currentUser.setProfilePic(byteArray);
                operate.updateUser(currentUser);

                // Show a confirmation message
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
