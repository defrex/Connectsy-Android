package com.connectsy.categories;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.widget.ImageView;

import com.connectsy.settings.Settings;

public class CategoryImageFetcher extends Object {
	@SuppressWarnings("unused")
	private static final String TAG = "CategoryImageFetcher";
	private String category;
	
	public CategoryImageFetcher(Context context, final ImageView view, String category) {
		this.category = category;
//		new ImageStore().getImage("category-"+category, getImageURL(), new ImageListener(){
//			public void onImageReady(Bitmap image) {
//				view.setImageDrawable(new BitmapDrawable(image));
//			}
//		}, false);
	}

	protected String getImageURL() {
		try {
			return Settings.API_DOMAIN+"/static/categories/"
					+URLEncoder.encode(category, "UTF-8")+".png";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

}
