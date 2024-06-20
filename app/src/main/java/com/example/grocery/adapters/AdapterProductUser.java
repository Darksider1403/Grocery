package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProductUser;
import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        final ModelProduct modelProduct = productsList.get(position);

        String discountAvailable = modelProduct.getDiscountNoteAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timeStamp = modelProduct.getTimeStamp();
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
            showQuantityDialog(modelProduct);
        });

        holder.itemView.setOnClickListener(v -> {
            // Show product details
        });
    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;

    @SuppressLint("SetTextI18n")
    private void showQuantityDialog(ModelProduct modelProduct) {
        // Check if any essential product data is null
        if (modelProduct == null || modelProduct.getProductId() == null || modelProduct.getProductTitle() == null) {
            Toast.makeText(context, "Product details are missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        // Init layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantity);
        TextView descriptionTv = view.findViewById(R.id.decriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalTv = view.findViewById(R.id.finalTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        // Get data from model
        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();
        String price;

        if ("true".equals(modelProduct.getDiscountNoteAvailable())) {
            price = modelProduct.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // No discount
            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("\\$", ""));
        finalCost = Double.parseDouble(price.replaceAll("\\$", ""));
        quantity = 1;

        // Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        // Set data
        try {
            Picasso.get().load(image).placeholder(R.drawable.local_grocery_store_primary).into(productIv);
        } catch (Exception e) {
            productIv.setImageResource(R.drawable.local_grocery_store_primary);
        }

        titleTv.setText(title);
        pQuantityTv.setText(productQuantity);
        descriptionTv.setText(description);
        discountedNoteTv.setText(discountNote);
        quantityTv.setText(String.valueOf(quantity));
        originalPriceTv.setText("$" + modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("$" + modelProduct.getDiscountPrice());
        finalTv.setText("$" + finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Increment quantity
        incrementBtn.setOnClickListener(v -> {
            finalCost = finalCost + cost;
            quantity++;
            finalTv.setText("$" + finalCost);
            quantityTv.setText(String.valueOf(quantity));
        });

        // Decrement quantity
        decrementBtn.setOnClickListener(v -> {
            if (quantity > 1) {
                finalCost = finalCost - cost;
                quantity--;
                finalTv.setText("$" + finalCost);
                quantityTv.setText(String.valueOf(quantity));
            }
        });

        continueBtn.setOnClickListener(v -> {
            String title1 = titleTv.getText().toString().trim();
            String quantity = quantityTv.getText().toString().trim();
            String priceEach = price;
            String totalPrice = finalTv.getText().toString().trim().replace("$", "");
            // Add to db(SQLite)
            addToCart(productId, title1, priceEach, totalPrice, quantity);
            dialog.dismiss();
        });
    }

    private int itemId = 1;

    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"Text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"Text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"Text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Adding to Cart....", Toast.LENGTH_SHORT).show();

        ((ShopDetailsActivity) context).cartCount();
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

    static class HolderProductUser extends RecyclerView.ViewHolder {
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
