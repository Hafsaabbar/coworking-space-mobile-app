package com.example.espacecoworking.adapters;

import android.content.Context;
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
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.SpaceImages;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class OwnerSpaceAdapter extends RecyclerView.Adapter<OwnerSpaceAdapter.SpaceViewHolder> {

    private Context context;
    private List<Space> spaceList;
    private Repository repository;
    private OnSpaceActionListener listener;

    public interface OnSpaceActionListener {
        void onEditSpace(Space space);
        void onDeleteSpace(Space space);
        void onViewBookings(Space space);
    }

    public OwnerSpaceAdapter(Context context, List<Space> spaces, OnSpaceActionListener listener) {
        this.context = context;
        this.spaceList = spaces;
        this.repository = Repository.getInstance(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_owner_space, parent, false);
        return new SpaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpaceViewHolder holder, int position) {
        Space space = spaceList.get(position);

        // Informations de base
        holder.txtName.setText(space.getName());
        holder.txtLocation.setText(space.getLocation());
        holder.txtCapacity.setText("Capacité : " + space.getCapacity() + " personnes");
        holder.txtPrice.setText(String.format("%.2f DH/jour", space.getPrice()));

        // Charger l'image de l'espace
        try {
            SpaceImages imgData = repository.getFirstImageBySpaceId(space.getSpaceId());
            if (imgData != null && imgData.getImage() != null && imgData.getImage().length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(imgData.getImage(), 0, imgData.getImage().length);
                holder.imgSpace.setImageBitmap(bmp);
            } else {
                holder.imgSpace.setImageResource(R.drawable.img);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.imgSpace.setImageResource(R.drawable.img);
        }

        // Compter les réservations
        List<Booking> bookings = repository.getBookingsBySpaceId(space.getSpaceId());
        int pendingCount = 0;
        int confirmedCount = 0;

        for (Booking booking : bookings) {
            if ("pending".equals(booking.getStatus())) {
                pendingCount++;
            } else if ("confirmed".equals(booking.getStatus())) {
                confirmedCount++;
            }
        }

        holder.txtPendingBookings.setText(pendingCount + " en attente");
        holder.txtConfirmedBookings.setText(confirmedCount + " confirmées");

        // Boutons d'action
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSpace(space);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSpace(space);
            }
        });

        holder.btnViewBookings.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewBookings(space);
            }
        });

        // Clic sur la carte pour éditer
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSpace(space);
            }
        });
    }

    @Override
    public int getItemCount() {
        return spaceList.size();
    }

    static class SpaceViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imgSpace;
        TextView txtName, txtLocation, txtCapacity, txtPrice;
        TextView txtPendingBookings, txtConfirmedBookings;
        MaterialButton btnEdit, btnDelete, btnViewBookings;

        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imgSpace = itemView.findViewById(R.id.imgSpace);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtCapacity = itemView.findViewById(R.id.txtCapacity);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtPendingBookings = itemView.findViewById(R.id.txtPendingBookings);
            txtConfirmedBookings = itemView.findViewById(R.id.txtConfirmedBookings);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewBookings = itemView.findViewById(R.id.btnViewBookings);
        }
    }
}