package com.example.grocery;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileEditUserActivity extends AppCompatActivity {

    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt;
    private Button updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_user);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        updateBtn = findViewById(R.id.updateBtn);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go back previous activity
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect location
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin update profile
            }
        });
    }
}