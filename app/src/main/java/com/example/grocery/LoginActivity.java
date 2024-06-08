package com.example.grocery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // UI views
    private EditText emailEt, passwordEt;
    private TextView forgotTv, noAccountTv;
    private Button loginBtn;

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

        Log.d("LoginActivity", "forgotTv ID: " + R.id.forgotTv);
        Log.d("LoginActivity", "noAccountTv ID: " + R.id.noAccountTv);


        if (forgotTv == null || noAccountTv == null) {
            throw new RuntimeException("Ensure forgotTv and noAccountTv are properly initialized. Check your layout file.");
        }

        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginActivity", "Forgot Password clicked");
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                if (forgotTv == null) {
                    Log.e("LoginActivity", "forgotTv is null");
                }
            }
        });

        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginActivity", "Register clicked");
                startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                if (noAccountTv == null) {
                    Log.e("LoginActivity", "noAccountTv is null");
                }
            }
        });
    }
}
