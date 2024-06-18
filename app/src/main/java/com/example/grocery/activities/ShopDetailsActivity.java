package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopDetailsActivity extends AppCompatActivity {
//Declare ui view

 private    ImageView shopIv;
 private TextView shopNameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv;
 private ImageButton callBtn,mapBtn, cartBtn,backBtn,filterProductBtn;
 private EditText searchProductEt;
 private RecyclerView productsRv;
 private String shopUid;
 private FirebaseAuth firebaseAuth;
 private String myLatitude,myLongtitude;
 private String shopLatitude,shopLongtitude,shopName,shopEmail,shopAddress,shopPhone;
 private ArrayList<ModelProduct>productsList;
 private AdapterProductUser adapterProductUser;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        //get UID shop
        shopUid=getIntent().getStringExtra("shopUid");


        phoneTv.findViewById(R.id.phoneTv);


        shopIv.findViewById(R.id.shopIv);
        shopNameTv.findViewById(R.id.shopNameTv);

        emailTv.findViewById(R.id.emailTV);
        openCloseTv.findViewById(R.id.openCloseTV);
        deliveryFeeTv.findViewById(R.id.deliveryFeeTv);
        addressTv.findViewById(R.id.addressTv);
        filteredProductsTv.findViewById(R.id.filteredProductsTv);
        callBtn.findViewById(R.id.callBtn);
        mapBtn.findViewById(R.id.mapBtn);
        cartBtn.findViewById(R.id.cartBtn);
        filterProductBtn.findViewById(R.id.filterProductBtn);
        backBtn.findViewById(R.id.backBtn);
        searchProductEt.findViewById(R.id.searchProductEt);
        productsRv= findViewById(R.id.productsRl);
        firebaseAuth=FirebaseAuth.getInstance();
        loadMyinfo();
        loadShopDetails();
        loadShopProducts();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Filter Products:")
                        .setItems(Constants.productCategories1, (dialog, which) -> {
                            // get selected item
                            String selectedItem = Constants.productCategories1[which];
                            filteredProductsTv.setText(selectedItem);

                            if (selectedItem.equals("All")) {
                                // load all
                                loadShopProducts();
                            } else {
                                // load filtered
                               adapterProductUser.getFilter().filter(selectedItem);
                            }
                        })
                        .show();
            }
        });
    }

    private void openMap() {
        String address= "https://maps.google.com.maps?sadd="+myLatitude +","+myLongtitude+"&daddr="+shopLatitude+","+shopLongtitude;
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);
    }

    private void diaPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("Tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this,""+shopPhone,Toast.LENGTH_SHORT).show();
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productsList.add(modelProduct);

                }
//                set up adapter
                adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productsList);
                productsRv.setAdapter(adapterProductUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get Shop data
                String name =""+snapshot.child("name").getValue();
                 shopName =""+snapshot.child("shopName").getValue();
                 shopEmail =""+snapshot.child("email").getValue();
                 shopPhone =""+snapshot.child("phone").getValue();
                 shopAddress =""+snapshot.child("address").getValue();
                 shopLatitude =""+snapshot.child("latitude").getValue();
                 shopLongtitude =""+snapshot.child("longtitude").getValue();
                String deliveryFee =""+snapshot.child("deliveryFee").getValue();
               String  profileImage =""+snapshot.child("profileImage").getValue();
               String  shopOpen =""+snapshot.child("shopOpen").getValue();

//               set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if(shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                }else {
                    openCloseTv.setText("Close");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                } catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadMyinfo() {
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
                            myLatitude = "" + ds.child("latitude").getValue();
                             myLongtitude = "" + ds.child("longtitude").getValue();


                            // set user data
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }


}