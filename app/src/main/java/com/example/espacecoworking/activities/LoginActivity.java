package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView txtForgotPassword, txtRegister;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private Repository repository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        repository = Repository.getInstance(this);
        sharedPreferences = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);

        initViews();
        setupListeners();

        if (getIntent().getBooleanExtra("verification_sent", false)) {
            showVerificationSentDialog();
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtRegister = findViewById(R.id.txtRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Réinitialiser les erreurs
        etEmail.setError(null);
        etPassword.setError(null);

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

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Authentification Firebase en premier
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        // Vérifier si l'email est vérifié
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            // Récupérer l'utilisateur local
                            User localUser = repository.getUserByEmail(email);

                            if (localUser != null) {
                                // SYNCHRONISATION AUTOMATIQUE du mot de passe
                                // Mettre à jour le mot de passe local avec celui de Firebase
                                localUser.setPassword(password);
                                repository.updateUser(localUser);

                                // Mettre à jour Firebase UID si nécessaire
                                if (localUser.getFirebaseUid() == null || localUser.getFirebaseUid().isEmpty()) {
                                    localUser.setFirebaseUid(mAuth.getCurrentUser().getUid());
                                    repository.updateUser(localUser);
                                }

                                // Sauvegarder la session
                                sharedPreferences.edit()
                                        .putInt("USER_ID", localUser.getUserId())
                                        .putString("USER_EMAIL", localUser.getEmail())
                                        .putString("USER_ROLE", localUser.getRole())
                                        .apply();

                                Toast.makeText(LoginActivity.this,
                                        "Connexion réussie!", Toast.LENGTH_SHORT).show();
                                redirectToHome(localUser);
                            } else {
                                mAuth.signOut();
                                progressBar.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);
                                Toast.makeText(LoginActivity.this,
                                        "Compte non trouvé. Veuillez vous inscrire.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mAuth.signOut();
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            showEmailNotVerifiedDialog(email);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        handleLoginError(task.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            etEmail.setError("Aucun compte trouvé avec cet email");
            etEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            etPassword.setError("Mot de passe incorrect");
            etPassword.requestFocus();
        } else {
            String errorMessage = exception.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("network")) {
                    Toast.makeText(this, "Erreur réseau. Vérifiez votre connexion", Toast.LENGTH_LONG).show();
                } else if (errorMessage.contains("too many requests") || errorMessage.contains("TOO_MANY_ATTEMPTS")) {
                    Toast.makeText(this, "Trop de tentatives. Réessayez plus tard", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEmailNotVerifiedDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email non vérifié")
                .setMessage("Veuillez vérifier votre email avant de vous connecter. Voulez-vous renvoyer l'email de vérification ?")
                .setPositiveButton("Renvoyer", (dialog, which) -> {
                    sendVerificationEmail(email);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void sendVerificationEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);

        String password = etPassword.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (verificationTask.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this,
                                                "Email de vérification envoyé à " + email,
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "Échec d'envoi de l'email de vérification",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    mAuth.signOut();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this,
                                "Impossible d'envoyer l'email de vérification",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showVerificationSentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Email envoyé")
                .setMessage("Un email de vérification vous a été envoyé. Veuillez vérifier votre boîte mail avant de vous connecter.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void redirectToHome(User user) {
        Intent intent;
        if ("client".equals(user.getRole())) {
            intent = new Intent(LoginActivity.this, ClientHomeActivity.class);
        } else if ("owner".equals(user.getRole())) {
            intent = new Intent(LoginActivity.this, OwnerHomeActivity.class);
        } else {
            Toast.makeText(this, "Rôle inconnu", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}