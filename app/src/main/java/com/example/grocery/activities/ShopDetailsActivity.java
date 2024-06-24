package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear list before adding data into it
                        ratingSum = 0;
                        int validRatingsCount = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String ratingStr = ds.child("rating").getValue(String.class);
                            if (ratingStr != null && !ratingStr.isEmpty()) {
                                try {
                                    float rating = Float.parseFloat(ratingStr);
                                    ratingSum += rating;
                                    validRatingsCount++;
                                } catch (NumberFormatException e) {
                                    Log.e("ShopDetailsActivity", "Invalid rating value: " + ratingStr, e);
                                }
                            } else {
                                Log.e("ShopDetailsActivity", "Null or empty rating value found.");
                            }
                        }

                        if (validRatingsCount > 0) {
                            float avgRating = ratingSum / validRatingsCount;
                            ratingBar.setRating(avgRating);
                        } else {
                            ratingBar.setRating(0); // Or any default value you prefer
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors
                        Log.e("ShopDetailsActivity", "Database error: " + error.getMessage());
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
    public TextView sTotalTv, dFeeTv, allTotalPriceTv,promoDescriptionTv,discountTv;
    public EditText promoCodeEt;
    public Button applyBtn;

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
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        applyBtn = view.findViewById(R.id.applyBtn);
        if(isPromoCodeApplied){
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);
        } else {
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
            applyBtn.setText("Apply");
        }
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
        if(isPromoCodeApplied){
            priceWithDiscount();
        }else {
            priceWithoutDiscount();
        }
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
            if (myLatitude.isEmpty() || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                Toast.makeText(ShopDetailsActivity.this, "Please enter your address before order", Toast.LENGTH_SHORT).show();
                return;
            }
            if (myPhone.isEmpty() || myPhone.equals("null")) {
                Toast.makeText(ShopDetailsActivity.this, "Please enter your phone before order", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cartItemList.isEmpty()) {
                Toast.makeText(ShopDetailsActivity.this, "No Item in cart", Toast.LENGTH_SHORT).show();
                return;
            }
            submitOrder();
        });
        //start validation code
        validateBtn.setOnClickListener(new View.OnClickListener() {

            /*Flow:
            *1) Get Code from EditText
            If not empty: promotion may be applied, otherwise no promotion *2) Check if code is valid i.e. Available id seller's promotion db If available: promotion may be applied, otherwise no promotion *3) Check if Expired or not
            *
            *
            If not expired: promotion may be applied, otherwise no promotion
            * 4) Check if Minimum Order price
            *
            If minimumOrderPrice is >= SubTotal Price: promotion available, otherwise no promotion*/
            @Override
            public void onClick(View v) {
                String promotionCode= promoCodeEt.getText().toString().trim();
                if(TextUtils.isEmpty(promotionCode)){
                    Toast.makeText(ShopDetailsActivity.this,"Please enter promo code",Toast.LENGTH_SHORT).show();
                }else{
                    checkCodeAvaibility(promotionCode);
                }
            }
        });
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromoCodeApplied = true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });
    }
    private void priceWithDiscount(){
        discountTv.setText("$"+ promoPrice);
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace( "$", ""))-Double.parseDouble(promoPrice)));

    }
    private void priceWithoutDiscount() {
        discountTv.setText("0$");
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace( "$", ""))));

    }
    public boolean isPromoCodeApplied = false;

    public String promoId, promoTimestamp, promoCode, promoDescription, promoExpDate, promoMinimumOrderPrice, promoPrice;
    private void checkCodeAvaibility(String promotionCode){
        //progress bar
        ProgressDialog progressDialog = new ProgressDialog( this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking Promo Code...");
        progressDialog.setCanceledOnTouchOutside (false);
        isPromoCodeApplied = false;
        applyBtn.setText("Apply");
        priceWithoutDiscount();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    progressDialog.dismiss();
                    for(DataSnapshot ds: snapshot.getChildren()){
                        promoId = ""+ds.child("id").getValue();
                        promoTimestamp=""+ds.child("timestamp").getValue();
                        promoCode = ""+ds.child("promoCode").getValue();
                        promoDescription = ""+ds.child("description").getValue();
                        promoExpDate = ""+ds.child("expireDate").getValue();
                        promoMinimumOrderPrice = ""+ds.child("minimumOrderPrice").getValue();
                        promoPrice = ""+ds.child("promoPrice").getValue();

                        //check code valid
                        checkCodeExpireDate();
                    }
                } else{
                    //Enter code that dont exsit
                    progressDialog.dismiss();
                    Toast.makeText(ShopDetailsActivity.this,"Invalid Code",Toast.LENGTH_SHORT).show();
                    applyBtn.setVisibility(View.GONE);
                    promoDescriptionTv.setVisibility(View.GONE);
                    promoDescriptionTv.setText("");
                }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        //Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;//it starts from instead of 1 thats why did +1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //concatenate date
        String todayDate = day + "/" + month + "/" + year; //e.g. 11/07/2020
        //check expire date
        try {
            SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = sdformat.parse(todayDate);
            Date expireDate = sdformat.parse(promoExpDate); // compare dates
            if (expireDate.compareTo(currentDate) > 0) {
                //date 1 occurs after date 2 (i.e. not expire date)
                checkMinimumOrderPrice();
            } else if (expireDate.compareTo(currentDate)<0) {
//date 1 occurs before date 2 (i.e. not expired)
                Toast.makeText(this, "The promotion code is expired on "+promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
                
            } else if (expireDate.compareTo(currentDate) == 0) {
                checkMinimumOrderPrice();
            }
        }
        catch (Exception e){
            Toast.makeText(ShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    private void checkMinimumOrderPrice() {

//each promo code have minimum order price requirement, if order price is less then required then don't allow to apply code
        if (Double.parseDouble(String.format("%.2f", allTotalPrice)) < Double.parseDouble(promoMinimumOrderPrice)){
            //current order price is less then minimum order price required by promo code, so don't allow to apply
            Toast.makeText( this, "This code is valid for order with minimum amount: $"+promoMinimumOrderPrice, Toast.LENGTH_SHORT).show(); applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    else {
            //current order price is equal to or greater than minimum order price required by promo code, allow to apply code
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
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
        hashMap.put("deliveryFee",""+deliveryFee);
        if(isPromoCodeApplied){
            hashMap.put("discount",""+promoCode);
        }
        else{
            hashMap.put("discount","0");
        }


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
                    progressDialog.dismiss();
                    Toast.makeText(ShopDetailsActivity.this, "Order placed success", Toast.LENGTH_SHORT).show();

                    prepareNotificationMessage(timestamp);



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

    private void prepareNotificationMessage(String orderId){
        //When user places order, send notificatioin to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subcribed by user
        String NOTIFICATION_TITLE = "New Order" + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try{
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); //since we are logged in as buyer to place order so current user uid is buyer uid
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https//fcm.googleapis.com/fcn/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                //after sending fcm start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, volleyError -> {
            //if failed sending fcm, still start order details activity
            Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderTo", shopUid);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        }){
            @Override
            public Map<String, String> getHeaders() {

                //put required heacers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}