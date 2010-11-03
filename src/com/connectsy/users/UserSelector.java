package com.connectsy.users;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.users.ContactCursor.Contact;
import com.connectsy.users.UserManager.User;

public class UserSelector extends Activity implements OnItemClickListener, 
		OnClickListener, DataUpdateListener {
	@SuppressWarnings("unused")
	private static String TAG = "UserSelector";
	UserSelectionAdapter adapter;
	UserManager manager;
    private ArrayList<User> chosenUsers;
    private ArrayList<Contact> chosenContacts;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selector);
        
        manager = new UserManager(this, this, UserManager.currentUsername(this));
        Button done = (Button)findViewById(R.id.user_select_done);
        done.setOnClickListener(this);
        
        Bundle e = getIntent().getExtras();
        if (e != null){
			try {
				if (e.containsKey("com.connectsy.users"))
					chosenUsers = User.deserializeList(
							e.getString("com.connectsy.users"));
				if (e.containsKey("com.connectsy.contacts"))
					chosenContacts = Contact.deserializeList(
							e.getString("com.connectsy.contacts"));
		        
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
        }
		
        update();
    }

	public void onItemClick(AdapterView<?> adapterView, View itemView, 
			int position, long id) {}

	private void update(){
        ArrayList<User> users = manager.getFriends(false, true);
        if (users != null){
            adapter = new UserSelectionAdapter(this, users);
            if (chosenUsers != null)
            	adapter.setSelectedFriends(chosenUsers);
            if (chosenContacts != null)
            	adapter.setSelectedContacts(chosenContacts);
            ListView lv = (ListView)findViewById(R.id.user_list);
            lv.setOnItemClickListener(this);
            lv.setAdapter(adapter);
        }else{ 
        	manager.refreshFriends(false, 0);
        	findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
        }
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.user_select_done){
			String users = User.serializeList(adapter.getSelectedFriends());
			String contacts = Contact.serializeList(adapter.getSelectedContacts());
			Intent i = new Intent();
			i.putExtra("com.connectsy.users", users);
			i.putExtra("com.connectsy.contacts",contacts);
			setResult(RESULT_OK, i);
			finish();
		}
	}

	public String serializeContacts(
			ArrayList<HashMap<String, Object>> contacts) {
		JSONArray jsonContacts = new JSONArray();
		for (HashMap<String, Object> contact: contacts)
			jsonContacts.put(new JSONObject(contact));
		return jsonContacts.toString();
	}

	public void onDataUpdate(int code, String response) {
		update();
    	findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
	}

	public void onRemoteError(int httpStatus, String response, int code) {}
}
