package com.connectsy2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.connectsy2.data.DataManager;
import com.connectsy2.users.Register;

public class Launcher extends Activity {
	
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        
        if (DataManager.getCache(this).getString("token", null) == null){
        	startActivity(new Intent(this, Register.class));
        	this.finish();
        }else{
    		if (PreferenceManager.getDefaultSharedPreferences(this)
    				.getBoolean("notifications", true)){
	        	//start the notification service
	    		Intent i = new Intent();
	    		i.setAction("com.connectsy2.START_NOTIFICATIONS");
	    		startService(i);
    		}
    		
        	startActivity(new Intent(this, Dashboard.class));
        	this.finish();
        }
    }
}