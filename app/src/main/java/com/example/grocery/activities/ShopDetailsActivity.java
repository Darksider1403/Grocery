package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.example.grocery.adapters.AdapterCartItem;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.models.ModelCartItem;
import com.example.grocery.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
//Declare ui view

    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductsTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private String myLatitude, myLongitude;
    private String shopLatitude, shopLongitude, shopName, shopEmail, shopAddress, shopPhone;
    public String deliveryFee;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    // cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //get UID shop
        shopUid = getIntent().getStringExtra("shopUid");

        phoneTv = findViewById(R.id.phoneTv);
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        // each shop have it own products and orders so if user add items to cart
        // and go back and open cart in different shop
        // then cart should be different
        // so delete cart data whenever user open this activity
        deleteCartData();

        if (shopIv == null || shopNameTv == null || phoneTv == null || emailTv == null || openCloseTv == null ||
                deliveryFeeTv == null || addressTv == null || callBtn == null || mapBtn == null || cartBtn == null || backBtn == null) {
            System.out.println("One of the views is null");
        }

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

        backBtn.setOnClickListener(v -> finish());

        cartBtn.setOnClickListener(v -> {
            // show cart dialog
            showCartDialog();
        });
        callBtn.setOnClickListener(v -> diaPhone());
        mapBtn.setOnClickListener(v -> openMap());
        filterProductBtn.setOnClickListener(v -> {
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
        });
    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"Text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"Text", "not null"}))
                .doneTableColumn();

        easyDB.deleteAllDataFromTable();    // delete all records from cart
    }

    public double allTotalPrice = 0.00;
    // need to access these views in adapter
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showCartDialog() {
        // init list
        cartItemList = new ArrayList<>();

        // inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.diaglog_cart, null);

        // init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"Text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"Text", "not null"}))
                .doneTableColumn();

        // get all records  from db
        Cursor res = easyDB.getAllData();

        while (res.moveToNext()) {
            String id = res.getString(1);
            String pid = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem cartItem = new ModelCartItem(
                    "" + id,
                    "" + pid,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity);

            cartItemList.add(cartItem);
        }

        // setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        // set to recycle view
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // reset total price on dialog
        dialog.setOnCancelListener(dialog1 -> {
            allTotalPrice = 0.00;
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openMap() {
        // Check if latitude and longitude values are not null or empty
        if (myLatitude == null || myLongitude == null || shopLatitude == null || shopLongitude == null ||
                myLatitude.isEmpty() || myLongitude.isEmpty() || shopLatitude.isEmpty() || shopLongitude.isEmpty()) {
            Toast.makeText(this, "Location coordinates are not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = "https://www.google.com/maps/dir/?api=1&origin=" + myLatitude + "," + myLongitude + "&destination=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Try opening without specifying the package
            intent.setPackage(null);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void diaPhone() {
        if (shopPhone == null || shopPhone.isEmpty()) {
            Toast.makeText(this, "Phone number is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone)));
            if (dialIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productsList.add(modelProduct);
                }
//                set up adapter
                adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get Shop data
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longtitude").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();

                // Ensure these values are not null
                if (myLatitude == null) {
                    myLatitude = ""; // or set to some default value
                }
                if (myLongitude == null) {
                    myLongitude = ""; // or set to some default value
                }
                if (shopLatitude == null) {
                    shopLatitude = ""; // or set to some default value
                }
                if (shopLongitude == null) {
                    shopLongitude = ""; // or set to some default value
                }

//               set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $" + deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")) {
                    openCloseTv.setText("Open");
                } else {
                    openCloseTv.setText("Close");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longtitude").getValue();


                            // set user data
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}