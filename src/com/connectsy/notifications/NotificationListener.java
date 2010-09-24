//todo - singleton-ify this dude

package com.connectsy.notifications;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.connectsy.LocManager;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.events.EventNotification;

public class NotificationListener implements ApiRequestListener {

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
	static final int PERIOD = 1000 * 60 * 5; // 5 min

	static final String TAG = "NotificationListener";
	static final int REGISTER = 0;
	static final int POLL = 1;

	Handler handler;
	boolean running;
	Context context;
	String clientId;
	LocManager location;
	HashMap<String, NotificationHandler> notificationHandlers;

	private NotificationListener() {
		// prep handler
		handler = new Handler();
		
		notificationHandlers = new HashMap<String, NotificationHandler>();
		notificationHandlers.put("invites", new EventNotification());
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
		
		//get clientid
		clientId = Settings.Secure.getString(context.getContentResolver(), 
				Settings.Secure.ANDROID_ID);
		if (clientId == null)
			clientId = "EMULATOR";
		
		//prep location listener
		location = new LocManager(context);

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
					//add geolocation data
					Location loc = location.getLocation();
					if (loc != null) {
						request.addGetArg("lat", String.valueOf(loc.getLatitude()));
						request.addGetArg("lng", String.valueOf(loc.getLongitude()));
					}
					//fire it off
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
				Log.e(TAG, "Failed notification register!");
			}
		} else if (code == POLL) {

			if (status == 200) {
				try {
					JSONArray notifications = new JSONObject(response)
							.getJSONArray("notifications");
					Log.d(TAG, "got notices: "+notifications);
					for (int i=0;i<notifications.length();i++){
						JSONObject notice = notifications.getJSONObject(i);
						if (notificationHandlers.containsKey(notice.getString("type")))
							notificationHandlers.get(notice.getString("type")).add(notice);
					}
					for (NotificationHandler handler: notificationHandlers.values())
						handler.send(context);
				} catch (JSONException e) {}
			} else {
				Log.e(TAG, "Failed notification poll");
			}

			notifyCallback();
		}
	}
	// nothing to see here, please move along...
	public void onApiRequestError(int httpStatus, int retCode) {}
}
