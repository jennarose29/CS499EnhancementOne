package com.example.robbinseventtracker.repository;

import android.content.Context;
import android.util.Log;

import com.example.robbinseventtracker.DatabaseHelper;
import com.example.robbinseventtracker.Event;

import java.util.List;

/**
 * EventRepository - Handles all event-related data operations
 * 
 * This repository follows the Repository pattern and provides:
 * - Clean separation between data access and business logic
 * - Single source of truth for event data
 * - Centralized error handling and logging
 * - Thread-safe operations for use with ViewModels
 */
public class EventRepository {
    private static final String TAG = "EventRepository";
    private DatabaseHelper databaseHelper;
    
    public EventRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }
    
    /**
     * Get all events for a specific user
     * 
     * @param userId The user ID
     * @return List of events for the user
     */
    public List<Event> getAllEvents(int userId) {
        try {
            List<Event> events = databaseHelper.getAllEventsList(userId);
            Log.d(TAG, "Retrieved " + events.size() + " events for user ID: " + userId);
            return events;
        } catch (Exception e) {
            Log.e(TAG, "Error getting events: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get a specific event by ID
     * 
     * @param eventId The event ID
     * @return The event object, or null if not found
     */
    public Event getEventById(int eventId) {
        try {
            android.database.Cursor cursor = databaseHelper.getEvent(eventId);
            
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                int notification = cursor.getInt(cursor.getColumnIndexOrThrow("notification"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                
                cursor.close();
                
                Event event = new Event(id, name, description, date, time, location, notification, userId);
                Log.d(TAG, "Retrieved event: " + name);
                return event;
            }
            
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting event by ID: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Add a new event
     * 
     * @param event The event to add
     * @return true if successful, false otherwise
     */
    public boolean addEvent(Event event) {
        if (event == null) {
            Log.w(TAG, "Cannot add null event");
            return false;
        }
        
        try {
            long result = databaseHelper.addEvent(
                event.getName(),
                event.getDescription(),
                event.getDate(),
                event.getTime(),
                event.getLocation(),
                event.getNotification(),
                event.getUserId()
            );
            
            boolean success = result > 0;
            if (success) {
                Log.d(TAG, "Event added successfully: " + event.getName());
            } else {
                Log.w(TAG, "Failed to add event: " + event.getName());
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error adding event: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Update an existing event
     * 
     * @param event The event to update
     * @return true if successful, false otherwise
     */
    public boolean updateEvent(Event event) {
        if (event == null || event.getId() <= 0) {
            Log.w(TAG, "Cannot update invalid event");
            return false;
        }
        
        try {
            boolean success = databaseHelper.updateEvent(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getDate(),
                event.getTime(),
                event.getLocation(),
                event.getNotification()
            );
            
            if (success) {
                Log.d(TAG, "Event updated successfully: " + event.getName());
            } else {
                Log.w(TAG, "Failed to update event: " + event.getName());
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
     * @param eventId The event ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteEvent(int eventId) {
        try {
            boolean success = databaseHelper.deleteEvent(eventId);
            
            if (success) {
                Log.d(TAG, "Event deleted successfully: ID " + eventId);
            } else {
                Log.w(TAG, "Failed to delete event: ID " + eventId);
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting event: " + e.getMessage(), e);
            return false;
        }
    }
}