package com.example.robbinseventtracker.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.robbinseventtracker.repository.UserRepository;

/**
 * LoginViewModel - Handles authentication logic and user session management
 * 
 * This ViewModel:
 * - Separates business logic from UI
 * - Provides LiveData for reactive UI updates
 * - Performs authentication operations on background threads
 * - Survives configuration changes (screen rotations)
 */
public class LoginViewModel extends AndroidViewModel {
    private static final String TAG = "LoginViewModel";
    
    private UserRepository userRepository;
    
    // LiveData for authentication results
    private MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> registrationResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // LiveData for authentication state
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    
    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application.getApplicationContext());
        checkLoginState();
    }
    
    /**
     * Check if user is currently logged in
     */
    private void checkLoginState() {
        android.content.SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
            "LoginPrefs", android.content.Context.MODE_PRIVATE);
        boolean loggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        isLoggedIn.postValue(loggedIn);
    }
    
    // Getters for LiveData
    public MutableLiveData<Boolean> getLoginResult() {
        return loginResult;
    }
    
    public MutableLiveData<Boolean> getRegistrationResult() {
        return registrationResult;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public MutableLiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }
    
    /**
     * Authenticate user with username and password
     * Performs authentication on background thread to prevent UI freezing
     * 
     * @param username The username
     * @param password The plain text password
     */
    public void login(String username, String password) {
        // Clear previous error messages
        errorMessage.postValue(null);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            errorMessage.postValue("Please enter a username");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errorMessage.postValue("Please enter a password");
            return;
        }
        
        // Perform authentication on background thread
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                boolean isAuthenticated = userRepository.authenticate(username.trim(), password);
                
                if (isAuthenticated) {
                    // Save login state
                    saveLoginState(username.trim());
                    Log.d(TAG, "Login successful for user: " + username);
                    loginResult.postValue(true);
                } else {
                    errorMessage.postValue("Invalid username or password");
                    loginResult.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during login: " + e.getMessage(), e);
                errorMessage.postValue("An error occurred during login");
                loginResult.postValue(false);
            } finally {
                executor.shutdown();
            }
        });
    }
    
    /**
     * Create a new user account
     * Performs registration on background thread to prevent UI freezing
     * 
     * @param username The desired username
     * @param password The desired password
     */
    public void register(String username, String password) {
        // Clear previous error messages
        errorMessage.postValue(null);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            errorMessage.postValue("Please enter a username");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errorMessage.postValue("Please enter a password");
            return;
        }
        
        if (password.length() < 6) {
            errorMessage.postValue("Password must be at least 6 characters long");
            return;
        }
        
        // Perform registration on background thread
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                boolean isCreated = userRepository.createUser(username.trim(), password);
                
                if (isCreated) {
                    // Automatically log in after registration
                    saveLoginState(username.trim());
                    Log.d(TAG, "Registration successful for user: " + username);
                    registrationResult.postValue(true);
                } else {
                    errorMessage.postValue("Username already exists");
                    registrationResult.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during registration: " + e.getMessage(), e);
                errorMessage.postValue("An error occurred during registration");
                registrationResult.postValue(false);
            } finally {
                executor.shutdown();
            }
        });
    }
    
    /**
     * Save login state to SharedPreferences
     * 
     * @param username The username to save
     */
    private void saveLoginState(String username) {
        android.content.SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
            "LoginPrefs", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
        isLoggedIn.postValue(true);
    }
    
    /**
     * Logout the current user
     */
    public void logout() {
        android.content.SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
            "LoginPrefs", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        isLoggedIn.postValue(false);
        Log.d(TAG, "User logged out");
    }
    
    /**
     * Get the current username
     * 
     * @return The current username, or empty string if not logged in
     */
    public String getCurrentUsername() {
        android.content.SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
            "LoginPrefs", android.content.Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }
}