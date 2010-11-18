package com.connectsy.users;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;

public class UserSelector extends Activity implements OnClickListener, 
		DataUpdateListener {
	@SuppressWarnings("unused")
	private static String TAG = "UserSelector";
	private UserSelectionAdapter adapter;
	private UserManager manager;
	private ArrayList<String> selected;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selector);
        
        manager = new UserManager(this, this, UserManager.currentUsername(this));
        findViewById(R.id.user_select_done).setOnClickListener(this);
        
        Bundle e = getIntent().getExtras();
        if (e != null && e.containsKey("com.connectsy.users"))
			selected = deserializeUsers(e.getString("com.connectsy.users"));
		
        update();
    }

	private void update(){
        ArrayList<String> users = manager.getFollowers();
        if (users != null){
            adapter = new UserSelectionAdapter(this, R.layout.user_list_item, 
            		users);
            if (selected != null)
            	adapter.setSelected(selected);
            ListView lv = (ListView)findViewById(R.id.user_list);
            lv.setAdapter(adapter);
        }else{ 
        	manager.refreshFollowers(0);
        	findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
        }
	}

	public void onClick(View v) {
		if (v.getId() == R.id.user_select_done){
			Intent i = new Intent();
			String users = serializeUsers(adapter.getSelected());
			i.putExtra("com.connectsy.users", users);
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
	
	public static String serializeUsers(ArrayList<String> usernames){
		return new JSONArray(usernames).toString();
	}
	
	public static ArrayList<String> deserializeUsers(String usernames){
		JSONArray json;
		ArrayList<String> ret = new ArrayList<String>();
		try {
			json = new JSONArray(usernames);
			for (int i=0;i<json.length();i++)
				ret.add(json.getString(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
