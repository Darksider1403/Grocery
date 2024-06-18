package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.grocery.ProgressDialogFragment;
import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText emailEt;
    private Button recoverBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        backBtn = findViewById(R.id.backBtn);
        emailEt = findViewById(R.id.emailEt);
        recoverBtn = findViewById(R.id.recoverBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener((v -> finish()));

        recoverBtn.setOnClickListener((v -> recoverPassword()));
    }

    String email;

    private void recoverPassword() {
        email = emailEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog("Sending instructions to reset password...");

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(authResult -> {
                    // instructions send
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Password reset instructions sent to your email...",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // failed sending instructions
                    hideProgressDialog();
                    Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressDialog(String message) {
        ProgressDialogFragment.newInstance(message).show(getSupportFragmentManager(), "progress");
    }

    private void hideProgressDialog() {
        Fragment prev = getSupportFragmentManager().findFragmentByTag("progress");
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }
}
