package com.connectsy.users;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;

public class UserManager extends DataManager {
	@SuppressWarnings("unused")
	private static final String TAG = "UserManager";
	
	private static final int GET_USER = 0;
	
	private ApiRequest apiRequest;
	
	public class User{
		public String username;
		public String ID;
		public String revision;
		public int created;
		
		public User(){}
		
		public User(JSONObject user) throws JSONException{
			username = user.getString("username");
			ID = user.getString("id");
			revision = user.getString("revision");
			created = user.getInt("created");
		}
	}
	
	public UserManager(Context c, DataUpdateListener passedListener, 
			String username) {
		super(c, passedListener);
		apiRequest = new ApiRequest(this, context, Method.GET, 
				"/users/"+username+"/", null, null, true, GET_USER);
	}
	
	public User getUser(String username){
		String userString = apiRequest.getCached();
		if (userString == null) return null;
		try {
			return new User(new JSONObject(userString));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void refreshUser(String username, int sentReturnCode){
		returnCode = sentReturnCode;
    	apiRequest.execute();
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		listener.onDataUpdate(returnCode, response);
	}
}
