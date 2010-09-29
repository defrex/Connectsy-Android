package com.connectsy.data;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public final class ImageStore extends AsyncTask<Object, Void, Object[]> {
	
	private static HashMap<String, SoftReference<Bitmap>> cache =
			new HashMap<String, SoftReference<Bitmap>>();
	
	public interface ImageListener {
		public void onImageReady(Bitmap image);
	}
	
	public void getImage(String key, String url, ImageListener listener, boolean force){
		if (cache.containsKey(key) && cache.get(key) != null && !force){
			Bitmap image = cache.get(key).get();
			listener.onImageReady(image);
		}else{
			execute(key, url, listener);
		}
	}
	
	@Override
	protected Object[] doInBackground(Object... params) {
		String key = (String)params[0];
		String url = (String)params[1];
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
			cache.put(key, new SoftReference<Bitmap>(bitmap));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		Object[] ret = {key, params[2]};
		return ret;
	}
	
	protected void onPostExecute(Object[] args) {
        if (args != null){
        	String key = (String)args[0];
        	if (cache.containsKey(key) && cache.get(key) != null){
				ImageListener listener = (ImageListener)args[1];
				listener.onImageReady(cache.get(key).get());
	        }
        }
    }
}
