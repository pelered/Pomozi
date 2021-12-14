package com.example.pomozi.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomozi.R;

class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewName,grad;
    public ImageView imageView;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.textView_name);
        imageView = itemView.findViewById(R.id.image_view_upload);
        grad=itemView.findViewById(R.id.card_grad);
    }
}
