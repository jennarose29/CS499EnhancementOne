package com.example.robbinseventtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * EventAdapter - RecyclerView adapter for displaying event list
 * 
 * This adapter now works with MVVM architecture:
 * - Uses List<Event> instead of Cursor for better performance
 * - Implements RecyclerView.Adapter for efficient scrolling
 * - Supports click listeners for edit and delete operations
 * - Follows ViewHolder pattern for better performance
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    
    private List<Event> eventList;
    private OnItemClickListener onItemClickListener;

    /**
     * Interface for handling item clicks
     */
    public interface OnItemClickListener {
        void onItemClick(Event event);
        void onItemDeleteClick(Event event);
    }

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList != null ? eventList : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Update the event list and notify observers
     * 
     * @param newEventList The new list of events
     */
    public void updateEvents(List<Event> newEventList) {
        this.eventList = newEventList != null ? newEventList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for event items
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewEventName;
        private TextView textViewEventDate;
        private TextView textViewEventTime;
        private Button buttonDeleteEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views
            textViewEventName = itemView.findViewById(R.id.textViewEventName);
            textViewEventDate = itemView.findViewById(R.id.textViewEventDate);
            textViewEventTime = itemView.findViewById(R.id.textViewEventTime);
            buttonDeleteEvent = itemView.findViewById(R.id.buttonDeleteEvent);
        }

        /**
         * Bind event data to the views
         * 
         * @param event The event to bind
         */
        public void bind(final Event event) {
            // Set event data
            textViewEventName.setText(event.getName());
            textViewEventDate.setText(event.getDate());
            textViewEventTime.setText(event.getTime());

            // Set click listener for the entire item (edit event)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(event);
                    }
                }
            });

            // Set click listener for delete button
            buttonDeleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemDeleteClick(event);
                    }
                }
            });
        }
    }
}