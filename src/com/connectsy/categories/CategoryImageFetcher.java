package com.connectsy.categories;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.widget.ImageView;

import com.connectsy.data.ImageFetcher;
import com.connectsy.settings.Settings;

public class CategoryImageFetcher extends ImageFetcher {
	@SuppressWarnings("unused")
	private static final String TAG = "CategoryImageFetcher";
	private String category;
	
	public CategoryImageFetcher(Context context, ImageView view, String category) {
		super(context, view);
		this.category = category;
	}

	@Override
	protected String getFilename() {
		return "category-"+category;
	}

	@Override
	protected String getCacheName() {
		return "category-"+category;
	}

	@Override
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
