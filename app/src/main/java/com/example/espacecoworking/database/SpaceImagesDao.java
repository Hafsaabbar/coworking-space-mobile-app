package com.example.espacecoworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.espacecoworking.models.SpaceImages;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpaceImagesDao {
    private DatabaseHelper dbHelper;

    public SpaceImagesDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long addSpaceImage(SpaceImages spaceImage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_IMAGE_SPACE_ID, spaceImage.getSpaceId());
        values.put(DatabaseHelper.KEY_IMAGE_DATA, spaceImage.getImage());

        long id = db.insert(DatabaseHelper.TABLE_SPACE_IMAGES, null, values);
        return id;
    }

    // Read - Get image by ID
    public SpaceImages getImageById(int imageId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpaceImages spaceImage = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_IMAGES,
                null,
                DatabaseHelper.KEY_IMAGE_ID + "=?",
                new String[]{String.valueOf(imageId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            spaceImage = cursorToSpaceImage(cursor);
            cursor.close();
        }

        return spaceImage;
    }

    // Read - Get all images
    public List<SpaceImages> getAllImages() {
        List<SpaceImages> images = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_IMAGES,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                images.add(cursorToSpaceImage(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return images;
    }

    // Read - Get images by space ID
    public List<SpaceImages> getImagesBySpaceId(int spaceId) {
        List<SpaceImages> images = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_IMAGES,
                null,
                DatabaseHelper.KEY_IMAGE_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null,
                DatabaseHelper.KEY_CREATED_AT + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                images.add(cursorToSpaceImage(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return images;
    }

    // Read - Get first image for a space (for thumbnail)
    public SpaceImages getFirstImageBySpaceId(int spaceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpaceImages spaceImage = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_IMAGES,
                null,
                DatabaseHelper.KEY_IMAGE_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null,
                DatabaseHelper.KEY_CREATED_AT + " ASC",
                "1");

        if (cursor != null && cursor.moveToFirst()) {
            spaceImage = cursorToSpaceImage(cursor);
            cursor.close();
        }

        return spaceImage;
    }

    // Get image count for a space
    public int getImageCountBySpaceId(int spaceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPACE_IMAGES,
                new String[]{"COUNT(*)"},
                DatabaseHelper.KEY_IMAGE_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)},
                null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    // Update
    public int updateSpaceImage(SpaceImages spaceImage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_IMAGE_SPACE_ID, spaceImage.getSpaceId());
        values.put(DatabaseHelper.KEY_IMAGE_DATA, spaceImage.getImage());
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.KEY_UPDATED_AT, currentDate);

        return db.update(DatabaseHelper.TABLE_SPACE_IMAGES, values,
                DatabaseHelper.KEY_IMAGE_ID + "=?",
                new String[]{String.valueOf(spaceImage.getImageId())});
    }

    // Delete
    public int deleteImage(int imageId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_IMAGES,
                DatabaseHelper.KEY_IMAGE_ID + "=?",
                new String[]{String.valueOf(imageId)});
    }

    // Delete all images for a space
    public int deleteImagesBySpaceId(int spaceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SPACE_IMAGES,
                DatabaseHelper.KEY_IMAGE_SPACE_ID + "=?",
                new String[]{String.valueOf(spaceId)});
    }

    // Helper method to convert cursor to SpaceImages object
    private SpaceImages cursorToSpaceImage(Cursor cursor) {
        SpaceImages spaceImage = new SpaceImages();
        spaceImage.setImageId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IMAGE_ID)));
        spaceImage.setSpaceId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IMAGE_SPACE_ID)));
        spaceImage.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IMAGE_DATA)));
        spaceImage.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
        spaceImage.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_UPDATED_AT)));
        return spaceImage;
    }
}
