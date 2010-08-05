package com.connectsy.events;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.events.EventManager.Event;
import com.connectsy.settings.Settings;
import com.wilson.android.library.DrawableManager;

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
		
        TextView desc = (TextView)view.findViewById(R.id.event_list_item_desc);
        desc.setText(event.description);
        
        TextView username = (TextView)view.findViewById(R.id.event_list_item_username);
        username.setText(event.creator);
        username.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", event.creator);
	    		context.startActivity(i);
			}
        });
        
        TextView where = (TextView)view.findViewById(R.id.event_list_item_where);
        where.setText(event.where);
        
        ImageView avatar = (ImageView)view.findViewById(R.id.event_list_item_avatar);
        DrawableManager dm = new DrawableManager();
        String avyUrl = Settings.API_DOMAIN+"/users/"+event.creator+"/avatar/";
        dm.fetchDrawableOnThread(avyUrl, avatar);
        
        return view;
	}
}
