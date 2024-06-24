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

    // view holder class
    static class HolderCartItem extends RecyclerView.ViewHolder {

        // ui views of row_cart_items.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            // init views
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

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        // get data
        ModelCartItem modelCartItem = cartItems.get(position);

        String id = modelCartItem.getId();
        String getpId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        // set data
        holder.itemTitleTv.setText(title);
        holder.itemPriceTv.setText(cost);
        holder.itemQuantityTv.setText("[" + quantity + "]");   // e.g. [3]
        holder.itemPriceEachTv.setText(price);

        // handle remove click listener, delete item from cart
        holder.itemRemoveTv.setOnClickListener(v -> {
                    // will create table if not exists, but in that case will must exist
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

                    // refresh list
                    cartItems.remove(position);
                    notifyItemChanged(position);
                    notifyDataSetChanged();


//            double tx = Double.parseDouble((((ShopDetailsActivity) context).allTotalPriceTv.getText().toString().trim().replace("$", "")));
//            double totalPrice = tx - Double.parseDouble(cost.replace("$", ""));
//            double deliveryFee =  Double.parseDouble((((ShopDetailsActivity) context).deliveryFee.replace("$", "")));
//            double sTotalPrice =  Double.parseDouble(String.format("%.2f", totalPrice)) - Double.parseDouble(String.format("$.2f", deliveryFee));
//            ((ShopDetailsActivity) context).allTotalPrice = 0.00;
//            ((ShopDetailsActivity) context).sTotalTv.setText("$" + String.format("%.2f", sTotalPrice));
//            ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", totalPrice))));

                    //adjust the subtotal after product remove
                    double subTotalWithoutDiscount = ((ShopDetailsActivity) context).allTotalPrice;
                    double subTotalAfterProductRemove = subTotalWithoutDiscount - Double.parseDouble(cost.replace("$", ""));
                    ((ShopDetailsActivity) context).allTotalPrice = subTotalAfterProductRemove;
                    ((ShopDetailsActivity) context).sTotalTv.setText("$" + String.format("%.2f", ((ShopDetailsActivity) context).allTotalPrice));
                    //once subtotal is updated...check minimum order price of promo code
                    double promoPrice = Double.parseDouble(((ShopDetailsActivity) context).promoPrice);
                    double deliveryFee = Double.parseDouble(((ShopDetailsActivity) context).deliveryFee.replace("$", ""));
                    //check if promo code applied
                    if (((ShopDetailsActivity) context).isPromoCodeApplied) {
                        //applied
                        if (subTotalAfterProductRemove < Double.parseDouble(((ShopDetailsActivity) context).promoMinimumOrderPrice)) {
                            //current order price is less then minimum required price
                            Toast.makeText(context, "This code is valid for order with minimum amount: $" + ((ShopDetailsActivity) context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                            ((ShopDetailsActivity) context).applyBtn.setVisibility(View.GONE);
                            ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.GONE); ((ShopDetailsActivity) context).promoDescriptionTv.setText("");
                            ((ShopDetailsActivity) context).discountTv.setText("$0");
                            ((ShopDetailsActivity) context).isPromoCodeApplied = false;
                            // show new net total after delivery fee
                            ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));
                        } else {
                            ((ShopDetailsActivity) context).applyBtn.setVisibility(View.VISIBLE);
                            ((ShopDetailsActivity) context). promoDescriptionTv.setVisibility(View.VISIBLE);
                            ((ShopDetailsActivity) context).promoDescriptionTv.setText(((ShopDetailsActivity) context).promoDescription);
                            //show new total price after adding delivery fee and subtracting promo fee
                            ((ShopDetailsActivity) context).isPromoCodeApplied = true;
                            ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee-promoPrice))));

                        }
                    }else{
                        ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));
                    }




            // after removing item from cart, update cart count
            ((ShopDetailsActivity) context).cartCount();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
