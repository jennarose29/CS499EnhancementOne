package com.example.robbinseventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.robbinseventtracker.security.PasswordManager;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper - Manages SQLite database operations for the Event Tracker app
 * 
 * Version 2 Changes:
 * - Migrated to BCrypt password hashing for enhanced security
 * - Added automatic migration of existing plain text passwords
 * - Improved error handling and logging
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    
    // Database Info
    private static final String DATABASE_NAME = "EventTracker.db";
    private static final int DATABASE_VERSION = 2; // Incremented for password hashing migration

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
        
        Log.d(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        if (oldVersion < 2) {
            // Migrate from version 1 to version 2: Hash existing passwords
            migratePasswordsToHash(db);
        }
    }
    
    /**
     * Migrate existing plain text passwords to BCrypt hashes
     * This ensures backward compatibility while improving security
     */
    private void migratePasswordsToHash(SQLiteDatabase db) {
        Log.d(TAG, "Starting password migration to BCrypt hashes");
        
        try {
            // Get all users with plain text passwords
            Cursor cursor = db.query(TABLE_USERS, 
                new String[]{KEY_USER_ID, KEY_USER_NAME, KEY_USER_PASSWORD},
                null, null, null, null, null);
            
            if (cursor != null) {
                int migratedCount = 0;
                
                while (cursor.moveToNext()) {
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME));
                    String plainPassword = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD));
                    
                    // Check if password is already hashed (BCrypt hashes start with $2a$)
                    if (plainPassword != null && !plainPassword.startsWith("$2a$")) {
                        // Hash the plain text password
                        String hashedPassword = PasswordManager.hashPassword(plainPassword);
                        
                        // Update the database with hashed password
                        ContentValues values = new ContentValues();
                        values.put(KEY_USER_PASSWORD, hashedPassword);
                        
                        int rowsUpdated = db.update(TABLE_USERS, values, 
                            KEY_USER_ID + "=?", 
                            new String[]{String.valueOf(userId)});
                        
                        if (rowsUpdated > 0) {
                            migratedCount++;
                            Log.d(TAG, "Migrated password for user: " + username);
                        }
                    }
                }
                
                cursor.close();
                Log.d(TAG, "Password migration completed. Migrated " + migratedCount + " passwords");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during password migration: " + e.getMessage(), e);
        }
    }

    /**
     * Add a new user with hashed password
     * 
     * @param username The username
     * @param password The plain text password (will be hashed before storage)
     * @return The row ID of the newly inserted user, or -1 if an error occurred
     */
    public long addUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Log.e(TAG, "Cannot add user: username or password is empty");
            return -1;
        }
        
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            
            // Hash the password before storing
            String hashedPassword = PasswordManager.hashPassword(password);
            
            ContentValues values = new ContentValues();
            values.put(KEY_USER_NAME, username);
            values.put(KEY_USER_PASSWORD, hashedPassword);

            // Insert row
            long id = db.insert(TABLE_USERS, null, values);
            db.close();
            
            if (id > 0) {
                Log.d(TAG, "User added successfully: " + username);
            } else {
                Log.e(TAG, "Failed to add user: " + username);
            }
            
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Check user credentials using BCrypt password verification
     * 
     * @param username The username
     * @param password The plain text password to verify
     * @return true if credentials are valid, false otherwise
     */
    public boolean checkUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            String[] columns = {KEY_USER_PASSWORD};
            String selection = KEY_USER_NAME + "=?";
            String[] selectionArgs = {username};

            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD));
                
                // Verify password against stored hash
                boolean isValid = PasswordManager.verifyPassword(password, storedHash);
                
                if (isValid) {
                    Log.d(TAG, "User authentication successful: " + username);
                } else {
                    Log.d(TAG, "User authentication failed: " + username);
                }
                
                return isValid;
            }
            
            Log.d(TAG, "User not found: " + username);
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking user credentials: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Get user ID by username
     * 
     * @param username The username
     * @return The user ID, or -1 if user not found
     */
    public int getUserId(String username) {
        if (username == null || username.isEmpty()) {
            return -1;
        }
        
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID},
                    KEY_USER_NAME + "=?", new String[]{username}, null, null, null);

            int userId = -1;
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID));
            }
            
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage(), e);
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Add a new event
     * 
     * @return The row ID of the newly inserted event, or -1 if an error occurred
     */
    public long addEvent(String name, String description, String date, String time,
                         String location, int notification, int userId) {
        try {
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
            
            if (id > 0) {
                Log.d(TAG, "Event added successfully: " + name);
            }
            
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding event: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Get all events for a specific user
     * 
     * @param userId The user ID
     * @return Cursor containing all events for the user
     */
    public Cursor getAllEvents(int userId) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.query(TABLE_EVENTS, null, KEY_EVENT_USER_ID + "=?",
                    new String[]{String.valueOf(userId)}, null, null, 
                    KEY_EVENT_DATE + " ASC, " + KEY_EVENT_TIME + " ASC");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all events: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get all events for a specific user as a list of Event objects
     * This method is used by the repository layer for MVVM architecture
     * 
     * @param userId The user ID
     * @return List of Event objects
     */
    public List<Event> getAllEventsList(int userId) {
        List<Event> events = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = getAllEvents(userId);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EVENT_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_DESCRIPTION));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_DATE));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_TIME));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_LOCATION));
                    int notification = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EVENT_NOTIFICATION));
                    
                    Event event = new Event(id, name, description, date, time, location, notification, userId);
                    events.add(event);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting events list: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return events;
    }

    /**
     * Get a specific event by ID
     * 
     * @param eventId The event ID
     * @return Cursor containing the event data
     */
    public Cursor getEvent(int eventId) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.query(TABLE_EVENTS, null, KEY_EVENT_ID + "=?",
                    new String[]{String.valueOf(eventId)}, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting event: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Update an existing event
     * 
     * @return true if update was successful, false otherwise
     */
    public boolean updateEvent(int id, String name, String description, String date,
                               String time, String location, int notification) {
        try {
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
            
            boolean success = rowsAffected > 0;
            if (success) {
                Log.d(TAG, "Event updated successfully: " + name);
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error updating event: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete an event
     * 
     * @param id The event ID
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEvent(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int rowsAffected = db.delete(TABLE_EVENTS, KEY_EVENT_ID + "=?",
                    new String[]{String.valueOf(id)});
            db.close();
            
            boolean success = rowsAffected > 0;
            if (success) {
                Log.d(TAG, "Event deleted successfully: ID " + id);
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting event: " + e.getMessage(), e);
            return false;
        }
    }
}