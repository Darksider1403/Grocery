package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProduct;
import com.example.grocery.R;
import com.example.grocery.activities.EditProductActivity;
import com.example.grocery.models.ModelProduct;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    static class HolderProductSeller extends RecyclerView.ViewHolder {
        /* holds views of recycler view */
        private ImageView productIconIv;
        private TextView discountedNoteIv, titleTv, quantityTv, discountedPriceTv, originalPriceTv;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteIv = itemView.findViewById(R.id.discountedNoteIv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);

        return new HolderProductSeller(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        // get data
        final ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountNoteAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimeStamp();
        String originalPrice = modelProduct.getOriginalPrice();

        // set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteIv.setText(discountNote);
        holder.discountedPriceTv.setText("$" + discountPrice);
        holder.originalPriceTv.setText("$" + originalPrice);

        if (discountAvailable != null && discountAvailable.equals("true")) {
            // product is on discount
            holder.discountedNoteIv.setVisibility(View.VISIBLE);
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            // add strike through on original price
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // product is not on discount
            holder.discountedNoteIv.setVisibility(View.GONE);
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.add_shopping_cart_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.add_shopping_cart_primary);
        }

        // handle item clicks, show item details
        holder.itemView.setOnClickListener(v -> {
            //handle item clicks, show item details (in bottom sheet)
            detailsBottomSheet(modelProduct); //here modelProduct contains detail of clicked product
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
        // set view to bottomsheet
        bottomSheetDialog.setContentView(view);

        //init views of bottomsheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountNoteAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimeStamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText("$" + discountPrice);
        originalPriceTv.setText("$" + originalPrice);
        if (discountAvailable != null && discountAvailable.equals("true")) {
            // product is on discount
            discountNoteTv.setVisibility(View.VISIBLE);
            discountedPriceTv.setVisibility(View.VISIBLE);
            // add strike through on original price
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // product is not on discount
            discountNoteTv.setVisibility(View.GONE);
            discountedPriceTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.add_shopping_cart_primary).into(productIconIv);
        } catch (Exception e) {
            productIconIv.setImageResource(R.drawable.add_shopping_cart_primary);
        }

        //show dialog
        bottomSheetDialog.show();
        //edit click
        editBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            //open edit product activity, pass id of product
            Intent intent = new Intent(context, EditProductActivity.class);
            intent.putExtra("productId", id);
            context.startActivity(intent);

        });

        //delete click
        deleteBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            //show delete confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete")
                    .setMessage("Are you sure want to delete product " + title + " ?")
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        //delete
                        deleteProduct(id); //id is the product id
                    })
                    .setNegativeButton("NO", (dialog, which) -> {
                        //cancel, dismiss dialog
                        dialog.dismiss();
                    });
        });
        //back click
        backBtn.setOnClickListener(v -> {
            //dismiss bottom sheet
            bottomSheetDialog.dismiss();
        });
    }

    private void deleteProduct(String id) {
        //delete product using its id

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products").child(id).removeValue()
                .addOnSuccessListener(unused -> {
                    //product deleted
                    Toast.makeText(context, "Product deleted...", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    //failed deleting product
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProduct(this, filterList);
        }

        return filter;
    }
}
