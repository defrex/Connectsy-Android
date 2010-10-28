package com.connectsy.events.comments;

import org.json.JSONException;

import android.content.Intent;

import com.connectsy.notifications.NotificationHandlerBase;

public class CommentNotification extends NotificationHandlerBase {

	@SuppressWarnings("unused")
	private static final String TAG = "CommentNotification";
	protected String tickerText = "New Connectsy Comment";
	
	@Override
	protected void prepareNotification() throws JSONException {
		String title = "new comment";
		String body = "new comment";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setType("vnd.android.cursor.item/vnd.connectsy.event");
		i.putExtra("com.connectsy.events.revision", 
				notifications.get(0).getString("event_revision"));
		sendNotification(title, body, i, "invite");
	}
}
