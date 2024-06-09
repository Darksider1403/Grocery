package com.example.grocery;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // UI views
    private EditText emailEt, passwordEt;
    private TextView forgotTv, noAccountTv;
    private Button loginBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init UI views
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        forgotTv = findViewById(R.id.forgotTv);
        noAccountTv = findViewById(R.id.noAccountTv);
        loginBtn = findViewById(R.id.loginBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        if (forgotTv == null || noAccountTv == null) {
            throw new RuntimeException("Ensure forgotTv and noAccountTv are properly initialized. Check your layout file.");
        }

        forgotTv.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this,
                        ForgotPasswordActivity.class)));

        noAccountTv.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
        });

        loginBtn.setOnClickListener(v -> {
            loginUser();
        });
    }

    private String email, password;

    private void loginUser() {
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // logged in successfully
                    makeMeOnline();
                })
                .addOnFailureListener(e -> {
                    // failed logging
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void makeMeOnline() {
        // after logging in, make user online
        progressDialog.setMessage("Checking User...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        // update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");

        ref.child(Objects.requireNonNull(firebaseAuth.getUid()))
                .updateChildren(hashMap).addOnSuccessListener(unused -> {
                    // updated successfully
                    checkUserType();
                })
                .addOnFailureListener(e -> {
                    // failed to update
                });

    }

    private void checkUserType() {
        // if user is seller, start seller main screen
        // if user is buyer, start user main screen

        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String accountType = "" + ds.child("accountType").getValue();
                            if (accountType.equals("Seller")) {
                                progressDialog.dismiss();
                                // start seller main screen
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();
                            } else {
                                progressDialog.dismiss();
                                // start user main screen
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
