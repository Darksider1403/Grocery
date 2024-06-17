package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAGS_CHANGED,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.setLanguageCode("en"); // Set to your desired locale

        // Start login activity after 1sec
        new Handler().postDelayed(() -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) {
                Log.d(TAG, "No current user, navigating to LoginActivity.");
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            } else {
                Log.d(TAG, "User logged in, checking user type.");
                // Check user type
                checkUserType();
            }
        }, 1000);
    }

    private void checkUserType() {
        String uid = firebaseAuth.getUid();
        Log.d(TAG, "Checking user type for UID: " + uid);

        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users");

        ref.orderByChild("uid").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "Snapshot exists: " + snapshot.exists());
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String accountType = "" + userSnapshot.child("accountType").getValue();
                                Log.d(TAG, "Account Type: " + accountType);

                                if ("Seller".equals(accountType)) {
                                    Log.d(TAG, "User is seller, navigating to MainSellerActivity.");
                                    startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                                    finish();
                                } else {
                                    Log.d(TAG, "User is buyer, navigating to MainUserActivity.");
                                    startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                                    finish();
                                }
                            }
                        } else {
                            Log.d(TAG, "User snapshot does not exist.");
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "DatabaseError: " + error.getMessage());
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }
}
