package com.example.time2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddGoalActivity extends AppCompatActivity {
    EditText goalTitle, goalCost;
    Button goalAdd;
    BottomNavigationView bottomNavigation;
    String TAG = "Add Goal Activity";
    FirebaseFirestore fStore;
    String userId;
    FirebaseAuth mAuth;
    Long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        // Initialize UI elements
        goalTitle = findViewById(R.id.editTitle);
        goalCost = findViewById(R.id.editCost);
        goalAdd = findViewById(R.id.goalAdd_button);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        mAuth = FirebaseAuth.getInstance();
        time = getCurrentTimestamp();

        // Initialize Cloud Firestore
        fStore = FirebaseFirestore.getInstance();

        goalAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String title = goalTitle.getText().toString().trim();
                String cost = goalCost.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    goalTitle.setError("Title of Goal is required.");
                    return;
                }

                if (TextUtils.isEmpty(cost)) {
                    goalCost.setError("Cost of Goal is required.");
                    return;
                }

                // Add Goal to Firebase
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(cost)) {

                    // Create a new user with a first and last name
                    Map<String, Object> goal = new HashMap<>();
                    goal.put("title", title);
                    goal.put("cost", cost);
                    goal.put("time", time);

                    // Error checking through logger
                    Log.d(TAG, "onSuccess: Title:" + title + "  Cost:" + cost);
                    Toast.makeText(AddGoalActivity.this, "Adding Goal Successful!", Toast.LENGTH_SHORT).show();

                    // Add a new document with a generated ID
                    userId = mAuth.getCurrentUser().getUid();
                    fStore.collection("User_Goal").document(userId)
                            .set(goal)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void Void) {
                                    Log.d(TAG, "User Goal Added");
                                }
                            });
                    startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                }
                else {
                    Toast.makeText(AddGoalActivity.this, "Error Adding Goal!", Toast.LENGTH_SHORT).show();
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

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }
}
