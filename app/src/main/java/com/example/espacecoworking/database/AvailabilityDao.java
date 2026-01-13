package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.SpaceAvailability;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDao {
    private DatabaseHelper dbHelper;

    public AvailabilityDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addAvailability(SpaceAvailability availability) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_AVAILABILITY_SPACE_ID, availability.getSpaceId());
        values.put(DatabaseHelper.KEY_AVAILABILITY_DAY_OF_WEEK, availability.getDayOfWeek());
        values.put(DatabaseHelper.KEY_AVAILABILITY_START_TIME, availability.getStartTime());
        values.put(DatabaseHelper.KEY_AVAILABILITY_END_TIME, availability.getEndTime());
        values.put(DatabaseHelper.KEY_AVAILABILITY_IS_AVAILABLE, availability.isAvailable() ? 1 : 0);

        long id = db.insert(DatabaseHelper.TABLE_SPACE_AVAILABILITY, null, values);
        return id;
    }

    // Read - Get availability by ID
    public SpaceAvailability getAvailabilityById(int availabilityId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpaceAvailability availability = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                null,
                DatabaseHelper.KEY_AVAILABILITY_ID + "=?",
                new String[]{String.valueOf(availabilityId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            availability = cursorToAvailability(cursor);
            cursor.close();
        }

        return availability;
    }

    // Read - Get all availabilities
    public List<SpaceAvailability> getAllAvailabilities() {
        List<SpaceAvailability> availabilities = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                availabilities.add(cursorToAvailability(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return availabilities;
    }

    // Read - Get availabilities by space ID
    public List<SpaceAvailability> getAvailabilitiesBySpaceId(int spaceId) {
        List<SpaceAvailability> availabilities = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                null,
                DatabaseHelper.KEY_AVAILABILITY_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                availabilities.add(cursorToAvailability(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return availabilities;
    }

    // Read - Get availabilities by space ID and day of week
    public List<SpaceAvailability> getAvailabilitiesBySpaceAndDay(int spaceId, String dayOfWeek) {
        List<SpaceAvailability> availabilities = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_AVAILABILITY_SPACE_ID + "=? AND " +
                DatabaseHelper.KEY_AVAILABILITY_DAY_OF_WEEK + "=?";
        String[] selectionArgs = new String[]{String.valueOf(spaceId), dayOfWeek};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.KEY_AVAILABILITY_START_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                availabilities.add(cursorToAvailability(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return availabilities;
    }

    // Read - Get available slots by space ID and day of week
    public List<SpaceAvailability> getAvailableSlots(int spaceId, String dayOfWeek) {
        List<SpaceAvailability> availabilities = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_AVAILABILITY_SPACE_ID + "=? AND " +
                DatabaseHelper.KEY_AVAILABILITY_DAY_OF_WEEK + "=? AND " +
                DatabaseHelper.KEY_AVAILABILITY_IS_AVAILABLE + "=?";
        String[] selectionArgs = new String[]{String.valueOf(spaceId), dayOfWeek, "1"};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.KEY_AVAILABILITY_START_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                availabilities.add(cursorToAvailability(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return availabilities;
    }

    // Update
    public int updateAvailability(SpaceAvailability availability) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_AVAILABILITY_SPACE_ID, availability.getSpaceId());
        values.put(DatabaseHelper.KEY_AVAILABILITY_DAY_OF_WEEK, availability.getDayOfWeek());
        values.put(DatabaseHelper.KEY_AVAILABILITY_START_TIME, availability.getStartTime());
        values.put(DatabaseHelper.KEY_AVAILABILITY_END_TIME, availability.getEndTime());
        values.put(DatabaseHelper.KEY_AVAILABILITY_IS_AVAILABLE, availability.isAvailable() ? 1 : 0);

        return db.update(DatabaseHelper.TABLE_SPACE_AVAILABILITY, values,
                DatabaseHelper.KEY_AVAILABILITY_ID + "=?",
                new String[]{String.valueOf(availability.getAvailabilityId())});
    }

    // Update availability status
    public int updateAvailabilityStatus(int availabilityId, boolean isAvailable) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_AVAILABILITY_IS_AVAILABLE, isAvailable ? 1 : 0);

        return db.update(DatabaseHelper.TABLE_SPACE_AVAILABILITY, values,
                DatabaseHelper.KEY_AVAILABILITY_ID + "=?",
                new String[]{String.valueOf(availabilityId)});
    }

    // Delete
    public int deleteAvailability(int availabilityId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                DatabaseHelper.KEY_AVAILABILITY_ID + "=?",
                new String[]{String.valueOf(availabilityId)});
    }

    // Delete all availabilities for a space
    public int deleteAvailabilitiesBySpaceId(int spaceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_AVAILABILITY,
                DatabaseHelper.KEY_AVAILABILITY_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)});
    }

    // Helper method to convert cursor to SpaceAvailability object
    private SpaceAvailability cursorToAvailability(Cursor cursor) {
        SpaceAvailability availability = new SpaceAvailability();
        availability.setAvailabilityId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AVAILABILITY_ID)));
        availability.setSpaceId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AVAILABILITY_SPACE_ID)));
        availability.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AVAILABILITY_DAY_OF_WEEK)));
        availability.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AVAILABILITY_START_TIME)));
        availability.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AVAILABILITY_END_TIME)));
        availability.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AVAILABILITY_IS_AVAILABLE)) == 1);
        return availability;
    }
}
