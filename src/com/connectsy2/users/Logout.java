package com.connectsy2.users;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.connectsy2.data.Analytics;
import com.connectsy2.data.DataManager;

public class Logout extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.pageView(this, this.getClass().getName());
        DataManager.cleanCache(this);
        
        //stop notification service
        Intent i = new Intent();
		i.setAction("com.connectsy2.STOP_NOTIFICATIONS");
		stopService(i);
        
        startActivity(new Intent(this, Login.class));
        this.finish();
    }
}