package com.connectsy.users;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.DataManager;
import com.connectsy.data.ApiRequest.Method;

public class UserManager extends DataManager {
	@SuppressWarnings("unused")
	private static final String TAG = "UserManager";

	private static final int GET_USER = 0;
	private static final int UPLOAD_AVATAR = 1;
	private static final int GET_FOLLOWING = 2;
	private static final int GET_FOLLOWERS = 3;
	private static final int FOLLOW = 4;
	
	private String username;
	
	public static class User{
		public String username;
		public String display_name;
		public String id;
		public Integer created;
		public Boolean following;
		public Boolean follower;
		
		public User(){}
		
		public User(String userString) throws JSONException{
			JSONObject user = new JSONObject(userString);
			if (user.has("id"))
				id = user.getString("id");
			if (user.has("username"))
				username = user.getString("username");
			if (user.has("display_name"))
				display_name = user.getString("display_name");
			if (user.has("following"))
				following = user.getBoolean("following");
			if (user.has("follower"))
				follower = user.getBoolean("follower");
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
				if (following != null)
					ret.put("following", following);
				if (follower != null)
					ret.put("follower", follower);
				if (id != null)
					ret.put("id", id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret.toString();
		}
		
		public static ArrayList<User> deserializeList(String usersStr) 
				throws JSONException{
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
			String username) {
		super(c, passedListener);
		if (username != null){
			this.username = username;
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
		new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/", true, GET_USER).execute();
	}
	
	private ApiRequest getFollowingRequest(){
		ApiRequest r = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/following/", true, GET_FOLLOWING);
		return r;
	}

	public ArrayList<String> getFollowing(){
		String followingString = getFollowingRequest().getCached();
		if (followingString == null)
			return null;
		ArrayList<String> following = new ArrayList<String>();
		try {
			JSONArray jsonFollowing = new JSONArray(followingString);
			for (int i=0;i<jsonFollowing.length();i++)
				following.add(jsonFollowing.getString(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return following;
	}
	
	public void refreshFollowing(int returnCode){
		this.returnCode = returnCode;
		getFollowingRequest().execute();
	}
	
	private ApiRequest getFollowersRequest(){
		ApiRequest r = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/followers/", true, GET_FOLLOWERS);
		return r;
	}

	public ArrayList<String> getFollowers(){
		String followersString = getFollowersRequest().getCached();
		if (followersString == null)
			return null;
		ArrayList<String> followers = new ArrayList<String>();
		try {
			JSONArray jsonFollowers = new JSONArray(followersString);
			for (int i=0;i<jsonFollowers.length();i++)
				followers.add(jsonFollowers.getString(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return followers;
	}
	
	public void refreshFollowers(int returnCode){
		this.returnCode = returnCode;
		getFollowersRequest().execute();
	}
	
	public void follow(int returnCode){
		this.returnCode = returnCode;
		ApiRequest r = new ApiRequest(this, context, Method.POST, 
				"/users/"+username+"/followers/", true, FOLLOW);
		r.setBodyString("{\"follow\": true}");
		r.execute();
	}
	
	public void unfollow(int returnCode){
		this.returnCode = returnCode;
		ApiRequest r = new ApiRequest(this, context, Method.POST, 
				"/users/"+username+"/followers/", true, FOLLOW);
		r.setBodyString("{\"follow\": false}");
		r.execute();
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
	}
	
	public static String currentUsername(Context context){
		return UserManager.getCache(context).getString("username", "");
	}
	
	public static User currentUser(Context context){
		return new UserManager(context, null, 
				UserManager.currentUsername(context)).getUser();
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		listener.onDataUpdate(returnCode, response);
	}
}
