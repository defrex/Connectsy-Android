package com.connectsy.data;

import android.content.Context;
import android.widget.ImageView;

import com.connectsy.settings.Settings;

public class AvatarFetcher extends ImageFetcher{
	@SuppressWarnings("unused")
	private final String TAG = "AvatarFetcher";
	private String username;
	protected int cacheLength = 5;
	
	public AvatarFetcher(Context context, String username, ImageView view){
		super(context, view);
		this.username = username;
	}
	
	protected String getFilename(){
		return "avatar-"+username;
	}
	protected String getCacheName(){
		return "avatar-"+username;
	}
	protected String getImageURL(){
		return Settings.API_DOMAIN+"/users/"+username+"/avatar/";
	}
}
