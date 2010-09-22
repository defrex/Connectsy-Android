package com.connectsy.settings;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.users.UserManager;

public class Preferences extends PreferenceActivity implements DataUpdateListener {

	@SuppressWarnings("unused")
	private static final String TAG = "Preferances";
	private static final int SELECT_AVATAR = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, 
			Preference preference){
		if (preference.getKey().equals("avatar"))
			startActivityForResult(new Intent(Intent.ACTION_PICK, 
					Images.Media.INTERNAL_CONTENT_URI), SELECT_AVATAR);
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
			Uri selectedImage = data.getData();
			try {
				new UserManager(this, this, UserManager.currentUsername(this))
						.uploadAvatar(selectedImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}

	public void onDataUpdate(int code, String response) {
		// TODO Auto-generated method stub
		
	}

	public void onRemoteError(int httpStatus, int code) {
		// TODO Auto-generated method stub
		
	}
}