package com.example.espacecoworking.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {

    private Context context;
    private List<byte[]> imageList;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public ImagePreviewAdapter(Context context, List<byte[]> images, OnImageClickListener listener) {
        this.context = context;
        this.imageList = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        byte[] imageData = imageList.get(position);

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        holder.imgPreview.setImageBitmap(bitmap);

        holder.imgRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imgPreview;
        ImageView imgRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imgPreview = itemView.findViewById(R.id.imgPreview);
            imgRemove = itemView.findViewById(R.id.imgRemove);
        }
    }
}