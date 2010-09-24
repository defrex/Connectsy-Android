package com.connectsy.notifications;

import android.os.Binder;

public class NotificationServiceBinder extends Binder {
	NotificationListener listener;

	public NotificationServiceBinder(NotificationListener listener){
		this.listener = listener;
	}
	
	public NotificationListener getListener() {
		return listener;
	}
}
