package com.connectsy2.events;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.connectsy2.R;

public class EventsAdapter extends ArrayAdapter<String> {
 
	public EventsAdapter(Context context, int viewResourceId,
			ArrayList<String> events) {
		super(context, viewResourceId, events);
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final String rev = getItem(position);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.event_list_item, parent, false);
		
		new EventRenderer(context, view, rev, true);
		
		return view;
	}
}
