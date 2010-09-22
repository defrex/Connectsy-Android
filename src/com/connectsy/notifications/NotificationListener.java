//todo - singleton-ify this dude

package com.connectsy.notifications;

import java.util.Timer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	static final int PERIOD = 1000; //1s
	
	static final int REGISTER = 0;
	static final int POLL = 1;
	
	Handler handler;
	boolean running;
	Context context;
	String clientId;
	
	private NotificationListener() {
		//prep handler
		handler = new Handler();
		
		//grab the clientid
		clientId = Settings.Secure.ANDROID_ID;
		//apparently ANDROID_ID may not be set in
		//an emulator, so this is the backup plan
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
		//if we're running, shut down
		stop();
		
		//update context
		context = c;
		
		//set the status
		running = true;
		
		//bootstrap the poll by registering for notifications
		ApiRequest request = new ApiRequest(this,
				context, Method.POST, "/notifications/register/",
				true, REGISTER);
		request.setBodyString(String.format(
				"{\"client_type\":\"%s\", \"client_id\":\"%s\"}",
				"generic_poll", clientId));
		request.execute();
	}
	
	/**
	 * Stops the notification listener at some
	 * point in the near future.
	 */
	public void stop() {
		running = false;
	}
	
	private void notifyCallback() {
		if (running) {
			final NotificationListener nl = this;
			handler.postDelayed(new Runnable() {
				public void run() {
					//fire off a new request
					ApiRequest request = new ApiRequest(nl,
							context, Method.GET, "/notifications/poll/",
							true, POLL);
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
				//TODO - handle the body
				try {
					JSONArray notifications = new JSONObject(response).getJSONArray("notifications");
					for (int i = 0; i < notifications.length(); i++) {
						JSONObject obj = notifications.getJSONObject(i);
						String type = obj.getString("type");
						
						//handle different notification types
						if (type.equals("event")) {
							//i'm guessing we want event name, event id, etc...
						}
					}
				} catch (JSONException e) {} //silent fail
				System.out.println("Poll success");
				System.out.println(response);
			} else {
				System.err.println("Failed notification poll");
			}
			
			notifyCallback();
		}
		
	}

	public void onApiRequestError(int httpStatus, int retCode) {
		//nothing to see here, please move along...
	}
}
