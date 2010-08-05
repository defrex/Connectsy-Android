package com.connectsy.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.connectsy.R;

public class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        Log.d("Preferences", android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
    }
}