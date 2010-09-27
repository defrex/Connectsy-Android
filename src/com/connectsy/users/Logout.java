package com.connectsy.users;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.connectsy.Launcher;
import com.connectsy.data.DataManager;

public class Logout extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager.cleanCache(this);
        
        //stop notification service
        Intent i = new Intent();
		i.setAction("com.connectsy.STOP_NOTIFICATIONS");
		stopService(i);
        
        Log.d("auth", "logged out, starting Launcher");
        startActivity(new Intent(this, Launcher.class));
        this.finish();
    }
}