package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.espacecoworking.R;
import com.example.espacecoworking.database.DatabaseHelper;
import com.example.espacecoworking.models.User;
import com.example.espacecoworking.repository.Repository;
import com.example.espacecoworking.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnRegister;
    private FirebaseAuth mAuth;
    private Repository repository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);

        mAuth = FirebaseAuth.getInstance();
        repository = Repository.getInstance(this);
        sharedPreferences = getSharedPreferences("EspaceCoworkingPrefs", MODE_PRIVATE);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.getWritableDatabase();

        initViews();
        checkCurrentUser();
        setupListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        int localUserId = sharedPreferences.getInt("USER_ID", -1);

        if (currentUser != null && localUserId != -1) {
            // User already logged in
            User user = repository.getUserById(localUserId);
            if (user != null) {
                redirectToHome(user);
            } else {
                // Clear invalid session
                mAuth.signOut();
                sharedPreferences.edit().clear().apply();
            }
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }

    private void redirectToHome(User user) {
        Intent intent;
        if ("client".equals(user.getRole())) {
            intent = new Intent(MainActivity.this, ClientHomeActivity.class);
        } else if ("owner".equals(user.getRole())) {
            intent = new Intent(MainActivity.this, OwnerHomeActivity.class);
        } else {
            return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
