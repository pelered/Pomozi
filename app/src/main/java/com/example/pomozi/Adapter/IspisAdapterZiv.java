package com.example.pomozi.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pomozi.Model.ZivUpload;
import com.example.pomozi.PrikazZivFragment;
import com.example.pomozi.R;


import java.util.List;
import java.util.Random;

public class IspisAdapterZiv extends RecyclerView.Adapter<IspisAdapterZiv.ImageViewHolder>{
    private Context mContext;
    private List<ZivUpload> mUploads;
    public IspisAdapterZiv(Context context, List<ZivUpload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    public IspisAdapterZiv.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.card_view_ziv, parent, false);
        return new IspisAdapterZiv.ImageViewHolder(v);
    }
    private String getFileExtension(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
    public void onBindViewHolder(@NonNull final IspisAdapterZiv.ImageViewHolder holder, final int position) {
        final ZivUpload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText("Status: "+uploadCurrent.getStatus());
        holder.grad.setText("Grad: " +uploadCurrent.getGrad());
        holder.itemView.setOnClickListener(v -> {
            final int random = new Random().nextInt(100);
            PrikazZivFragment fragment=new PrikazZivFragment();
            Bundle args = new Bundle();
            args.putString("oznaka", uploadCurrent.getKey());
            fragment.setArguments(args);
            FragmentTransaction ft =((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, fragment);
            ft.addToBackStack("tag_ispis"+random);
            ft.commit();
        });

        if(uploadCurrent.getUrl()!=null && uploadCurrent.getUrl().size()!=0 ){
            Log.d("IspisA:",uploadCurrent.toString());
            if(uploadCurrent.getUrl().get("0_key").contains("mp4")){
                RequestOptions requestOptions = new RequestOptions();
                Glide.with(mContext)
                        .load(uploadCurrent.getUrl().get("0_key"))
                        .apply(requestOptions)
                        .thumbnail(Glide.with(mContext).load(uploadCurrent.getUrl().get("0_key")))
                        .into(holder.imageView);
            }else{
                Glide.with(mContext)
                        .load(uploadCurrent.getUrl().get("0_key"))
                        .optionalFitCenter()
                        .into(holder.imageView);
            }


        }
    }
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName,grad;
        public ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textView_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
            grad=itemView.findViewById(R.id.card_grad);
        }
    }
}
