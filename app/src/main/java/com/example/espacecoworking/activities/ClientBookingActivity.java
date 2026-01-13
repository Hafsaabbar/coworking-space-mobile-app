package com.example.espacecoworking.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.espacecoworking.R;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.adapters.BookingAdapter;
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ClientBookingActivity extends AppCompatActivity implements BookingAdapter.OnBookingActionListener {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerViewBookings;
    private TextView txtNoBookings;

    private Repository repository;
    private SharedPreferences sharedPreferences;
    private BookingAdapter bookingAdapter;
    private List<Booking> allBookings;
    private List<Booking> filteredBookings;
    private int currentClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clientBookings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        repository = Repository.getInstance(this);
        sharedPreferences = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);
        currentClientId = sharedPreferences.getInt("USER_ID", -1);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTabs();
        loadBookings();
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        txtNoBookings = findViewById(R.id.txtNoBookings);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        filteredBookings = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, filteredBookings, this);
        recyclerViewBookings.setAdapter(bookingAdapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterBookingsByStatus(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadBookings() {
        allBookings = repository.getBookingsByClientId(currentClientId);
        filterBookingsByStatus(0); // Show pending by default
    }

    private void filterBookingsByStatus(int tabPosition) {
        filteredBookings.clear();

        String status;
        switch (tabPosition) {
            case 0: // En attente
                status = "pending";
                break;
            case 1: // Confirmées
                status = "confirmed";
                break;
            case 2: // Terminées
                status = "completed";
                break;
            default:
                status = "pending";
        }

        for (Booking booking : allBookings) {
            if (booking.getStatus().equals(status)) {
                filteredBookings.add(booking);
            }
        }

        bookingAdapter.notifyDataSetChanged();

        if (filteredBookings.isEmpty()) {
            txtNoBookings.setVisibility(View.VISIBLE);
            recyclerViewBookings.setVisibility(View.GONE);
        } else {
            txtNoBookings.setVisibility(View.GONE);
            recyclerViewBookings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancelBooking(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Annuler la réservation")
                .setMessage("Voulez-vous vraiment annuler cette réservation ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    int result = repository.updateBookingStatus(booking.getBookingId(), "cancelled");
                    if (result > 0) {
                        Toast.makeText(this, "Réservation annulée", Toast.LENGTH_SHORT).show();
                        loadBookings(); // Refresh
                    } else {
                        Toast.makeText(this, "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }
}