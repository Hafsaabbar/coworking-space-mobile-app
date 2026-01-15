package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.Client;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private RadioButton rbClient, rbOwner;
    private MaterialButton btnRegister;
    private TextView txtLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private Repository repository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        repository = Repository.getInstance(this);
        sharedPreferences = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        rbClient = findViewById(R.id.rbClient);
        rbOwner = findViewById(R.id.rbOwner);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String role = rbClient.isChecked() ? "client" : "owner";

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nom requis");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email invalide");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Téléphone requis");
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 8) {
            etPhone.setError("Numéro de téléphone invalide");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            etPassword.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check if email already exists in local database
        if (repository.emailExists(email)) {
            etEmail.setError("Cet email est déjà utilisé");
            etEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Save user to local database with Firebase UID
                            saveUserToLocalDatabase(firebaseUser);
                            // Send verification email
                            sendEmailVerification(firebaseUser);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            etPassword.setError("Minimum 8 caractères");
            etPassword.requestFocus();
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            etPassword.setError("Doit contenir au moins une majuscule");
            etPassword.requestFocus();
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            etPassword.setError("Doit contenir au moins une minuscule");
            etPassword.requestFocus();
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            etPassword.setError("Doit contenir au moins un chiffre");
            etPassword.requestFocus();
            return false;
        }

        if (!password.matches(".*[@#$%^&+=!].*")) {
            etPassword.setError("Doit contenir au moins un caractère spécial (@#$%^&+=!)");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Erreur")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleRegistrationError(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            etPassword.setError("Mot de passe trop faible. " + exception.getMessage());
            etPassword.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            etEmail.setError("Format email invalide");
            etEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            etEmail.setError("Cet email est déjà utilisé");
            etEmail.requestFocus();
        } else {
            String errorMessage = exception.getMessage();
            if (errorMessage.contains("email address is already in use")) {
                etEmail.setError("Cet email est déjà utilisé");
                etEmail.requestFocus();
            } else if (errorMessage.contains("network error")) {
                Toast.makeText(this, "Erreur réseau. Vérifiez votre connexion", Toast.LENGTH_LONG).show();
            } else {
                showErrorDialog(getFirebaseErrorMessage(exception));
            }
        }
    }

    private void sendEmailVerification(FirebaseUser firebaseUser) {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Email de vérification envoyé à " + firebaseUser.getEmail(),
                                Toast.LENGTH_LONG).show();

                        // Sign out and redirect to login
                        mAuth.signOut();
                        redirectToLoginWithMessage();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Échec d'envoi de l'email de vérification",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToLocalDatabase(FirebaseUser firebaseUser) {
        // Récupérer les données du formulaire
        String name = etName.getText().toString().trim();
        String email = firebaseUser.getEmail();
        String phone = etPhone.getText().toString().trim();
        String role = rbClient.isChecked() ? "client" : "owner";
        String firebaseUid = firebaseUser.getUid();
        String password = etPassword.getText().toString().trim();

        // Créer utilisateur
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPhone(phone);
        user.setFirebaseUid(firebaseUid);
        user.setPassword(password);

        long userId = repository.addUser(user);

        if (userId > 0 && "client".equals(role)) {
            Client client = new Client();
            client.setClientId((int) userId);
            client.setPreferences("");
            repository.addClient(client);
        }

        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
    }

    private void redirectToLoginWithMessage() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("verification_sent", true);
        startActivity(intent);
        finish();
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "Erreur inconnue";

        String error = exception.getMessage();
        if (error.contains("password is invalid")) {
            return "Mot de passe invalide";
        } else if (error.contains("email address is badly formatted")) {
            return "Format email invalide";
        } else if (error.contains("email address is already in use")) {
            return "Email déjà utilisé";
        } else if (error.contains("network error")) {
            return "Problème de connexion réseau";
        } else if (error.contains("too many requests")) {
            return "Trop de tentatives. Réessayez plus tard";
        } else {
            return error;
        }
    }
}