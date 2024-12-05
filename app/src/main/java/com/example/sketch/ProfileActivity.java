package com.example.sketch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


        TextView editinfo = findViewById(R.id.edit);
        TextView confirmedit = findViewById(R.id.confirmedit);
        TextView displayname = findViewById(R.id.FullName);
        TextView displayusername = findViewById(R.id.username);
        TextView displayemail = findViewById(R.id.email);
        TextView displaypassword = findViewById(R.id.password);
        ImageView editPicture = findViewById(R.id.profilepic);
        ImageView goBackArrow = findViewById(R.id.gobackview);
        Button logOff = findViewById(R.id.logoffButton);
        Button delete = findViewById(R.id.deleteAccount);

        EditText newemail = findViewById(R.id.editemail);
        EditText newpassword = findViewById(R.id.editpassword);
        TextView canceledit = findViewById(R.id.canceledit);
        EditText editfirstname = findViewById(R.id.editFirstName);
        EditText editlastname = findViewById(R.id.editLastName);
        EditText editusername = findViewById(R.id.editusername);
        EditText confirmedpassword = findViewById(R.id.confirmpassword);
        LinearLayout layout = findViewById(R.id.confimlayout);
        ImageView confrimimage = findViewById(R.id.confirmedimage);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        goBackArrow.setOnClickListener(goBack ->{

            Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
            startActivity(intent);
        });

        logOff.setOnClickListener(off ->{
            editor.putString("username", null);
            editor.putString("password",null);
            editor.apply();


            Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
            startActivity(intent);

        });



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
                displaypassword.setText(build); // just show the user the length of there password


                try {
                    if(user.getProfilepic() != null || user.getProfilepic().length != 0){
                        currentUser.setProfilePic(user.getProfilepic());
                        byte[] bytes = user.getProfilepic();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        editPicture.setImageBitmap(bitmap);
                    }
                } catch (NullPointerException e) {
                    editPicture.setImageResource(R.mipmap.place_holder_profile); // set the default if the error is thrown

                }

                newemail.setText(currentUser.getEmail());
                editfirstname.setText(currentUser.getFirstname());
                editlastname.setText(currentUser.getLastname());
               editusername.setText(currentUser.getUserName());
                newpassword.setText(currentUser.getPassword()); //this so the user can see there password

            }

        }

        delete.setOnClickListener(remove ->{

            builder.setTitle("Confirm Deletion")
                    .setMessage("This action is can not undone. Are you sure you want to procced")
                    .setCancelable(false);

            builder.setPositiveButton("Delete Account", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    operate.deleteUser(currentUser.getid());
                    editor.putString("username", null);
                    editor.putString("password",null);
                    editor.apply();
                    Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });


            AlertDialog alert = builder.create();
            alert.show();
        });

        editinfo.setOnClickListener(edit ->{
            editinfo.setVisibility(View.GONE);
            confirmedit.setVisibility(View.VISIBLE);


            //get rid of the old text
            displayname.setVisibility(View.GONE);
            displayusername.setVisibility(View.GONE);
            displayemail.setVisibility(View.GONE);
            displaypassword.setVisibility(View.GONE);

            //display the edit feilds
            newemail.setVisibility(View.VISIBLE);
            newpassword.setVisibility(View.VISIBLE);
            canceledit.setVisibility(View.VISIBLE);
            editfirstname.setVisibility(View.VISIBLE);
            editlastname.setVisibility(View.VISIBLE);
            editusername.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
            confirmedpassword.setVisibility(View.VISIBLE);
            confrimimage.setVisibility(View.VISIBLE);

            canceledit.setOnClickListener(cancel -> {
                editinfo.setVisibility(View.VISIBLE);
                confirmedit.setVisibility(View.GONE);

                Intent intent = new Intent(ProfileActivity.this,ProfileActivity.class);
                startActivity(intent);

                //get rid of the old text
                displayname.setVisibility(View.VISIBLE);
                displayusername.setVisibility(View.VISIBLE);
                displayemail.setVisibility(View.VISIBLE);
                displaypassword.setVisibility(View.VISIBLE);


                //display the edit feilds
                newemail.setVisibility(View.GONE);
                newpassword.setVisibility(View.GONE);
                canceledit.setVisibility(View.GONE);
                editfirstname.setVisibility(View.GONE);
                editlastname.setVisibility(View.GONE);
                editusername.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
                confirmedpassword.setVisibility(View.GONE);
                confrimimage.setVisibility(View.GONE);

            });

            confirmedit.setOnClickListener(confirmed ->{
                editinfo.setVisibility(View.VISIBLE);
                confirmedit.setVisibility(View.GONE);

                //get rid of the old text
                displayname.setVisibility(View.VISIBLE);
                displayusername.setVisibility(View.VISIBLE);
                displayemail.setVisibility(View.VISIBLE);
                displaypassword.setVisibility(View.VISIBLE);


                //display the edit feilds
                newemail.setVisibility(View.GONE);
                newpassword.setVisibility(View.GONE);
                canceledit.setVisibility(View.GONE);
                editfirstname.setVisibility(View.GONE);
                editlastname.setVisibility(View.GONE);
                editusername.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
                confirmedpassword.setVisibility(View.GONE);
                confrimimage.setVisibility(View.GONE);

                String firstnameText = editfirstname.getText().toString();
                String lastnameText = editlastname.getText().toString();
                String usernameText = editusername.getText().toString();
                String cpasswordText = confirmedpassword.getText().toString();
                String passwordText = newpassword.getText().toString();
                String emailText = newemail.getText().toString();

                if(!usernameText.isEmpty() || !usernameText.isBlank()){
                    currentUser.setUserName(usernameText);
                }
                if(!emailText.isEmpty() || !emailText.isBlank()){
                    currentUser.setEmail(emailText);
                }
                if(!firstnameText.isEmpty() || !firstnameText.isBlank()){
                    currentUser.setFirstname(firstnameText);
                }
                if(!lastnameText.isEmpty() || !lastnameText.isBlank()){
                    currentUser.setLastname(lastnameText);
                }
                boolean password_change = false;
                if(!passwordText.isEmpty()  && !passwordText.equals(currentUser.getPassword())){

                    password_change = true;

                    if(passwordText.length() > 7 && passwordText.equals(cpasswordText)) {
                        //check for special symbols
                        if(passwordText.contains("@") || passwordText.contains("_") || passwordText.contains("*") || passwordText.contains("!") || passwordText.contains("#")) {
                            if(passwordText.contains("1") || passwordText.contains("2") || passwordText.contains("3") || passwordText.contains("4") || passwordText.contains("5") || passwordText.contains("6") || passwordText.contains("7") || passwordText.contains("8") || passwordText.contains("9") || passwordText.contains("0") ) {
                                //make a the changes

                                password_change = false;
                                currentUser.setPassword(passwordText);
                            }else{
                                builder.setTitle("Number Needed")
                                        .setMessage("Password must have at least one Number.");
                            }

                        }else {
                            builder.setTitle("Special Character ")
                                    .setMessage("Password must have at least one special character.");
                           // alert.show();
                        }
                    }else {
                        builder.setTitle("Short Password")
                                .setMessage("Password Must be at least 8 character long and must match the confirmed password.");

                    }
                    AlertDialog alert = builder.create();
                    alert.show();

                }
                if(!password_change) {
                    operate.updateUser(currentUser);
                    editor.putString("username", currentUser.getUserName());
                    editor.putString("password", currentUser.getPassword());
                    editor.apply();
                    Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }



            });


        });

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

                // Show a confirmation message for debugging purposes
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
