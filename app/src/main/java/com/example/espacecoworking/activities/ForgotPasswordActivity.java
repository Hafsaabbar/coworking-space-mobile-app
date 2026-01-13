package com.example.espacecoworking.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.espacecoworking.R;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnReset;
    private TextView txtBackToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgotPassword), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        repository = Repository.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnReset.setOnClickListener(v -> resetPassword());
        txtBackToLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

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

        // Vérifier si l'email existe dans la base locale
        User localUser = repository.getUserByEmail(email);
        if (localUser == null) {
            etEmail.setError("Aucun compte trouvé avec cet email");
            etEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnReset.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnReset.setEnabled(true);

                    if (task.isSuccessful()) {
                        showSuccessDialog(email);
                    } else {
                        handleResetError(task.getException());
                    }
                });
    }

    private void handleResetError(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            etEmail.setError("Aucun compte trouvé avec cet email");
            etEmail.requestFocus();
        } else {
            String errorMessage = exception.getMessage();
            if (errorMessage.contains("network error")) {
                Toast.makeText(this,
                        "Erreur réseau. Vérifiez votre connexion",
                        Toast.LENGTH_LONG).show();
            } else if (errorMessage.contains("too many requests")) {
                Toast.makeText(this,
                        "Trop de tentatives. Réessayez plus tard",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        "Erreur: " + getFirebaseErrorMessage(exception),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(ForgotPasswordActivity.this)
                .setTitle("Email envoyé")
                .setMessage("Un email de réinitialisation a été envoyé à " + email +
                        "\n\n⚠️ IMPORTANT : Après avoir réinitialisé votre mot de passe via l'email, " +
                        "vous devrez vous connecter avec votre nouveau mot de passe. " +
                        "Le système synchronisera automatiquement votre mot de passe.")
                .setPositiveButton("Compris", (dialog, which) -> finish())
                .setNeutralButton("Renvoyer", (dialog, which) -> resendResetEmail(email))
                .setCancelable(false)
                .show();
    }

    private void resendResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        btnReset.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnReset.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Email renvoyé avec succès",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Échec de renvoi de l'email",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "Erreur inconnue";

        String error = exception.getMessage();
        if (error.contains("user not found")) {
            return "Aucun compte trouvé avec cet email";
        } else if (error.contains("network error")) {
            return "Problème de connexion réseau";
        } else if (error.contains("too many requests")) {
            return "Trop de tentatives. Réessayez plus tard";
        } else {
            return error;
        }
    }
}