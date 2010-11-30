package com.connectsy2.settings;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.connectsy2.R;
import com.connectsy2.data.Analytics;
import com.connectsy2.data.AvatarFetcher;
import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.users.UserManager;

public class Preferences extends PreferenceActivity implements DataUpdateListener {

	@SuppressWarnings("unused")
	private static final String TAG = "Preferances";
	private static final int SELECT_AVATAR = 0;
	private static final int UPLOAD_AVATAR = 1;
	private static final int SELECT_TONE = 2;
	private static final int CHANGE_PASSWORD = 3;
	private ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        Analytics.pageView(this, this.getClass().getName());
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, 
			Preference preference){
		boolean ret = super.onPreferenceTreeClick(preferenceScreen, preference);
		String key = preference.getKey();
		if (key == null) return ret;
		if (key.equals("avatar")){
			startActivityForResult(new Intent(Intent.ACTION_PICK, 
					Images.Media.INTERNAL_CONTENT_URI), SELECT_AVATAR);
		}else if (key.equals("notifications")){
			Context c = getBaseContext();
			Intent i = new Intent();
			i.setAction("com.connectsy2.START_NOTIFICATIONS");
			if (PreferenceManager.getDefaultSharedPreferences(c)
					.getBoolean("notifications", true)){
				c.startService(i);
			}else{
				c.stopService(i);
			}
		}else if (key.equals("notification_sound_uri")){
			Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, 
					RingtoneManager.TYPE_NOTIFICATION);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, 
					"Select Tone");
			String uri = PreferenceManager.getDefaultSharedPreferences(this)
					.getString("notification_sound_uri", null);
			if (uri != null)
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, 
						Uri.parse(uri));
			this.startActivityForResult(intent, SELECT_TONE);
		}else if (key.equals("password")){
			showDialog(CHANGE_PASSWORD);
		}
//		else if (key.equals("social_twitter")){
//			OAuthSignpostClient client = new OAuthSignpostClient(
//					Settings.TWITTER_KEY, Settings.TWITTER_SECRET, "oob");
//	        Twitter jtwit = new Twitter("yourtwittername", client);
//	        URI url = client.authorizeUrl();
//	        startActivityForResult(new Intent())
//		}
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
	    }else if (requestCode == SELECT_TONE 
	    		&& resultCode == Activity.RESULT_OK) {
	    	Uri uri = data.getParcelableExtra(
	    			RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
	    	if (uri != null){
	    		SharedPreferences prefs = 
	    				PreferenceManager.getDefaultSharedPreferences(this);
	    		prefs.edit().putString("notification_sound_uri", 
	    				uri.toString()).commit();
	    	}
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}

	public void onDataUpdate(int code, String response) {
		if (loadingDialog != null) loadingDialog.dismiss();
		if (code == UPLOAD_AVATAR){
			AvatarFetcher.download(UserManager.currentUsername(this), null, true);
		}else if (code == CHANGE_PASSWORD){
			toast("Password Changed");
		}
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		if (loadingDialog != null) loadingDialog.dismiss();
		if (code == CHANGE_PASSWORD){
			try {
				JSONObject e = new JSONObject(response);
				if (e.getString("error").equals("INVALID_PASSWORD"))
					toast("Invalid password, please try again");
				else
					toast("Password Change Error: "+httpStatus);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case CHANGE_PASSWORD:
			final View v = LayoutInflater.from(this).inflate(
					R.layout.preferances_password, this.getListView(), false);
			
	    	dialog = new AlertDialog.Builder(this)
	    		.setView(v)
    	       	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	       		public void onClick(DialogInterface dialog, int id) {
    	       			dialog.cancel();}})
    	       	.setPositiveButton("Save", new DialogInterface.OnClickListener() {
    	       		public void onClick(DialogInterface dialog, int id) {
    	       			String oldPassword = ((EditText)v.findViewById(
    	       					R.id.preferances_password_old)).getText()
    	       					.toString();
    	       			if (oldPassword.equals("")){
    	       				toast("Current Password Required");
    	       				return;
    	       			}

    	       			String newPassword1 = ((EditText)v.findViewById(
    	       					R.id.preferances_password_new1)).getText()
    	       					.toString();
    	       			if (newPassword1.equals("")){
    	       				toast("New Password Required");
    	       				return;
    	       			}

    	       			String newPassword2 = ((EditText)v.findViewById(
    	       					R.id.preferances_password_new2)).getText()
    	       					.toString();
    	       			
    	       			if (!newPassword1.equals(newPassword2)){
    	       				toast("New Passwords Don't Match");
    	       				return;
    	       			}
    	       			
	       				try {
							new UserManager(Preferences.this, Preferences.this, 
									UserManager.currentUsername(Preferences.this))
									.changePassword(oldPassword, newPassword1, 
											CHANGE_PASSWORD);
		    				loadingDialog = ProgressDialog.show(Preferences.this, 
		    						"", "Changing Password...", true);
						} catch (IOException e) {
							e.printStackTrace();
						}
    	       		}
    	       	}).create();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	private void toast(String message){
		Toast t = Toast.makeText(this, message, 5000);
		t.setGravity(Gravity.TOP, 0, 20);
		t.show();
	}
}