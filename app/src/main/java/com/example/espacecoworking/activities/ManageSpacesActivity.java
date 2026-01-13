package com.example.espacecoworking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.OwnerSpaceAdapter;
import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.repository.Repository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageSpacesActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewSpaces;
    private TextView txtNoSpaces;
    private FloatingActionButton fabAddSpace;

    private Repository repository;
    private OwnerSpaceAdapter spaceAdapter;
    private List<Space> spaceList;
    private int ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_spaces);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        setupRecyclerView();
        loadSpaces();

        fabAddSpace.setOnClickListener(v -> {
            Intent intent = new Intent(ManageSpacesActivity.this, AddEditSpaceActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewSpaces = findViewById(R.id.recyclerViewSpaces);
        txtNoSpaces = findViewById(R.id.txtNoSpaces);
        fabAddSpace = findViewById(R.id.fabAddSpace);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.manage_spaces);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        spaceList = new ArrayList<>();
        spaceAdapter = new OwnerSpaceAdapter(this, spaceList, new OwnerSpaceAdapter.OnSpaceActionListener() {
            @Override
            public void onEditSpace(Space space) {
                Intent intent = new Intent(ManageSpacesActivity.this, AddEditSpaceActivity.class);
                intent.putExtra("SPACE_ID", space.getSpaceId());
                startActivity(intent);
            }

            @Override
            public void onDeleteSpace(Space space) {
                showDeleteDialog(space);
            }

            @Override
            public void onViewBookings(Space space) {
                Intent intent = new Intent(ManageSpacesActivity.this, OwnerBookingActivity.class);
                intent.putExtra("SPACE_ID", space.getSpaceId());
                startActivity(intent);
            }
        });

        recyclerViewSpaces.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSpaces.setAdapter(spaceAdapter);
    }

    private void loadSpaces() {
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
    }

    private void showDeleteDialog(Space space) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_space_title)
                .setMessage(R.string.delete_space_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteSpace(space))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteSpace(Space space) {
        // Supprimer dans l'ordre : images, disponibilités, relations, puis l'espace
        repository.deleteImagesBySpaceId(space.getSpaceId());
        repository.deleteAvailabilitiesBySpaceId(space.getSpaceId());
        repository.deleteSpaceOwnersBySpaceId(space.getSpaceId());

        int result = repository.deleteSpace(space.getSpaceId());

        if (result > 0) {
            Toast.makeText(this, R.string.space_deleted, Toast.LENGTH_SHORT).show();
            loadSpaces();
        } else {
            Toast.makeText(this, R.string.delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSpaces();
    }
}