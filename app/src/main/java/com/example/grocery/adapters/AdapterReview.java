package com.example.grocery.adapters;

import static java.text.DateFormat.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {

    private Context context;
    private ArrayList<ModelReview> reviewArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout row_review
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data at postition
        ModelReview modelReview = reviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String timestamp = modelReview.getTimestamp();
        String review = modelReview.getReview();

        //we also need info (profile image, name) of user who wrote the review: we can do it using uid of user
        loadUserDetail(modelReview, holder);

        //convert timestamp to proper format dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = android.text.format.DateFormat.format("dd/MM/yyyy", calendar).toString();

        //set data
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
        holder.dateTv.setText(dateFormat);

    }

    private void loadUserDetail(ModelReview modelReview, HolderReview holder) {
        //uid of user who wrote review
        String uid = modelReview.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://grocery-c0677-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get user info, use same key name as in fire base
                        String name = ""+dataSnapshot.child("name").getValue();
                        String profileImage = ""+dataSnapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTv.setText(name);
                        try{
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(holder.profileIv);
                        }
                        catch (Exception e){
                            //if anything goes wrong setting image (exepction occurs), set default image
                            holder.profileIv.setImageResource(R.drawable.ic_store_grey);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size(); // return list size
    }

    //view holder class, holds/inits views of recyclerview
    class HolderReview extends RecyclerView.ViewHolder{

        //ui views of layout row_review
        private ImageView profileIv;
        private TextView nameTv, dateTv, reviewTv;
        private RatingBar ratingBar;
        public HolderReview(@NonNull View itemView) {
            super(itemView);

            //init views of row_review
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);

        }
    }
}
