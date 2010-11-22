//todo - singleton-ify this dude

package com.connectsy.notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.connectsy.LocManager;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;

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

	static final String TAG = "NotificationListener";
	static final int REGISTER = 0;
	static final int POLL = 1;

	Handler handler;
	boolean running;
	Context context;
	String clientId;
	LocManager location;
	private NotificationHandler notificationHandler;

	private NotificationListener() {
		// prep handler
		handler = new Handler();
		notificationHandler = new NotificationHandler();
	}
	
	public NotificationHandler getHandler(){
		return notificationHandler;
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
		if (!PreferenceManager.getDefaultSharedPreferences(c)
				.getBoolean("preference_notifications", true))
			return;
		
		// update context
		context = c;
		
		//get clientid
		clientId = Settings.Secure.getString(context.getContentResolver(), 
				Settings.Secure.ANDROID_ID);
		if (clientId == null)
			clientId = "EMULATOR";

		//prep location listener
		if (com.connectsy.settings.Settings.BACKGROUND_LOCATION)
			location = new LocManager(context);

		// set the status
		running = true;

		// bootstrap the poll by registering for notifications
		registerForNotifications();
	}

	/**
	 * Stops the notification listener at some point in the near future.
	 */
	public void stop() {
		running = false;
	}
	
	private void registerForNotifications() {
		ApiRequest request = new ApiRequest(this, context, Method.POST,
				"/notifications/register/", true, REGISTER);
		request.setBodyString(String.format(
				"{\"client_type\":\"%s\", \"client_id\":\"%s\"}",
				"generic_poll", clientId));
		request.setSilent(true);
		request.execute();
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
					if (location != null){
						Location loc = location.getLocation();
						if (loc != null) {
							request.addGetArg("lat", 
									String.valueOf(loc.getLatitude()));
							request.addGetArg("lng", 
									String.valueOf(loc.getLongitude()));
						}
					}
					request.setSilent(true);
					//fire it off
					request.execute();
				}
			}, com.connectsy.settings.Settings.NOTIFICATION_UPDATE_PERIOD);
		}
	}

	public void onApiRequestFinish(int status, String response, int code) {
		if (code == REGISTER) {
			if (status == 200) {
				//throw a party or something
			} else {
				Log.e(TAG, "Failed notification register!");
			}
			notifyCallback();
		} else if (code == POLL) {

			if (status == 200) {
				try {
					JSONArray notifications = new JSONObject(response)
							.getJSONArray("notifications");
					notificationHandler.send(context, notifications);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			
				//start notifying
				notifyCallback();
			} else {
				final NotificationListener self = this;
				handler.postDelayed(new Runnable() {
					public void run() {
						self.registerForNotifications();
					}
				}, com.connectsy.settings.Settings.NOTIFICATION_UPDATE_PERIOD);
			}
		}
	}
	// nothing to see here, please move along...
	public void onApiRequestError(int httpStatus, String response, int retCode) {}
}
