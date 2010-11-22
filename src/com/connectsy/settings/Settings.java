package com.connectsy.settings;

public class Settings {
	private Settings(){}
	// localhost:  "http://10.0.2.2:8080" - special android localhost ip
	// devserver:  "http://dev.connectsy.com"
	// production: "http://api1.connectsy.com"
	public final static String API_DOMAIN = "http://dev.connectsy.com";
	public final static int CACHE_VERSION = 1;
	public final static String PREFS_NAME = "consy";
	public final static int NOTIFICATION_UPDATE_PERIOD = 5000;//1000 * 60 * 2; // 2 min
	public final static boolean BACKGROUND_LOCATION = false;
//	public final static String MAPS_KEY = "0rr3ewYP3KKB_YCjDIHFBtI0AwGhwcY--yrfq_w";
}
