package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv, ratingsTv;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview> reviewArrayList; // will contain list of all reviews
    private AdapterReview adapterReview;
    private String shopUid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        ratingsTv = findViewById(R.id.ratingsTv);
        reviewsRv = findViewById(R.id.reviewsRv);

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails(); //for shop name, image
        loadReviews(); //for reviews list, avg rating

        backBtn.setOnClickListener(v -> {
            finish(); //go prveious activity
        });
    }

    private float ratingSum = 0;

    private void loadReviews() {
        reviewArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("rating").getValue());
                            ratingSum = ratingSum + rating; //for avg rating, add(addtition of) all ratings, later will divide it by number of reviews

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this, reviewArrayList);
                        //set to recyleview
                        reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum / numberOfReviews;

                        ratingsTv.setText(String.format("%.2f", avgRating) + "[" + numberOfReviews + "]"); //e.g. 4.7 [10]
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String shopName = "" + dataSnapshot.child("shopName").getValue();
                        String profileImage = "" + dataSnapshot.child("profileImage").getValue();

                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(profileIv);
                        } catch (Exception e) {
                            //if anything goes wrong setting image (exepction occurs), set default image
                            profileIv.setImageResource(R.drawable.ic_store_grey);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
