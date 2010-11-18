package com.connectsy.events;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.connectsy.ActionBarHandler;
import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.events.attendants.AttendantManager;
import com.connectsy.events.attendants.AttendantManager.Status;
import com.connectsy.settings.MainMenu;
import com.connectsy.users.UserManager;
import com.connectsy.users.UserManager.User;

public class EventView extends Activity implements DataUpdateListener,
		OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "EventView";

	private Event event;
	private String eventRev;
	private EventManager eventManager;
	private AttendantManager attManager;
	private Integer curUserStatus;

	private int pendingOperations = 0;

	private static final int REFRESH_EVENT = 0;
	private static final int REFRESH_ATTENDANTS = 1;
	private static final int ATT_SET = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_view);

		//set up logo clicks
        new ActionBarHandler(this);
        
//		findViewById(R.id.ab_refresh).setOnClickListener(this);
		findViewById(R.id.event_view_comments).setOnClickListener(this);
		findViewById(R.id.event_view_attendants).setOnClickListener(this);

		Bundle e = getIntent().getExtras();
		eventRev = e.getString("com.connectsy.events.revision");
		event = getEventManager().getEvent(eventRev);
		update();
		refresh();
		
		if (e.containsKey("com.connectsy.events.comments")){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.dir/vnd.connectsy.event.comment");
			i.putExtra("com.connectsy.events.revision", eventRev);
			startActivity(i);
		}else if (e.containsKey("com.connectsy.events.attendants")){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.dir/vnd.connectsy.event.attendant");
			i.putExtra("com.connectsy.events.revision", eventRev);
			startActivity(i);
		}
	}

	private void update() {
		if (event != null) {
			setUserStatus(null, false);
			new EventRenderer(this, findViewById(R.id.event), eventRev, false);
			User curUser = UserManager.currentUser(this);
			if (!event.creator.equals(curUser.username)) {
				findViewById(R.id.event_view_ab_in).setVisibility(View.VISIBLE);
				findViewById(R.id.event_view_ab_in_seperator).setVisibility(
						View.VISIBLE);

				ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
				in.setOnClickListener(this);
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
		}
	}

	private void setUserStatus(Integer status) {
		setUserStatus(status, true);
	}
	private void setUserStatus(Integer status, boolean doRequest) {
		if (status != null)
			curUserStatus = status;
		else if (curUserStatus == null)
			return;

		ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
		if (curUserStatus == Status.ATTENDING) {
			in.setSelected(true);
			in.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_check_selected));
		} else {
			in.setSelected(false);
			in.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_check));
		}
		if (doRequest) {
			getAttManager().setStatus(curUserStatus, ATT_SET);
			pendingOperations++;
			setRefreshing(true);
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.ab_refresh) {
			refresh();
		} else if (v.getId() == R.id.event_view_ab_in) {
			ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
			if (in.isSelected())
				setUserStatus(Status.NOT_ATTENDING);
			else
				setUserStatus(Status.ATTENDING);
		} else if (v.getId() == R.id.event_view_comments){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.dir/vnd.connectsy.event.comment");
			i.putExtra("com.connectsy.events.revision", eventRev);
			startActivity(i);
		} else if (v.getId() == R.id.event_view_attendants){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.dir/vnd.connectsy.event.attendant");
			i.putExtra("com.connectsy.events.revision", eventRev);
			startActivity(i);
		}
	}

	private void refresh() {
		if (event == null) {
			getEventManager().refreshEvent(eventRev, REFRESH_EVENT);
		}else{
			getAttManager().refreshAttendants(REFRESH_ATTENDANTS);
		}
		pendingOperations++;
		setRefreshing(true);
	}

	public void onDataUpdate(int code, String response) {
		if (code == REFRESH_EVENT){
			event = getEventManager().getEvent(eventRev);
			update();
			refresh();
		}else if (code == ATT_SET) {
			refresh();
		} else if (code == REFRESH_ATTENDANTS){
			curUserStatus = getAttManager().getCurrentUserStatus(true);
		}else{
			update();
		}
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, String response, int returnCode) {
		if (httpStatus == 403 && returnCode == ATT_SET) {
			Toast.makeText(this, "You are not invited.", 500).show();
			setUserStatus(Status.NOT_ATTENDING, false);
		}
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	private void setRefreshing(boolean on) {
		if (on) {
//			findViewById(R.id.ab_refresh).setVisibility(View.GONE);
			findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
		} else {
//			findViewById(R.id.ab_refresh).setVisibility(View.VISIBLE);
			findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		return MainMenu.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return MainMenu.onOptionsItemSelected(this, item);
	}

	private AttendantManager getAttManager() {
		return getAttManager(false);
	}
	private AttendantManager getAttManager(boolean forceNew) {
		if ((attManager == null || forceNew) && event != null)
			attManager = new AttendantManager(this, this, event.ID,
					event.attendants);
		return attManager;
	}

	private EventManager getEventManager() {
		return getEventManager(false);
	}
	private EventManager getEventManager(boolean forceNew) {
		if (eventManager == null || forceNew)
			eventManager = new EventManager(this, this, null, null);
		return eventManager;
	}

}
