package com.example.grocery.adapters;

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

import com.example.grocery.FilterProductUser;
import com.example.grocery.R;
import com.example.grocery.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = new ArrayList<>(productsList);
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        ModelProduct modelProduct = productsList.get(position);

        String discountAvailable = modelProduct.getDiscountNoteAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String originalPrice = modelProduct.getOriginalPrice();
        String productIcon = modelProduct.getProductIcon();

        // Set data
        holder.titleTv.setText(productTitle);
        holder.discountedNoteIv.setText(discountNote);
        holder.descriptionTv.setText(productDescription);
        holder.originalPriceTv.setText("$" + originalPrice);
        holder.discountedPriceTv.setText("$" + discountPrice);

        if ("true".equals(discountAvailable)) {
            // Product is on discount
            holder.discountedNoteIv.setVisibility(View.VISIBLE);
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            // Add strike-through on original price
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // Product is not on discount
            holder.discountedNoteIv.setVisibility(View.GONE);
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.add_shopping_cart_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.add_shopping_cart_primary);
        }

        holder.addToCartTv.setOnClickListener(v -> {
            // Add to cart
        });

        holder.itemView.setOnClickListener(v -> {
            // Show product details
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {
        // UI views
        private ImageView productIconIv;
        private TextView discountedNoteIv, titleTv, descriptionTv, addToCartTv, discountedPriceTv, originalPriceTv;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteIv = itemView.findViewById(R.id.discountedNoteIv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }
}
