package com.connectsy.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public final class ImageStore extends AsyncTask<Void, Void, Void> {
	
	private static HashMap<String, SoftReference<Bitmap>> cache =
			new HashMap<String, SoftReference<Bitmap>>();
	
	private String key;
	private String url; 
	private ImageListener listener;
	
	public interface ImageListener {
		public void onImageReady(Bitmap image);
	}
	
	public void getImage(String key, String url, ImageListener listener, boolean force){
		this.key = key;
		this.url = url;
		this.listener = listener;
		if (force || !returnFromCache()) execute();
	}
	
	private boolean returnFromCache(){
    	if (cache.containsKey(key)){
    		SoftReference<Bitmap> s = cache.get(key);
    		if (s != null){
    			Bitmap b = s.get();
    			if (b != null){
					listener.onImageReady(cache.get(key).get());
					return true;
    			}
    		}
        	cache.remove(key);
        }
    	return false;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
			cache.put(key, new SoftReference<Bitmap>(bitmap));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// 404 from URL, do nothing.
		}
		return null;
	}
	
	protected void onPostExecute() {
		returnFromCache();
    }
}
