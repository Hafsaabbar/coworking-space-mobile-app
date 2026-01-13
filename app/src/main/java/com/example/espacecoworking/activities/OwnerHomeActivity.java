package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.OwnerSpaceAdapter;
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class OwnerHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private RecyclerView recyclerViewSpaces;
    private TextView txtNoSpaces, txtTotalSpaces, txtPendingBookings, txtConfirmedBookings;
    private FloatingActionButton fabAddSpace;

    private Repository repository;
    private OwnerSpaceAdapter spaceAdapter;
    private List<Space> spaceList;
    private int ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        initViews();
        repository = Repository.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);
        ownerId = prefs.getInt("USER_ID", -1);

        if (ownerId == -1) {
            Toast.makeText(this, "Session invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        loadData();

        fabAddSpace.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerHomeActivity.this, AddEditSpaceActivity.class);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        recyclerViewSpaces = findViewById(R.id.recyclerViewSpaces);
        txtNoSpaces = findViewById(R.id.txtNoSpaces);
        txtTotalSpaces = findViewById(R.id.txtTotalSpaces);
        txtPendingBookings = findViewById(R.id.txtPendingBookings);
        txtConfirmedBookings = findViewById(R.id.txtConfirmedBookings);
        fabAddSpace = findViewById(R.id.fabAddSpace);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Charger les infos utilisateur dans le header
        View headerView = navigationView.getHeaderView(0);
        TextView txtUserName = headerView.findViewById(R.id.txtUserName);
        TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);

        User user = repository.getUserById(ownerId);
        if (user != null) {
            txtUserName.setText(user.getName());
            txtUserEmail.setText(user.getEmail());
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_spaces) {
                startActivity(new Intent(this, ManageSpacesActivity.class));
            } else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, OwnerBookingActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, OwnerProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupRecyclerView() {
        spaceList = new ArrayList<>();
        spaceAdapter = new OwnerSpaceAdapter(this, spaceList, new OwnerSpaceAdapter.OnSpaceActionListener() {
            @Override
            public void onEditSpace(Space space) {
                Intent intent = new Intent(OwnerHomeActivity.this, AddEditSpaceActivity.class);
                intent.putExtra("SPACE_ID", space.getSpaceId());
                startActivity(intent);
            }

            @Override
            public void onDeleteSpace(Space space) {
                deleteSpace(space);
            }

            @Override
            public void onViewBookings(Space space) {
                Intent intent = new Intent(OwnerHomeActivity.this, OwnerBookingActivity.class);
                intent.putExtra("SPACE_ID", space.getSpaceId());
                startActivity(intent);
            }
        });

        recyclerViewSpaces.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSpaces.setAdapter(spaceAdapter);
    }

    private void loadData() {
        spaceList.clear();
        spaceList.addAll(repository.getSpacesByOwnerId(ownerId));
        spaceAdapter.notifyDataSetChanged();

        if (spaceList.isEmpty()) {
            txtNoSpaces.setVisibility(View.VISIBLE);
            recyclerViewSpaces.setVisibility(View.GONE);
        } else {
            txtNoSpaces.setVisibility(View.GONE);
            recyclerViewSpaces.setVisibility(View.VISIBLE);
        }

        // Mettre à jour les stats
        updateStats();
    }

    private void updateStats() {
        txtTotalSpaces.setText(String.valueOf(spaceList.size()));

        // Compter les réservations en attente et confirmées
        int pendingCount = 0;
        int confirmedCount = 0;

        for (Space space : spaceList) {
            List<Booking> bookings = repository.getBookingsBySpaceId(space.getSpaceId());
            for (Booking booking : bookings) {
                if ("pending".equals(booking.getStatus())) {
                    pendingCount++;
                } else if ("confirmed".equals(booking.getStatus())) {
                    confirmedCount++;
                }
            }
        }

        txtPendingBookings.setText(String.valueOf(pendingCount));
        txtConfirmedBookings.setText(String.valueOf(confirmedCount));
    }

    private void deleteSpace(Space space) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Supprimer l'espace")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet espace ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Supprimer les images
                    repository.deleteImagesBySpaceId(space.getSpaceId());
                    // Supprimer les disponibilités
                    repository.deleteAvailabilitiesBySpaceId(space.getSpaceId());
                    // Supprimer la relation propriétaire-espace
                    repository.deleteSpaceOwnersBySpaceId(space.getSpaceId());
                    // Supprimer l'espace
                    int result = repository.deleteSpace(space.getSpaceId());

                    if (result > 0) {
                        Toast.makeText(this, "Espace supprimé", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
