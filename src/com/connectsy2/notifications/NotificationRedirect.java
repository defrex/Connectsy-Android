package com.connectsy2.notifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class NotificationRedirect extends Activity implements ServiceConnection {
	Intent next;
	String from;
	
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Bundle e = getIntent().getExtras();
        next = (Intent) e.get("next_intent");
        from = e.getString("from");

		Intent service = new Intent();
		service.setAction("com.connectsy2.START_NOTIFICATIONS");
		bindService(service, this, BIND_AUTO_CREATE);
    }

	public void onServiceConnected(ComponentName name, IBinder service) {
		((NotificationServiceBinder)service).clearPendingNotifications();
		this.startActivity(next);
		this.finish();
	}

	public void onServiceDisconnected(ComponentName name) {}
    
    public static Intent wrapIntent(Context context, Intent intent, 
    		String notificationHandler){
    	Intent i = new Intent(context, NotificationRedirect.class);
    	i.putExtra("next_intent", intent);
    	i.putExtra("from", notificationHandler);
    	return i;
    }

	@Override
	protected void onDestroy() {
		unbindService(this);
		super.onDestroy();
	}
}
