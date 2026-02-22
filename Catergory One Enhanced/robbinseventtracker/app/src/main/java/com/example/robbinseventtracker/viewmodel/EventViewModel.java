package com.example.robbinseventtracker.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.robbinseventtracker.Event;
import com.example.robbinseventtracker.repository.EventRepository;
import com.example.robbinseventtracker.repository.UserRepository;

import java.util.List;

/**
 * EventViewModel - Handles event management logic and data operations
 * 
 * This ViewModel:
 * - Separates business logic from UI
 * - Provides LiveData for reactive UI updates
 * - Performs database operations on background threads
 * - Survives configuration changes (screen rotations)
 * - Manages event list lifecycle
 */
public class EventViewModel extends AndroidViewModel {
    private static final String TAG = "EventViewModel";
    
    private EventRepository eventRepository;
    private UserRepository userRepository;
    
    // LiveData for event list
    private MutableLiveData<List<Event>> events = new MutableLiveData<>();
    
    // LiveData for operation results
    private MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Current user ID
    private int currentUserId;
    
    public EventViewModel(@NonNull Application application) {
        super(application);
        this.eventRepository = new EventRepository(application.getApplicationContext());
        this.userRepository = new UserRepository(application.getApplicationContext());
        
        // Get current user
        String username = getCurrentUsername();
        if (!username.isEmpty()) {
            currentUserId = userRepository.getUserId(username);
            loadEvents();
        }
    }
    
    // Getters for LiveData
    public MutableLiveData<List<Event>> getEvents() {
        return events;
    }
    
    public MutableLiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Load all events for the current user
     * Performs database operation on background thread
     */
    public void loadEvents() {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Event> eventList = eventRepository.getAllEvents(currentUserId);
                if (eventList != null) {
                    events.postValue(eventList);
                    Log.d(TAG, "Loaded " + eventList.size() + " events");
                } else {
                    events.postValue(new java.util.ArrayList<>());
                    Log.w(TAG, "Failed to load events: returned null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading events: " + e.getMessage(), e);
                errorMessage.postValue("Failed to load events");
            } finally {
                executor.shutdown();
            }
        });
    }
    
    /**
     * Add a new event
     * Performs database operation on background thread
     * 
     * @param event The event to add
     */
    public void addEvent(Event event) {
        // Clear previous error messages
        errorMessage.postValue(null);
        
        if (event == null) {
            errorMessage.postValue("Invalid event data");
            return;
        }
        
        // Validate required fields
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            errorMessage.postValue("Event name is required");
            return;
        }
        
        if (event.getDate() == null || event.getDate().trim().isEmpty()) {
            errorMessage.postValue("Event date is required");
            return;
        }
        
        if (event.getTime() == null || event.getTime().trim().isEmpty()) {
            errorMessage.postValue("Event time is required");
            return;
        }
        
        // Set the current user ID
        event.setUserId(currentUserId);
        
        // Perform database operation on background thread
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                boolean success = eventRepository.addEvent(event);
                
                if (success) {
                    operationSuccess.postValue(true);
                    loadEvents(); // Refresh the event list
                } else {
                    errorMessage.postValue("Failed to add event");
                    operationSuccess.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding event: " + e.getMessage(), e);
                errorMessage.postValue("An error occurred while adding the event");
                operationSuccess.postValue(false);
            } finally {
                executor.shutdown();
            }
        });
    }
    
    /**
     * Update an existing event
     * Performs database operation on background thread
     * 
     * @param event The event to update
     */
    public void updateEvent(Event event) {
        // Clear previous error messages
        errorMessage.postValue(null);
        
        if (event == null || event.getId() <= 0) {
            errorMessage.postValue("Invalid event data");
            return;
        }
        
        // Validate required fields
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            errorMessage.postValue("Event name is required");
            return;
        }
        
        if (event.getDate() == null || event.getDate().trim().isEmpty()) {
            errorMessage.postValue("Event date is required");
            return;
        }
        
        if (event.getTime() == null || event.getTime().trim().isEmpty()) {
            errorMessage.postValue("Event time is required");
            return;
        }
        
        // Perform database operation on background thread
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                boolean success = eventRepository.updateEvent(event);
                
                if (success) {
                    operationSuccess.postValue(true);
                    loadEvents(); // Refresh the event list
                } else {
                    errorMessage.postValue("Failed to update event");
                    operationSuccess.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating event: " + e.getMessage(), e);
                errorMessage.postValue("An error occurred while updating the event");
                operationSuccess.postValue(false);
            } finally {
                executor.shutdown();
            }
        });
    }
    
    /**
     * Delete an event
     * Performs database operation on background thread
     * 
     * @param eventId The event ID to delete
     */
    public void deleteEvent(int eventId) {
        if (eventId <= 0) {
            errorMessage.postValue("Invalid event ID");
            return;
        }
        
        // Perform database operation on background thread
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                boolean success = eventRepository.deleteEvent(eventId);
                
                if (success) {
                    operationSuccess.postValue(true);
                    loadEvents(); // Refresh the event list
                } else {
                    errorMessage.postValue("Failed to delete event");
                    operationSuccess.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting event: " + e.getMessage(), e);
                errorMessage.postValue("An error occurred while deleting the event");
                operationSuccess.postValue(false);
            } finally {
                executor.shutdown();
            }
        });
    }
    
    /**
     * Get a specific event by ID
     * 
     * @param eventId The event ID
     * @return The event, or null if not found
     */
    public Event getEventById(int eventId) {
        return eventRepository.getEventById(eventId);
    }
    
    /**
     * Get the current username
     * 
     * @return The current username
     */
    private String getCurrentUsername() {
        android.content.SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
            "LoginPrefs", android.content.Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }
    
    /**
     * Refresh the event list
     */
    public void refresh() {
        loadEvents();
    }
}