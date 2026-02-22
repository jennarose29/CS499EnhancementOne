package com.example.robbinseventtracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DashboardActivity extends Activity {
    private static final int SMS_PERMISSION_CODE = 100;

    // UI elements
    private Button buttonAddEvent;

    // Database helper
    private DatabaseHelper databaseHelper;

    // User info
    private String currentUsername;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get current user
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");
        currentUserId = databaseHelper.getUserId(currentUsername);

        // Initialize UI elements
        buttonAddEvent = findViewById(R.id.buttonAddEvent);

        // Set up add event button click listener
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open event entry activity
                Intent intent = new Intent(DashboardActivity.this, EventEntryActivity.class);
                startActivity(intent);
            }
        });

        // Check for SMS permission
        checkSmsPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload events when returning to this activity
        loadEvents();
    }

    /**
     * Load events from database
     * This is a placeholder method that doesn't actually load events
     * since we're using static example events in the layout
     */
    public void loadEvents() {
        // This is a placeholder method that doesn't do anything yet
        // Since we're using static example events in the layout,
        // we don't actually need to load events from the database
        Toast.makeText(this, "Events refreshed", Toast.LENGTH_SHORT).show();
    }

    private void checkSmsPermission() {
        // Check if permission is already granted
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Show permission dialog
            showSmsPermissionDialog();
        }
    }

    private void showSmsPermissionDialog() {
        // Create custom dialog for SMS permission
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_sms_permission);
        dialog.setCancelable(false);

        Button buttonAllow = dialog.findViewById(R.id.buttonAllowPermission);
        Button buttonDeny = dialog.findViewById(R.id.buttonDenyPermission);

        buttonAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Request permission
                requestPermissions(new String[]{android.Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            }
        });

        buttonDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(DashboardActivity.this,
                        "SMS notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS notifications disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear login state
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Return to login screen
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
