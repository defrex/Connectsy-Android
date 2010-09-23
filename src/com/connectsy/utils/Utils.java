package com.connectsy.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ListView;

import com.connectsy.R;

public class Utils {

	public static String maybeTruncate(String t, int num, boolean doIt){
		if (doIt && t.length() > num)
			return t.substring(0, num)+"...";
		return t;
	}
	
	public static void setFooterView(Context context, ListView parent){
		if (parent.getFooterViewsCount() == 0){
			LayoutInflater i = LayoutInflater.from(context);
			parent.addFooterView(i.inflate(R.layout.list_view_footer, parent, false));
		}
	}
}
