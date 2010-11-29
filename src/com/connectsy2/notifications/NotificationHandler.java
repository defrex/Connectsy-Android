package com.connectsy2.notifications;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.connectsy2.R;
import com.connectsy2.events.EventNotification;
import com.connectsy2.events.attendants.AttendantNotification;
import com.connectsy2.events.comments.CommentNotification;
import com.connectsy2.users.FriendNotification;

public class NotificationHandler {

	@SuppressWarnings("unused")
	private static final String TAG = "NotificationHandlerBase";
	private Notification notification;
	private HashMap<String, NotificationContentProvider> notificationContent;
	private ArrayList<JSONObject> nots = new ArrayList<JSONObject>();
	
	public static class NotificationContent{
		public Intent intent;
		public String body;
		public String title;
		public String ticker;
		public String username;
	}
	
	public static interface NotificationContentListener{
		public void sendNotification(NotificationContent result);
	}
	
	public interface NotificationContentProvider{
		public void prepNotification(Context context, 
				ArrayList<JSONObject> notifications,
				NotificationContentListener callback) throws JSONException;
	}
	
	class DefaultNotificationContentProvider implements NotificationContentProvider{
		public void prepNotification(Context context,
				ArrayList<JSONObject> notifications, 
				NotificationContentListener callback) {
			NotificationContent not = new NotificationContent();
			not.title = "Activity on Connectsy";
			not.body = notifications.size()+" new notifications";
			not.ticker = not.title;
			callback.sendNotification(not);
		}
	}
	
	public NotificationHandler(){
		notificationContent = new HashMap<String, NotificationContentProvider>();
		notificationContent.put("default", new DefaultNotificationContentProvider());
		notificationContent.put("comment", new CommentNotification());
		notificationContent.put("invite", new EventNotification());
		notificationContent.put("attendant", new AttendantNotification());
		notificationContent.put("follow", new FriendNotification());
	}
	
	public void send(final Context context, JSONArray notifications) 
			throws JSONException{
		if (notifications.length() == 0) return;
		
		if (!PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("notifications", true))
			return;
		
		for (int i=0;i<notifications.length();i++){
			JSONObject not = notifications.getJSONObject(i);
			if (PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean("notifications_"+not.getString("type"), true))
				nots.add(not);
		}
		if (nots.size() == 0) return;

		String type = nots.get(0).getString("type");
		for (int i=1;i<nots.size();i++)
			if (!type.equals( nots.get(i).getString("type"))){
				type = "default";
				break;
			}
		final String finalType = type;
		
		notificationContent.get(type).prepNotification(context, nots, 
			new NotificationContentListener(){
				public void sendNotification(NotificationContent not) {
					if (not.intent == null)
						not.intent = new Intent(context, NotificationList.class);
					else
						not.intent = NotificationRedirect.wrapIntent(context, 
								not.intent, finalType);
					
					PendingIntent pi = PendingIntent.getActivity(context, 0, 
							not.intent, PendingIntent.FLAG_UPDATE_CURRENT);
					//TODO: fix not.ticker on next line
					Notification n = getNotification("New activity on Connectsy");
					n.setLatestEventInfo(context, not.title, not.body, pi);
					NotificationManager notManager = (NotificationManager) 
						context.getSystemService(Context.NOTIFICATION_SERVICE);
					notManager.notify(1, n);
				}
			});
	}

	private Notification getNotification(String ticker) {
		if (notification == null) {
			notification = new Notification(R.drawable.notification,
					ticker, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			notification.ledARGB = 0xff00ff00;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		return notification;
	}

	public ArrayList<JSONObject> getNotifications() {
		return nots;
	}

	public void clearNotifications() {
		nots = new ArrayList<JSONObject>();
	}

	public void getContentForNotification(Context c, JSONObject notification,
			NotificationContentListener callback) throws JSONException{
		ArrayList<JSONObject> pass = new ArrayList<JSONObject>();
		pass.add(notification);
		notificationContent.get(notification.getString("type"))
				.prepNotification(c, pass, callback);
	}
}
