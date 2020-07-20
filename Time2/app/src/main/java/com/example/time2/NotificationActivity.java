package com.example.time2;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class NotificationActivity extends AppCompatActivity {

    private EditText editHours, editMins;
    Button saveNotifications;
    ImageButton btn;
    private String CHANNEL_NAME = "Default Channel";
    private String CHANNEL_ID = "com.example.time2" + CHANNEL_NAME;

    int mHour, mMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_notification);
        createChannel();

        //Initialize UI elements
        editHours = findViewById(R.id.editHour);
        editMins = findViewById(R.id.editMinute);
        saveNotifications = findViewById(R.id.button_saveNotification);

        btn = findViewById(R.id.imageButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMin = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(NotificationActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String sHour = String.valueOf(hourOfDay);
                        String sMin = String.valueOf(minute);
                        editHours.setText(sHour);
                        editMins.setText(sMin);
                    }
                }, mHour, mMin, true);
                timePickerDialog.show();
            }
        });

        saveNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Converts string data into integer data to use for Calendar Object
                 String hours = editHours.getText().toString().trim();
                 String minutes = editMins.getText().toString().trim();

                 // Error Checking
                if (TextUtils.isEmpty(hours)) {
                    editHours.setError("Hour is required in numeric format.");
                    return;
                }

                if (TextUtils.isEmpty(minutes)) {
                    editMins.setError("Minutes are required in numeric format.");
                    return;
                }

                if (!TextUtils.isEmpty(hours) && !TextUtils.isEmpty(minutes)) {
                    // Toast
                    Toast.makeText(NotificationActivity.this, "Reminder Set!", Toast.LENGTH_SHORT).show();

                    // Pushes the data to the calendar object and sets it as the user's inputted notification time
                    int iHours = Integer.parseInt(hours);
                    int iMins = Integer.parseInt(minutes);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, iHours);
                    calendar.set(Calendar.MINUTE, iMins);

                    // Alerts the receiver and sends the notification
                    Intent intent = new Intent(NotificationActivity.this, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationActivity.this, 0, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                } else {
                    Toast.makeText(NotificationActivity.this, "Error Setting Reminder!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // For API 26.0 and Above, we need to create a channel to create notifications
    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("This is the default notification settings");
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }
}
