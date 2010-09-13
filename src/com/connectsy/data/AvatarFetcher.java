package com.connectsy.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.connectsy.settings.Settings;

public class AvatarFetcher extends AsyncTask<Void, Void, Boolean>{
	private final String TAG = "AvatarFetcher";
	private String username;
	private ImageView view;
	private SharedPreferences cache;
	private Context context;
	
	public AvatarFetcher(Context context, String username, ImageView view){
		this.username = username;
		this.view = view;
		this.context = context;
		cache = DataManager.getCache(context);
		long expNum = cache.getLong(getCacheName(), 0);
		if (expNum != 0){
			if (isStillGood(new Date(expNum))){
				renderCached();
				return;
			}else{
				cleanCachedFile();
			}
		}
		execute();
	}

	private boolean isStillGood(Date expiry){
		Date now = new Date();
		now.setHours(now.getHours()+2);
		return (expiry.compareTo(now) <= 0);
		
	}

	private String getFilename(){
		return "avatar-"+username;
	}
	private String getCacheName(){
		return "avatar-"+username;
	}
	
	private void cleanCachedFile() {
		context.deleteFile(getFilename());
	}
	
	private void renderCached() {
		try {
			view.setImageDrawable(new BitmapDrawable(context.openFileInput(getFilename())));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		String url = Settings.API_DOMAIN+"/users/"+username+"/avatar/";
		HttpGet request = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = client.execute(request);
			response.getEntity().writeTo(context.openFileOutput(getFilename(), Context.MODE_PRIVATE));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

	protected void onPostExecute(Boolean worked) {
		if (!worked) return;
		cache.edit().putLong(getCacheName(), new Date().getTime()).commit();
		renderCached();
	}
}
