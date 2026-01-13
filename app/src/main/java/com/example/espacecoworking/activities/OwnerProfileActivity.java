package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OwnerProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private MaterialToolbar toolbar;
    private ShapeableImageView imgProfile;
    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnUpdateProfile;
    private ProgressBar progressBar;

    private Repository repository;
    private int ownerId;
    private User currentUser;
    private byte[] selectedImageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ownerProfile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        repository = Repository.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);
        ownerId = prefs.getInt("USER_ID", -1);

        if (ownerId == -1) {
            Toast.makeText(this, R.string.session_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        currentUser = repository.getUserById(ownerId);
        if (currentUser != null) {
            etName.setText(currentUser.getName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());

            // Charger l'image de profil si disponible
            if (currentUser.getImage() != null && currentUser.getImage().length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        currentUser.getImage(), 0, currentUser.getImage().length
                );
                imgProfile.setImageBitmap(bitmap);
            }
        }
    }

    private void setupListeners() {
        findViewById(R.id.fabEditImage).setOnClickListener(v -> selectImage());

        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        findViewById(R.id.txtChangePassword).setOnClickListener(v -> showChangePasswordDialog());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                // Redimensionner l'image
                Bitmap resizedBitmap = resizeBitmap(bitmap, 500, 500);
                imgProfile.setImageBitmap(resizedBitmap);

                // Convertir en byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                selectedImageBytes = stream.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
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

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Nom requis");
            etName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdateProfile.setEnabled(false);

        currentUser.setName(name);
        currentUser.setPhone(phone);

        if (selectedImageBytes != null) {
            currentUser.setImage(selectedImageBytes);
        }

        int result = repository.updateUser(currentUser);

        progressBar.setVisibility(View.GONE);
        btnUpdateProfile.setEnabled(true);

        if (result > 0) {
            Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Changer le mot de passe")
                .setView(dialogView)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    String oldPassword = etOldPassword.getText().toString();
                    String newPassword = etNewPassword.getText().toString();
                    String confirmPassword = etConfirmPassword.getText().toString();

                    if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = repository.changePassword(ownerId, oldPassword, newPassword);

                    if (success) {
                        Toast.makeText(this, R.string.password_changed, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}