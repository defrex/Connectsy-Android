package com.connectsy.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.*;

import android.content.Context;

import com.connectsy.data.DataManager;
import com.connectsy.data.ApiRequest;

public class CommentManager extends DataManager {
	
	/**
	 * Holds comment data
	 */
	public class Comment {
		String id;
		String event;
		String user;
		String nonce;
		String comment;
		int timestamp;
		
		public Comment(JSONObject obj) throws JSONException {
			id = obj.getString("id");
			event = obj.getString("event");
			user = obj.getString("user");
			nonce = obj.getString("nonce");
			timestamp = (int)obj.getLong("timestamp");
			comment = obj.getString("comment");
		}
		
		public String getId() {
			return id;
		}
		public String getEvent() {
			return event;
		}
		public String getUser() {
			return user;
		}
		public String getNonce() {
			return nonce;
		}
		public String getComment() {
			return comment;
		}
		public int getTimestamp() {
			return timestamp;
		}
	}
	//////////////////////////////////////////////
	
	//ApiRequest identifiers
	final static int GET_COMMENTS = 0;
	
	
	EventManager.Event event;
	int lastTimestamp = 0;
	
	public CommentManager(Context c, DataUpdateListener l, EventManager.Event event) {
		super(c, l);
		this.event = event;
	}
	
	/**
	 * Fetches new comments in the background
	 */
	public void refreshComments(int returnCode) {
		this.returnCode = returnCode;
		String path = String.format("/events/%s/comments/", event.ID);
		//don't fetch comments we don't have to
		if (lastTimestamp > 0)
			path += "?since=" + String.valueOf(lastTimestamp);
		
		ApiRequest request = new ApiRequest(this, this.context, ApiRequest.Method.GET, path, true, 0);
		request.execute();
		pendingUpdates++;
	}
	
	/**
	 * Fetches all cached comments
	 * @return array of comments
	 */
	public Collection<Comment> getComments() {
		List<Comment> comments = new ArrayList<Comment>();
		int since = 0;
		
		//keep looping over all cached comment requests
		while (true) {
			ApiRequest request = new ApiRequest(this,
					this.context,
					ApiRequest.Method.GET,
					String.format("/events/%s/comments/", event.ID)
						+ (since == 0 ? "" : "?start=" + String.valueOf(since)),
					true,
					0);
			
			String result = request.getCached();
			if (result != null) {
				try {
					JSONArray jsonComments = new JSONObject(result).getJSONArray("comments");
					for (int i=0; i<jsonComments.length(); i++) {
						Comment c = new Comment(jsonComments.getJSONObject(i));
						comments.add(c);
						//set the last comment, so that we know where to start looking for
						//the next request
						since = c.getTimestamp();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				break;
			}
				
		};
		
		lastTimestamp = since;
		return comments;
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		if (code == this.GET_COMMENTS && !response.equals("")) {
			try {
				//save the last timestamp in the array
				JSONArray jsonComments = new JSONObject(response).getJSONArray("comments");
				lastTimestamp = new Comment(jsonComments.getJSONObject(jsonComments.length() - 1)).getTimestamp();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		pendingUpdates--;
		
		if (pendingUpdates == 0) {
			listener.onDataUpdate(returnCode, response);
		}
	}
}
