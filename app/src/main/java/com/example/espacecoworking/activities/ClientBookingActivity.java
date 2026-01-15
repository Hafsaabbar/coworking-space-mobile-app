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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        // ✅ MISE À JOUR AUTOMATIQUE : Changer les statuts expirés à "completed"
        repository.updateExpiredBookingsToCompleted();

        // Charger les réservations du client
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
        // ✅ VALIDATION : Vérifier que la réservation n'est pas en cours
        if (!canCancelBooking(booking)) {
            Toast.makeText(this, "Impossible d'annuler une réservation passée ou en cours",
                    Toast.LENGTH_SHORT).show();
            return;
        }

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

    /**
     * Vérifie que la réservation n'a pas commencé et peut être annulée
     * @param booking La réservation à vérifier
     * @return true si annulation possible, false sinon
     */
    private boolean canCancelBooking(Booking booking) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            Date now = new Date();
            Date startDateTime = sdf.parse(booking.getDate() + " " + booking.getStartTime());

            // On ne peut annuler que si la réservation n'a pas commencé
            return now.getTime() < startDateTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }
}