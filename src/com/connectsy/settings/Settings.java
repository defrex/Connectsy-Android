package com.connectsy.settings;

public class Settings {
	private Settings(){}
	// localhost:  "http://10.0.2.2:8080" - special android localhost ip
	// devserver:  "http://dev.connectsy.com"
	// production: "http://api1.connectsy.com"
	public final static String API_DOMAIN = "http://api1.connectsy.com";
	public final static int CACHE_VERSION = 5;
	public final static String PREFS_NAME = "consy";
}
