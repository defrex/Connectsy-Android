package com.connectsy2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.connectsy2.R;

public class Utils {

	public static String maybeTruncate(String t, int num){
		if (t.length() > num)
			return t.substring(0, num)+"...";
		return t;
	}
	
	public static void setFooterView(Context context, ListView parent){
		if (parent.getFooterViewsCount() == 0){
			LayoutInflater i = LayoutInflater.from(context);
			View footer = i.inflate(R.layout.list_view_footer, parent, false);
			parent.addFooterView(footer, null, false);
		}
	}
}
