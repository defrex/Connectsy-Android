package com.connectsy.users;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.connectsy.Launcher;

public class Logout extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor dataEditor = getSharedPreferences("consy", 0).edit(); 
        dataEditor.remove("authed");
        dataEditor.remove("token");
        dataEditor.commit();
        
        Log.d("auth", "logged out, starting Launcher");
        startActivity(new Intent(this, Launcher.class));
        this.finish();
    }
}