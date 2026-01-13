package com.example.espacecoworking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.SpaceAvailability;

import java.util.List;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.AvailabilityViewHolder> {

    private Context context;
    private List<SpaceAvailability> availabilityList;

    public AvailabilityAdapter(Context context, List<SpaceAvailability> availabilities) {
        this.context = context;
        this.availabilityList = availabilities;
    }

    @NonNull
    @Override
    public AvailabilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_availability, parent, false);
        return new AvailabilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailabilityViewHolder holder, int position) {
        SpaceAvailability availability = availabilityList.get(position);

        holder.txtDay.setText(availability.getDayOfWeek());
        holder.txtTime.setText(availability.getStartTime() + " - " + availability.getEndTime());

        if (availability.isAvailable()) {
            holder.txtStatus.setText("Disponible");
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.txtStatus.setText("Non disponible");
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return availabilityList.size();
    }

    static class AvailabilityViewHolder extends RecyclerView.ViewHolder {
        TextView txtDay, txtTime, txtStatus;

        public AvailabilityViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDay = itemView.findViewById(R.id.txtDay);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}