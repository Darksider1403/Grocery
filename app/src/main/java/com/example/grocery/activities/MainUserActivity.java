package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.ProgressDialogFragment;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterShop;
import com.example.grocery.models.ModelShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainUserActivity extends AppCompatActivity {
    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    private RelativeLayout shopsRl, ordersRl;
    private ImageButton logoutBtn, editProfileBtn;
    private ImageView profileIv;
    private RecyclerView shopsRv;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        profileIv = findViewById(R.id.profileIv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);

        shopsRv = findViewById(R.id.shopsRv);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        showShopsUI();

        logoutBtn.setOnClickListener(v -> {
            // make offline
            // sign out
            // go to login screen
            makeMeOffline();
        });

        editProfileBtn.setOnClickListener(v -> {
            //open edit profile activity
            startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
        });

        tabShopsTv.setOnClickListener(v -> showShopsUI());
        tabOrdersTv.setOnClickListener(v -> showOrdersUI());
    }

    private void showShopsUI() {
        // show shops ui, hide orders ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        // show orders ui, hide shop ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void makeMeOffline() {
        // after logging in, make user online
        showProgressDialog("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        // update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");

        ref.child(Objects.requireNonNull(firebaseAuth.getUid()))
                .updateChildren(hashMap).addOnSuccessListener(unused -> {
                    // updated successfully
                    firebaseAuth.signOut();
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();

                            // set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.person_grey).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.person_grey);
                            }

                            // load only those shops that are in the city of user
                            loadShops(city);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadShops(String myCity) {
        // init lít
        shopsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clear list before adding
                        shopsList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = "" + ds.child("city").getValue();

                            // show only user city shops
                            if (shopCity.equals(myCity)) {
                                shopsList.add(modelShop);
                            }

                            // display all shops, skip the if statement and add this
                            // shopsList.add(modelShop);
                        }

                        // setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this, shopsList);

                        //set adapter to recycle list
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
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