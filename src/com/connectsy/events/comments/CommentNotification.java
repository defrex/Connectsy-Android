package com.connectsy.events.comments;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventList;
import com.connectsy.events.EventManager;
import com.connectsy.events.EventManager.Event;
import com.connectsy.notifications.NotificationHandlerBase;

public class CommentNotification extends NotificationHandlerBase implements DataUpdateListener {

	@SuppressWarnings("unused")
	private static final String TAG = "CommentNotification";
	private static final int GET_EVENT = 0;
	protected String tickerText = "New Connectsy Comment";
	
	@Override
	protected void prepareNotification() throws JSONException {
		String title;
		String body;
		Intent i;
		Event event = null;
		
		boolean oneEvent = true;
		String rev = notifications.get(0).getString("event_revision");
		for (JSONObject notice: notifications)
			if (!rev.equals(notice.getString("event_revision")))
				oneEvent = false;
		if (oneEvent){
			EventManager eventManager = new EventManager(context, this, null,
					null);
			event = eventManager.getEvent(rev);
			if (event == null) {
				eventManager.refreshEvent(rev, GET_EVENT);
				return;
			}
			i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy.events.revision", event.revision);
			
			if (notifications.size() == 1) {
				String commenter = notifications.get(0).getString("commenter");
				title = "New Connectsy Comment";
				body = commenter + " commented on " + event.where + ".";
			} else {
				title = "New Connectsy Comments";
				body = notifications.size() + " new comments on " + event.where + ".";
			}
		}else{
			title = "New Connectsy Comments";
			body = notifications.size() + " new comments on events you're attending.";
			i = new Intent(context, EventList.class);
			i.putExtra("filter", EventManager.Filter.FRIENDS);
		}
		sendNotification(title, body, i, "invite");
	}
	
	public void onDataUpdate(int code, String response) {
		if (code == GET_EVENT) {
			try {
				prepareNotification();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void onRemoteError(int httpStatus, String response, int code) {}
}
