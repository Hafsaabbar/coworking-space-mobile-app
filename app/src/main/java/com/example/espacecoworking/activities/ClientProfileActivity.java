package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ClientProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private MaterialToolbar toolbar;
    private ImageView imgProfile;
    private FloatingActionButton fabEditImage;
    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnUpdateProfile;
    private TextView txtChangePassword;
    private ProgressBar progressBar;

    private Repository repository;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    private byte[] selectedImageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clientProfile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        repository = Repository.getInstance(this);
        sharedPreferences = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);

        initViews();
        setupToolbar();
        loadUserData();
        setupListeners();
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgProfile = findViewById(R.id.imgProfile);
        fabEditImage = findViewById(R.id.fabEditImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        txtChangePassword = findViewById(R.id.txtChangePassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        int userId = sharedPreferences.getInt("USER_ID", -1);
        if (userId != -1) {
            currentUser = repository.getUserById(userId);
            if (currentUser != null) {
                etName.setText(currentUser.getName());
                etEmail.setText(currentUser.getEmail());
                etPhone.setText(currentUser.getPhone());

                // Load profile image
                if (currentUser.getImage() != null && currentUser.getImage().length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                            currentUser.getImage(), 0, currentUser.getImage().length);
                    imgProfile.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void setupListeners() {
        fabEditImage.setOnClickListener(v -> openImagePicker());

        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        txtChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                // Resize bitmap
                bitmap = resizeBitmap(bitmap, 500, 500);

                imgProfile.setImageBitmap(bitmap);

                // Convert to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                selectedImageBytes = stream.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image",
                        Toast.LENGTH_SHORT).show();
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

        if (TextUtils.isEmpty(name)) {
            etName.setError("Nom requis");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Téléphone requis");
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
            Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
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
                .setPositiveButton("Changer", (dialog, which) -> {
                    String oldPassword = etOldPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
                        Toast.makeText(this, "Tous les champs sont requis",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Minimum 6 caractères",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Les mots de passe ne correspondent pas",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean success = repository.changePassword(
                            currentUser.getUserId(), oldPassword, newPassword);

                    if (success) {
                        Toast.makeText(this, "Mot de passe changé", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ancien mot de passe incorrect",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
    
}