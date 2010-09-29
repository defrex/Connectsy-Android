package com.connectsy.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.connectsy.data.ImageStore.ImageListener;
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

	public void fetch(){
		fetch(false);
	}
	public void fetch(boolean force){
		new ImageStore().getImage(
				getCacheName(), getImageURL(), new ImageListener(){
			public void onImageReady(Bitmap image) {
				view.setImageDrawable(new BitmapDrawable(image));
			}
		}, force);
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
