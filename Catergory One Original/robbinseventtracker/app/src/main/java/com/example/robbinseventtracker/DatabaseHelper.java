package com.example.robbinseventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "EventTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EVENTS = "events";

    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_USER_PASSWORD = "password";

    // Event Table Columns
    private static final String KEY_EVENT_ID = "id";
    private static final String KEY_EVENT_NAME = "name";
    private static final String KEY_EVENT_DESCRIPTION = "description";
    private static final String KEY_EVENT_DATE = "date";
    private static final String KEY_EVENT_TIME = "time";
    private static final String KEY_EVENT_LOCATION = "location";
    private static final String KEY_EVENT_NOTIFICATION = "notification";
    private static final String KEY_EVENT_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_NAME + " TEXT UNIQUE,"
                + KEY_USER_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create events table
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + KEY_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EVENT_NAME + " TEXT,"
                + KEY_EVENT_DESCRIPTION + " TEXT,"
                + KEY_EVENT_DATE + " TEXT,"
                + KEY_EVENT_TIME + " TEXT,"
                + KEY_EVENT_LOCATION + " TEXT,"
                + KEY_EVENT_NOTIFICATION + " INTEGER,"
                + KEY_EVENT_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + KEY_EVENT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, username);
        values.put(KEY_USER_PASSWORD, password);

        // Insert row
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_USER_ID};
        String selection = KEY_USER_NAME + "=?" + " AND " + KEY_USER_PASSWORD + "=?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID},
                KEY_USER_NAME + "=?", new String[]{username}, null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public long addEvent(String name, String description, String date, String time,
                         String location, int notification, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT_NAME, name);
        values.put(KEY_EVENT_DESCRIPTION, description);
        values.put(KEY_EVENT_DATE, date);
        values.put(KEY_EVENT_TIME, time);
        values.put(KEY_EVENT_LOCATION, location);
        values.put(KEY_EVENT_NOTIFICATION, notification);
        values.put(KEY_EVENT_USER_ID, userId);

        // Insert row
        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return id;
    }

    public Cursor getAllEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, KEY_EVENT_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, KEY_EVENT_DATE + " ASC, " + KEY_EVENT_TIME + " ASC");
    }

    public Cursor getEvent(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, KEY_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
    }

    public boolean updateEvent(int id, String name, String description, String date,
                               String time, String location, int notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT_NAME, name);
        values.put(KEY_EVENT_DESCRIPTION, description);
        values.put(KEY_EVENT_DATE, date);
        values.put(KEY_EVENT_TIME, time);
        values.put(KEY_EVENT_LOCATION, location);
        values.put(KEY_EVENT_NOTIFICATION, notification);

        // Update row
        int rowsAffected = db.update(TABLE_EVENTS, values, KEY_EVENT_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_EVENTS, KEY_EVENT_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }
}
