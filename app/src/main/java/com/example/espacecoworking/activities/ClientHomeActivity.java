package com.example.espacecoworking.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.SpaceAdapter;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.SpaceAvailability;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ClientHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewSpaces;
    private TextInputEditText etCity;
    private TextInputEditText etCapacity, etDate, etStartTime, etEndTime;
    private MaterialButton btnApplyFilter, btnResetFilter;
    private TextView txtNoSpaces, txtFilterSummary;

    private Repository repository;
    private SharedPreferences sharedPreferences;
    private SpaceAdapter spaceAdapter;
    private List<Space> allSpaces;
    private List<Space> filteredSpaces;
    private User currentUser;
    private OnBackPressedCallback onBackPressedCallback;

    // Filtres
    private String selectedCity = "";
    private int selectedCapacity = 0;
    private String selectedDate = "";
    private String selectedStartTime = "";
    private String selectedEndTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_home);

        repository = Repository.getInstance(this);
        sharedPreferences = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);

        initViews();
        loadCurrentUser();
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        loadSpaces();
        setupListeners();

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (isEnabled()) {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onBackPressedCallback != null) {
            onBackPressedCallback.setEnabled(true);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        recyclerViewSpaces = findViewById(R.id.recyclerViewSpaces);
        etCity = findViewById(R.id.etCity);
        etCapacity = findViewById(R.id.etCapacity);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        txtNoSpaces = findViewById(R.id.txtNoSpaces);
        txtFilterSummary = findViewById(R.id.txtFilterSummary);
    }

    private void loadCurrentUser() {
        int userId = sharedPreferences.getInt("USER_ID", -1);
        if (userId != -1) {
            currentUser = repository.getUserById(userId);
            updateNavigationHeader();
        }
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView txtUserName = headerView.findViewById(R.id.txtUserName);
        TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);

        if (currentUser != null) {
            txtUserName.setText(currentUser.getName());
            txtUserEmail.setText(currentUser.getEmail());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Déjà sur la page d'accueil
            } else if (id == R.id.nav_bookings) {
                Intent intent = new Intent(this, ClientBookingActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ClientProfileActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupRecyclerView() {
        recyclerViewSpaces.setLayoutManager(new LinearLayoutManager(this));
        filteredSpaces = new ArrayList<>();
        spaceAdapter = new SpaceAdapter(this, filteredSpaces);
        recyclerViewSpaces.setAdapter(spaceAdapter);
    }

    private void loadSpaces() {
        allSpaces = repository.getAllSpaces();
        filteredSpaces.clear();
        filteredSpaces.addAll(allSpaces);
        spaceAdapter.notifyDataSetChanged();

        updateUI();
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
        btnApplyFilter.setOnClickListener(v -> applyFilters());
        btnResetFilter.setOnClickListener(v -> resetFilters());

        // Ajouter un listener pour le bouton "Done" du clavier pour la ville
        etCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyFilters();
                return true;
            }
            return false;
        });

        // Ajouter un listener pour le bouton "Done" du clavier pour la capacité
        etCapacity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyFilters();
                return true;
            }
            return false;
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    etDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (isStartTime) {
                        selectedStartTime = time;
                        etStartTime.setText(time);
                    } else {
                        selectedEndTime = time;
                        etEndTime.setText(time);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        picker.show();
    }

    private void applyFilters() {
        selectedCity = etCity.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

        if (!capacityStr.isEmpty()) {
            try {
                selectedCapacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                selectedCapacity = 0;
                Toast.makeText(this, "Veuillez entrer un nombre valide pour la capacité", Toast.LENGTH_SHORT).show();
            }
        } else {
            selectedCapacity = 0;
        }

        filteredSpaces.clear();

        for (Space space : allSpaces) {
            boolean matches = true;

            // Filtre par ville (recherche partielle, insensible à la casse)
            if (!selectedCity.isEmpty()) {
                String spaceLocation = space.getLocation().toLowerCase();
                String searchCity = selectedCity.toLowerCase();

                // Recherche partielle dans le nom de la ville
                if (!spaceLocation.contains(searchCity)) {
                    matches = false;
                }
            }

            // Filtre par capacité
            if (selectedCapacity > 0) {
                if (space.getCapacity() < selectedCapacity) {
                    matches = false;
                }
            }

            // Filtre par disponibilité (date et horaire)
            if (!selectedDate.isEmpty() && !selectedStartTime.isEmpty() && !selectedEndTime.isEmpty()) {
                if (!isSpaceAvailable(space.getSpaceId(), selectedDate, selectedStartTime, selectedEndTime)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredSpaces.add(space);
            }
        }

        spaceAdapter.notifyDataSetChanged();
        updateUI();
        updateFilterSummary();

        // Masquer le clavier après application des filtres
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }
    }

    private boolean isSpaceAvailable(int spaceId, String date, String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));

            String[] daysOfWeek = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
            String dayOfWeek = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];

            // Vérifier les disponibilités définies
            List<SpaceAvailability> availabilities = repository.getAvailabilitiesBySpaceAndDay(spaceId, dayOfWeek);

            boolean isAvailable = false;
            for (SpaceAvailability av : availabilities) {
                if (av.isAvailable() &&
                        startTime.compareTo(av.getStartTime()) >= 0 &&
                        endTime.compareTo(av.getEndTime()) <= 0) {
                    isAvailable = true;
                    break;
                }
            }

            if (!isAvailable) return false;

            // Vérifier qu'il n'y a pas de conflit avec des réservations existantes
            return !repository.hasConflict(spaceId, date, startTime, endTime, -1);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateFilterSummary() {
        StringBuilder summary = new StringBuilder("Filtres actifs: ");
        int filterCount = 0;

        if (!selectedCity.isEmpty()) {
            summary.append("Ville: ").append(selectedCity);
            filterCount++;
        }

        if (selectedCapacity > 0) {
            if (filterCount > 0) summary.append(", ");
            summary.append("Capacité: ").append(selectedCapacity).append("+ personnes");
            filterCount++;
        }

        if (!selectedDate.isEmpty()) {
            if (filterCount > 0) summary.append(", ");
            summary.append("Date: ").append(etDate.getText().toString());
            filterCount++;
        }

        if (!selectedStartTime.isEmpty() && !selectedEndTime.isEmpty()) {
            if (filterCount > 0) summary.append(", ");
            summary.append("Horaire: ").append(selectedStartTime).append(" - ").append(selectedEndTime);
            filterCount++;
        }

        if (filterCount > 0) {
            txtFilterSummary.setText(summary.toString());
            txtFilterSummary.setVisibility(View.VISIBLE);
        } else {
            txtFilterSummary.setVisibility(View.GONE);
        }
    }

    private void resetFilters() {
        selectedCity = "";
        selectedCapacity = 0;
        selectedDate = "";
        selectedStartTime = "";
        selectedEndTime = "";

        etCity.setText("");
        etCapacity.setText("");
        etDate.setText("");
        etStartTime.setText("");
        etEndTime.setText("");

        filteredSpaces.clear();
        filteredSpaces.addAll(allSpaces);
        spaceAdapter.notifyDataSetChanged();

        updateUI();
        txtFilterSummary.setVisibility(View.GONE);
        Toast.makeText(this, "Filtres réinitialisés", Toast.LENGTH_SHORT).show();

        // Masquer le clavier après réinitialisation
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }
    }

    private void updateUI() {
        if (filteredSpaces.isEmpty()) {
            txtNoSpaces.setVisibility(View.VISIBLE);
            recyclerViewSpaces.setVisibility(View.GONE);
        } else {
            txtNoSpaces.setVisibility(View.GONE);
            recyclerViewSpaces.setVisibility(View.VISIBLE);
        }
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    sharedPreferences.edit().clear().apply();

                    Intent intent = new Intent(ClientHomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}