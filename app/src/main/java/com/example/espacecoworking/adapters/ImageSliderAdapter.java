package com.example.espacecoworking.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.SpaceImages;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private List<SpaceImages> imagesList;

    // Constructeur
    public ImageSliderAdapter(List<SpaceImages> imagesList) {
        this.imagesList = imagesList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {

        SpaceImages imgData = imagesList.get(position);

        // 1. Si on a une  image stockée (byte[])
        if (imgData.getImage() != null && imgData.getImage().length > 0) {
            try {
                // On transforme les bytes en Bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(imgData.getImage(), 0, imgData.getImage().length);
                holder.imageView.setImageBitmap(bmp);
            } catch (Exception e) {
                // Si l'image est corrompue
                holder.imageView.setImageResource(R.drawable.img);
            }
        }
        // 2. Sinon
        else {
            holder.imageView.setImageResource(R.drawable.img);
        }
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    // Le ViewHolder garde en mémoire les vues pour éviter de les rechercher tout le temps
    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            // On lie l'ImageView du XML
            imageView = itemView.findViewById(R.id.imgSlider);
        }
    }
}