package com.example.espacecoworking.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.ImagePreviewAdapter;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.SpaceAvailability;
import com.example.espacecoworking.models.SpaceImages;
import com.example.espacecoworking.models.SpaceOwner;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditSpaceActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int MAX_IMAGES = 5;

    private MaterialToolbar toolbar;
    private TextInputEditText etSpaceName, etLocation, etCapacity, etDescription, etPrice;
    private RecyclerView recyclerViewImages;
    private MaterialButton btnAddImages, btnSave, btnSetAvailability;
    private LinearLayout availabilitySection;

    private Repository repository;
    private ImagePreviewAdapter imageAdapter;
    private List<byte[]> selectedImages;
    private Map<String, AvailabilityTime> availabilityMap;
    private int spaceId = -1;
    private int ownerId;
    private boolean isEditMode = false;

    // Classe interne pour stocker les horaires
    private static class AvailabilityTime {
        String startTime = "08:00";
        String endTime = "18:00";
        boolean isAvailable = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_space);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        repository = Repository.getInstance(this);
        availabilityMap = new HashMap<>();
        initializeAvailability();

        SharedPreferences prefs = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);
        ownerId = prefs.getInt("USER_ID", -1);

        if (ownerId == -1) {
            Toast.makeText(this, R.string.session_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        spaceId = getIntent().getIntExtra("SPACE_ID", -1);
        isEditMode = spaceId != -1;

        setupToolbar();
        setupRecyclerView();
        setupListeners();

        if (isEditMode) {
            loadSpaceData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSpaceName = findViewById(R.id.etSpaceName);
        etLocation = findViewById(R.id.etLocation);
        etCapacity = findViewById(R.id.etCapacity);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        btnAddImages = findViewById(R.id.btnAddImages);
        btnSetAvailability = findViewById(R.id.btnSetAvailability);
        btnSave = findViewById(R.id.btnSave);
        availabilitySection = findViewById(R.id.availabilitySection);
    }

    private void initializeAvailability() {
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (String day : days) {
            availabilityMap.put(day, new AvailabilityTime());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (isEditMode) {
                getSupportActionBar().setTitle(R.string.edit_space);
            } else {
                getSupportActionBar().setTitle(R.string.add_space);
            }
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        selectedImages = new ArrayList<>();
        imageAdapter = new ImagePreviewAdapter(this, selectedImages, position -> {
            selectedImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, selectedImages.size());
        });

        recyclerViewImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        btnAddImages.setOnClickListener(v -> selectImages());
        btnSetAvailability.setOnClickListener(v -> showAvailabilityDialog());
        btnSave.setOnClickListener(v -> saveSpace());
    }

    private void showAvailabilityDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_availability, null);
        LinearLayout daysContainer = dialogView.findViewById(R.id.daysContainer);

        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        for (String day : days) {
            View dayView = getLayoutInflater().inflate(R.layout.item_day_availability, null);
            CheckBox cbDay = dayView.findViewById(R.id.cbDay);
            TextView txtStartTime = dayView.findViewById(R.id.txtStartTime);
            TextView txtEndTime = dayView.findViewById(R.id.txtEndTime);

            cbDay.setText(day);
            AvailabilityTime time = availabilityMap.get(day);
            cbDay.setChecked(time.isAvailable);
            txtStartTime.setText(time.startTime);
            txtEndTime.setText(time.endTime);

            cbDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                time.isAvailable = isChecked;
                txtStartTime.setEnabled(isChecked);
                txtEndTime.setEnabled(isChecked);
            });

            txtStartTime.setEnabled(time.isAvailable);
            txtEndTime.setEnabled(time.isAvailable);

            txtStartTime.setOnClickListener(v -> showTimePicker(txtStartTime, time, true));
            txtEndTime.setOnClickListener(v -> showTimePicker(txtEndTime, time, false));

            daysContainer.addView(dayView);
        }

        new AlertDialog.Builder(this)
                .setTitle("Définir les disponibilités")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .setNegativeButton("Annuler", null)
                .show();

        updateAvailabilitySummary();
    }

    private void showTimePicker(TextView textView, AvailabilityTime time, boolean isStartTime) {
        String currentTime = isStartTime ? time.startTime : time.endTime;
        String[] parts = currentTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        TimePickerDialog picker = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String newTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    textView.setText(newTime);
                    if (isStartTime) {
                        time.startTime = newTime;
                    } else {
                        time.endTime = newTime;
                    }
                }, hour, minute, true);
        picker.show();
    }

    private void updateAvailabilitySummary() {
        int count = 0;
        for (AvailabilityTime time : availabilityMap.values()) {
            if (time.isAvailable) count++;
        }

        if (count > 0) {
            availabilitySection.setVisibility(View.VISIBLE);
            TextView summary = availabilitySection.findViewById(R.id.txtAvailabilitySummary);
            summary.setText(count + " jour(s) disponible(s)");
        }
    }

    private void loadSpaceData() {
        Space space = repository.getSpaceById(spaceId);
        if (space != null) {
            etSpaceName.setText(space.getName());
            etLocation.setText(space.getLocation());
            etCapacity.setText(String.valueOf(space.getCapacity()));
            etDescription.setText(space.getDescription());
            etPrice.setText(String.valueOf(space.getPrice()));

            List<SpaceImages> existingImages = repository.getImagesBySpaceId(spaceId);
            for (SpaceImages img : existingImages) {
                if (img.getImage() != null) {
                    selectedImages.add(img.getImage());
                }
            }
            imageAdapter.notifyDataSetChanged();

            // Charger les disponibilités
            List<SpaceAvailability> availabilities = repository.getAvailabilitiesBySpaceId(spaceId);
            for (SpaceAvailability av : availabilities) {
                AvailabilityTime time = availabilityMap.get(av.getDayOfWeek());
                if (time != null) {
                    time.isAvailable = av.isAvailable();
                    time.startTime = av.getStartTime();
                    time.endTime = av.getEndTime();
                }
            }
            updateAvailabilitySummary();
        }
    }

    private void selectImages() {
        if (selectedImages.size() >= MAX_IMAGES) {
            Toast.makeText(this, "Maximum " + MAX_IMAGES + " images", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int remaining = MAX_IMAGES - selectedImages.size();

                for (int i = 0; i < Math.min(count, remaining); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    addImageFromUri(imageUri);
                }

                if (count > remaining) {
                    Toast.makeText(this, "Seules " + remaining + " images ont été ajoutées",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                addImageFromUri(imageUri);
            }

            imageAdapter.notifyDataSetChanged();
        }
    }

    private void addImageFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 800);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();

            selectedImages.add(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    private void saveSpace() {
        String name = etSpaceName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etSpaceName.setError("Nom requis");
            etSpaceName.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Localisation requise");
            etLocation.requestFocus();
            return;
        }

        if (capacityStr.isEmpty()) {
            etCapacity.setError("Capacité requise");
            etCapacity.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Prix requis");
            etPrice.requestFocus();
            return;
        }

        int capacity;
        double price;

        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                etCapacity.setError("Capacité doit être > 0");
                etCapacity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCapacity.setError("Capacité invalide");
            etCapacity.requestFocus();
            return;
        }

        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                etPrice.setError("Prix doit être ≥ 0");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Prix invalide");
            etPrice.requestFocus();
            return;
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Veuillez ajouter au moins une image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier qu'au moins un jour est disponible
        boolean hasAvailability = false;
        for (AvailabilityTime time : availabilityMap.values()) {
            if (time.isAvailable) {
                hasAvailability = true;
                break;
            }
        }

        if (!hasAvailability) {
            Toast.makeText(this, "Veuillez définir au moins un jour de disponibilité",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer ou mettre à jour l'espace
        Space space = new Space();
        if (isEditMode) {
            space.setSpaceId(spaceId);
        }
        space.setName(name);
        space.setLocation(location);
        space.setCapacity(capacity);
        space.setDescription(description);
        space.setPrice(price);

        long result;
        if (isEditMode) {
            result = repository.updateSpace(space);

            repository.deleteImagesBySpaceId(spaceId);
            repository.deleteAvailabilitiesBySpaceId(spaceId);

            for (byte[] imageData : selectedImages) {
                SpaceImages spaceImage = new SpaceImages();
                spaceImage.setSpaceId(spaceId);
                spaceImage.setImage(imageData);
                repository.addSpaceImage(spaceImage);
            }

            saveAvailabilities(spaceId);

            Toast.makeText(this, "Espace modifié avec succès", Toast.LENGTH_SHORT).show();
        } else {
            result = repository.addSpace(space);

            if (result > 0) {
                int newSpaceId = (int) result;

                for (byte[] imageData : selectedImages) {
                    SpaceImages spaceImage = new SpaceImages();
                    spaceImage.setSpaceId(newSpaceId);
                    spaceImage.setImage(imageData);
                    repository.addSpaceImage(spaceImage);
                }

                saveAvailabilities(newSpaceId);

                SpaceOwner spaceOwner = new SpaceOwner();
                spaceOwner.setOwnerId(ownerId);
                spaceOwner.setSpaceId(newSpaceId);
                repository.addSpaceOwner(spaceOwner);

                Toast.makeText(this, "Espace ajouté avec succès", Toast.LENGTH_SHORT).show();
            }
        }

        if (result > 0) {
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAvailabilities(int spaceId) {
        for (Map.Entry<String, AvailabilityTime> entry : availabilityMap.entrySet()) {
            if (entry.getValue().isAvailable) {
                SpaceAvailability availability = new SpaceAvailability();
                availability.setSpaceId(spaceId);
                availability.setDayOfWeek(entry.getKey());
                availability.setStartTime(entry.getValue().startTime);
                availability.setEndTime(entry.getValue().endTime);
                availability.setAvailable(true);
                repository.addAvailability(availability);
            }
        }
    }
}