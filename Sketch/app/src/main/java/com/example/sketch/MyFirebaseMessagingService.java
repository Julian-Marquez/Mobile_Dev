package com.example.sketch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "Firebase";
    private static String channel_name = "Simple_Channel";
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send";
    private static final String SERVER_KEY = "BOwfNyRavBSYhD5iVoZazZ0kUmbz6Cd-1g59vj2m_8zow6Lo7L02Q0ufosBsKWX8jgGlpATQHypn-SgXubpgMLU"; 


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Extract notification data
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Default Title";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "Default Body";

        // Show the notification using application context
        Context context = getApplicationContext();
        sendNotification(title,body,1,context);
        //BOwfNyRavBSYhD5iVoZazZ0kUmbz6Cd-1g59vj2m_8zow6Lo7L02Q0ufosBsKWX8jgGlpATQHypn-SgXubpgMLU
    }


    public void sendNotification(String Title, String Body,int notificationId,Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = channel_name;
            CharSequence channelName = "Sketch Alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("Channel for Sketch Application");

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channel_name)
                .setSmallIcon(R.drawable.collab)
                .setContentTitle(Title)
                .setContentText(Body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
