package com.connectsy.users;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.connectsy.ActionBarHandler;
import com.connectsy.R;
import com.connectsy.data.DataManager;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.settings.MainMenu;
import com.connectsy.settings.Settings;
import com.connectsy.users.UserManager.User;
import com.wilson.android.library.DrawableManager;

public class UserView extends Activity implements OnClickListener, 
		DataUpdateListener, OnItemClickListener {
    @SuppressWarnings("unused")
	private static final String TAG = "UserView";
    private UserManager userManager;
    private User user;
    private String username;
    private UserAdapter adapter;
    private UserAdapter pendingAdapter;
    private int operationsPending = 0;
    private static final int REFRESH_USER = 0;
    private static final int SELECT_AVATAR = 1;
    private static final int BEFRIEND = 2;
    private static final int REFRESH_FRIENDS = 3;
    private static final int REFRESH_PENDING_FRIENDS = 4;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_view);

        ActionBarHandler abHandler = new ActionBarHandler(this);
        ImageView abProfile = (ImageView)findViewById(R.id.ab_profile);
        abProfile.setOnClickListener(abHandler);
        ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
        abNewEvent.setOnClickListener(abHandler);
        
        ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
        abRefresh.setOnClickListener(this);
        
        username = getIntent().getExtras().getString("com.connectsy.user.username");

    	userManager = new UserManager(this, this, username);
    	update();
    	refresh();
    }
	
	private void update(){
    	user = userManager.getUser();
    	
        TextView uname = (TextView)findViewById(R.id.user_view_username);
        uname.setText(username);
		
        ImageView avatar = (ImageView)findViewById(R.id.user_view_avatar);
        String avyUrl = Settings.API_DOMAIN+"/users/"+username+"/avatar/";
        new DrawableManager().fetchDrawableOnThread(avyUrl, avatar);
        if (username.equals(DataManager.getCache(this).getString("username", null))){
        	avatar.setClickable(true);
        	avatar.setOnClickListener(this);
        }
        
		if (user != null){
			ArrayList<User> friends = userManager.getFriends(false);
	        if (adapter != null){
	        	adapter.clear();
	        	for (int n = 0;n < friends.size();n++)
	        		adapter.add(friends.get(n));
	    		adapter.notifyDataSetChanged();
	        }else{
	            adapter = new UserAdapter(this, R.layout.user_list_item, friends);
	        }
			
	        ListView lv = (ListView)findViewById(R.id.friends_list);
	        lv.setOnItemClickListener(this);
	        lv.setAdapter(adapter);
	        
	        String curUser = UserManager.getCache(this).getString("username", "");
	        if (!user.username.equals(curUser)){
	        	boolean isFriend = false;
	        	for (int i = 0;i < friends.size();i++)
	        		if (friends.get(i).username.equals(curUser))
	        			isFriend = true;
	        	if (!isFriend){
	        		Button f = (Button)findViewById(R.id.user_view_befriend);
	        		f.setVisibility(Button.VISIBLE);
	        		f.setText("Become friends!");
	        		f.setOnClickListener(this);
	        	}
	        }else{
				ArrayList<User> pendingFriends = userManager.getFriends(true);
		        if (pendingAdapter != null){
		        	pendingAdapter.clear();
		        	for (int n = 0;n < pendingFriends.size();n++)
		        		pendingAdapter.add(pendingFriends.get(n));
		        	pendingAdapter.notifyDataSetChanged();
		        }else{
		        	pendingAdapter = new UserAdapter(this, R.layout.user_list_item, friends);
		        }
				
		        ListView plv = (ListView)findViewById(R.id.pending_friends_list);
		        plv.setOnItemClickListener(this);
		        plv.setAdapter(pendingAdapter);
	        }
		}
	}
    
	private void changeAvatar(){
		startActivityForResult(new Intent(Intent.ACTION_PICK, 
				Images.Media.INTERNAL_CONTENT_URI), SELECT_AVATAR);
	}
	
	private void befriend(){
		userManager.befriend(BEFRIEND);
		operationsPending++;
		setRefreshing(true);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
			Uri selectedImage = data.getData();
			try {
				userManager.uploadAvatar(selectedImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    } 
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }

	public void onClick(View v) {
    	if (v.getId() == R.id.ab_refresh) refresh();
    	if (v.getId() == R.id.user_view_avatar) changeAvatar();
    	if (v.getId() == R.id.user_view_befriend) befriend();
	}
	private void refresh(){
		userManager.refreshUser(REFRESH_USER);
		userManager.refreshFriends(false, REFRESH_FRIENDS);
		userManager.refreshFriends(true, REFRESH_PENDING_FRIENDS);
		operationsPending += 3;
		setRefreshing(true);
	}
	
    private void setRefreshing(boolean on) {
    	if (on){
	        findViewById(R.id.ab_refresh).setVisibility(View.GONE);
	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
    	}else{
	        findViewById(R.id.ab_refresh).setVisibility(View.VISIBLE);
	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
    	}
    }

	public void onDataUpdate(int code, String response) {
		if (code == BEFRIEND){
    		Button f = (Button)findViewById(R.id.user_view_befriend);
    		f.setVisibility(Button.VISIBLE);
		}
    	update();
		operationsPending--;
		if (operationsPending < 1){
			setRefreshing(false);
			operationsPending = 0;
		}
	}

	public void onRemoteError(int httpStatus, int code) {
		setRefreshing(false);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}
