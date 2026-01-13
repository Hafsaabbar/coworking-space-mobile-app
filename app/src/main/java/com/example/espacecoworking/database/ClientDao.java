package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.Client;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientDao {
    private DatabaseHelper dbHelper;

    public ClientDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addClient(Client client) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_CLIENT_ID, client.getClientId());
        values.put(DatabaseHelper.KEY_CLIENT_PREFERENCES, client.getPreferences());

        long id = db.insert(DatabaseHelper.TABLE_CLIENTS, null, values);
        return id;
    }

    // Read - Get client by ID
    public Client getClientById(int clientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Client client = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_CLIENTS,
                null,
                DatabaseHelper.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            client = cursorToClient(cursor);
            cursor.close();
        }

        return client;
    }

    // Read - Get all clients
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_CLIENTS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                clients.add(cursorToClient(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return clients;
    }

    // Update
    public int updateClient(Client client) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_CLIENT_PREFERENCES, client.getPreferences());
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.KEY_UPDATED_AT, currentDate);

        return db.update(DatabaseHelper.TABLE_CLIENTS, values,
                DatabaseHelper.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(client.getClientId())});
    }

    // Delete
    public int deleteClient(int clientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CLIENTS,
                DatabaseHelper.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)});
    }

    // Check if client exists
    public boolean clientExists(int clientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CLIENTS,
                new String[]{DatabaseHelper.KEY_CLIENT_ID},
                DatabaseHelper.KEY_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // Helper method to convert cursor to Client object
    private Client cursorToClient(Cursor cursor) {
        Client client = new Client();
        client.setClientId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CLIENT_ID)));
        client.setPreferences(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CLIENT_PREFERENCES)));
        client.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
        client.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_UPDATED_AT)));
        return client;
    }
}
