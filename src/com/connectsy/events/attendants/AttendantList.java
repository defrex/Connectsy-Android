package com.connectsy.events.attendants;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager;
import com.connectsy.events.EventManager.Event;
import com.connectsy.events.attendants.AttendantManager.Attendant;

public class AttendantList extends Activity implements OnClickListener, 
		DataUpdateListener {
    private String eventRev;
    private Event event;
	private AttendantsAdapter adapter;
	private AttendantManager manager;
	private int pendingOperations = 0;
	private static final int REFRESH_ATTS = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendant_list);
        
        findViewById(R.id.ab_refresh).setOnClickListener(this);
        
        Intent i = getIntent();
		eventRev = i.getExtras().getString("com.connectsy.events.revision");
		// Event should always be cached when this Activity is loaded.
		event = new EventManager(this, null, null, null).getEvent(eventRev);
        
        updateData();
        
        ListView lv = (ListView)findViewById(R.id.attendants_list);
        lv.setAdapter(adapter);
        refresh();
    }

	private void updateData() {
		ArrayList<Attendant> atts = getAttManager().getAttendants();
		if (adapter == null) {
			adapter = new AttendantsAdapter(this,
					R.layout.attendant_list_item, atts);
		} else {
			adapter.clear();
			for (Attendant a : atts)
				adapter.add(a);
			adapter.notifyDataSetChanged();
		}
		((ListView) findViewById(R.id.attendants_list)).setAdapter(adapter);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.ab_refresh) refresh();
	}
	
	private void refresh(){
		getAttManager().refreshAttendants(REFRESH_ATTS );
		setRefreshing(true);
		pendingOperations++;
	}

	public void onDataUpdate(int code, String response) {
		updateData();
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	private void setRefreshing(boolean on) {
		if (on) {
			findViewById(R.id.ab_refresh).setVisibility(View.GONE);
			findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.ab_refresh).setVisibility(View.VISIBLE);
			findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
		}
	}
	
	private AttendantManager getAttManager() {
		return getAttManager(false);
	}
	private AttendantManager getAttManager(boolean forceNew) {
		if ((manager == null || forceNew) && event != null)
			manager = new AttendantManager(this, this, event.ID,
					event.attendants);
		return manager;
	}
}
