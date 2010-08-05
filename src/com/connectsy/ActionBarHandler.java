package com.connectsy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;

import com.connectsy.events.EventNew;

public class ActionBarHandler implements OnClickListener{
	private Activity activity;
	
	public ActionBarHandler(Activity a){
		activity = a;
	}
	
	public void onClick(View abAction) {
    	if (abAction.getId() == R.id.ab_profile){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.user");
			SharedPreferences data = activity.getSharedPreferences("consy", 0);
			String username = data.getString("username", null);
			i.putExtra("com.connectsy.user.username", username);
			activity.startActivity(i);
    	}else if (abAction.getId() == R.id.ab_new_event){
    		activity.startActivity(new Intent(activity, EventNew.class));
    	}
	}

}
