package com.connectsy.events;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.notifications.NotificationHandler.NotificationContent;
import com.connectsy.notifications.NotificationHandler.NotificationContentListener;
import com.connectsy.notifications.NotificationHandler.NotificationContentProvider;

public class EventNotification implements DataUpdateListener, 
		NotificationContentProvider {

	@SuppressWarnings("unused")
	private static final String TAG = "EventNotification";
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
		if (nots.size() == 1) {
			String rev = nots.get(0).getString("event_revision");
			EventManager eventManager = new EventManager(context, this, null,
					null);
			Event event = eventManager.getEvent(rev);
			if (event == null) {
				eventManager.refreshEvent(rev, GET_EVENT);
				return;
			}
			
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy.events.revision", rev);
			
			not.intent = i;
			not.title = event.creator + " shared a plan";
			not.body = event.what;
			not.ticker = "New Connectsy Event";
			not.username = event.creator;
		} else {
			not.title = nots.size()+" plans shared";
			not.body = "";
			not.ticker = "New Connectsy Events";
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
