package com.example.grocery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

        // start login activity after 1sec
        new Handler().postDelayed(() -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) {
                Log.d(TAG, "No current user, navigating to LoginActivity.");
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            } else {
                Log.d(TAG, "User logged in, checking user type.");
                // check user type
                checkUserType();
            }
        }, 1000);

    }

    private void checkUserType() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange called...");
                        if (snapshot.exists()) {
                            boolean userFound = false;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String accountType = "" + ds.child("accountType").getValue();
                                Log.d(TAG, "Account type: " + accountType);
                                if ("Seller".equals(accountType)) {
                                    userFound = true;
                                    startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                                    finish();
                                } else {
                                    userFound = true;
                                    startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                                    finish();
                                }
                            }
                            if (!userFound) {
                                Log.d(TAG, "User data not found, navigating to LoginActivity.");
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                finish();
                            }
                        } else {
                            Log.d(TAG, "Snapshot does not exist, navigating to LoginActivity.");
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