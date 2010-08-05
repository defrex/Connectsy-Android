package com.connectsy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.connectsy.events.EventList;
import com.connectsy.events.EventManager;
import com.connectsy.events.EventNew;

public class Dashboard extends Activity implements OnClickListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        ActionBarHandler abHandler = new ActionBarHandler(this);
        ImageView abProfile = (ImageView)findViewById(R.id.ab_profile);
        abProfile.setOnClickListener(abHandler);
        ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
        abNewEvent.setOnClickListener(abHandler);
        
        Button events_nearby = (Button)findViewById(R.id.dashboard_events_nearby);
        events_nearby.setOnClickListener(this);
        Button events_friends = (Button)findViewById(R.id.dashboard_events_friends);
        events_friends.setOnClickListener(this);
        Button events_category = (Button)findViewById(R.id.dashboard_events_category);
        events_category.setOnClickListener(this);
        Button events_new = (Button)findViewById(R.id.dashboard_events_new);
        events_new.setOnClickListener(this);
        Button profile = (Button)findViewById(R.id.dashboard_profile);
        profile.setOnClickListener(this);
        Button account = (Button)findViewById(R.id.dashboard_account);
        account.setOnClickListener(this);
    }

	public void onClick(View v) {
		if (v.getId() == R.id.dashboard_events_new){
    		startActivity(new Intent(this, EventNew.class));
    		return;
    	}else if (v.getId() == R.id.dashboard_profile){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.user");
			SharedPreferences data = getSharedPreferences("consy", 0);
			String username = data.getString("username", null);
			i.putExtra("com.connectsy.user.username", username);
			startActivity(i);
    		return;
    	}else if (v.getId() == R.id.dashboard_account){
    		return;
    	}
		
		Intent i = new Intent(this, EventList.class);
		
		if (v.getId() == R.id.dashboard_events_friends){
    		i.putExtra("filter", EventManager.Filter.FRIENDS);
    	}else if (v.getId() == R.id.dashboard_events_category){
    		i.putExtra("filter", EventManager.Filter.CATEGORY);
    		i.putExtra("category", "all");
    	}
		
		startActivity(i);
	}
}
