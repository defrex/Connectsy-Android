package com.connectsy.users;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.connectsy.notifications.NotificationHandler.NotificationContent;
import com.connectsy.notifications.NotificationHandler.NotificationContentListener;
import com.connectsy.notifications.NotificationHandler.NotificationContentProvider;

public class FriendNotification implements NotificationContentProvider {

	@SuppressWarnings("unused")
	private static final String TAG = "FriendNotification";

	public void prepNotification(Context context, 
			ArrayList<JSONObject> nots, NotificationContentListener callback) 
			throws JSONException {
		NotificationContent not = new NotificationContent();
		
		if (nots.size() == 1) {
			String username = nots.get(0).getString("username");
			
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.user");
			i.putExtra("com.connectsy.user.username", username);
			i.putExtra("com.connectsy.user.tab", "following");
			
			not.intent = i;
			not.username = username;
			not.title = "New Follower";
			not.body = username+" is following you";
			not.ticker = "New follower";
		} else {
			not.title = "New Followers";
			not.body = nots.size() + " new users are following you";
			not.ticker = "New followers";
		}

		callback.sendNotification(not);
	}
}
