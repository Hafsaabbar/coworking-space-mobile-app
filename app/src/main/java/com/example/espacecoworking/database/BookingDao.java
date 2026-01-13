package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.Booking;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingDao {
    private DatabaseHelper dbHelper;

    public BookingDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addBooking(Booking booking) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_BOOKING_CLIENT_ID, booking.getClientId());
        values.put(DatabaseHelper.KEY_BOOKING_SPACE_ID, booking.getSpaceId());
        values.put(DatabaseHelper.KEY_BOOKING_DATE, booking.getDate());
        values.put(DatabaseHelper.KEY_BOOKING_START_TIME, booking.getStartTime());
        values.put(DatabaseHelper.KEY_BOOKING_END_TIME, booking.getEndTime());
        values.put(DatabaseHelper.KEY_BOOKING_STATUS, booking.getStatus());

        long id = db.insert(DatabaseHelper.TABLE_BOOKINGS, null, values);
        return id;
    }

    // Read - Get booking by ID
    public Booking getBookingById(int bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Booking booking = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.KEY_BOOKING_ID + "=?",
                new String[]{String.valueOf(bookingId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            booking = cursorToBooking(cursor);
            cursor.close();
        }

        return booking;
    }

    // Read - Get all bookings
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null, null, null, null, null,
                DatabaseHelper.KEY_BOOKING_DATE + " DESC, " + DatabaseHelper.KEY_BOOKING_START_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return bookings;
    }

    // Read - Get bookings by client ID
    public List<Booking> getBookingsByClientId(int clientId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.KEY_BOOKING_CLIENT_ID + "=?",
                new String[]{String.valueOf(clientId)},
                null, null,
                DatabaseHelper.KEY_BOOKING_DATE + " DESC, " + DatabaseHelper.KEY_BOOKING_START_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return bookings;
    }

    // Read - Get bookings by space ID
    public List<Booking> getBookingsBySpaceId(int spaceId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.KEY_BOOKING_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null,
                DatabaseHelper.KEY_BOOKING_DATE + " DESC, " + DatabaseHelper.KEY_BOOKING_START_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return bookings;
    }

    // Read - Get bookings by status
    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.KEY_BOOKING_STATUS + "=?",
                new String[]{status},
                null, null,
                DatabaseHelper.KEY_BOOKING_DATE + " DESC, " + DatabaseHelper.KEY_BOOKING_START_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return bookings;
    }

    // Read - Get bookings by date
    public List<Booking> getBookingsByDate(String date) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.KEY_BOOKING_DATE + "=?",
                new String[]{date},
                null, null,
                DatabaseHelper.KEY_BOOKING_START_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return bookings;
    }

    // Read - Get bookings by space and date
    public List<Booking> getBookingsBySpaceAndDate(int spaceId, String date) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_BOOKING_SPACE_ID + "=? AND " +
                DatabaseHelper.KEY_BOOKING_DATE + "=?";
        String[] selectionArgs = new String[]{String.valueOf(spaceId), date};

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.KEY_BOOKING_START_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return bookings;
    }

    // Check for booking conflicts
    public boolean hasConflict(int spaceId, String date, String startTime, String endTime, int excludeBookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BOOKINGS +
                " WHERE " + DatabaseHelper.KEY_BOOKING_SPACE_ID + " = ? " +
                " AND " + DatabaseHelper.KEY_BOOKING_DATE + " = ? " +
                " AND " + DatabaseHelper.KEY_BOOKING_STATUS + " != 'cancelled' " +
                " AND " + DatabaseHelper.KEY_BOOKING_ID + " != ? " + // Exclure soi-même (pour modification)
                " AND " + DatabaseHelper.KEY_BOOKING_START_TIME + " < ? " + // Start DB < End Req
                " AND " + DatabaseHelper.KEY_BOOKING_END_TIME + " > ?";     // End DB > Start Req

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(spaceId), date, String.valueOf(excludeBookingId),
                endTime, startTime
        });

        boolean hasConflict = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasConflict = cursor.getInt(0) > 0;
            cursor.close();
        }

        return hasConflict;
    }

    // Update
    public int updateBooking(Booking booking) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_BOOKING_CLIENT_ID, booking.getClientId());
        values.put(DatabaseHelper.KEY_BOOKING_SPACE_ID, booking.getSpaceId());
        values.put(DatabaseHelper.KEY_BOOKING_DATE, booking.getDate());
        values.put(DatabaseHelper.KEY_BOOKING_START_TIME, booking.getStartTime());
        values.put(DatabaseHelper.KEY_BOOKING_END_TIME, booking.getEndTime());
        values.put(DatabaseHelper.KEY_BOOKING_STATUS, booking.getStatus());
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.KEY_UPDATED_AT, currentDate);

        return db.update(DatabaseHelper.TABLE_BOOKINGS, values,
                DatabaseHelper.KEY_BOOKING_ID + "=?",
                new String[]{String.valueOf(booking.getBookingId())});
    }

    // Update booking status
    public int updateBookingStatus(int bookingId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_BOOKING_STATUS, status);
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.KEY_UPDATED_AT, currentDate);

        return db.update(DatabaseHelper.TABLE_BOOKINGS, values,
                DatabaseHelper.KEY_BOOKING_ID + "=?",
                new String[]{String.valueOf(bookingId)});
    }

    // Delete
    public int deleteBooking(int bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_BOOKINGS,
                DatabaseHelper.KEY_BOOKING_ID + "=?",
                new String[]{String.valueOf(bookingId)});
    }

    // Helper method to convert cursor to Booking object
    private Booking cursorToBooking(Cursor cursor) {
        Booking booking = new Booking();
        booking.setBookingId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_ID)));
        booking.setClientId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_CLIENT_ID)));
        booking.setSpaceId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_SPACE_ID)));
        booking.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_DATE)));
        booking.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_START_TIME)));
        booking.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_END_TIME)));
        booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BOOKING_STATUS)));
        booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
        booking.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_UPDATED_AT)));
        return booking;
    }
}
