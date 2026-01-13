package com.example.espacecoworking.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "coworking_space.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_CLIENTS = "Clients";
    public static final String TABLE_SPACES = "Spaces";
    public static final String TABLE_SPACE_IMAGES = "SpaceImages";
    public static final String TABLE_BOOKINGS = "Bookings";
    public static final String TABLE_SPACE_AVAILABILITY = "SpaceAvailability";
    public static final String TABLE_SPACE_OWNERS = "SpaceOwners";

    // Common Column Names
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_UPDATED_AT = "updated_at";

    // USERS Table Columns
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_ROLE = "role";
    public static final String KEY_USER_PHONE = "phone";
    //firebase uid
    public static final String KEY_USER_FIREBASE_UID = "firebase_uid";
    public static final String KEY_USER_IMAGE = "image";

    // CLIENTS Table Columns
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_CLIENT_PREFERENCES = "preferences";

    // SPACES Table Columns
    public static final String KEY_SPACE_ID = "space_id";
    public static final String KEY_SPACE_NAME = "name";
    public static final String KEY_SPACE_LOCATION = "location";
    public static final String KEY_SPACE_CAPACITY = "capacity";
    public static final String KEY_SPACE_DESCRIPTION = "description";
    public static final String KEY_SPACE_PRICE = "price";

    // SPACE_IMAGES Table Columns
    public static final String KEY_IMAGE_ID = "image_id";
    public static final String KEY_IMAGE_SPACE_ID = "space_id";
    public static final String KEY_IMAGE_DATA = "image";

    // BOOKINGS Table Columns
    public static final String KEY_BOOKING_ID = "booking_id";
    public static final String KEY_BOOKING_CLIENT_ID = "client_id";
    public static final String KEY_BOOKING_SPACE_ID = "space_id";
    public static final String KEY_BOOKING_DATE = "date";
    public static final String KEY_BOOKING_START_TIME = "start_time";
    public static final String KEY_BOOKING_END_TIME = "end_time";
    public static final String KEY_BOOKING_STATUS = "status";

    // SPACE_AVAILABILITY Table Columns
    public static final String KEY_AVAILABILITY_ID = "availability_id";
    public static final String KEY_AVAILABILITY_SPACE_ID = "space_id";
    public static final String KEY_AVAILABILITY_DAY_OF_WEEK = "day_of_week";
    public static final String KEY_AVAILABILITY_START_TIME = "start_time";
    public static final String KEY_AVAILABILITY_END_TIME = "end_time";
    public static final String KEY_AVAILABILITY_IS_AVAILABLE = "is_available";

    // SPACE_OWNERS Table Columns
    public static final String KEY_SP_OW_ID = "sp_ow_id";
    public static final String KEY_OWNER_ID = "owner_id";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create USERS table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_NAME + " TEXT NOT NULL,"
                + KEY_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + KEY_USER_PASSWORD + " TEXT NOT NULL,"
                + KEY_USER_ROLE + " TEXT NOT NULL,"
                + KEY_USER_PHONE + " TEXT,"
                + KEY_USER_FIREBASE_UID + " TEXT,"
                + KEY_USER_IMAGE + " BLOB,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME"
                + ")";

        // Create CLIENTS table
        String CREATE_CLIENTS_TABLE = "CREATE TABLE " + TABLE_CLIENTS + "("
                + KEY_CLIENT_ID + " INTEGER PRIMARY KEY,"
                + KEY_CLIENT_PREFERENCES + " TEXT,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_CLIENT_ID + ") REFERENCES "
                + TABLE_USERS + "(" + KEY_USER_ID + ") ON DELETE CASCADE"
                + ")";

        // Create SPACES table
        String CREATE_SPACES_TABLE = "CREATE TABLE " + TABLE_SPACES + "("
                + KEY_SPACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SPACE_NAME + " TEXT NOT NULL,"
                + KEY_SPACE_LOCATION + " TEXT NOT NULL,"
                + KEY_SPACE_CAPACITY + " INTEGER NOT NULL,"
                + KEY_SPACE_DESCRIPTION + " TEXT,"
                + KEY_SPACE_PRICE + " REAL,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME"
                + ")";

        // Create SPACE_IMAGES table
        String CREATE_SPACE_IMAGES_TABLE = "CREATE TABLE " + TABLE_SPACE_IMAGES + "("
                + KEY_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_IMAGE_SPACE_ID + " INTEGER NOT NULL,"
                + KEY_IMAGE_DATA + " BLOB NOT NULL,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_IMAGE_SPACE_ID + ") REFERENCES "
                + TABLE_SPACES + "(" + KEY_SPACE_ID + ") ON DELETE CASCADE"
                + ")";

        // Create BOOKINGS table
        String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + "("
                + KEY_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_BOOKING_CLIENT_ID + " INTEGER NOT NULL,"
                + KEY_BOOKING_SPACE_ID + " INTEGER NOT NULL,"
                + KEY_BOOKING_DATE + " TEXT NOT NULL,"
                + KEY_BOOKING_START_TIME + " TEXT NOT NULL,"
                + KEY_BOOKING_END_TIME + " TEXT NOT NULL,"
                + KEY_BOOKING_STATUS + " TEXT NOT NULL,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_BOOKING_CLIENT_ID + ") REFERENCES "
                + TABLE_CLIENTS + "(" + KEY_CLIENT_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_BOOKING_SPACE_ID + ") REFERENCES "
                + TABLE_SPACES + "(" + KEY_SPACE_ID + ") ON DELETE CASCADE"
                + ")";

        // Create SPACE_AVAILABILITY table
        String CREATE_SPACE_AVAILABILITY_TABLE = "CREATE TABLE " + TABLE_SPACE_AVAILABILITY + "("
                + KEY_AVAILABILITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_AVAILABILITY_SPACE_ID + " INTEGER NOT NULL,"
                + KEY_AVAILABILITY_DAY_OF_WEEK + " TEXT NOT NULL,"
                + KEY_AVAILABILITY_START_TIME + " TEXT NOT NULL,"
                + KEY_AVAILABILITY_END_TIME + " TEXT NOT NULL,"
                + KEY_AVAILABILITY_IS_AVAILABLE + " INTEGER NOT NULL DEFAULT 1,"
                + "FOREIGN KEY(" + KEY_AVAILABILITY_SPACE_ID + ") REFERENCES "
                + TABLE_SPACES + "(" + KEY_SPACE_ID + ") ON DELETE CASCADE"
                + ")";

        // Create SPACE_OWNERS table
        String CREATE_SPACE_OWNERS_TABLE = "CREATE TABLE " + TABLE_SPACE_OWNERS + "("
                + KEY_SP_OW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_OWNER_ID + " INTEGER NOT NULL,"
                + KEY_SPACE_ID + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + KEY_OWNER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_SPACE_ID + ") REFERENCES " + TABLE_SPACES + "(" + KEY_SPACE_ID + ") ON DELETE CASCADE,"
                + "UNIQUE(" + KEY_OWNER_ID + ", " + KEY_SPACE_ID + ")" // <--- Ajout ici
                + ")";

        // Execute table creation
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CLIENTS_TABLE);
        db.execSQL(CREATE_SPACES_TABLE);
        db.execSQL(CREATE_SPACE_IMAGES_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);
        db.execSQL(CREATE_SPACE_AVAILABILITY_TABLE);
        db.execSQL(CREATE_SPACE_OWNERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPACE_OWNERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPACE_AVAILABILITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPACE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    public synchronized void closeDatabase() {
        if (instance != null) {
            SQLiteDatabase db = instance.getWritableDatabase();
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}
