package com.example.robbinseventtracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EventAdapter extends BaseAdapter {
    private Context context;
    private Cursor cursor;
    private DatabaseHelper databaseHelper;

    public EventAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }

        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
        String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
        int notification = cursor.getInt(cursor.getColumnIndexOrThrow("notification"));
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));

        return new Event(id, name, description, date, time, location, notification, userId);
    }

    @Override
    public long getItemId(int position) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return 0;
        }
        return cursor.getInt(cursor.getColumnIndexOrThrow("id"));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        }

        // Move cursor to position
        if (!cursor.moveToPosition(position)) {
            return convertView;
        }

        // Get data from cursor
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));

        // Find views
        TextView textViewEventName = convertView.findViewById(R.id.textViewEventName);
        TextView textViewEventDate = convertView.findViewById(R.id.textViewEventDate);
        TextView textViewEventTime = convertView.findViewById(R.id.textViewEventTime);
        Button buttonDeleteEvent = convertView.findViewById(R.id.buttonDeleteEvent);

        // Set data to views
        textViewEventName.setText(name);
        textViewEventDate.setText(date);
        textViewEventTime.setText(time);

        // Set click listener for the item to edit event
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open event entry activity for editing
                Intent intent = new Intent(context, EventEntryActivity.class);
                intent.putExtra("event_id", id);
                context.startActivity(intent);
            }
        });

        // Set click listener for delete button
        buttonDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete event from database
                if (databaseHelper.deleteEvent(id)) {
                    // Refresh data
                    ((DashboardActivity) context).loadEvents();
                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return convertView;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }
}
