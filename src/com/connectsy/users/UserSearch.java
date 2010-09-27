package com.connectsy.users;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy.R;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.users.UserManager.User;
import com.connectsy.utils.Utils;

public class UserSearch extends Activity implements OnClickListener, 
		ApiRequestListener, DataUpdateListener{
	private static final int REFRESH_USER = 1;
	private static final int SEARCH_USERS = 2;
	@SuppressWarnings("unused")
	private static final String TAG = "UserSearch";
	UserAdapter adapter;
	String lastResponse;
	boolean canRequest = true;
	boolean requestPending = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search);
        
        ImageView search = (ImageView)findViewById(R.id.ab_user_search);
        search.setOnClickListener(this);

		ListView resultList = (ListView)findViewById(R.id.user_search_results);
		resultList.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> listView, View userView, 
					int position, long id) {
				User user = (User) listView.getAdapter().getItem(position);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", user.username);
				startActivity(i);
			}
        });
//        EditText box = (EditText) findViewById(R.id.user_search_box);
//        box.addTextChangedListener(new TextWatcher(){
//			public void afterTextChanged(Editable s) {
//				if (canRequest){
//					if (!requestPending) doSearch();
//					canRequest = false;
//					new Handler().postDelayed(new Runnable() {
//						public void run() {
//							Log.d(TAG, "setting canRequest");
//							canRequest = true;
//						}
//					}, 500);
//				}
//			}
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//			public void onTextChanged(CharSequence s, int start, int before, int count) {}
//        });
    }

    private void updateDisplay(String response){
    	if (response != null) lastResponse = response;
		try {
			ArrayList<User> users = new ArrayList<User>();
			JSONArray usersJson = new JSONObject(lastResponse)
					.getJSONArray("results");
			for (int i=0;i<usersJson.length();i++){
				JSONObject userJson = usersJson.getJSONObject(i);
				User u = manager(userJson.getString("username")).getUser();
				if (u == null){
					manager(userJson.getString("username")).refreshUser(REFRESH_USER);
				}else{
					users.add(u);
				}
			}

			ListView result_list = (ListView)findViewById(R.id.user_search_results);
			if (adapter == null){
				adapter = new UserAdapter(this, R.id.user_search_results, users, false);
				result_list.setAdapter(adapter);
			}else{
	        	adapter.clear();
	        	for (int i = 0;i < users.size();i++)
	        		adapter.add(users.get(i));
	    		adapter.notifyDataSetChanged();
			}
			Utils.setFooterView(this, result_list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private void doSearch(){
        EditText search = (EditText)findViewById(R.id.user_search_box);
        String q = search.getText().toString();
        if (!q.equals("")){
			requestPending = true;
			ApiRequest r = new ApiRequest(this, this, Method.GET, "/users/", 
					true, SEARCH_USERS);
			r.addGetArg("q", q);
			r.execute();
        }
    }
    
	public void onClick(View v) {
		doSearch();
	}

	public void onApiRequestFinish(int status, String response, int code) {
		requestPending = false;
		updateDisplay(response);
	}

	public void onDataUpdate(int code, String response) {
		updateDisplay(null);
	}

	private UserManager manager(String username){
		return new UserManager(this, this, username);
	}

	public void onApiRequestError(int httpStatus, int retCode) {}
	public void onRemoteError(int httpStatus, int code) {}
}
