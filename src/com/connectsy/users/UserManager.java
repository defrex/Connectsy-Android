package com.connectsy.users;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.util.Log;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.DataManager;
import com.connectsy.data.ApiRequest.Method;

public class UserManager extends DataManager {
	@SuppressWarnings("unused")
	private static final String TAG = "UserManager";

	private static final int GET_USER = 0;
	private static final int UPLOAD_AVATAR = 1;
	private static final int BEFRIEND = 2;
	private static final int UNFRIEND = 3;
	
	private String username;
	
	public static class User{
		public String username;
		public String display_name;
		public String id;
		public Integer friendStatus;
		public Integer created;
		public Boolean friendStatusPending = false;
		
		public User(){}
		
		public User(String userString) throws JSONException{
			JSONObject user = new JSONObject(userString);
			if (user.has("id"))
				id = user.getString("id");
			if (user.has("username"))
				username = user.getString("username");
			if (user.has("display_name"))
				display_name = user.getString("display_name");
			if (user.has("friend_status"))
				friendStatus = user.getInt("friend_status");
			if (user.has("friend_status_pending"))
				friendStatusPending = user.getBoolean("friend_status_pending");
			if (user.has("created"))
				created = user.getInt("created");
		}
		
		public String toString(){
			return username;
		}
		
		public String serialize(){
			JSONObject ret = new JSONObject();
			try {
				if (username != null)
					ret.put("username", username);
				if (display_name != null)
					ret.put("display_name", display_name);
				if (created != null)
					ret.put("created", created);
				if (friendStatusPending != null)
					ret.put("friendStatusPending", friendStatusPending);
				if (friendStatus != null)
					ret.put("friendStatus", friendStatus);
				if (id != null)
					ret.put("id", id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret.toString();
		}
		
		public static ArrayList<User> deserializeList(String usersStr) throws JSONException{
			JSONArray usersJSON = new JSONArray(usersStr);
			ArrayList<User> users = new ArrayList<User>();
			for(int i=0;i<usersJSON.length();i++)
				users.add(new User(usersJSON.getString(i)));
			return users;
		}
		
		public static String serializeList(ArrayList<User> users){
			JSONArray usersJSON = new JSONArray();
			for (int i=0;i<users.size();i++)
				usersJSON.put(users.get(i).serialize());
			return usersJSON.toString();
		}
	}
	
	public UserManager(Context c, DataUpdateListener passedListener, String pUsername) {
		super(c, passedListener);
		if (pUsername != null){
			username = pUsername;
		}else{
			username = UserManager.currentUsername(c);
		}
	}
	
	public User getUser(){
		String userString = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/", true, GET_USER).getCached();
		if (userString == null) return null;
		try {
			return new User(userString);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void refreshUser(int sentReturnCode){
		returnCode = sentReturnCode;
		Log.d(TAG, "refreshUser");
		new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/", true, GET_USER).execute();
	}
	
	private ApiRequest getFriendsRequest(boolean pending){
		ApiRequest r = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/friends/", true, GET_USER);
		if (pending) r.addGetArg("pending", "true");
		return r;
	}

	public ArrayList<User> getFriends(boolean pending){
		return getFriends(pending, false);
	}
	public ArrayList<User> getFriends(boolean pending, boolean returnNull){
		String friendString = getFriendsRequest(pending).getCached();
		ArrayList<User> friends = new ArrayList<User>();
		if (friendString != null){
			try {
				JSONArray jsonFriends = new JSONObject(friendString).getJSONArray("friends");
				for (int i=0;i<jsonFriends.length();i++){
					ApiRequest r = new ApiRequest(this, context, Method.GET, 
							"/users/"+jsonFriends.getString(i)+"/", true, GET_USER);
					String uString = r.getCached();
					if (uString != null){
						JSONObject userJSON = new JSONObject(uString);
						userJSON.put("friend_status_pending", pending);
						friends.add(new User(userJSON.toString()));
					}else{
						r.execute();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if (returnNull){
			return null;
		}
		return friends;
	}
	
	public void refreshFriends(boolean pending, int sentReturnCode){
		returnCode = sentReturnCode;
		getFriendsRequest(pending).execute();
	}
	
	public void befriend(int sentReturnCode){
		returnCode = sentReturnCode;
		new ApiRequest(this, context, Method.POST, 
				"/users/"+username+"/friends/", true, BEFRIEND).execute();
	}
	
	public void unfriend(int returnCode){
		this.returnCode = returnCode;
		new ApiRequest(this, context, Method.DELETE, 
				"/users/"+currentUsername(context)+"/friends/"+username+"/", 
				true, UNFRIEND).execute();
	}
	
	public void uploadAvatar(Uri avatar, int returnCode) throws IOException{
		this.returnCode = returnCode;
  	  	AssetFileDescriptor file = context.getContentResolver()
		  		.openAssetFileDescriptor(avatar, "r");
		ApiRequest r = new ApiRequest(this, context, Method.PUT, 
				"/users/"+username+"/avatar/", true, UPLOAD_AVATAR);
		r.setBodyFile(file);
		r.setHeader("Content-Type", context.getContentResolver().getType(avatar));
		r.execute();
		Log.d(TAG, "uploading avatar");
	}
	
	public static String currentUsername(Context context){
		return UserManager.getCache(context).getString("username", "");
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		if (code == UPLOAD_AVATAR)
			Log.d(TAG, "uploading avatar returned "+status+" with response "+response);
		listener.onDataUpdate(returnCode, response);
	}
}
