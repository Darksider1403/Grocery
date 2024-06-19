package com.example.grocery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolerOrderUser>{
    Context context;
    ArrayList<ModelOrderUser>orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }


    @NonNull
    @Override
    public HolerOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflare layut
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolerOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolerOrderUser holder, int position) {
//get data
        ModelOrderUser modelOrderUser = orderUserList.get(position);
        String orderId = modelOrderUser.getOrderId();
        String orderBy = modelOrderUser.getOrderBy();
        String orderCost = modelOrderUser.getOrderCost();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderTime = modelOrderUser.getOrderTime();
        String orderTo = modelOrderUser.getOrderTo();
//set data
        holder.amountTv.setText("Amount $:" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Orderid"+orderId);
        //change status
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }else if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        //conver timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();
        holder.dateTv.setText(formatedDate);

        //Get shopinfo
        loadShopInfo(modelOrderUser,holder);

    }


    private void loadShopInfo(ModelOrderUser modelOrderUser, HolerOrderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(modelOrderUser.getOrderTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = ""+snapshot.child("shopName").getValue();
                holder.shopNameTv.setText(shopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    class HolerOrderUser extends RecyclerView.ViewHolder{
        //View of layout
       private TextView orderIdTv,dateTv,shopNameTv,amountTv,statusTv;
       private ImageView nextIv;

        public HolerOrderUser(@NonNull View itemView) {
            super(itemView);
            orderIdTv=itemView.findViewById(R.id.orderIdTv);
            dateTv=itemView.findViewById(R.id.dateTv);
            shopNameTv=itemView.findViewById(R.id.shopNameTv);
            statusTv=itemView.findViewById(R.id.statusTv);
            amountTv=itemView.findViewById(R.id.amountTv);
            nextIv=itemView.findViewById(R.id.nextIv);
        }
    }
}
