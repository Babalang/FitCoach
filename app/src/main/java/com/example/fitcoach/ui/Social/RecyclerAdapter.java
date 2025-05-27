package com.example.fitcoach.ui.Social;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.fitcoach.R;

import java.time.Instant;
import java.util.List;

import ads_mobile_sdk.v7;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyviewHolder> {

    Context context;
    List<User> userList;

    public RecyclerAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_social,parent,false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyviewHolder holder, int position) {
        holder.idUser.setText(userList.get(position).getNom());

        holder.scoreUser.setText(userList.get(position).getScore());
    }

    @Override
    public int getItemCount() {
        if(userList != null){
            return userList.size();
        }
        return 0;

    }

    public class MyviewHolder extends RecyclerView.ViewHolder {
        TextView idUser;
        TextView scoreUser;

        public MyviewHolder(View itemView) {
            super(itemView);
            idUser = (TextView)itemView.findViewById(R.id.id);
            scoreUser = (TextView)itemView.findViewById(R.id.score);
        }
    }
}
