package com.connectsy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.connectsy.data.DataManager;
import com.connectsy.users.Register;

public class Launcher extends Activity {
	
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        
        if (DataManager.getCache(this).getString("token", null) == null){
        	startActivity(new Intent(this, Register.class));
        	this.finish();
        }else{
        	//start the notification service
    		Intent i = new Intent();
    		i.setAction("com.connectsy.START_NOTIFICATIONS");
    		startService(i);
    		
        	startActivity(new Intent(this, Dashboard.class));
        	this.finish();
        }
    }
}