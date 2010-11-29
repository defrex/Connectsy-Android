package com.connectsy2.events.comments;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.events.EventManager;
import com.connectsy2.events.EventManager.Event;
import com.connectsy2.notifications.NotificationHandler.NotificationContent;
import com.connectsy2.notifications.NotificationHandler.NotificationContentListener;
import com.connectsy2.notifications.NotificationHandler.NotificationContentProvider;

public class CommentNotification implements DataUpdateListener, 
		NotificationContentProvider {

	@SuppressWarnings("unused")
	private static final String TAG = "CommentNotification";
	private static final int GET_EVENT = 0;
	private Context context;
	private ArrayList<JSONObject> nots;
	private NotificationContentListener callback;

	public void prepNotification(Context context, 
			ArrayList<JSONObject> nots, 
			NotificationContentListener callback) 
			throws JSONException {
		this.context = context;
		this.nots = nots;
		this.callback = callback;
		prepareNotification();
	}

	private void prepareNotification() throws JSONException {
		NotificationContent not = new NotificationContent();
		Event event = null;
		
		boolean oneEvent = true;
		String rev = nots.get(0).getString("event_revision");
		for (int i=1;i<nots.size();i++)
			if (!rev.equals(nots.get(i).getString("event_revision")))
				oneEvent = false;
		if (oneEvent){
			EventManager eventManager = new EventManager(context, this, null,
					null);
			event = eventManager.getEvent(rev);
			if (event == null) {
				eventManager.refreshEvent(rev, GET_EVENT);
				return;
			}

			if (nots.size() == 1) {
				String commenter = nots.get(0).getString("commenter");
				
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.event");
				i.putExtra("com.connectsy2.events.revision", rev);
				i.putExtra("com.connectsy2.events.comments", true);
				
				not.intent = i;
				not.username = commenter;
				not.title = commenter+" posted a comment";
				not.body = nots.get(0).getString("comment");
			} else {
				not.title = nots.size()+" comments";
				not.body = "By";
				for (int i=1;i<nots.size();i++)
					not.body += " "+nots.get(i).getString("commenter")+",";
				not.body = not.body.substring(0, not.body.length()-1);
				not.ticker = "New Comment";
			}
		}else{
			not.title = nots.size()+" comments";
			not.body = "By";
			for (int i=1;i<nots.size();i++)
				not.body += " "+nots.get(i)
						.getString("commenter")+",";
			not.body = not.body.substring(0, not.body.length()-1);
			not.ticker = "New Comments";
		}
		callback.sendNotification(not);
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
