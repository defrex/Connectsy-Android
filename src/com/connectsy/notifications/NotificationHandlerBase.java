package com.connectsy.notifications;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.connectsy.R;
import com.connectsy.notifications.NotificationHandler;
import com.connectsy.notifications.NotificationRedirect;

public abstract class NotificationHandlerBase implements NotificationHandler {

	@SuppressWarnings("unused")
	private static final String TAG = "NotificationHandlerBase";
	private boolean pending = false;
	private Notification notification;
	protected Context context;
	protected ArrayList<JSONObject> notifications;
	
	public NotificationHandlerBase(){
		notifications = new ArrayList<JSONObject>();
	}

	protected abstract int getNotificationID();
	protected abstract String getTickerText();
	protected abstract void prepareNotification() throws JSONException;
	
	public void add(JSONObject notification){
		notifications.add(notification);
		pending = true;
	}
	
	public void send(Context context) throws JSONException{
		this.context = context;
		if (pending) prepareNotification();
	}

	protected void sendNotification(String title, String body, Intent i, 
			String type) throws JSONException {
		pending = false;
//		i = NotificationRedirect.wrapIntent(context, i, type);
		PendingIntent pi = PendingIntent.getActivity(context, 0, i, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification n = getNotification();
		n.setLatestEventInfo(context, title, body, pi);
		NotificationManager notManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notManager.notify(getNotificationID(), n);
		notification = null;
	}

	private Notification getNotification() {
		if (notification == null) {
			notification = new Notification(R.drawable.notification,
					getTickerText(), System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			notification.ledARGB = 0xff00ff00;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		return notification;
	}
	
	public void comfirmed(Context context) {
		notifications = new ArrayList<JSONObject>();
	}
}
