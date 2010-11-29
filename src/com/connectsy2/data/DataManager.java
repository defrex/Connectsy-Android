package com.connectsy2.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.connectsy2.data.ApiRequest.ApiRequestListener;
import com.connectsy2.settings.Settings;
import com.connectsy2.users.Logout;

public abstract class DataManager implements ApiRequestListener {
	@SuppressWarnings("unused")
	private static final String TAG = "DataManager";
	public int returnCode;
	public int pendingUpdates = 0;
	public Context context;
	public DataUpdateListener listener;
	
	public interface DataUpdateListener{
		public void onDataUpdate(int code, String response);
		public void onRemoteError(int httpStatus, String response, int code);
	}
	
	public DataManager(Context c, DataUpdateListener passedListener){
		context = c;
		listener = passedListener;
	}
	
	public static SharedPreferences getCache(Context c){
		SharedPreferences d = c.getSharedPreferences(Settings.PREFS_NAME, 
				Context.MODE_PRIVATE);
		if (d.getInt("CACHE_VERSION", 0) != Settings.CACHE_VERSION)
			d.edit().clear().putInt("CACHE_VERSION", Settings.CACHE_VERSION).commit();
		return d;
	}
	
	public static void cleanCache(Context context){
		getCache(context).edit().clear()
				.putInt("CACHE_VERSION", Settings.CACHE_VERSION).commit();
	}
	
	public void onApiRequestFinish(int status, String response, int code){
		listener.onDataUpdate(code, response);
	};
	
	public void onApiRequestError(int httpStatus, String response, int code) {
		if (httpStatus == 401){
            context.startActivity(new Intent(context, Logout.class));
        	((Activity) context).finish();
		}
		listener.onRemoteError(httpStatus, response, returnCode);
	}
}