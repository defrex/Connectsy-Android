package com.connectsy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.connectsy.data.DataManager;
import com.connectsy.users.Login;

public class Launcher extends Activity {
	static final int AUTHENTICATE_USER = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences data = DataManager.getCache(this);
        boolean authed = data.getBoolean("authed", false);
        if (!authed){
        	startActivityForResult(new Intent(this, Login.class), AUTHENTICATE_USER);
        }else{
        	//start the notification service
    		Intent i = new Intent();
    		i.setAction("com.connectsy.START_NOTIFICATIONS");
    		startService(i);
    		
        	startActivity(new Intent(this, Dashboard.class));
        	this.finish();
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == AUTHENTICATE_USER) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor dataEditor = DataManager.getCache(this).edit(); 
                dataEditor.putBoolean("authed", true);
                dataEditor.commit();
            	startActivity(new Intent(this, Dashboard.class));
            }
        }
        this.finish();
    }
}