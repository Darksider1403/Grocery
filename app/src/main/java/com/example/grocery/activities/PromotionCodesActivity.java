package com.example.grocery.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterPromotionShop;
import com.example.grocery.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class PromotionCodesActivity extends AppCompatActivity {
    private ImageButton backBtn,addPromoBtn, filteredBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        addPromoBtn = findViewById(R.id.addPromoBtn);
        filteredBtn = findViewById(R.id.filterBtn);
        filteredTv = findViewById(R.id.filteredTv);
        promoRv = findViewById(R.id.promoRv);

        //init firebase auth to get current user
        firebaseAuth = FirebaseAuth.getInstance();
        loadAllPromoCodes();

        backBtn.setOnClickListener(v -> finish());
        addPromoBtn.setOnClickListener(v -> startActivity(new Intent(PromotionCodesActivity.this,AddPromotionCodeActivity.class)));

        //handle filter button click, show filter dialog
        filteredBtn.setOnClickListener(v -> filterDialog());
    }

    private void filterDialog() {
        //option to display in dialog
        String[] options = {"All", "Expired", "Not Expired"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, (dialog, i) -> {
                    //handle item click
                    if (i==0) {
                        //All clicked
                        filteredTv.setText("All Promotion Codes");
                        loadAllPromoCodes();
                    }
                    else if (i==1) {
                        //Expired clicked
                        filteredTv.setText("Expired Promotion Codes");
                        loadAllPromoCodes();
                    }
                    else if (i==2) {
                        //Not Expired clicked
                        filteredTv.setText("Not Expired Promotion Codes");
                        loadAllPromoCodes();
                    }
                })
        .show();
    }

    private void loadAllPromoCodes() {
        //init list
        promotionArrayList = new ArrayList<>();

        //db reference Users > current user > Promotion > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //setup adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadExpirePromoCodes() {
        //get current date
        DecimalFormat mFormat = new DecimalFormat("");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = day +"/"+ month +"/" + year; //e.g 23/06/2024

        //init list
        promotionArrayList = new ArrayList<>();

        //db reference Users > current user > Promotion > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            assert modelPromotion != null;
                            String expDate = modelPromotion.getExpireDate();

                            /*------Check for expired------*/
                            try {
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdformat.parse(todayDate);
                                Date expireDate = sdformat.parse(expDate);
                                assert expireDate != null;
                                if (expireDate.compareTo(currentDate) > 0) {
                                    //date 1 occurs after date 2
                                }
                                else if (expireDate.compareTo(currentDate) < 0) {
                                    //date 1 occurs before date 2 (i.e Expired)
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) == 0) {
                                    //both date equals
                                }
                            } catch (ParseException e) {

                            }

                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //setup adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNotExpirePromoCodes() {
        //get current date
        DecimalFormat mFormat = new DecimalFormat("");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = day +"/"+ month +"/" + year; //e.g 23/06/2024

        //init list
        promotionArrayList = new ArrayList<>();

        //db reference Users > current user > Promotion > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            assert modelPromotion != null;
                            String expDate = modelPromotion.getExpireDate();

                            /*------Check for expired------*/
                            try {
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdformat.parse(todayDate);
                                Date expireDate = sdformat.parse(expDate);
                                assert expireDate != null;
                                if (expireDate.compareTo(currentDate) > 0) {
                                    //date 1 occurs after date 2
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) < 0) {
                                    //date 1 occurs before date 2 (i.e Expired)
                                }
                                else if (expireDate.compareTo(currentDate) == 0) {
                                    //both date equals
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                            } catch (ParseException e) {

                            }

                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        //setup adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}