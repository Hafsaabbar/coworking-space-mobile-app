package com.example.espacecoworking.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OwnerBookingAdapter extends RecyclerView.Adapter<OwnerBookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private Repository repository;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onConfirmBooking(Booking booking);
        void onRejectBooking(Booking booking);
        void onViewDetails(Booking booking);
    }

    public OwnerBookingAdapter(Context context, List<Booking> bookings, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookings;
        this.repository = Repository.getInstance(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_owner_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        Space space = repository.getSpaceById(booking.getSpaceId());
        User client = repository.getUserById(booking.getClientId());

        // Informations de l'espace
        if (space != null) {
            holder.txtSpaceName.setText(space.getName());
            holder.txtLocation.setText(space.getLocation());

            // Charger l'image de l'espace
            try {
                SpaceImages imgData = repository.getFirstImageBySpaceId(space.getSpaceId());
                if (imgData != null && imgData.getImage() != null) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(imgData.getImage(), 0, imgData.getImage().length);
                    holder.imgSpace.setImageBitmap(bmp);
                } else {
                    holder.imgSpace.setImageResource(R.drawable.img);
                }
            } catch (Exception e) {
                holder.imgSpace.setImageResource(R.drawable.img);
            }

            // Calculer et afficher le prix total
            double totalAmount = calculateTotalAmount(booking, space);
            holder.txtTotalAmount.setText(String.format(Locale.getDefault(), "%.2f DH", totalAmount));
        }

        // Informations du client
        if (client != null) {
            holder.txtClientName.setText("Client: " + client.getName());
        }

        // Informations de la réservation
        holder.txtDate.setText(booking.getDate());
        holder.txtTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
        holder.txtStatus.setText(getStatusText(booking.getStatus()));

        // Couleur du statut
        int statusColor = getStatusColor(booking.getStatus());
        holder.txtStatus.setBackgroundColor(statusColor);

        // Gestion des boutons selon le statut
        if ("pending".equals(booking.getStatus())) {
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);

            holder.btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmBooking(booking);
                }
            });

            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectBooking(booking);
                }
            });
        } else {
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        // Clic sur la carte pour voir les détails
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(booking);
            }
        });
    }

    private double calculateTotalAmount(Booking booking, Space space) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(booking.getStartTime());
            Date endDate = sdf.parse(booking.getEndTime());

            if (startDate != null && endDate != null) {
                long diffInMillis = endDate.getTime() - startDate.getTime();
                double hours = diffInMillis / (1000.0 * 60 * 60);
                return hours * space.getPrice();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "En attente";
            case "confirmed":
                return "Confirmée";
            case "completed":
                return "Terminée";
            case "cancelled":
                return "Annulée";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "pending":
                return Color.parseColor("#FFA500");
            case "confirmed":
                return Color.parseColor("#4CAF50");
            case "completed":
                return Color.parseColor("#2196F3");
            case "cancelled":
                return Color.parseColor("#F44336");
            default:
                return Color.parseColor("#9E9E9E");
        }
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imgSpace;
        TextView txtSpaceName, txtLocation, txtClientName, txtDate, txtTime, txtStatus, txtTotalAmount;
        MaterialButton btnConfirm, btnReject;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imgSpace = itemView.findViewById(R.id.imgSpace);
            txtSpaceName = itemView.findViewById(R.id.txtSpaceName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtClientName = itemView.findViewById(R.id.txtClientName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}