package com.example.espacecoworking.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.AvailabilityAdapter;
import com.example.espacecoworking.adapters.ImageSliderAdapter;
import com.example.espacecoworking.models.Booking;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.SpaceAvailability;
import com.example.espacecoworking.models.SpaceImages;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SpaceDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private TextView txtName, txtLocation, txtCapacity, txtDesc, txtIndicator, txtPrice;
    private TextInputEditText etBookingDate, etStartTime, etEndTime;
    private RecyclerView recyclerViewAvailabilities;
    private TextView txtNoAvailabilities;
    private MaterialCardView cardPriceCalculation;
    private TextView txtDuration, txtPricePerHour, txtTotalPrice;
    private Button btnBook;

    private Repository repository;
    private int spaceId;
    private int currentClientId;
    private Space currentSpace;
    private AvailabilityAdapter availabilityAdapter;
    private List<SpaceAvailability> availabilityList;

    private String selectedDate = "";
    private String selectedStartTime = "";
    private String selectedEndTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_detail);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spaceDetails), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setElevation(0);
        }

        initViews();

        repository = Repository.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);
        currentClientId = prefs.getInt("USER_ID", -1);

        spaceId = getIntent().getIntExtra("SPACE_ID", -1);

        loadSpaceDetails();
        loadImages();
        loadAvailabilities();
        setupListeners();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        txtIndicator = findViewById(R.id.txtIndicator);
        txtName = findViewById(R.id.txtName);
        txtLocation = findViewById(R.id.txtLocation);
        txtCapacity = findViewById(R.id.txtCapacity);
        txtDesc = findViewById(R.id.txtDesc);
        txtPrice = findViewById(R.id.txtPrice);

        etBookingDate = findViewById(R.id.etBookingDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);

        recyclerViewAvailabilities = findViewById(R.id.recyclerViewAvailabilities);
        txtNoAvailabilities = findViewById(R.id.txtNoAvailabilities);

        cardPriceCalculation = findViewById(R.id.cardPriceCalculation);
        txtDuration = findViewById(R.id.txtDuration);
        txtPricePerHour = findViewById(R.id.txtPricePerHour);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        btnBook = findViewById(R.id.btnBook);
    }

    private void loadSpaceDetails() {
        currentSpace = repository.getSpaceById(spaceId);
        if (currentSpace != null) {
            txtName.setText(currentSpace.getName());
            txtLocation.setText(currentSpace.getLocation());
            txtCapacity.setText("Capacité : " + currentSpace.getCapacity() + " personnes");
            txtDesc.setText(currentSpace.getDescription());
            txtPrice.setText(String.format("%.2f DH", currentSpace.getPrice()));
        }
    }

    private void loadImages() {
        try {
            List<SpaceImages> images = repository.getImagesBySpaceId(spaceId);
            ImageSliderAdapter adapter = new ImageSliderAdapter(images);
            viewPagerImages.setAdapter(adapter);

            final int totalImages = images.size();
            txtIndicator.setText("1/" + totalImages);

            viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    txtIndicator.setText((position + 1) + "/" + totalImages);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            List<SpaceImages> fallback = new ArrayList<>();
            fallback.add(new SpaceImages());
            viewPagerImages.setAdapter(new ImageSliderAdapter(fallback));
        }
    }

    private void loadAvailabilities() {
        availabilityList = repository.getAvailabilitiesBySpaceId(spaceId);

        if (availabilityList.isEmpty()) {
            txtNoAvailabilities.setVisibility(View.VISIBLE);
            recyclerViewAvailabilities.setVisibility(View.GONE);
        } else {
            txtNoAvailabilities.setVisibility(View.GONE);
            recyclerViewAvailabilities.setVisibility(View.VISIBLE);

            availabilityAdapter = new AvailabilityAdapter(this, availabilityList);
            recyclerViewAvailabilities.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewAvailabilities.setAdapter(availabilityAdapter);
        }
    }

    private void setupListeners() {
        etBookingDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));

        btnBook.setOnClickListener(v -> validateAndBook());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());

                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etBookingDate.setText(displayFormat.format(calendar.getTime()));

                    calculatePrice();
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

                    calculatePrice();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        picker.show();
    }

    private void calculatePrice() {
        if (!selectedDate.isEmpty() && !selectedStartTime.isEmpty() && !selectedEndTime.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date startDate = sdf.parse(selectedStartTime);
                Date endDate = sdf.parse(selectedEndTime);

                if (startDate != null && endDate != null) {
                    long diffInMillis = endDate.getTime() - startDate.getTime();

                    if (diffInMillis <= 0) {
                        Toast.makeText(this, "L'heure de fin doit être après l'heure de début", Toast.LENGTH_SHORT).show();
                        cardPriceCalculation.setVisibility(View.GONE);
                        return;
                    }

                    double hours = diffInMillis / (1000.0 * 60 * 60);
                    double pricePerHour = currentSpace.getPrice();
                    double totalPrice = hours * pricePerHour;

                    txtDuration.setText(String.format(Locale.getDefault(), "%.2f heures", hours));
                    txtPricePerHour.setText(String.format(Locale.getDefault(), "%.2f DH", pricePerHour));
                    txtTotalPrice.setText(String.format(Locale.getDefault(), "%.2f DH", totalPrice));

                    cardPriceCalculation.setVisibility(View.VISIBLE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                cardPriceCalculation.setVisibility(View.GONE);
            }
        } else {
            cardPriceCalculation.setVisibility(View.GONE);
        }
    }

    private void validateAndBook() {
        if (currentClientId == -1) {
            Toast.makeText(this, "Veuillez vous connecter pour réserver", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStartTime.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner l'heure de début", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedEndTime.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner l'heure de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier que l'heure de fin est après l'heure de début
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(selectedStartTime);
            Date endDate = sdf.parse(selectedEndTime);

            if (startDate != null && endDate != null && endDate.getTime() <= startDate.getTime()) {
                Toast.makeText(this, "L'heure de fin doit être après l'heure de début", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Format d'heure invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier la disponibilité
        if (!checkAvailability()) {
            Toast.makeText(this, "Cet espace n'est pas disponible pour cette période", Toast.LENGTH_LONG).show();
            return;
        }

        // Vérifier les conflits avec d'autres réservations
        if (repository.hasConflict(spaceId, selectedDate, selectedStartTime, selectedEndTime, -1)) {
            Toast.makeText(this, "Cette période est déjà réservée", Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher un récapitulatif avant de confirmer
        showBookingConfirmation();
    }

    private boolean checkAvailability() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(selectedDate));

            String[] daysOfWeek = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
            String dayOfWeek = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];

            List<SpaceAvailability> dayAvailabilities = repository.getAvailabilitiesBySpaceAndDay(spaceId, dayOfWeek);

            for (SpaceAvailability av : dayAvailabilities) {
                if (av.isAvailable() &&
                        selectedStartTime.compareTo(av.getStartTime()) >= 0 &&
                        selectedEndTime.compareTo(av.getEndTime()) <= 0) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showBookingConfirmation() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(selectedStartTime);
            Date endDate = sdf.parse(selectedEndTime);

            double hours = (endDate.getTime() - startDate.getTime()) / (1000.0 * 60 * 60);
            double totalPrice = hours * currentSpace.getPrice();

            // ✅ CORRECTION : Utiliser le bon format pour parser la date
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String displayDate = displayFormat.format(dateParser.parse(selectedDate));

            String message = String.format(
                    "Espace: %s\n" +
                            "Date: %s\n" +
                            "Horaire: %s - %s\n" +
                            "Durée: %.2f heures\n" +
                            "Prix total: %.2f DH\n\n" +
                            "Voulez-vous confirmer cette réservation?",
                    currentSpace.getName(),
                    displayDate,
                    selectedStartTime,
                    selectedEndTime,
                    hours,
                    totalPrice
            );

            new AlertDialog.Builder(this)
                    .setTitle("Confirmer la réservation")
                    .setMessage(message)
                    .setPositiveButton("Confirmer", (dialog, which) -> createBooking())
                    .setNegativeButton("Annuler", null)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du calcul", Toast.LENGTH_SHORT).show();
        }
    }

    private void createBooking() {
        Booking newBooking = new Booking();
        newBooking.setClientId(currentClientId);
        newBooking.setSpaceId(spaceId);
        newBooking.setDate(selectedDate);
        newBooking.setStartTime(selectedStartTime);
        newBooking.setEndTime(selectedEndTime);
        newBooking.setStatus("pending");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        newBooking.setCreatedAt(sdf.format(new Date()));

        long result = repository.addBooking(newBooking);

        if (result > 0) {
            Toast.makeText(this, "Réservation créée avec succès! En attente de confirmation.", Toast.LENGTH_LONG).show();

            // Rediriger vers la page des réservations
            Intent intent = new Intent(this, ClientBookingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (result == -1) {
            Toast.makeText(this, "Cette période est déjà réservée", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erreur lors de la création de la réservation", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}