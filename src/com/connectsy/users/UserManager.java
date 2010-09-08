package com.connectsy.users;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.util.Log;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;

public class UserManager extends DataManager {
	@SuppressWarnings("unused")
	private static final String TAG = "UserManager";

	private static final int GET_USER = 0;
	private static final int UPLOAD_AVATAR = 1;
	private static final int BEFRIEND = 2;
	
	private String username;
	
	public static class User{
		public String username;
		public String revision;
		public int created;
		public boolean friendStatusPending = false;
		
		public User(){}
		
		public User(String userString) throws JSONException{
			JSONObject user = new JSONObject(userString);
			username = user.getString("username");
			revision = user.getString("revision");
			if (user.has("friend_status_pending"))
				friendStatusPending = user.getBoolean("friend_status_pending");
			if (user.has("created"))
				created = user.getInt("created");
		}
		
		public String serialize(){
			JSONObject ret = new JSONObject();
			try {
				ret.put("username", username);
				ret.put("revision", revision);
				ret.put("created", created);
				ret.put("friendStatusPending", friendStatusPending);
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
	
	public UserManager(Context c, DataUpdateListener passedListener, 
			String pUsername) {
		super(c, passedListener);
		if (pUsername != null){
			username = pUsername;
		}else{
			username = UserManager.getCache(c).getString("username", "");
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
		new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/", true, GET_USER).execute();
	}
	
	private ApiRequest getFriendsRequest(boolean pending){
		ApiRequest r = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/friends/", true, GET_USER);
		if (pending){
			ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(); 
			args.add(new BasicNameValuePair("pending", "true"));
			r.setGetArgs(args);
		}
		return r;
	}
	
	public ArrayList<User> getFriends(boolean pending){
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
	
	public void uploadAvatar(Uri avatar) throws IOException{
  	  	AssetFileDescriptor file = context.getContentResolver()
		  		.openAssetFileDescriptor(avatar, "r");
		ApiRequest r = new ApiRequest(this, context, Method.PUT, 
				"/users/"+username+"/avatar/", true, UPLOAD_AVATAR);
		r.setBodyFile(file);
		r.setHeader("Content-Type", context.getContentResolver().getType(avatar));
		r.execute();
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		Log.d(TAG, response);
		listener.onDataUpdate(returnCode, response);
	}
}
