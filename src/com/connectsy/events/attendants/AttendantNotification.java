package com.connectsy.events.attendants;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager;
import com.connectsy.events.EventManager.Event;
import com.connectsy.notifications.NotificationHandler.NotificationContent;
import com.connectsy.notifications.NotificationHandler.NotificationContentListener;
import com.connectsy.notifications.NotificationHandler.NotificationContentProvider;

public class AttendantNotification implements DataUpdateListener, 
		NotificationContentProvider {

	@SuppressWarnings("unused")
	private static final String TAG = "AttendantNotification";
	private static final int GET_EVENT = 0;
	private Context context;
	private ArrayList<JSONObject> nots;
	private NotificationContentListener callback;

	public void prepNotification(Context context, 
			ArrayList<JSONObject> nots, NotificationContentListener callback) 
			throws JSONException {
		this.context = context;
		this.nots = nots;
		this.callback = callback;
		prepareNotification();
	}
	
	protected void prepareNotification() throws JSONException {
		NotificationContent not = new NotificationContent();
		Event event = null;
		
		boolean oneEvent = true;
		String rev = nots.get(0).getString("event_revision");
		for (int i=1;i<nots.size();i++)
			if (!rev.equals(nots.get(i).getString("event_revision")))
				oneEvent = false;
		if (oneEvent){
			EventManager eventManager = new EventManager(context, this, null,
					null);
			event = eventManager.getEvent(rev);
			if (event == null) {
				eventManager.refreshEvent(rev, GET_EVENT);
				return;
			}

			if (nots.size() == 1) {
				String att = nots.get(0).getString("attendant");
				
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.event");
				i.putExtra("com.connectsy.events.revision", rev);
				i.putExtra("com.connectsy.events.attendants", true);
				
				not.intent = i;
				not.username = att;
				not.title = att+" is in";
				not.body = "plan: "+event.what;
			} else {
				not.title = nots.size()+" users are in";
				not.body = "";
				not.ticker = "New user in";
			}
		}else{
			not.title = nots.size()+" users are in";
			not.body = "";
			not.ticker = "New users in";
		}
		callback.sendNotification(not);
	}
	
	public void onDataUpdate(int code, String response) {
		if (code == GET_EVENT) {
			try {
				prepareNotification();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public void onRemoteError(int httpStatus, String response, int code) {}
}
