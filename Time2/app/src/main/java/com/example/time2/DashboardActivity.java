package com.example.time2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity for loading the user dashboard
 *
 * This activity is used to display the user's goal summary
 */
public class DashboardActivity extends AppCompatActivity{
    BottomNavigationView bottomNavigation;
    TextView goalTitle, goalCost;
    TextView displayName;
    TextView output;
    private RecyclerView fStoreList;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String TAG = "Dashboard Activity:";
    String userId, date;
    double income, percentage, goal_cost, time;
    private SharedPreferences sharedPref;
    float timestamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI elements
        bottomNavigation = findViewById(R.id.bottom_navigation);
        displayName = findViewById(R.id.welcome);
        goalCost = findViewById(R.id.goal_cost);
        goalTitle = findViewById(R.id.goal_title);
        output = findViewById(R.id.time);
        date = getDate((long) timestamp);


        // Initialize FireStore
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        //Used shared pref to fetch display name
        sharedPref = getSharedPreferences("pref", MODE_PRIVATE);
        displayName.setText(String.format("Welcome %s!", sharedPref.getString("userName", "")), TextView.BufferType.EDITABLE);


        userId = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("User_Goal").document(userId);

         // Displays Welcome message to include set displayName
         DocumentReference profileReference = fStore.collection("User_Pref").document(userId);

         profileReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
             @SuppressLint("SetTextI18n")
             @Override
             public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                 //displayName.setText("Welcome " + value.getString("name") + "!"); // Used SharedPreferences instead
                 income = Double.parseDouble(Objects.requireNonNull(value.getString("income")));
                 percentage = Double.parseDouble(Objects.requireNonNull(value.getString("saving")));
             }
         });

        // Retrieve the data and set the data to local variables
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.getString("title").isEmpty()) {
                    goalTitle.setText("No Set Goal");
                } else {
                    goalTitle.setText(value.getString("title"));
                }

                if(value.getString("cost").isEmpty()) {
                    goalCost.setText("0");
                } else {
                    goalCost.setText("Cost: " + value.getString("cost"));
                }


                if((!value.getString("title").isEmpty()) && (!value.getString("cost").isEmpty())) {
                    goal_cost = Double.parseDouble(Objects.requireNonNull(value.getString("cost")));
                    timestamp = value.getLong("time");

                    percentage /= 100f;
                    time = (goal_cost / (income * percentage)) * 30f;

                    float resultMilli = (float) (time * 86400);
                    float newTimestamp = resultMilli + timestamp;
                    float result = (float) ((((System.currentTimeMillis() - (newTimestamp*1000)) / 8.64e+7)) * -1) + 1;

                    // Convert to string to display output
                    output.setText(Integer.toString((int) result) + " days until completion on " + getDate((long) newTimestamp));
                } else {
                    output.setText("Please add a goal");
                }
            }
     });

        // Bottom Navigation Implementation
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // SWITCH to decide which case/fragment to go to
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                        return true;
                    case R.id.navigation_settings:
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        return true;
                    case R.id.navigation_goal:
                        startActivity(new Intent(getApplicationContext(),AddGoalActivity.class));
                        return true;
                }
                return false;
            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}