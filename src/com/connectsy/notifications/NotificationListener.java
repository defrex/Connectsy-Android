//todo - singleton-ify this dude

package com.connectsy.notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;

import com.connectsy.R;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventList;
import com.connectsy.events.EventManager;
import com.connectsy.events.EventManager.Event;

public class NotificationListener implements ApiRequestListener,
		DataUpdateListener {

	// singleton junk
	static NotificationListener instance;

	public static NotificationListener getInstance() {
		if (instance == null) {
			synchronized (NotificationListener.class) {
				if (instance == null) {
					instance = new NotificationListener();
				}
			}
		}
		return instance;
	}

	/**
	 * How long to wait in between polls
	 */
	static final int PERIOD = 10000; // 10s

	static final int REGISTER = 0;
	static final int POLL = 1;
	static final int GET_EVENT = 2;
	static final int NOTIFICATION_ID = 3;

	Handler handler;
	boolean running;
	Context context;
	String clientId;
	Notification notification;
	JSONArray notifications;

	private NotificationListener() {
		// prep handler
		handler = new Handler();

		// grab the clientid
		clientId = Settings.Secure.ANDROID_ID;
		// apparently ANDROID_ID may not be set in
		// an emulator, so this is the backup plan
		if (clientId == null)
			clientId = "EMULATOR";
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * Starts the notification listener.
	 */
	public void start(Context c) {
		// if we're running, shut down
		stop();

		// update context
		context = c;

		// set the status
		running = true;

		// bootstrap the poll by registering for notifications
		ApiRequest request = new ApiRequest(this, context, Method.POST,
				"/notifications/register/", true, REGISTER);
		request.setBodyString(String.format(
				"{\"client_type\":\"%s\", \"client_id\":\"%s\"}",
				"generic_poll", clientId));
		request.execute();
	}

	/**
	 * Stops the notification listener at some point in the near future.
	 */
	public void stop() {
		running = false;
	}

	private void notifyCallback() {
		if (running) {
			final NotificationListener nl = this;
			handler.postDelayed(new Runnable() {
				public void run() {
					// fire off a new request
					ApiRequest request = new ApiRequest(nl, context,
							Method.GET, "/notifications/poll/", true, POLL);
					request.addGetArg("client_id", clientId);
					request.execute();
				}
			}, PERIOD);
		}
	}

	public void onApiRequestFinish(int status, String response, int code) {
		if (code == REGISTER) {
			if (status == 200) {
				notifyCallback();
			} else {
				System.err.println("Failed notification register!");
			}
		} else if (code == POLL) {

			if (status == 200) {
				// TODO - handle the body
				try {
					notifications = new JSONObject(response)
							.getJSONArray("notifications");
					sendNotification();
				} catch (JSONException e) {
				} // silent fail
				System.out.println("Poll success");
				System.out.println(response);
			} else {
				System.err.println("Failed notification poll");
			}

			notifyCallback();
		}
	}

	private void sendNotification() throws JSONException {
		String title;
		String body;
		Intent i;
		/*
		 * if (notifications.length() == 0) { return; }else
		 */if (notifications.length() == 1) {
			String rev = notifications.getJSONObject(0).getString(
					"event_revision");
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
			body = notifications.length() + " new events by your friends.";
			i = new Intent(context, EventList.class);
			i.putExtra("filter", EventManager.Filter.FRIENDS);
		}
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

	// nothing to see here, please move along...
	public void onRemoteError(int httpStatus, int code) {}
	public void onApiRequestError(int httpStatus, int retCode) {}
}
