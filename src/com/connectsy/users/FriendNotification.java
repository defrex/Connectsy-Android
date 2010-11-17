package com.connectsy.users;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
		SharedPreferences data = context.getSharedPreferences("consy", 0);
		String curUsername = data.getString("username", null);
		i.putExtra("com.connectsy.user.username", curUsername);
		
		if (notifications.size() == 1) {
			title = "New Friend Request";
			body = username+" want to be your friend on Connectsy.";
		} else {
			title = "New Friend Requests";
			body = notifications.size() + " people want to be your friends.";
		}
		
		sendNotification(title, body, i, "friend");
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
