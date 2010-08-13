package com.connectsy.users;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;

public class UserManager extends DataManager {
	@SuppressWarnings("unused")
	private static final String TAG = "UserManager";

	private static final int GET_USER = 0;
	private static final int UPLOAD_AVATAR = 1;
	
	private ApiRequest apiRequest;
	private String username;
	
	public class User{
		public String username;
		public String revision;
		public int created;
		
		public User(){}
		
		public User(JSONObject user) throws JSONException{
			username = user.getString("username");
			revision = user.getString("revision");
			//created = user.getInt("created");
		}
	}
	
	public UserManager(Context c, DataUpdateListener passedListener, 
			String pUsername) {
		super(c, passedListener);
		username = pUsername;
	}
	
	public User getUser(){
		String userString = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/", true, GET_USER).getCached();
		if (userString == null) return null;
		try {
			return new User(new JSONObject(userString));
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
	
	public void uploadAvatar(Uri avatar) throws IOException{
  	  	AssetFileDescriptor file = context.getContentResolver()
		  		.openAssetFileDescriptor(avatar, "r");
		ApiRequest r = new ApiRequest(this, context, Method.PUT, 
				"/users/"+username+"/avatar/", true, UPLOAD_AVATAR);
		r.setBodyFile(file);
		r.execute();
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		listener.onDataUpdate(returnCode, response);
	}
}
