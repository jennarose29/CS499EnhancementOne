package com.example.robbinseventtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robbinseventtracker.viewmodel.EventViewModel;
import com.example.robbinseventtracker.viewmodel.LoginViewModel;

import java.util.List;

/**
 * DashboardActivity - Main dashboard using MVVM architecture
 * 
 * This activity now follows MVVM pattern:
 * - View: Handles UI interactions and displays event list
 * - ViewModel: Manages event data and business logic
 * - Model: Database operations (handled by Repository)
 * 
 * Benefits of MVVM implementation:
 * - Separates UI from business logic
 * - Background threading prevents UI freezing
 * - LiveData provides automatic UI updates
 * - Survives configuration changes
 */
public class DashboardActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 100;

    // UI elements
    private Button buttonAddEvent;
    private RecyclerView recyclerViewEvents;

    // ViewModels
    private EventViewModel eventViewModel;
    private LoginViewModel loginViewModel;

    // Adapter for event list
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize ViewModels
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI elements
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);

        // Setup RecyclerView
        setupRecyclerView();

        // Set up add event button click listener
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open event entry activity
                Intent intent = new Intent(DashboardActivity.this, EventEntryActivity.class);
                startActivity(intent);
            }
        });

        // Observe event list changes
        eventViewModel.getEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> eventList) {
                if (eventList != null) {
                    eventAdapter.updateEvents(eventList);
                    Toast.makeText(DashboardActivity.this, 
                        "Events updated: " + eventList.size() + " events", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe operation results
        eventViewModel.getOperationSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                // Operations like add/delete/update automatically refresh the list
                // This observer can be used for additional feedback if needed
            }
        });

        // Observe error messages
        eventViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check for SMS permission
        checkSmsPermission();
    }

    /**
     * Setup RecyclerView for displaying events
     */
    private void setupRecyclerView() {
        // Initialize adapter with empty list
        eventAdapter = new EventAdapter(new java.util.ArrayList<Event>());
        
        // Set up RecyclerView with LinearLayoutManager
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);
        
        // Enable item click handling for edit/delete
        eventAdapter.setOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                // Open event for editing
                openEventForEditing(event);
            }
            
            @Override
            public void onItemDeleteClick(Event event) {
                // Confirm and delete event
                confirmDeleteEvent(event);
            }
        });
    }

    /**
     * Open event for editing
     * 
     * @param event The event to edit
     */
    private void openEventForEditing(Event event) {
        Intent intent = new Intent(DashboardActivity.this, EventEntryActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    /**
     * Confirm and delete an event
     * 
     * @param event The event to delete
     */
    private void confirmDeleteEvent(final Event event) {
        // Create confirmation dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        
        Button buttonYes = dialog.findViewById(R.id.buttonYes);
        Button buttonNo = dialog.findViewById(R.id.buttonNo);
        
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Delegate delete operation to ViewModel
                eventViewModel.deleteEvent(event.getId());
            }
        });
        
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        eventViewModel.refresh();
    }

    /**
     * Check for SMS permission
     */
    private void checkSmsPermission() {
        // Check if permission is already granted
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Show permission dialog
            showSmsPermissionDialog();
        }
    }

    /**
     * Show SMS permission dialog
     */
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

    /**
     * Logout the current user
     */
    private void logout() {
        // Delegate logout to ViewModel
        loginViewModel.logout();

        // Return to login screen
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}