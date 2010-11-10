package com.connectsy.events;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy.ActionBarHandler;
import com.connectsy.R;
import com.connectsy.categories.CategoryManager.Category;
import com.connectsy.data.DataManager;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Filter;
import com.connectsy.settings.MainMenu;
import com.connectsy.utils.Utils;

public class EventList extends Activity implements DataUpdateListener, 
		OnClickListener, OnItemClickListener, LocationListener {
	@SuppressWarnings("unused")
	private static final String TAG = "EventList";
	private EventsAdapter adapter;
    private EventManager eventManager = null;
    private Filter filter;
    private String category;
    private final int GET_EVENTS = 0;
    private final int SELECT_CATEGORY = 1;
	
    public static enum Init {CATEGORY, FRIENDS}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);
        
        findViewById(R.id.ab_new_event).setOnClickListener(
        		new ActionBarHandler(this));
        findViewById(R.id.ab_refresh).setOnClickListener(this);
//        findViewById(R.id.event_list_tab_invited).setOnClickListener(this);
//        findViewById(R.id.event_list_tab_created).setOnClickListener(this);
//        findViewById(R.id.event_list_tab_public).setOnClickListener(this);
        
        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("filter")){
    		filter = (Filter) b.get("filter");
	        if (b.containsKey("category"))
	        	category = b.getString("category");
        }else{
        	filter = Filter.INVITED;
        }
        
        updateData();
        
        ListView lv = (ListView)findViewById(R.id.events_list);
        lv.setOnItemClickListener(this);
        lv.setAdapter(adapter);
		Utils.setFooterView(this, lv);
        refresh();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }

	public void onDataUpdate(int code, String response) {
		updateData();
		setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		setRefreshing(false);
	}

	public void onClick(View v) {
    	if (v.getId() == R.id.ab_refresh) refresh();
//		Filter newFilter = null;
//    	}else if (v.getId() == R.id.event_list_tab_invited){
//    		newFilter = Filter.INVITED;
//    	}else if (v.getId() == R.id.event_list_tab_created){
//    		newFilter = Filter.CREATED;
//    	}else if (v.getId() == R.id.event_list_tab_public){
//    		newFilter = Filter.PUBLIC;
//    	}else if (v.getId() == R.id.event_list_heading){
//    		Intent i = new Intent(Intent.ACTION_CHOOSER);
//    		i.setType("vnd.android.cursor.item/vnd.connectsy.category");
//    		startActivityForResult(i, SELECT_CATEGORY);
//    	}
//    	if (newFilter != null && filter != newFilter){
//    		filter = newFilter;
//    		updateData();
//    		refresh();
//    	}
    	
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == RESULT_OK && requestCode == SELECT_CATEGORY){
			try {
				category = new Category(data.getExtras().getString("com.connectsy.category")).name;
				DataManager.getCache(this).edit()
						.putString("category_saved", category).commit();
				//((TextView)findViewById(R.id.event_list_heading_text)).setText("Category: "+category);
				updateData();
				refresh();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateData(){
        
        //TextView heading = (TextView)findViewById(R.id.event_list_heading_text);
//        if (filter == Filter.INVITED){
//			findViewById(R.id.event_list_tab_invited).setSelected(true);
//			findViewById(R.id.event_list_tab_created).setSelected(false);
//			findViewById(R.id.event_list_tab_public).setSelected(false);
//        	findViewById(R.id.event_list_heading_wrapper)
//					.setVisibility(View.GONE);
//        	//heading.setText("All Events");
//        }if (filter == Filter.CREATED){
//			findViewById(R.id.event_list_tab_invited).setSelected(false);
//			findViewById(R.id.event_list_tab_created).setSelected(true);
//			findViewById(R.id.event_list_tab_public).setSelected(false);
//        	findViewById(R.id.event_list_heading_wrapper)
//					.setVisibility(View.GONE);
//        	//heading.setText("My Events");
//        }if (filter == Filter.PUBLIC){
//			findViewById(R.id.event_list_tab_invited).setSelected(false);
//			findViewById(R.id.event_list_tab_created).setSelected(false);
//			findViewById(R.id.event_list_tab_public).setSelected(true);
//        	findViewById(R.id.event_list_heading_wrapper)
//					.setVisibility(View.VISIBLE);
//			LinearLayout cat = (LinearLayout)findViewById(R.id.event_list_heading);
//			cat.setClickable(true);
//			cat.setOnClickListener(this);
//			String heading = category;
//			if (heading == null)
//				heading = "Select a Category";
//			((TextView)findViewById(R.id.event_list_heading_text))
//					.setText(heading);
//        }
        
        eventManager = new EventManager(this, this, filter, category);
        ArrayList<String> revs = eventManager.getRevisions();
        if (adapter != null){
        	adapter.clear();
        	for (int n = 0;n < revs.size();n++)
        		adapter.add(revs.get(n));
    		adapter.notifyDataSetChanged();
        }else{
            adapter = new EventsAdapter(this, R.layout.event_list_item, revs);
        }
	}
	
	private void refresh(){
		eventManager.refreshRevisions(GET_EVENTS);
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
		String rev = adapter.getItem(position);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setType("vnd.android.cursor.item/vnd.connectsy.event");
		i.putExtra("com.connectsy.events.revision", rev);
		startActivity(i);
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}