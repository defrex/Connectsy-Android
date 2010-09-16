package com.connectsy.events;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.connectsy.R;
import com.connectsy.events.EventManager.Event;

public class EventsAdapter extends ArrayAdapter<Event> {
 
	public EventsAdapter(Context context, int viewResourceId,
			ArrayList<Event> events) {
		super(context, viewResourceId, events);
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final Event event = getItem(position);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.event_list_item, parent, false);
		
        return EventView.renderView(context, view, event, true);
	}
}
