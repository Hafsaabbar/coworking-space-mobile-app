package com.example.espacecoworking.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.activities.SpaceDetailActivity;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.SpaceImages;
import com.example.espacecoworking.repository.Repository;

import java.util.List;

public class SpaceAdapter extends RecyclerView.Adapter<SpaceAdapter.SpaceViewHolder> {

    private Context context;
    private List<Space> spaceList;
    private Repository repository;

    public SpaceAdapter(Context context, List<Space> spaces) {
        this.context = context;
        this.spaceList = spaces;
        this.repository = Repository.getInstance(context);
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // on utilise ton layout item_space.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_client_space, parent, false);
        return new SpaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpaceViewHolder holder, int position) {
        Space space = spaceList.get(position);

        // 1. Remplissage des textes
        holder.txtName.setText(space.getName());
        holder.txtLocation.setText(space.getLocation());
        holder.txtCapacity.setText("Capacité : " + space.getCapacity());

        // 2. GESTION DE L'IMAGE
        try {
            // On demande au repository la première image liée à cet espace
            SpaceImages imgData = repository.getFirstImageBySpaceId(space.getSpaceId());

            if (imgData != null && imgData.getImage() != null && imgData.getImage().length > 0) {
                // Conversion du BLOB en Bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(imgData.getImage(), 0, imgData.getImage().length);
                holder.imgSpace.setImageBitmap(bmp);
            } else {
                // Si pas d'image en base, on garde l'image par défaut du XML
                holder.imgSpace.setImageResource(R.drawable.img);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.imgSpace.setImageResource(R.drawable.img);
        }

        // 3. Clic sur la carte pour aller aux détails
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SpaceDetailActivity.class);
            intent.putExtra("SPACE_ID", space.getSpaceId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return spaceList.size();
    }

    static class SpaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSpace;
        TextView txtName, txtLocation, txtCapacity;

        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSpace = itemView.findViewById(R.id.imgSpace);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtCapacity = itemView.findViewById(R.id.txtCapacity);
        }
    }
}