package com.example.robbinseventtracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventEntryActivity extends Activity {
    // UI elements
    private EditText editTextEventName, editTextEventDescription, editTextDate, editTextTime, editTextLocation;
    private Switch switchNotification;
    private RadioGroup radioGroupNotificationTime;
    private Button buttonSaveEvent;

    // Database helper
    private DatabaseHelper databaseHelper;

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

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get current user
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("username", "");
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
    }

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

    private void updateDateField() {
        String format = "MMM d, yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        editTextDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void updateTimeField() {
        String format = "h:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.US);
        editTextTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void loadEventData(int eventId) {
        Cursor cursor = databaseHelper.getEvent(eventId);

        if (cursor != null && cursor.moveToFirst()) {
            // Get data from cursor
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            int notification = cursor.getInt(cursor.getColumnIndexOrThrow("notification"));

            // Set data to views
            editTextEventName.setText(name);
            editTextEventDescription.setText(description);
            editTextDate.setText(date);
            editTextTime.setText(time);
            editTextLocation.setText(location);
            switchNotification.setChecked(notification > 0);

            // Set notification radio button
            if (notification == 15) {
                radioGroupNotificationTime.check(R.id.radioButton15min);
            } else if (notification == 30) {
                radioGroupNotificationTime.check(R.id.radioButton30min);
            } else if (notification == 60) {
                radioGroupNotificationTime.check(R.id.radioButton1hour);
            } else if (notification == 1440) {
                radioGroupNotificationTime.check(R.id.radioButton1day);
            }

            cursor.close();
        }
    }

    private void saveEvent() {
        // Get data from views
        String name = editTextEventName.getText().toString().trim();
        String description = editTextEventDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        int notification = 0;

        // Validate input
        if (name.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please enter event name, date, and time", Toast.LENGTH_SHORT).show();
            return;
        }

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

        boolean success;
        if (eventId == -1) {
            // Add new event
            long result = databaseHelper.addEvent(name, description, date, time, location, notification, currentUserId);
            success = result > 0;
        } else {
            // Update existing event
            success = databaseHelper.updateEvent(eventId, name, description, date, time, location, notification);
        }

        if (success) {
            // Schedule notification if enabled
            if (switchNotification.isChecked()) {
                sendSmsNotification(name, date, time);
            }

            Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show();
        }
    }

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

                // Note: In a real implementation, you would:
                // 1. Get the user's phone number
                // 2. Create the SMS message
                // 3. Use SmsManager to send the message
                // SmsManager smsManager = SmsManager.getDefault();
                // smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS notification", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
