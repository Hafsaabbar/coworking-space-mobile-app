package com.example.espacecoworking.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.BookingAdapter;
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.repository.Repository;
import com.example.espacecoworking.utils.CalendarIntegration;
import com.example.espacecoworking.utils.NotificationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
        setContentView(R.layout.activity_client_booking);
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
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadBookings() {
        allBookings = repository.getBookingsByClientId(currentClientId);
        checkForNewConfirmations(allBookings);
        updateTabTitles();
        filterBookingsByStatus(tabLayout.getSelectedTabPosition());
    }

    private void updateTabTitles() {
        int pendingCount = 0;
        int confirmedCount = 0;
        int completedCount = 0;

        for (Booking booking : allBookings) {
            switch (booking.getStatus()) {
                case "pending":
                    pendingCount++;
                    break;
                case "confirmed":
                    confirmedCount++;
                    break;
                case "completed":
                    completedCount++;
                    break;
            }
        }

        tabLayout.getTabAt(0).setText("En attente (" + pendingCount + ")");
        tabLayout.getTabAt(1).setText("Confirmées (" + confirmedCount + ")");
        tabLayout.getTabAt(2).setText("Terminées (" + completedCount + ")");
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

    private void checkForNewConfirmations(List<Booking> bookings) {
        Set<String> notifiedBookings = sharedPreferences.getStringSet("notifiedBookings", new HashSet<>());
        Set<String> newNotifiedBookings = new HashSet<>(notifiedBookings);

        for (Booking booking : bookings) {
            if ("confirmed".equals(booking.getStatus()) && !notifiedBookings.contains(String.valueOf(booking.getBookingId()))) {
                Space space = repository.getSpaceById(booking.getSpaceId());
                String spaceName = (space != null) ? space.getName() : "Espace inconnu";

                NotificationUtils.showBookingConfirmationNotification(this, "Réservation Confirmée", "Votre réservation pour " + spaceName + " a été confirmée.");
                showCalendarDialog(booking);
                newNotifiedBookings.add(String.valueOf(booking.getBookingId()));
            }
        }
        sharedPreferences.edit().putStringSet("notifiedBookings", newNotifiedBookings).apply();
    }

    private void showCalendarDialog(Booking booking) {
        Space space = repository.getSpaceById(booking.getSpaceId());
        String spaceName = (space != null) ? space.getName() : "Espace inconnu";

        new AlertDialog.Builder(this)
                .setTitle("Ajouter au calendrier")
                .setMessage("Voulez-vous ajouter cette réservation à votre calendrier ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date startDate = sdf.parse(booking.getDate() + " " + booking.getStartTime());
                        Date endDate = sdf.parse(booking.getDate() + " " + booking.getEndTime());

                        long startTimeMillis = startDate.getTime();
                        long endTimeMillis = endDate.getTime();

                        CalendarIntegration.addBookingToCalendar(this,
                                "Réservation: " + spaceName,
                                "Réservation de l'espace de coworking.",
                                startTimeMillis,
                                endTimeMillis);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors de la création de l\'événement", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
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
                        Toast.makeText(this, "Erreur lors de l\'annulation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }
}
