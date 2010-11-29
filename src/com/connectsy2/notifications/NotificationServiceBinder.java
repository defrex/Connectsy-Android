package com.connectsy2.notifications;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Binder;

import com.connectsy2.notifications.NotificationHandler.NotificationContentListener;

public class NotificationServiceBinder extends Binder {
	NotificationListener listener;

	public NotificationServiceBinder(NotificationListener listener){
		this.listener = listener;
	}
	
	public NotificationListener getListener() {
		return listener;
	}
	
	public ArrayList<JSONObject> getPendingNotifications(){
		return listener.getHandler().getNotifications();
	}
	
	public void clearPendingNotifications(){
		listener.getHandler().clearNotifications();
	}

	public void getContentForNotification(Context c, JSONObject notification,
			NotificationContentListener callback) throws JSONException{
		listener.getHandler().getContentForNotification(c, notification,  callback);
	}
}
