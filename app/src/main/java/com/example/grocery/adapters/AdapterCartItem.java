package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    static class HolderCartItem extends RecyclerView.ViewHolder {

        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitems, parent, false);
        return new HolderCartItem(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        ModelCartItem modelCartItem = cartItems.get(position);

        if (modelCartItem == null) {
            return;
        }

        String id = modelCartItem.getId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        holder.itemTitleTv.setText(title);
        holder.itemPriceTv.setText("$" + cost);
        holder.itemQuantityTv.setText("[" + quantity + "]");
        holder.itemPriceEachTv.setText("$" + price + "/Kg");

        holder.itemRemoveTv.setOnClickListener(v -> {
            EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                    .setTableName("ITEMS_TABLE")
                    .addColumn(new Column("Item_id", new String[]{"Text", "unique"}))
                    .addColumn(new Column("Item_PID", new String[]{"Text", "not null"}))
                    .addColumn(new Column("Item_Name", new String[]{"Text", "not null"}))
                    .addColumn(new Column("Item_Price_Each", new String[]{"Text", "not null"}))
                    .addColumn(new Column("Item_Price", new String[]{"Text", "not null"}))
                    .addColumn(new Column("Item_Quantity", new String[]{"Text", "not null"}))
                    .doneTableColumn();
            easyDB.deleteRow(1, id);

            Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();

            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());

            double subTotalWithoutDiscount = ((ShopDetailsActivity) context).allTotalPrice;
            double subTotalAfterProductRemove = subTotalWithoutDiscount - Double.parseDouble(cost.replace("$", ""));
            ((ShopDetailsActivity) context).allTotalPrice = subTotalAfterProductRemove;
            ((ShopDetailsActivity) context).sTotalTv.setText("$" + String.format("%.2f", ((ShopDetailsActivity) context).allTotalPrice));

            double promoPrice = Double.parseDouble(((ShopDetailsActivity) context).promoPrice);
            double deliveryFee = Double.parseDouble(((ShopDetailsActivity) context).deliveryFee.replace("$", ""));

            if (((ShopDetailsActivity) context).isPromoCodeApplied) {
                if (subTotalAfterProductRemove < Double.parseDouble(((ShopDetailsActivity) context).promoMinimumOrderPrice)) {
                    Toast.makeText(context, "This code is valid for order with minimum amount: $" + ((ShopDetailsActivity) context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                    ((ShopDetailsActivity) context).applyBtn.setVisibility(View.GONE);
                    ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.GONE);
                    ((ShopDetailsActivity) context).promoDescriptionTv.setText("");
                    ((ShopDetailsActivity) context).discountTv.setText("$0");
                    ((ShopDetailsActivity) context).isPromoCodeApplied = false;
                    ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", subTotalAfterProductRemove + deliveryFee));
                } else {
                    ((ShopDetailsActivity) context).applyBtn.setVisibility(View.VISIBLE);
                    ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.VISIBLE);
                    ((ShopDetailsActivity) context).promoDescriptionTv.setText(((ShopDetailsActivity) context).promoDescription);
                    ((ShopDetailsActivity) context).isPromoCodeApplied = true;
                    ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", subTotalAfterProductRemove + deliveryFee - promoPrice));
                }
            } else {
                ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", subTotalAfterProductRemove + deliveryFee));
            }

            ((ShopDetailsActivity) context).cartCount();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
