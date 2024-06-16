package com.example.grocery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
        ModelProduct modelProduct = productList.get(position);
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

        if (discountAvailable.equals("true")) {
            // product is on discount
            holder.discountedNoteIv.setVisibility(View.VISIBLE);
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            // add strike through on original price
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // product is not on discount
            holder.discountedNoteIv.setVisibility(View.GONE);
            holder.discountedPriceTv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.add_shopping_cart_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.add_shopping_cart_primary);
        }

        holder.itemView.setOnClickListener(v -> {
            // handle item clicks, show item details

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
