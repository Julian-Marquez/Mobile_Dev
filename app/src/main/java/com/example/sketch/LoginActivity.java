package com.example.sketch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private List<User> allUsers;
    private DatabaseOperations operate = new DatabaseOperations(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loginpage);

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        allUsers = new ArrayList<>();
        operate.open(); // open the data base for usage

        allUsers = operate.getDataBaseUsers();

        Button loginButton = findViewById(R.id.loginButton);
        TextView forgot = findViewById(R.id.forgotPassword);
        EditText userName = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        TextView signuplink =findViewById(R.id.signupText);
        ImageView goBackArrow = findViewById(R.id.gobackview);

        goBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });


        forgot.setOnClickListener(direct ->{

            Intent intent = new Intent(LoginActivity.this,RecoveryActivity.class);
            startActivity(intent);

        });

        loginButton.setOnClickListener(login ->{

            boolean userexist = false;

            String username = userName.getText().toString();
            String userPassword = password.getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);


            for(User user : allUsers){
                if(username.equalsIgnoreCase(user.getUserName()) && userPassword.equalsIgnoreCase(user.getPassword())
                || username.equalsIgnoreCase(user.getEmail()) && userPassword.equalsIgnoreCase(user.getPassword()) ){ // this allows the user to either login with there email or username

                    // this will help the stay logged in as they tranverse through pages
                    editor.putString("username", user.getUserName());
                    editor.putString("password", user.getPassword());
                    editor.apply();

                    builder.setTitle("Welcome Back " + user.getFirstname() ) // welcome the user
                            .setMessage("You have Succesfully logged in.");

                    userexist = true;
                }
               else if(username.equalsIgnoreCase(user.getUserName()) && !userPassword.equalsIgnoreCase(user.getPassword())
                        || username.equalsIgnoreCase(user.getEmail()) && !userPassword.equalsIgnoreCase(user.getPassword())){
                    builder.setTitle("Incorrect Password") // welcome the user
                            .setMessage("Password is incorrect.");
                }
                else{
                    builder.setTitle("User Not Found") // welcome the user
                            .setMessage("No User was found with that email or password.");
                }
            }


            boolean finalUserexist = userexist;
            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(finalUserexist) {
                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        }
                        else{
                            dialogInterface.dismiss();
                        }
                    }
                });



            AlertDialog alert = builder.create();
            alert.show();

        });

        signuplink.setOnClickListener(point ->{ //link to get them to the sign up page
            signUp();

        });

    }

    public void signUp(){
        setContentView(R.layout.newprofilepage);
        Button signUpButton = findViewById(R.id.signupButton);
        EditText newLastName = findViewById(R.id.newLastName);
        EditText newEmail = findViewById(R.id.newemail);
        EditText newFirtsName = findViewById(R.id.newfirstName);
        EditText newuserName = findViewById(R.id.newusername);
        EditText newpassword = findViewById(R.id.newpassword);
        EditText confirmpassword = findViewById(R.id.confirmpassword);
        TextView cancellink =findViewById(R.id.cancelText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);




        signUpButton.setOnClickListener(sign ->{
            String email = newEmail.getText().toString();
            String firstname = newFirtsName.getText().toString();
            String lastname = newLastName.getText().toString();
            String username = newuserName.getText().toString();
            String password = newpassword.getText().toString();
            String confirmedpassword = confirmpassword.getText().toString();

            boolean usercraeted = false;
            boolean userExist = false;

            //check no feilds are blank
            if(!confirmedpassword.isEmpty() || !email.isEmpty() || !firstname.isEmpty() || !lastname.isEmpty() || !username.isEmpty() || !password.isEmpty()){
               //check for approiate lenght
                for(User user : operate.getDataBaseUsers()){ //check if the email is already in use
                    if(user.getEmail().equalsIgnoreCase(email)){
                        userExist = true;
                    }
                }

                if(password.length() > 7 && password.equals(confirmedpassword) && !userExist) {
                    //check for special symbols
                    if(password.contains("@") || password.contains("_") || password.contains("*") || password.contains("!") || password.contains("#")) {
                        //make a new user
                        if(password.contains("1") || password.contains("2") || password.contains("3") || password.contains("4") || password.contains("5") || password.contains("6") || password.contains("7") || password.contains("8") || password.contains("9") || password.contains("0")) {
                            //make a the changes
                            User newuser = new User(firstname, lastname, email, password, username);

                            operate.open();
                            operate.insertUser(firstname, lastname, username, email, password, "profile_pic"); //profile picture is empty for now
                            // operate.close();

                            allUsers.add(newuser); // add the new user to the list

                            SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            // this will help the stay logged in as they tranverse through pages
                            editor.putString("username", newuser.getUserName());
                            editor.putString("password", newuser.getPassword());
                            editor.apply();

                            builder.setTitle("Welcome " + firstname) // welcome the user
                                    .setMessage("User succesfully created");
                            usercraeted = true;
                        } else{
                            builder.setTitle("Number Needed")
                                    .setMessage("Password must have at least one Number.");
                            }

                    }else {
                        builder.setTitle("Special Character ")
                                .setMessage("Password must have at least one special character.");
                    }
                }else {
                    builder.setTitle("Short Password")
                            .setMessage("Password Must be at least 8 character long and must match the confirmed password.");
                    if(userExist){
                        builder.setTitle("User email Taken")
                                .setMessage("The email " + email + " already exist within the database");
                    }
                }
            }else{
                builder.setTitle("Blank Feilds")
                        .setMessage("All feilds must be not be blank");

            }
            boolean finalUsercraeted = usercraeted;
            builder.setCancelable(false) // Prevent dialog from being dismissed by clicking outside
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss(); // dismiss when pressed
                            if(finalUsercraeted){
                                Intent intent = new Intent(LoginActivity.this,ProfileActivity.class);
                                startActivity(intent);

                            }
                        }
                    });
            // Create and show the alert dialog
            AlertDialog alert = builder.create();
            alert.show();
        });

        cancellink.setOnClickListener(cancel ->{
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

        });


    }

}
