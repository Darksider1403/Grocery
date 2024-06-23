package com.example.grocery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class WriteReviewActivity extends AppCompatActivity {
    private String shopUid;
    private static final String TAG = "WriteReviewActivity";
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv, labelTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        labelTv = findViewById(R.id.labelTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        shopUid = getIntent().getStringExtra("shopUid");
        if (shopUid == null) {
            Toast.makeText(this, "No shop ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Shop UID: " + shopUid);
        //Load previous review of the user to this shop
        loadMyReview();
        //Load shop info
        loadShopInfo();
        //go back previous acti
        backBtn.setOnClickListener(v -> finish());

        submitBtn.setOnClickListener(v -> {
            //input data
            inputData();
        });
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = "" + snapshot.child("shopName").getValue();
                String shopImage = "" + snapshot.child("profileImage").getValue();
                //set shop info to ui
                shopNameTv.setText(shopName);
                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_grey).into(profileIv);
                } catch (Exception e) {
                    profileIv.setImageResource(R.drawable.ic_store_grey);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Ratings").child(Objects.requireNonNull(firebaseAuth.getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //My review in shop

                            //getReview data
                            String uid = "" + snapshot.child("uid").getValue();
                            String ratings = "" + snapshot.child("ratings").getValue();
                            String review = "" + snapshot.child("review").getValue();
                            String timestamp = "" + snapshot.child("timestamp").getValue();
                            //set review data to our ui
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEt.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void inputData() {
        String ratings = "" + ratingBar.getRating();
        String review = reviewEt.getText().toString().trim();
        //For time of review
        String timestamp = "" + System.currentTimeMillis();
        //Set updata
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("ratings", ratings);
        hashMap.put("review", review);
        hashMap.put("timestamp", timestamp);

        //Input data DB>USERS>shopUid>Ratings
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Ratings").child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(hashMap)
                .addOnSuccessListener(unused -> Toast.makeText(WriteReviewActivity.this, "Review published sucessful..", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(WriteReviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}