package com.example.sketch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;

import java.util.List;
import java.util.Random;

public class RecoveryActivity extends AppCompatActivity {

    private static final String API_KEY = "81aa707e01e93e21ac332bd9fc171da5";
    private static final String SECRET = "432e844ba63ffef98af8bb01195135b5";
    private User currentUser = null;
    private  int random_code = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recover);


        TextView cancel = findViewById(R.id.cancel);
        EditText username = findViewById(R.id.username);
        EditText recoverCode = findViewById(R.id.recoveryCode);
        Button sendEmail = findViewById(R.id.recoverButton);
        Button submitCode = findViewById(R.id.submitCode);
        LinearLayout codeLayout = findViewById(R.id.codeLayout);
        LinearLayout userLayout = findViewById(R.id.usernameLayout);
        codeLayout.setVisibility(View.GONE); // by default

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecoveryActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });


        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        DatabaseOperations operate = new DatabaseOperations(this);

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Incorrect Email or Username")
                        .setMessage("The provided email or username was not found")
                        .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                String auth = username.getText().toString();

                if (!auth.isEmpty()) {
                    List<User> allUsers = operate.getDataBaseUsers();
                    for (User user : allUsers) {
                        if (user.getUserName().equals(auth) || user.getEmail().equals(auth)) {
                            userLayout.setVisibility(View.GONE);
                            submitCode.setVisibility(View.VISIBLE);
                            codeLayout.setVisibility(View.VISIBLE);
                            sendEmail.setVisibility(View.GONE);

                            builder.setTitle("Email Sent")
                                    .setMessage("An email with a recovery code has just been sent to your account.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });

                            currentUser = user;
                            Random rand = new Random();
                            random_code = rand.nextInt(899999) + 100000;  // Generates a 6-digit code
                            Log.d("Recovery Code", "Generated code: " + random_code);
                            new SendEmailTask(user.getEmail()).execute();

                        }
                        }
                        builder.show().setCancelable(true);

                    }
                }

        });


        submitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                builder.setTitle("Invalid Code")
                        .setMessage("The Code provided does not match our most recent email. Please Try Again.")
                        .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                String test_code = recoverCode.getText().toString();
                String string_code = Integer.toString(random_code);

                sendEmail.setVisibility(View.VISIBLE);
                submitCode.setVisibility(View.GONE);
                userLayout.setVisibility(View.VISIBLE);
                codeLayout.setVisibility(View.GONE);
                sendEmail.setText("Send Another Code");

                if(test_code.equals(string_code)){

                    editor.putString("username", currentUser.getUserName());
                    editor.putString("password", currentUser.getPassword());
                    editor.apply();

                    builder.setTitle("Consider The Following")
                            .setMessage("We recommend that you update your password with the edit tool.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    Intent intent = new Intent(RecoveryActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                }
                            });
                }

                builder.show().setCancelable(true);
            }
        });

    }

    private class SendEmailTask extends AsyncTask<Void, Void, SendEmailsResponse> {
        private final String recipientEmail;

        SendEmailTask(String recipientEmail) {
            this.recipientEmail = recipientEmail;
        }

        @Override
        protected SendEmailsResponse doInBackground(Void... voids) {
            try {
                ClientOptions options = ClientOptions.builder()
                        .baseUrl("https://api.us.mailjet.com")
                        .apiKey(API_KEY)
                        .apiSecretKey(SECRET)
                        .build();

                MailjetClient client = new MailjetClient(API_KEY, SECRET);

                String name  = currentUser.getFirstname();
                String senderEmail = "turbotowing505@gmail.com"; //this email is specifiy my personal work email
                TransactionalEmail email = TransactionalEmail
                        .builder()
                        .from(new SendContact(senderEmail, "Sketch Mobile Department"))
                        .to(new SendContact(currentUser.getEmail(), name))
                        .subject("Recovery Code")
                        .textPart("")
                        .htmlPart("<h3>Hello "  + name + ",</h3><br />Your Recovery Code is " + random_code)
                        .build();

                // Create and send the email request
                SendEmailsRequest request = SendEmailsRequest
                        .builder()
                        .message(email)
                        .build();

                // Send the email and return the response
                return request.sendWith(client);
            } catch (MailjetException e) {
                Log.e("Mailjet", "Error sending email", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(SendEmailsResponse response) {
            if (response != null) {
                Log.d("Mailjet", "Email sent successfully: " + response);
            } else {
                Log.d("Mailjet", "Failed to send email");
            }
        }
    }
}
