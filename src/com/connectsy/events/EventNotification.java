package com.connectsy.events;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.notifications.NotificationHandler;
import com.connectsy.notifications.NotificationRedirect;

public class EventNotification implements NotificationHandler, DataUpdateListener {

	private static final String TAG = "EventNotification";
	private static final int GET_EVENT = 0;
	private static final int NOTIFICATION_ID = 0;
	ArrayList<JSONObject> notifications;
	boolean pending = false;
	Notification notification;
	Context context;
	
	public EventNotification(){
		notifications = new ArrayList<JSONObject>();
	}
	
	public void add(JSONObject notification){
		notifications.add(notification);
		pending = true;
	}
	
	public void send(Context context) throws JSONException{
		this.context = context;
		if (pending) sendNotification();
	}

	private void sendNotification() throws JSONException {
		pending = false;
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
			title = "New Event";
			body = event.creator + " is going to " + event.where + ".";
			i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy.events.revision", event.revision);
		} else {
			title = "New Events";
			body = notifications.size() + " new events by your friends.";
			i = new Intent(context, EventList.class);
			i.putExtra("filter", EventManager.Filter.FRIENDS);
		}
		i = NotificationRedirect.wrapIntent(context, i, "invite");
		PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
		Notification n = getNotification();
		n.setLatestEventInfo(context, title, body, pi);
		NotificationManager notManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notManager.notify(NOTIFICATION_ID, n);
	}

	private Notification getNotification() {
		if (notification == null) {
			notification = new Notification(R.drawable.notification,
					"New Connectsy Event", System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
		}
		return notification;
	}

	public void onDataUpdate(int code, String response) {
		if (code == GET_EVENT) {
			try {
				sendNotification();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void onRemoteError(int httpStatus, int code) {}

	public void comfirmed(Context context) {
		notifications = new ArrayList<JSONObject>();
	}
}
