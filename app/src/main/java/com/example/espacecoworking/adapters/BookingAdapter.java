package com.example.espacecoworking.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private Repository repository;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookings, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookings;
        this.repository = Repository.getInstance(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        Space space = repository.getSpaceById(booking.getSpaceId());

        if (space != null) {
            holder.txtSpaceName.setText(space.getName());
            holder.txtLocation.setText(space.getLocation());

            // Calculer et afficher le prix total
            double totalAmount = calculateTotalAmount(booking, space);
            holder.txtTotalAmount.setText(String.format(Locale.getDefault(), "%.2f DH", totalAmount));
        }

        holder.txtDate.setText(booking.getDate());
        holder.txtTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
        holder.txtStatus.setText(getStatusText(booking.getStatus()));

        // Set status color
        int statusColor = getStatusColor(booking.getStatus());
        holder.txtStatus.setBackgroundColor(statusColor);

        // Show/hide cancel button based on status
        if ("pending".equals(booking.getStatus()) || "confirmed".equals(booking.getStatus())) {
            holder.btnCancelBooking.setVisibility(View.VISIBLE);
            holder.btnCancelBooking.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking);
                }
            });
        } else {
            holder.btnCancelBooking.setVisibility(View.GONE);
        }
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
                return Color.parseColor("#FFA500"); // Orange
            case "confirmed":
                return Color.parseColor("#4CAF50"); // Green
            case "completed":
                return Color.parseColor("#2196F3"); // Blue
            case "cancelled":
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.parseColor("#9E9E9E"); // Grey
        }
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtSpaceName, txtLocation, txtDate, txtTime, txtStatus, txtTotalAmount;
        MaterialButton btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSpaceName = itemView.findViewById(R.id.txtSpaceName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}