package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constants;
import com.example.grocery.ProgressDialogFragment;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCartItem;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelCartItem;
import com.example.grocery.models.ModelProduct;
import com.example.grocery.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
//Declare ui view

    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductsTv, cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn, reviewBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private String myLatitude, myLongitude, myPhone;
    private String shopLatitude, shopLongitude, shopName, shopEmail, shopAddress, shopPhone;
    public String deliveryFee;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;
    private ProgressDialog progressDialog;

    // cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;
    private EasyDB easyDB;

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
        cartCountTv = findViewById(R.id.cartCountTv);
        productsRv.setLayoutManager(new LinearLayoutManager(this));
        reviewBtn = findViewById(R.id.reviewBtn);
        ratingBar = findViewById(R.id.ratingBar);

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews(); //avg rating, set on ratingbar
        //Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"Text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"Text", "not null"}))
                .doneTableColumn();

        // each shop have it own products and orders so if user add items to cart
        // and go back and open cart in different shop
        // then cart should be different
        // so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

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

        //handle reviewBtn click, open reivews activity
        reviewBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //pass shop uid to show its reviews
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid). child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        ratingSum = 0;
                        for(DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("rating").getValue());
                            ratingSum = ratingSum +rating; //for avg rating, add(addtition of) all ratings, later will divide it by number of reviews


                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingBar.setRating(avgRating);
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

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();    // delete all records from cart
    }

    @SuppressLint("SetTextI18n")
    public void cartCount() {
        // get cart count
        int count = easyDB.getAllData().getCount();

        if (count <= 0) {
            // no item in cart, hide cart count text view
            cartCountTv.setVisibility(View.GONE);
        } else {
            // have items in cart, show cart count text view and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);
        }
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

            ModelCartItem modelCartItem = new ModelCartItem(
                    "" + id,
                    "" + pid,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity);

            cartItemList.add(modelCartItem);
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

        // reset total price on dialog dismiss
        dialog.setOnCancelListener(dialogInterface -> allTotalPrice = 0.00);

        // place order
        checkoutBtn.setOnClickListener(v -> {
            Log.d("DEBUG", "myLatitude: " + myLatitude);
            Log.d("DEBUG", "myLongitude: " + myLongitude);
            Log.d("DEBUG", "myPhone: " + myPhone);
            if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                Toast.makeText(ShopDetailsActivity.this, "Please enter your address before order", Toast.LENGTH_SHORT).show();
                return;
            }
            if (myPhone.equals("") || myPhone.equals("null") || myPhone.equals("") || myPhone.equals("null")) {
                Toast.makeText(ShopDetailsActivity.this, "Please enter your phone before order", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cartItemList.size() == 0) {
                Toast.makeText(ShopDetailsActivity.this, "No Item in cart", Toast.LENGTH_SHORT).show();
                return;
            }
            submitOrder();
        });
    }

    private void submitOrder() {
        showProgressDialog("Placing Order...");
        progressDialog.show();
        //for order id and order time
        String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("$", ""); //remove $ if contains

        //setup oder data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress"); // In Progress/Completed/Cancelled
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);

        //add To DB
        final DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    //order info added now add order items
                    for (int i = 0; i < cartItemList.size(); i++) {
                        String pId = cartItemList.get(i).getpId();
                        String id = cartItemList.get(i).getId();
                        String cost1 = cartItemList.get(i).getCost();
                        String name = cartItemList.get(i).getName();
                        String price = cartItemList.get(i).getPrice();
                        String quantity = cartItemList.get(i).getQuantity();

                        HashMap<String, String> hashMap1 = new HashMap<>();
                        hashMap1.put("pId", pId);
                        hashMap1.put("name", name);
                        hashMap1.put("cost", cost1);
                        hashMap1.put("price", price);
                        hashMap1.put("quantity", quantity);

                        ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                    }
                    hideProgressDialog();
                    Toast.makeText(ShopDetailsActivity.this, "Order placed success", Toast.LENGTH_SHORT).show();

                    // open order details, we need to keys there, orderId, orderTo
                    Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                    intent.putExtra("orderId", shopUid);
                    intent.putExtra("orderTo", timestamp);

                    startActivity(intent);
                }).addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();

                            // set user data

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}