package com.connectsy2.events.attendants;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy2.R;
import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.events.EventManager;
import com.connectsy2.events.EventManager.Event;
import com.connectsy2.events.attendants.AttendantManager.Attendant;
import com.connectsy2.events.attendants.AttendantManager.Status;
import com.connectsy2.users.UserManager;
import com.connectsy2.users.UserManager.User;

public class AttendantList extends Activity implements OnClickListener, 
		DataUpdateListener {
	private String TAG = "AttendantList";
    private String eventRev;
    private Event event;
	private AttendantsAdapter adapter;
	private AttendantManager manager;
	private int pendingOperations = 0;
	private static final int REFRESH_ATTS = 1;
	private static final int ATT_SET = 2;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendant_list);
        
        findViewById(R.id.ab_refresh).setOnClickListener(this);
        
        Intent i = getIntent();
		eventRev = i.getExtras().getString("com.connectsy2.events.revision");
		// Event should always be cached when this Activity is loaded.
		event = new EventManager(this, null, null, null).getEvent(eventRev);

		if (!event.creator.equals(UserManager.currentUsername(this))) {
			findViewById(R.id.event_view_ab_in).setVisibility(View.VISIBLE);
			findViewById(R.id.event_view_ab_in_seperator).setVisibility(
					View.VISIBLE);
			findViewById(R.id.event_view_ab_in).setOnClickListener(this);
			updateUserStatus();
		}
		
        updateData();
        
        ListView lv = (ListView)findViewById(R.id.attendants_list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> view, View itemView, 
					int position, long id) {
				Attendant att = (Attendant) view.getAdapter().getItem(position);
				if (att.username != null){
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setType("vnd.android.cursor.item/vnd.connectsy.user");
					i.putExtra("com.connectsy2.user.username", att.username);
					startActivity(i);
				}
			}
        });
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
		else if (v.getId() == R.id.event_view_ab_in) {
			ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
			if (in.isSelected())
				setUserStatus(Status.NOT_ATTENDING);
			else
				setUserStatus(Status.ATTENDING);
		}
	}

	private void updateUserStatus(){
		User curUser = UserManager.currentUser(this);
		ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
		if (getAttManager().isUserAttending(curUser.id)) {
			in.setSelected(true);
			in.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_check_selected));
		} else {
			in.setSelected(false);
			in.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_check));
		}
	}
	
	private void setUserStatus(Integer status) {
		getAttManager().setStatus(status, ATT_SET);
		pendingOperations++;
		setRefreshing(true);
	}

	private void refresh(){
		getAttManager().refreshAttendants(REFRESH_ATTS);
		setRefreshing(true);
		pendingOperations++;
	}

	public void onDataUpdate(int code, String response) {
		if (code == REFRESH_ATTS){
			updateData();
			if (!event.creator.equals(UserManager.currentUsername(this)))
				updateUserStatus();
		}else if (code == ATT_SET){
			refresh();
		}
		
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
