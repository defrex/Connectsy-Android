package com.connectsy.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {
	NotificationListener listener;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		listener = NotificationListener.getInstance();
		listener.start(this);
	}
	
	@Override
	public void onDestroy() {
		listener.stop();
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
