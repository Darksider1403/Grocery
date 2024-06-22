package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constants;
import com.example.grocery.ProgressDialogFragment;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.models.ModelOrderShop;
import com.example.grocery.models.ModelProduct;
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

public class MainSellerActivity extends AppCompatActivity {
    private TextView nameTv, shopNameTV,filteredOrdersTv, emailTV, tabProductsTv, tabOrdersTv, filteredProductsTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn,filterOrderBtn, editProfileBtn, addProductBtn, filterProductBtn, reviewsBtn,settingsBtn;
    private ImageView profileIv;
    private RelativeLayout productsRl, ordersRl;
    private RecyclerView productsRv,ordersRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;
    private ArrayList<ModelOrderShop>orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv = findViewById(R.id.nameTv);
        shopNameTV = findViewById(R.id.shopNameTV);
        emailTV = findViewById(R.id.emailTv);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);

        searchProductEt = findViewById(R.id.searchProductEt);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);

        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        profileIv = findViewById(R.id.profileIv);
        productsRl = findViewById(R.id.productRl);
        ordersRl = findViewById(R.id.ordersRl);
        productsRv = findViewById(R.id.productsRv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        // search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                try {
                    adapterProductSeller.getFilter().filter(s);
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

        logoutBtn.setOnClickListener(v -> {
            // make offline
            // sign out
            // go to login screen
            makeMeOffline();
        });

        editProfileBtn.setOnClickListener(v -> {
            //open edit profile activity
            startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
        });
        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Fillter options:").setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            filteredOrdersTv.setText("Showing all Orders");
                            adapterOrderShop.getFilter().filter("");//showing all
                        } else {
                            String optionClicked = options[which];
                            filteredOrdersTv.setText("Showing" + optionClicked + "Orders");
                            adapterOrderShop.getFilter().filter(optionClicked);
                        }
                    }
                }).show();
            }
        });



        addProductBtn.setOnClickListener(v -> startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class)));
        tabOrdersTv.setOnClickListener(v -> showOrdersUI());
        tabProductsTv.setOnClickListener(v -> showProductsUI());
        filterProductBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Choose Category:")
                    .setItems(Constants.productCategories1, (dialog, which) -> {
                        // get selected item
                        String selectedItem = Constants.productCategories1[which];
                        filteredProductsTv.setText(selectedItem);

                        if (selectedItem.equals("All")) {
                            // load all
                            loadAllProducts();
                        } else {
                            // load filtered
                            loadFilteredProducts(selectedItem);
                        }
                    })
                    .show();
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open same reviews activity as used in user main page
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this,SettingsActivity.class));
            }
        });

    }



    private void loadAllOrders() {
        orderShopArrayList= new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderShopArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                    //add to list
                    orderShopArrayList.add(modelOrderShop);
                }
                adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,orderShopArrayList);
                ordersRv.setAdapter(adapterOrderShop);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFilteredProducts(String selectedItem) {
        productList = new ArrayList<>();

        // get all products
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // before getting reset list
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String productCategory = "" + ds.child("productCategory").getValue();

                            // if selected category matches product category then add in list
                            if (selectedItem.equals(productCategory)) {
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }

                        // setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);

                        // set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        // get all products
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // before getting reset list
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }

                        // setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);

                        // set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void showProductsUI() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);
        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
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
                    Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
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
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String email = "" + ds.child("email").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();


                            nameTv.setText(name);
                            shopNameTV.setText(shopName);
                            emailTV.setText(email);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.local_grocery_store_grey).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.local_grocery_store_grey);
                            }
                        }
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
