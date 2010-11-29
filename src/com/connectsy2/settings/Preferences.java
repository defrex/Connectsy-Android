package com.connectsy2.settings;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;

import com.connectsy2.R;
import com.connectsy2.data.AvatarFetcher;
import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.users.UserManager;

public class Preferences extends PreferenceActivity implements DataUpdateListener {

	@SuppressWarnings("unused")
	private static final String TAG = "Preferances";
	private static final int SELECT_AVATAR = 0;
	private static final int UPLOAD_AVATAR = 1;
	private ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, 
			Preference preference){
		boolean ret = super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference.getKey().equals("avatar")){
			startActivityForResult(new Intent(Intent.ACTION_PICK, 
					Images.Media.INTERNAL_CONTENT_URI), SELECT_AVATAR);
		}else if (preference.getKey().equals("notifications")){
			Context c = getBaseContext();
			Intent i = new Intent();
			i.setAction("com.connectsy2.START_NOTIFICATIONS");
			if (PreferenceManager.getDefaultSharedPreferences(c)
					.getBoolean("notifications", true)){
				c.startService(i);
			}else{
				c.stopService(i);
			}
		}
		return ret;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
			Uri selectedImage = data.getData();
			try {
				new UserManager(this, this, UserManager.currentUsername(this))
						.uploadAvatar(selectedImage, UPLOAD_AVATAR);
				loadingDialog = ProgressDialog.show(this, "", 
						"Uploading Avatar...", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}

	public void onDataUpdate(int code, String response) {
        AvatarFetcher.download(UserManager.currentUsername(this), null, true);
		if (loadingDialog != null) loadingDialog.dismiss();
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		if (loadingDialog != null) loadingDialog.dismiss();
	}
}