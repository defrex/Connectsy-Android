package com.connectsy.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupCatcher extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent();
		i.setAction("com.connectsy.START_NOTIFICATIONS");
		context.startService(i);
	}

}
