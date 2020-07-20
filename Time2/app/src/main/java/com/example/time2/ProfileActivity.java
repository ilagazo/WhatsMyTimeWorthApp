package com.example.time2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for User Preferences
 *
 * This activity is where new users get redirected to when they first login
 * and would be asked for their profile details and budget goal related preferences
 *
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    BottomNavigationView bottomNavigation;
    ImageView imageView;
    EditText editName, editIncome, editSaving;
    Button saveBtn;
    Button notificationBtn;
    private static final int CHOOSE_IMAGE = 101;
    String profileImageUrl;
    Uri uriProfileImage;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String TAG = "Add User Preferences";
    String userId;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editName = findViewById(R.id.editTextTextPersonName);
        editIncome = findViewById(R.id.editTextNumber);
        editSaving = findViewById(R.id.editTextNumber2);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressbar);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        saveBtn = findViewById(R.id.button2);
        notificationBtn = findViewById(R.id.button_notification);

        // Initialize UI elements
        bottomNavigation = findViewById(R.id.bottom_navigation);

        loadUserInformation();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            }
        });

        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
            }
        });

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


    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        sharedPref = getSharedPreferences("pref", MODE_PRIVATE);
        editName.setText(sharedPref.getString("userName", ""), TextView.BufferType.EDITABLE);
        editIncome.setText(sharedPref.getString("income", ""), TextView.BufferType.EDITABLE);
        editSaving.setText(sharedPref.getString("saving", ""), TextView.BufferType.EDITABLE);
    }


    private void loadUserInformation() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl().toString()).into(imageView);
            }
            if (user.getDisplayName() != null) {
                editName.setText(user.getDisplayName());
            }
        }
    }


    /**
     * Method to get user's name and income and saves them to a firestore collection
     */
    private void saveUserInformation() {
        sharedPref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String displayName = editName.getText().toString();
        if(displayName.isEmpty()) {
            editName.setError("Name required");
            editName.requestFocus();
            return;
        }

        String userIncome = editIncome.getText().toString();
        if(userIncome.isEmpty()) {
            editIncome.setError("Amount Required");
            editIncome.requestFocus();
            return;
        }

        if (!TextUtils.isDigitsOnly(userIncome)) {
            editIncome.setError("Only Numeric Digits Accepted.");
            return;
        }

        String userSaving = editSaving.getText().toString();
        if(userSaving.isEmpty()) {
            editSaving.setError("Enter desired percentage of saving");
            editSaving.requestFocus();
            return;
        }

        if (!TextUtils.isDigitsOnly(userSaving)) {
            editSaving.setError("Only Numeric Digits Accepted.");
            return;
        }

        /**
         * This method adds the user preferences to the firestore collections and sharedPreferences
         */
        if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(userIncome) && !TextUtils.isEmpty(userSaving)) {
            Map<String, Object> userPref = new HashMap<>();
            userPref.put("name", displayName);
            userPref.put("income", userIncome);
            userPref.put("saving", userSaving);

            // Add new document
            userId = mAuth.getCurrentUser().getUid();
            fStore.collection("User_Pref").document(userId)
                    .set(userPref)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User Preference Added");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding preference");
                        }
                    });
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && profileImageUrl != null){
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)


                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        editor.putString("userName", editName.getText().toString());
        editor.putString("income", editIncome.getText().toString());
        editor.putString("saving", editSaving.getText().toString());
        editor.apply();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data !=null && data.getData()!=null){
            uriProfileImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    private void uploadImageToFirebaseStorage() {
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

        if(uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);

                    profileImageUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                }
            })
                 .addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                      progressBar.setVisibility(View.GONE);
                  }
             });

        }
    }

    private void showImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


    /**
     * Method that signs out current user and then displays the login activity
     * @param view
     */
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut(); //logout
        Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {

    }
}