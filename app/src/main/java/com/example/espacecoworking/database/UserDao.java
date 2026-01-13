package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDao {
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USER_NAME, user.getName());
        values.put(DatabaseHelper.KEY_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.KEY_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.KEY_USER_ROLE, user.getRole());
        values.put(DatabaseHelper.KEY_USER_PHONE, user.getPhone());
        values.put(DatabaseHelper.KEY_USER_FIREBASE_UID, user.getFirebaseUid());
        values.put(DatabaseHelper.KEY_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(DatabaseHelper.KEY_USER_IMAGE, user.getImage());

        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        return id;
    }

    // Read - Get user by ID
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.KEY_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    // Read - Get user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.KEY_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    // Read - Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    // Read - Get users by role
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.KEY_USER_ROLE + "=?",
                new String[]{role},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    // Update
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USER_NAME, user.getName());
        values.put(DatabaseHelper.KEY_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.KEY_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.KEY_USER_ROLE, user.getRole());
        values.put(DatabaseHelper.KEY_USER_FIREBASE_UID, user.getFirebaseUid());
        values.put(DatabaseHelper.KEY_USER_PHONE, user.getPhone());
        values.put(DatabaseHelper.KEY_USER_IMAGE, user.getImage());
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.KEY_UPDATED_AT, currentDate);

        return db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.KEY_USER_ID + "=?",
                new String[]{String.valueOf(user.getUserId())});
    }

    // Delete
    public int deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_USERS,
                DatabaseHelper.KEY_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    // Authentication
    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.KEY_USER_EMAIL + "=? AND " + DatabaseHelper.KEY_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    // Check if email exists
    public boolean emailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.KEY_USER_ID},
                DatabaseHelper.KEY_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // Helper method to convert cursor to User object
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ROLE)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PHONE)));
        user.setFirebaseUid(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_FIREBASE_UID)));
        user.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_IMAGE)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
        user.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_UPDATED_AT)));
        return user;
    }
}
