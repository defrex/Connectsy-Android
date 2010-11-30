package com.connectsy2.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReciever extends BroadcastReceiver {

	@SuppressWarnings("unused")
	private static final String TAG = "NotificationBroadcastReciever";

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent();
		i.setAction("com.connectsy2.START_NOTIFICATIONS");
		context.startService(i);
	}

}
