package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.Space;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpaceDao {
    private DatabaseHelper dbHelper;

    public SpaceDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addSpace(Space space) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_SPACE_NAME, space.getName());
        values.put(DatabaseHelper.KEY_SPACE_LOCATION, space.getLocation());
        values.put(DatabaseHelper.KEY_SPACE_CAPACITY, space.getCapacity());
        values.put(DatabaseHelper.KEY_SPACE_DESCRIPTION, space.getDescription());
        values.put(DatabaseHelper.KEY_SPACE_PRICE, space.getPrice());

        long id = db.insert(DatabaseHelper.TABLE_SPACES, null, values);
        return id;
    }

    // Read - Get space by ID
    public Space getSpaceById(int spaceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Space space = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACES,
                null,
                DatabaseHelper.KEY_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            space = cursorToSpace(cursor);
            cursor.close();
        }

        return space;
    }

    // Read - Get all spaces
    public List<Space> getAllSpaces() {
        List<Space> spaces = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACES,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaces.add(cursorToSpace(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaces;
    }

    // Read - Get spaces by location
    public List<Space> getSpacesByLocation(String location) {
        List<Space> spaces = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACES,
                null,
                DatabaseHelper.KEY_SPACE_LOCATION + " LIKE ?",
                new String[]{"%" + location + "%"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaces.add(cursorToSpace(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaces;
    }

    // Read - Get spaces by minimum capacity
    public List<Space> getSpacesByMinCapacity(int minCapacity) {
        List<Space> spaces = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACES,
                null,
                DatabaseHelper.KEY_SPACE_CAPACITY + ">=?",
                new String[]{String.valueOf(minCapacity)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaces.add(cursorToSpace(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaces;
    }

    // Read - Get spaces by owner ID
    public List<Space> getSpacesByOwnerId(int ownerId) {
        List<Space> spaces = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT s.* FROM " + DatabaseHelper.TABLE_SPACES + " s " +
                "INNER JOIN " + DatabaseHelper.TABLE_SPACE_OWNERS + " so " +
                "ON s." + DatabaseHelper.KEY_SPACE_ID + " = so." + DatabaseHelper.KEY_SPACE_ID +
                " WHERE so." + DatabaseHelper.KEY_OWNER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(ownerId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaces.add(cursorToSpace(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaces;
    }

    // Update
    public int updateSpace(Space space) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_SPACE_NAME, space.getName());
        values.put(DatabaseHelper.KEY_SPACE_LOCATION, space.getLocation());
        values.put(DatabaseHelper.KEY_SPACE_CAPACITY, space.getCapacity());
        values.put(DatabaseHelper.KEY_SPACE_DESCRIPTION, space.getDescription());
        values.put(DatabaseHelper.KEY_SPACE_PRICE, space.getPrice());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.KEY_UPDATED_AT, currentDate);

        return db.update(DatabaseHelper.TABLE_SPACES, values,
                DatabaseHelper.KEY_SPACE_ID + "=?",
                new String[]{String.valueOf(space.getSpaceId())});
    }

    // Delete
    public int deleteSpace(int spaceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACES,
                DatabaseHelper.KEY_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)});
    }

    // Search spaces by name or location
    public List<Space> searchSpaces(String keyword) {
        List<Space> spaces = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_SPACE_NAME + " LIKE ? OR " +
                DatabaseHelper.KEY_SPACE_LOCATION + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + keyword + "%", "%" + keyword + "%"};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACES,
                null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaces.add(cursorToSpace(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaces;
    }

    // Helper method to convert cursor to Space object
    private Space cursorToSpace(Cursor cursor) {
        Space space = new Space();
        space.setSpaceId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_ID)));
        space.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_NAME)));
        space.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_LOCATION)));
        space.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_CAPACITY)));
        space.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_DESCRIPTION)));
        space.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
        space.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_UPDATED_AT)));
        space.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_PRICE)));
        return space;
    }
}
