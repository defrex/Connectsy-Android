package com.connectsy.notifications;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public interface NotificationHandler {
	public void add(JSONObject notification) throws JSONException;
	public void send(Context context) throws JSONException;
	public void comfirmed(Context context);
}
