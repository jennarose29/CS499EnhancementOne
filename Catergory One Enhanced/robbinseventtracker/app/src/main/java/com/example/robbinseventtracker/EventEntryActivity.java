package com.example.robbinseventtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.robbinseventtracker.viewmodel.EventViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * EventEntryActivity - Handles event creation and editing using MVVM architecture
 * 
 * This activity now follows MVVM pattern:
 * - View: Handles UI interactions and displays form
 * - ViewModel: Manages event data and validation logic
 * - Model: Database operations (handled by Repository)
 * 
 * Benefits of MVVM implementation:
 * - Separates UI from business logic
 * - Background threading prevents UI freezing
 * - LiveData provides automatic operation result feedback
 * - Survives configuration changes
 */
public class EventEntryActivity extends AppCompatActivity {
    // UI elements
    private EditText editTextEventName, editTextEventDescription, editTextDate, editTextTime, editTextLocation;
    private Switch switchNotification;
    private RadioGroup radioGroupNotificationTime;
    private Button buttonSaveEvent;

    // ViewModel
    private EventViewModel eventViewModel;

    // Calendar for date and time pickers
    private Calendar calendar;

    // Event ID for editing (-1 for new event)
    private int eventId = -1;

    // Current user ID
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_entry);

        // Initialize ViewModel
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Get current user
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("username", "");
        com.example.robbinseventtracker.DatabaseHelper databaseHelper = 
            new com.example.robbinseventtracker.DatabaseHelper(this);
        currentUserId = databaseHelper.getUserId(currentUsername);

        // Initialize UI elements
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextLocation = findViewById(R.id.editTextLocation);
        switchNotification = findViewById(R.id.switchNotification);
        radioGroupNotificationTime = findViewById(R.id.radioGroupNotificationTime);
        buttonSaveEvent = findViewById(R.id.buttonSaveEvent);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Check if editing an existing event
        if (getIntent().hasExtra("event_id")) {
            eventId = getIntent().getIntExtra("event_id", -1);
            loadEventData(eventId);
        }

        // Set up date picker
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Set up time picker
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Set up save button click listener
        buttonSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });

        // Observe operation results
        eventViewModel.getOperationSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    Toast.makeText(EventEntryActivity.this, 
                        "Event saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        // Observe error messages
        eventViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(EventEntryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Show time picker dialog
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTimeField();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    /**
     * Update date field with selected date
     */
    private void updateDateField() {
        String format = "MMM d, yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        editTextDate.setText(dateFormat.format(calendar.getTime()));
    }

    /**
     * Update time field with selected time
     */
    private void updateTimeField() {
        String format = "h:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.US);
        editTextTime.setText(timeFormat.format(calendar.getTime()));
    }

    /**
     * Load event data for editing
     * 
     * @param eventId The event ID to load
     */
    private void loadEventData(int eventId) {
        Event event = eventViewModel.getEventById(eventId);
        
        if (event != null) {
            // Set data to views
            editTextEventName.setText(event.getName());
            editTextEventDescription.setText(event.getDescription());
            editTextDate.setText(event.getDate());
            editTextTime.setText(event.getTime());
            editTextLocation.setText(event.getLocation());
            switchNotification.setChecked(event.getNotification() > 0);

            // Set notification radio button
            int notification = event.getNotification();
            if (notification == 15) {
                radioGroupNotificationTime.check(R.id.radioButton15min);
            } else if (notification == 30) {
                radioGroupNotificationTime.check(R.id.radioButton30min);
            } else if (notification == 60) {
                radioGroupNotificationTime.check(R.id.radioButton1hour);
            } else if (notification == 1440) {
                radioGroupNotificationTime.check(R.id.radioButton1day);
            }
        }
    }

    /**
     * Save event - delegates to ViewModel
     */
    private void saveEvent() {
        // Get data from views
        String name = editTextEventName.getText().toString().trim();
        String description = editTextEventDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        int notification = 0;

        // Get notification value
        if (switchNotification.isChecked()) {
            int selectedRadioButtonId = radioGroupNotificationTime.getCheckedRadioButtonId();
            if (selectedRadioButtonId == R.id.radioButton15min) {
                notification = 15;
            } else if (selectedRadioButtonId == R.id.radioButton30min) {
                notification = 30;
            } else if (selectedRadioButtonId == R.id.radioButton1hour) {
                notification = 60;
            } else if (selectedRadioButtonId == R.id.radioButton1day) {
                notification = 1440;
            }
        }

        // Create event object
        Event event = new Event(eventId, name, description, date, time, location, notification, currentUserId);

        // Delegate save operation to ViewModel
        if (eventId == -1) {
            // Add new event
            eventViewModel.addEvent(event);
        } else {
            // Update existing event
            eventViewModel.updateEvent(event);
        }

        // Schedule notification if enabled
        if (switchNotification.isChecked()) {
            sendSmsNotification(name, date, time);
        }
    }

    /**
     * Send SMS notification (placeholder implementation)
     */
    private void sendSmsNotification(String eventName, String date, String time) {
        // Check if SMS permission is granted
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            try {
                // In a real app, you would get the phone number from user settings
                // For this project, we'll just show a toast indicating that a notification would be sent
                Toast.makeText(this,
                        "SMS notification would be sent for: " + eventName + " on " + date + " at " + time,
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS notification", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}