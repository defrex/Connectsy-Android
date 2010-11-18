package com.connectsy.users;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.connectsy.notifications.NotificationHandlerBase;

public class FriendNotification extends NotificationHandlerBase {

	@SuppressWarnings("unused")
	private static final String TAG = "FriendNotification";
	
	@Override
	public void send(Context context) throws JSONException {
		if (!PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("preference_notifications_follow", true))
			return;
		else
			super.send(context);
	}
	
	@Override
	protected void prepareNotification() throws JSONException {
		String title;
		String body;
		String username = notifications.get(0).getString("username");
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setType("vnd.android.cursor.item/vnd.connectsy.user");
		i.putExtra("com.connectsy.user.username", username);
		i.putExtra("com.connectsy.user.tab", "following");
		
		if (notifications.size() == 1) {
			title = "New Follower";
			body = username+" is following you on Connectsy";
		} else {
			title = "New Followers";
			body = notifications.size() + " new people are following you";
		}
		
		sendNotification(title, body, i, "follow");
	}

	@Override
	protected int getNotificationID() {
		return 30;
	}

	@Override
	protected String getTickerText() {
		return "New Connectsy Follower";
	}
}
