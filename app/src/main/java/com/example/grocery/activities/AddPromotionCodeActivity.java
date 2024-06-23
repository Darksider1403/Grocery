package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class AddPromotionCodeActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageView imageIv;
    private EditText promoCodeEt, promoDescriptionEt, promoPriceEt, minimumOrderPriceEt;
    private Button addBtn;
    private TextView expireDateTv, titleTv;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    private String promoId;
    private boolean isUpdating = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);
        //init
        backBtn = findViewById(R.id.backBtn);
        imageIv = findViewById(R.id.imageIv);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumOrderPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        addBtn = findViewById(R.id.addBtn);
        titleTv = findViewById(R.id.titleTv);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // get promo id from intent
        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null) {
            promoId = intent.getStringExtra("promoId");
            titleTv.setText("Update Promotion Code");
            addBtn.setText("Update");

            isUpdating = true;
            loadPromoInfo();
        } else {
            promoId = intent.getStringExtra("promoId");
            titleTv.setText("Add Promotion Code");
            addBtn.setText("Add");

            isUpdating = false;
        }

        backBtn.setOnClickListener(v -> finish());
        //Handle date
        expireDateTv.setOnClickListener(v -> datePickDialog());
        //Handle click add promo to db
        addBtn.setOnClickListener(v ->  inputData());
    }

    private void loadPromoInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("User");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String id = "" + snapshot.child("id").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String promoCode = "" + snapshot.child("promoCode").getValue();
                        String promoPrice = "" + snapshot.child("promoPrice").getValue();
                        String miniOrderPrice = "" + snapshot.child("miniOrderPrice").getValue();
                        String expiredDate = "" + snapshot.child("expiredDate").getValue();

                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumOrderPriceEt.setText(miniOrderPrice);
                        expireDateTv.setText(expiredDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;

    private void inputData() {
        //input data
        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();

        if (TextUtils.isEmpty(promoCode)) {
            Toast.makeText(this, "Enter discount code...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(promoPrice)) {
            Toast.makeText(this, "Enter promotion price...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(minimumOrderPrice)) {
            Toast.makeText(this, "Enter minimum order price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(expireDate)) {
            Toast.makeText(this, "Enter discount code...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating) {
            updateDataToDB();
        } else {
            addDataToDB();
        }
    }

    private void updateDataToDB() {
        progressDialog.setMessage("Adding Promotion Code");
        progressDialog.show();

        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("description", "" + description);
        hashMap.put("promoCode", "" + promoCode);
        hashMap.put("promoPrice", "" + promoPrice);
        hashMap.put("minimumOrderPrice", "" + minimumOrderPrice);
        hashMap.put("expireDate", "" + expireDate);

        //init reference
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions").child(promoId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    // updated
                    progressDialog.dismiss();
                    Toast.makeText(AddPromotionCodeActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddPromotionCodeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addDataToDB() {
        progressDialog.setMessage("Adding Promotion Code");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("description", "" + description);
        hashMap.put("promoCode", "" + promoCode);
        hashMap.put("promoPrice", "" + promoPrice);
        hashMap.put("minimumOrderPrice", "" + minimumOrderPrice);
        hashMap.put("expireDate", "" + expireDate);
        //init reference
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    //code added
                    progressDialog.dismiss();
                    Toast.makeText(AddPromotionCodeActivity.this, "Promotion code added", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(AddPromotionCodeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void datePickDialog() {
        //get current date
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        //date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            DecimalFormat mFormat = new DecimalFormat("00");
            String pDay = mFormat.format(dayOfMonth);
            String pMonth = mFormat.format(monthOfYear);
            String pYear = "" + year;
            String pDate = pDay + "/" + pMonth + "/" + pYear;//26/2/2090
            expireDateTv.setText(pDate);
        }, mYear, mMonth, mDay);
        //show dialog
        datePickerDialog.show();
        //diasable past date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }
}