package com.example.robbinseventtracker.repository;

import android.content.Context;
import android.util.Log;

import com.example.robbinseventtracker.DatabaseHelper;

/**
 * UserRepository - Handles all user-related data operations
 * 
 * This repository follows the Repository pattern and provides:
 * - Clean separation between data access and business logic
 * - Single source of truth for user data
 * - Centralized error handling and logging
 * - Easy testing through abstraction
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private DatabaseHelper databaseHelper;
    
    public UserRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }
    
    /**
     * Authenticate a user with username and password
     * 
     * @param username The username
     * @param password The plain text password (will be verified against hashed password)
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Log.w(TAG, "Authentication failed: username or password is empty");
            return false;
        }
        
        try {
            boolean isAuthenticated = databaseHelper.checkUser(username, password);
            Log.d(TAG, "Authentication for user '" + username + "': " + 
                  (isAuthenticated ? "SUCCESS" : "FAILED"));
            return isAuthenticated;
        } catch (Exception e) {
            Log.e(TAG, "Error during authentication: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create a new user account
     * 
     * @param username The desired username
     * @param password The plain text password (will be hashed before storage)
     * @return true if account creation was successful, false otherwise
     */
    public boolean createUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Log.w(TAG, "User creation failed: username or password is empty");
            return false;
        }
        
        // Validate password strength (minimum 6 characters)
        if (password.length() < 6) {
            Log.w(TAG, "User creation failed: password too short");
            return false;
        }
        
        try {
            long result = databaseHelper.addUser(username, password);
            boolean success = result > 0;
            
            if (success) {
                Log.d(TAG, "User created successfully: " + username);
            } else {
                Log.w(TAG, "User creation failed: username likely already exists: " + username);
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage(), e);
            return false;
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
        
        try {
            return databaseHelper.getUserId(username);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Check if a username exists
     * 
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean userExists(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        return getUserId(username) != -1;
    }
}