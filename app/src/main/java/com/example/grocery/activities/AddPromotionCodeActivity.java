package com.example.grocery.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodeActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageView imageIv;
    private EditText promoCodeEt,promoDescriptionEt,promoPriceEt,minimumOrderPriceEt,expireDateTv;
    private Button addBtn;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
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
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //Handle date
        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickDialog();
            }
        });
        //Handle click add promo to db
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }
    private String description,promoCode,promoPrice,minimumOrderPrice,expireDate;
    private void inputData() {
        //input data
        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate=expireDateTv.getText().toString().trim();

        if(TextUtils.isEmpty(promoCode)){
            Toast.makeText(this,"Enter discount code...",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(description)){
            Toast.makeText(this,"Enter description...",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(promoPrice)){
            Toast.makeText(this,"Enter promotion price...",Toast.LENGTH_SHORT).show();
            return;
        } if(TextUtils.isEmpty(minimumOrderPrice)){
            Toast.makeText(this,"Enter minimum order price",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(expireDate)){
            Toast.makeText(this,"Enter discount code...",Toast.LENGTH_SHORT).show();
            return;
        }

        addDataToDB();
    }

    private void addDataToDB() {
        progressDialog.setMessage("Adding Promotion Code");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("description",""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice",""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);
        //init reference
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //code added
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this,"Promotion code added",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void datePickDialog() {
        //get current date
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        //date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                DecimalFormat mFormat = new DecimalFormat("00");
                String pDay = mFormat.format(dayOfMonth);
                String pMonth = mFormat.format(monthOfYear);
                String pYear = ""+year;
                String pDate = pDay +"/"+pMonth+"/"+pYear;//26/2/2090
                expireDateTv.setText(pDate);
            }
        },mYear,mMonth,mDay);
        //show dialog
        datePickerDialog.show();
        //diasable past date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);

    }
}