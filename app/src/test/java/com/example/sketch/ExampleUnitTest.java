package com.example.sketch;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM Token", token);
                        assertNotNull(token);
                        // Send this token to your backend to associate with the user
                    }
                });

    }
}