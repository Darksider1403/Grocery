package com.example.grocery.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private SwitchCompat fcmSwitch;
    private TextView notificationStatusTv;
    private static  final String enableMessage= "Notification are enable";
    private static  final String disableMessage= "Notification are disable";
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;
    boolean isChecked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        fcmSwitch = findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        backBtn = findViewById(R.id.backBtn);
        firebaseAuth = FirebaseAuth.getInstance();
//init shared preferences
        sp = getSharedPreferences ("SETTINGS_SP", MODE_PRIVATE);
//check last selected option; true/fall
        isChecked=sp.getBoolean("FCM_ENABLE",false);
        fcmSwitch.setChecked(isChecked);
        if(isChecked){
            notificationStatusTv.setText(enableMessage);
        }else {
            notificationStatusTv.setText(disableMessage);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    subscribeToTopic();
                }else {
                    unSubscribeToTopic();
                }
            }
        });
    }
    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Subscibe successfull
                        spEditor=sp.edit();
                        spEditor.putBoolean("FCM_ENABLE",true);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this,""+enableMessage,Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enableMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Subscibe failed
                        Toast.makeText(SettingsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void unSubscribeToTopic (){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Unsubscribe
                        spEditor=sp.edit();
                        spEditor.putBoolean("FCM_ENABLE",false);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this,""+disableMessage,Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disableMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Unsubscribe fail
                        Toast.makeText(SettingsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}