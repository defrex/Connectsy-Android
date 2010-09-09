package com.connectsy.users;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.connectsy.R;
import com.connectsy.users.UserManager.User;

public class UserSelector extends Activity implements OnItemClickListener, OnClickListener {
	private String TAG = "UserSelector";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selector);
        
        Intent i = getIntent();
        ArrayList<User> users = null;
        if (i.hasExtra("com.connectsy.users")){
			try {
				Log.d(TAG, i.getExtras().getString("com.connectsy.users"));
				users = User.deserializeList(i.getExtras().getString("com.connectsy.users"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
        UserAdapter adapter = new UserAdapter(this, R.layout.user_list_item, users, true);
        ListView lv = (ListView)findViewById(R.id.user_list);
        lv.setOnItemClickListener(this);
        lv.setAdapter(adapter);
        
        Button done = (Button)findViewById(R.id.user_select_done);
        done.setOnClickListener(this);
    }

	public void onItemClick(AdapterView<?> adapterView, View itemView, int position, long id) {
		
	}

	public void onClick(View v) {
		if (v.getId() == R.id.user_select_done){
			
		}
	}
}
