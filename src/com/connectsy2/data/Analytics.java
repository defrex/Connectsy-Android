package com.connectsy2.data;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;


public class Analytics {
	private static final String TAG = "Analytics";
	private static GoogleAnalyticsTracker tracker;
	
	private static GoogleAnalyticsTracker getTracker(Context c){
		if (tracker == null){
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.start("UA-19600520-2", 20, c);
			Log.d(TAG, "started tracker");
		}
		return tracker;
	}
	
	public static void pageView(Context c, String url){
		getTracker(c).trackPageView(url);
	}
	
	public static void event(Context c, String category, String action){
		getTracker(c).trackEvent(category, action, null, 0);
	}

}
