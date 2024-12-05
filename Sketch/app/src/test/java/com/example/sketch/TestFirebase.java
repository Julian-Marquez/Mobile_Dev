package com.example.sketch;

import static org.junit.Assert.assertNotNull;

import android.util.Log;


import com.google.firebase.messaging.FirebaseMessaging;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestFirebase {

    @Test
    public void testTokenRetrieval() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);  // To wait for the async task to complete

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM Token", token);
                        assertNotNull("FCM Token is null", token);  // Assert that the token is not null
                    } else {
                        Log.e("FCM Token", "Token retrieval failed", task.getException());
                    }
                    latch.countDown();
                });

        latch.await();
    }
}
