package com.connectsy.events;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.connectsy.ActionBarHandler;
import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.events.EventManager.Filter;
import com.connectsy.settings.MainMenu;

public class EventList extends Activity implements DataUpdateListener, 
		OnClickListener, OnItemClickListener {
	private static final String TAG = "EventList";
	private EventsAdapter adapter;
    private EventManager eventManager = null;
    private ArrayList<Event> events;
    private Filter filter;
    private String category;
    private static int GET_EVENTS = 0;
	
    public static enum Init {CATEGORY, FRIENDS}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);
        
        ActionBarHandler abHandler = new ActionBarHandler(this);
        ImageView abProfile = (ImageView)findViewById(R.id.ab_profile);
        abProfile.setOnClickListener(abHandler);
        ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
        abNewEvent.setOnClickListener(abHandler);

        ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
        abRefresh.setOnClickListener(this);
        
        updateData();
        
        TextView heading = (TextView)findViewById(R.id.event_list_heading_text);
        if (filter == Filter.ALL)
        	heading.setText("Nearby Events");
        if (filter == Filter.FRIENDS)
        	heading.setText("Friends Events");
        if (filter == Filter.CATEGORY)
        	heading.setText(category+" Events");
        
        ListView lv = (ListView)findViewById(R.id.events_list);
        lv.setOnItemClickListener(this);
        lv.setAdapter(adapter);
        refresh();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }

	public void onDataUpdate(int code) {
		updateData();
		setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, int code) {
		setRefreshing(false);
	}

	public void onClick(View v) {
    	if (v.getId() == R.id.ab_refresh) refresh();
	}
	
	private void updateData(){
        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null && b.containsKey("filter")){
    		filter = (Filter) b.get("filter");
	        if (b.containsKey("category"))
	        	category = b.getString("category");
        }else{
        	filter = Filter.ALL;
        }
        eventManager = new EventManager(this, this, filter, category);
        events = eventManager.getEvents();
        if (adapter != null){
        	adapter.clear();
        	for (int n = 0;n < events.size();n++)
        		adapter.add(events.get(n));
    		adapter.notifyDataSetChanged();
        }else{
            adapter = new EventsAdapter(this, R.layout.event_list_item, events);
        }
	}
	
	private void refresh(){
		eventManager.refreshEvents(GET_EVENTS);
		setRefreshing(true);
	}
	
    private void setRefreshing(boolean on) {
    	if (on){
	        findViewById(R.id.ab_refresh).setVisibility(View.GONE);
	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
    	}else{
	        findViewById(R.id.ab_refresh).setVisibility(View.VISIBLE);
	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
    	}
    }

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Event event = adapter.getItem(position);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setType("vnd.android.cursor.item/vnd.connectsy.event");
		i.putExtra("com.connectsy.events.revision", event.revision);
		startActivity(i);
	}
}