package com.example.robbinseventtracker.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordManager - Handles secure password hashing and verification using BCrypt
 * 
 * This class provides industry-standard password security by:
 * 1. Hashing passwords with BCrypt before storage
 * 2. Using salt to prevent rainbow table attacks
 * 3. Verifying passwords against stored hashes during authentication
 * 
 * BCrypt automatically handles salt generation and is designed to be slow,
 * making brute-force attacks computationally expensive.
 */
public class PasswordManager {
    
    // Number of rounds for BCrypt hashing (higher = more secure but slower)
    // 12 rounds provides strong security while maintaining reasonable performance
    private static final int SALT_ROUNDS = 12;
    
    /**
     * Hash a plain text password using BCrypt
     * 
     * @param plainPassword The plain text password to hash
     * @return The hashed password string (includes salt)
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Generate salt and hash the password
        // BCrypt.gensalt() creates a random salt with the specified number of rounds
        String salt = BCrypt.gensalt(SALT_ROUNDS);
        
        // BCrypt.hashpw() combines the password with the salt and produces the hash
        // The resulting hash includes the salt, algorithm version, and cost factor
        return BCrypt.hashpw(plainPassword, salt);
    }
    
    /**
     * Verify a plain text password against a stored hash
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The stored hashed password
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            // BCrypt.checkpw() extracts the salt from the hash and verifies the password
            // This is a constant-time comparison to prevent timing attacks
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }
    
    /**
     * Check if a password needs rehashing (e.g., if security standards have changed)
     * 
     * @param hashedPassword The stored hashed password
     * @return true if the password should be rehashed with current settings
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return true;
        }
        
        try {
            // Extract the cost factor from the hash
            // BCrypt hash format: $2a$[cost]$[22 character salt][31 character hash]
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) {
                return true;
            }
            
            int currentCost = Integer.parseInt(parts[2]);
            return currentCost < SALT_ROUNDS;
        } catch (Exception e) {
            return true;
        }
    }
}