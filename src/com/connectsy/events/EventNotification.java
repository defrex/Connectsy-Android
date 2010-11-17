package com.connectsy.events;

import org.json.JSONException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.notifications.NotificationHandlerBase;

public class EventNotification extends NotificationHandlerBase implements DataUpdateListener {

	@SuppressWarnings("unused")
	private static final String TAG = "EventNotification";
	private static final int GET_EVENT = 0;
	protected String tickerText = "New Connectsy Event";
	
	@Override
	protected void prepareNotification() throws JSONException {
		String title;
		String body;
		Intent i;
		if (notifications.size() == 1) {
			String rev = notifications.get(0).getString("event_revision");
			EventManager eventManager = new EventManager(context, this, null,
					null);
			Event event = eventManager.getEvent(rev);
			if (event == null) {
				eventManager.refreshEvent(rev, GET_EVENT);
				return;
			}
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (!prefs.getBoolean("preference_notifications_public", true) &&
					event.broadcast)
				return;
			if (!prefs.getBoolean("preference_notifications_private", true) &&
					!event.broadcast)
				return;
			title = event.creator + " shared a plan";
			body = event.what;
			i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy.events.revision", event.revision);
		} else {
			title = notifications.size()+" plans shared";
			body = "";
			i = new Intent(context, EventList.class);
			i.putExtra("filter", EventManager.Filter.INVITED);
		}
		sendNotification(title, body, i, "invite");
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
