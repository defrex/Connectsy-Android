package com.connectsy.data;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.connectsy.data.ImageStore.ImageListener;
import com.connectsy.settings.Settings;

public class AvatarFetcher extends Object{
	@SuppressWarnings("unused")
	private final String TAG = "AvatarFetcher";
	
	public AvatarFetcher(String username, final ImageView view, boolean force){
		new ImageStore().getImage("avatar-"+username, 
				Settings.API_DOMAIN+"/users/"+username+"/avatar/", new ImageListener(){
			public void onImageReady(Bitmap image) {
				view.setImageDrawable(new BitmapDrawable(image));
			}
		}, force);
	}
}
