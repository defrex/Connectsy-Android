package com.connectsy2.users;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy2.R;
import com.connectsy2.data.ApiRequest;
import com.connectsy2.data.ApiRequest.ApiRequestListener;
import com.connectsy2.data.ApiRequest.Method;

public class UserSearch extends Activity implements OnClickListener, 
		ApiRequestListener{
	@SuppressWarnings("unused")
	private static final String TAG = "UserSearch";
	private static final int SEARCH_USERS = 2;
	UserAdapter adapter;
	String lastResponse;
	boolean canRequest = true;
	boolean requestPending = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search);
        
        findViewById(R.id.ab_user_search).setOnClickListener(this);
        ((ListView)findViewById(R.id.user_search_results))
        		.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> av, View v,
					int position, long id) {
				String username = (String) av.getAdapter().getItem(position);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy2.user.username", username);
				startActivity(i);
			}
        });
    }

    private void updateDisplay(String response){
    	if (response != null) lastResponse = response;
		try {
			ArrayList<String> usernames = new ArrayList<String>();
			JSONArray usersJson = new JSONArray(lastResponse);
			for (int i=0;i<usersJson.length();i++)
				usernames.add(usersJson.getString(i));

			ListView result_list = (ListView)findViewById(R.id.user_search_results);
			if (adapter == null){
				adapter = new UserAdapter(this, usernames);
				result_list.setAdapter(adapter);
			}else{
	        	adapter.update(usernames);
			}
			//Utils.setFooterView(this, result_list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private void doSearch(){
        EditText search = (EditText)findViewById(R.id.user_search_box);
        String q = search.getText().toString().toLowerCase();
        if (!q.equals("")){
			requestPending = true;
			setRefreshing(true);
			ApiRequest r = new ApiRequest(this, this, Method.GET, "/users/", 
					true, SEARCH_USERS);
			r.addGetArg("q", q);
			r.execute();
        }
    }
    
	public void onClick(View v) {
		doSearch();
	}
	
    private void setRefreshing(boolean on) {
    	if (on){
	        findViewById(R.id.ab_user_search).setVisibility(View.GONE);
	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
    	}else{
	        findViewById(R.id.ab_user_search).setVisibility(View.VISIBLE);
	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
    	}
    }

	public void onApiRequestFinish(int status, String response, int code) {
		requestPending = false;
		setRefreshing(false);
		updateDisplay(response);
	}
	
	public void onApiRequestError(int httpStatus, String response, int retCode) {}
}
