package com.example.robbinseventtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.robbinseventtracker.viewmodel.LoginViewModel;

/**
 * LoginActivity - Handles user authentication using MVVM architecture
 * 
 * This activity now follows MVVM pattern:
 * - View: Handles UI interactions and displays data
 * - ViewModel: Manages authentication logic and business rules
 * - Model: Database and password hashing (handled by Repository and DatabaseHelper)
 * 
 * Benefits of MVVM implementation:
 * - Clear separation of concerns
 * - Testable business logic
 * - Survives configuration changes
 * - Reactive UI updates with LiveData
 */
public class LoginActivity extends AppCompatActivity {
    // UI elements
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonCreateAccount;

    // ViewModel
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI elements
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Observe login state - auto-navigate if already logged in
        loginViewModel.getIsLoggedIn().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoggedIn) {
                if (isLoggedIn) {
                    navigateToDashboard();
                }
            }
        });

        // Observe login result
        loginViewModel.getLoginResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    Toast.makeText(LoginActivity.this, 
                        "Login successful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe registration result
        loginViewModel.getRegistrationResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    Toast.makeText(LoginActivity.this, 
                        "Account created successfully", Toast.LENGTH_SHORT).show();
                    // Navigation will happen automatically via isLoggedIn observer
                }
            }
        });

        // Observe error messages
        loginViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up login button click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set up create account button click listener
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    /**
     * Handle login - delegates to ViewModel
     */
    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Delegate authentication to ViewModel
        loginViewModel.login(username, password);
    }

    /**
     * Handle account creation - delegates to ViewModel
     */
    private void createAccount() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Delegate registration to ViewModel
        loginViewModel.register(username, password);
    }

    /**
     * Navigate to dashboard
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }
}