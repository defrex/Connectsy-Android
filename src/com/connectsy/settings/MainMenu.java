package com.connectsy.settings;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.connectsy.R;
import com.connectsy.users.Logout;

public class MainMenu {
	public static final int MENU_SETTINGS = 0;
	public static final int MENU_LOGOUT = 1;
	
	public static boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MainMenu.MENU_SETTINGS, 0, R.string.menu_settings)
			.setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, MainMenu.MENU_LOGOUT, 0, R.string.menu_logout)
			.setIcon(android.R.drawable.ic_lock_power_off);
        return true;
	}
	
    public static boolean onOptionsItemSelected(Activity a, MenuItem item) {
        switch (item.getItemId()) {
        case MainMenu.MENU_SETTINGS:
        	a.startActivity(new Intent(a, Preferences.class));
        	return true;
        case MainMenu.MENU_LOGOUT:
        	a.startActivity(new Intent(a, Logout.class));
        	a.finish();
        	return true;
        }
        return false;
    }
}
