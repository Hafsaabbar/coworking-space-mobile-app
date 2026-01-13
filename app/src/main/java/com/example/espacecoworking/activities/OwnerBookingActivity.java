package com.example.espacecoworking.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.OwnerBookingAdapter;
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class OwnerBookingActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ChipGroup chipGroupStatus;
    private Chip chipAll, chipPending, chipConfirmed, chipCompleted, chipCancelled;
    private RecyclerView recyclerViewBookings;
    private TextView txtNoBookings;

    private Repository repository;
    private OwnerBookingAdapter bookingAdapter;
    private List<Booking> allBookings;
    private List<Booking> filteredBookings;
    private int ownerId;
    private int filterSpaceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_booking);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ownerBookings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        repository = Repository.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);
        ownerId = prefs.getInt("USER_ID", -1);
        filterSpaceId = getIntent().getIntExtra("SPACE_ID", -1);

        if (ownerId == -1) {
            Toast.makeText(this, R.string.session_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupChipGroup();
        setupRecyclerView();
        loadBookings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipConfirmed = findViewById(R.id.chipConfirmed);
        chipCompleted = findViewById(R.id.chipCompleted);
        chipCancelled = findViewById(R.id.chipCancelled);
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        txtNoBookings = findViewById(R.id.txtNoBookings);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.manage_bookings);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupChipGroup() {
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                filterBookings();
            }
        });
    }

    private void setupRecyclerView() {
        allBookings = new ArrayList<>();
        filteredBookings = new ArrayList<>();

        bookingAdapter = new OwnerBookingAdapter(this, filteredBookings,
                new OwnerBookingAdapter.OnBookingActionListener() {
                    @Override
                    public void onConfirmBooking(Booking booking) {
                        showConfirmDialog(booking);
                    }

                    @Override
                    public void onRejectBooking(Booking booking) {
                        showRejectDialog(booking);
                    }

                    @Override
                    public void onViewDetails(Booking booking) {
                        // Optionnel : afficher les détails de la réservation
                    }
                });

        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookings.setAdapter(bookingAdapter);
    }

    private void loadBookings() {
        allBookings.clear();

        // Charger les réservations de tous les espaces du propriétaire
        List<Space> ownerSpaces = repository.getSpacesByOwnerId(ownerId);

        for (Space space : ownerSpaces) {
            // Si un filtre d'espace est appliqué, ne charger que cet espace
            if (filterSpaceId != -1 && space.getSpaceId() != filterSpaceId) {
                continue;
            }

            List<Booking> spaceBookings = repository.getBookingsBySpaceId(space.getSpaceId());
            allBookings.addAll(spaceBookings);
        }

        filterBookings();
    }

    private void filterBookings() {
        filteredBookings.clear();

        int checkedId = chipGroupStatus.getCheckedChipId();

        if (checkedId == chipAll.getId()) {
            filteredBookings.addAll(allBookings);
        } else if (checkedId == chipPending.getId()) {
            for (Booking booking : allBookings) {
                if ("pending".equals(booking.getStatus())) {
                    filteredBookings.add(booking);
                }
            }
        } else if (checkedId == chipConfirmed.getId()) {
            for (Booking booking : allBookings) {
                if ("confirmed".equals(booking.getStatus())) {
                    filteredBookings.add(booking);
                }
            }
        } else if (checkedId == chipCompleted.getId()) {
            for (Booking booking : allBookings) {
                if ("completed".equals(booking.getStatus())) {
                    filteredBookings.add(booking);
                }
            }
        } else if (checkedId == chipCancelled.getId()) {
            for (Booking booking : allBookings) {
                if ("cancelled".equals(booking.getStatus())) {
                    filteredBookings.add(booking);
                }
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

    private void showConfirmDialog(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_booking_title)
                .setMessage(R.string.confirm_booking_message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> confirmBooking(booking))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRejectDialog(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reject_booking_title)
                .setMessage(R.string.reject_booking_message)
                .setPositiveButton("Refuser", (dialog, which) -> rejectBooking(booking))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmBooking(Booking booking) {
        int result = repository.updateBookingStatus(booking.getBookingId(), "confirmed");

        if (result > 0) {
            Toast.makeText(this, R.string.booking_confirmed, Toast.LENGTH_SHORT).show();
            loadBookings();
        } else {
            Toast.makeText(this, "Erreur lors de la confirmation", Toast.LENGTH_SHORT).show();
        }
    }

    private void rejectBooking(Booking booking) {
        int result = repository.updateBookingStatus(booking.getBookingId(), "cancelled");

        if (result > 0) {
            Toast.makeText(this, R.string.booking_rejected, Toast.LENGTH_SHORT).show();
            loadBookings();
        } else {
            Toast.makeText(this, "Erreur lors du refus", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }
}