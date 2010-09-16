package com.connectsy.utils;

public class Utils {

	public static String maybeTruncate(String t, int num, boolean doIt){
		if (doIt && t.length() > num)
			return t.substring(0, num)+"...";
		return t;
	}
	
	public static String distanceFrom(double lat, double lng){
		return "1.2 km North";
	}
}
