package com.example.time2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class AlarmReceiver extends BroadcastReceiver {

    private String CHANNEL_NAME = "Default Channel";
    private String CHANNEL_ID = "com.example.time2" + CHANNEL_NAME;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Create Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle("What's Your Time Worth?")
                .setContentText("Value your time by saving today!")
                .setSmallIcon(R.drawable.ic_baseline_access_time_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Build and Send Notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }
}