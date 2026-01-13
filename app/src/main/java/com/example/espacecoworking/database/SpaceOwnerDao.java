package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.SpaceOwner;
import java.util.ArrayList;
import java.util.List;

public class SpaceOwnerDao {
    private DatabaseHelper dbHelper;

    public SpaceOwnerDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addSpaceOwner(SpaceOwner spaceOwner) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_OWNER_ID, spaceOwner.getOwnerId());
        values.put(DatabaseHelper.KEY_SPACE_ID, spaceOwner.getSpaceId());

        long id = db.insert(DatabaseHelper.TABLE_SPACE_OWNERS, null, values);
        return id;
    }

    // Read - Get space owner by ID
    public SpaceOwner getSpaceOwnerById(int spOwId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpaceOwner spaceOwner = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                null,
                DatabaseHelper.KEY_SP_OW_ID + "=?",
                new String[]{String.valueOf(spOwId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            spaceOwner = cursorToSpaceOwner(cursor);
            cursor.close();
        }

        return spaceOwner;
    }

    // Read - Get all space owners
    public List<SpaceOwner> getAllSpaceOwners() {
        List<SpaceOwner> spaceOwners = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaceOwners.add(cursorToSpaceOwner(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaceOwners;
    }

    // Read - Get spaces by owner ID
    public List<SpaceOwner> getSpacesByOwnerId(int ownerId) {
        List<SpaceOwner> spaceOwners = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                null,
                DatabaseHelper.KEY_OWNER_ID + "=?",
                new String[]{String.valueOf(ownerId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaceOwners.add(cursorToSpaceOwner(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaceOwners;
    }

    // Read - Get owners by space ID
    public List<SpaceOwner> getOwnersBySpaceId(int spaceId) {
        List<SpaceOwner> spaceOwners = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                null,
                DatabaseHelper.KEY_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                spaceOwners.add(cursorToSpaceOwner(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return spaceOwners;
    }

    // Read - Get space owner by owner ID and space ID
    public SpaceOwner getSpaceOwnerByOwnerAndSpace(int ownerId, int spaceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpaceOwner spaceOwner = null;

        String selection = DatabaseHelper.KEY_OWNER_ID + "=? AND " +
                DatabaseHelper.KEY_SPACE_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ownerId), String.valueOf(spaceId)};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            spaceOwner = cursorToSpaceOwner(cursor);
            cursor.close();
        }

        return spaceOwner;
    }

    // Check if owner owns space
    public boolean ownerOwnsSpace(int ownerId, int spaceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_OWNER_ID + "=? AND " +
                DatabaseHelper.KEY_SPACE_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ownerId), String.valueOf(spaceId)};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                new String[]{DatabaseHelper.KEY_SP_OW_ID},
                selection, selectionArgs, null, null, null);

        boolean owns = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return owns;
    }

    // Get space count for an owner
    public int getSpaceCountByOwnerId(int ownerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_OWNERS,
                new String[]{"COUNT(*)"},
                DatabaseHelper.KEY_OWNER_ID + "=?",
                new String[]{String.valueOf(ownerId)},
                null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    // Update
    public int updateSpaceOwner(SpaceOwner spaceOwner) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_OWNER_ID, spaceOwner.getOwnerId());
        values.put(DatabaseHelper.KEY_SPACE_ID, spaceOwner.getSpaceId());

        return db.update(DatabaseHelper.TABLE_SPACE_OWNERS, values,
                DatabaseHelper.KEY_SP_OW_ID + "=?",
                new String[]{String.valueOf(spaceOwner.getSpOwId())});
    }

    // Delete
    public int deleteSpaceOwner(int spOwId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_OWNERS,
                DatabaseHelper.KEY_SP_OW_ID + "=?",
                new String[]{String.valueOf(spOwId)});
    }

    // Delete by owner ID and space ID
    public int deleteSpaceOwnerByOwnerAndSpace(int ownerId, int spaceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.KEY_OWNER_ID + "=? AND " +
                DatabaseHelper.KEY_SPACE_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(ownerId), String.valueOf(spaceId)};

        return db.delete(DatabaseHelper.TABLE_SPACE_OWNERS, whereClause, whereArgs);
    }

    // Delete all space owners for a specific space
    public int deleteSpaceOwnersBySpaceId(int spaceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_OWNERS,
                DatabaseHelper.KEY_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)});
    }

    // Delete all spaces owned by a specific owner
    public int deleteSpaceOwnersByOwnerId(int ownerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_OWNERS,
                DatabaseHelper.KEY_OWNER_ID + "=?",
                new String[]{String.valueOf(ownerId)});
    }

    // Helper method to convert cursor to SpaceOwner object
    private SpaceOwner cursorToSpaceOwner(Cursor cursor) {
        SpaceOwner spaceOwner = new SpaceOwner();
        spaceOwner.setSpOwId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SP_OW_ID)));
        spaceOwner.setOwnerId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_OWNER_ID)));
        spaceOwner.setSpaceId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SPACE_ID)));
        return spaceOwner;
    }
}