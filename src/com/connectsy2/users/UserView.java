package com.connectsy2.users;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy2.ActionBarHandler;
import com.connectsy2.R;
import com.connectsy2.data.AvatarFetcher;
import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.events.EventManager;
import com.connectsy2.events.EventsAdapter;
import com.connectsy2.events.EventManager.Filter;
import com.connectsy2.settings.MainMenu;
import com.connectsy2.users.UserManager.User;

public class UserView extends Activity implements OnClickListener, DataUpdateListener {
	@SuppressWarnings("unused")
	private static final String TAG = "UserView";
    private String curUsername;
    private UserManager userManager;
    private User user;
    private String username;
    private String tabSelected = "events";
    private int operationsPending = 0;
	private UserAdapter followerAdapter;
	private EventsAdapter eventsAdapter;
	private UserAdapter followingAdapter;
	private ProgressDialog loadingDialog;
    private static final int REFRESH_USER = 0;
    private static final int SELECT_AVATAR = 1;
    private static final int FOLLOW = 2;
	private static final int UNFOLLOW = 9;
    private static final int REFRESH_FOLLOWERS = 3;
    private static final int REFRESH_FOLLOWING = 4;
	private static final int UPLOAD_AVATAR = 7;
	private static final int REFRESH_EVENTS = 8;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_view);

        //set up logo clicks
        new ActionBarHandler(this);
        
        ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
        abRefresh.setOnClickListener(this);
        ImageView abSearch = (ImageView)findViewById(R.id.ab_user_search);
        abSearch.setOnClickListener(this);
        
        Bundle e = getIntent().getExtras();
        username = e.getString("com.connectsy2.user.username");
        curUsername = UserManager.currentUsername(this);

        findViewById(R.id.user_view_events_button).setOnClickListener(this);
        findViewById(R.id.user_view_followers_button).setOnClickListener(this);
        findViewById(R.id.user_view_following_button).setOnClickListener(this);
        findViewById(R.id.user_view_follow).setOnClickListener(this);
        findViewById(R.id.user_view_unfollow).setOnClickListener(this);
        ListView events = (ListView)findViewById(R.id.user_view_events);
        events.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> eventsView, View eventView, 
					int position, long id) {
				String rev = (String) eventsView.getAdapter().getItem(position);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.event");
				i.putExtra("com.connectsy2.events.revision", rev);
				startActivity(i);
			}
        });
        
        OnItemClickListener userClickListener = new OnItemClickListener(){
			public void onItemClick(AdapterView<?> view, View itemView, 
					int position, long id) {
				String username = (String) view.getAdapter().getItem(position);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy2.user.username", username);
				startActivity(i);
			}
        };
        ((ListView) findViewById(R.id.user_view_followers))
				.setOnItemClickListener(userClickListener);
        ((ListView) findViewById(R.id.user_view_following))
				.setOnItemClickListener(userClickListener);

		user = getUserManager().getUser();
		updateUser();
		if (e.containsKey("com.connectsy2.user.tab"))
			updateTab(e.getString("com.connectsy2.user.tab"));
		else
			updateTab(null);
    	refresh();
    }
	
    private void updateUser(){
        TextView uname = (TextView)findViewById(R.id.user_view_username);
        uname.setText(username);
        
        ImageView avatar = (ImageView)findViewById(R.id.user_view_avatar);
        AvatarFetcher.download(username, avatar, false);
        if (username.equals(curUsername)){
        	avatar.setClickable(true);
        	avatar.setOnClickListener(this);
        }
		
        if (user != null && !username.equals(curUsername)){
    		ImageView f = (ImageView)findViewById(R.id.user_view_follow);
    		ImageView unf = (ImageView)findViewById(R.id.user_view_unfollow);
        	if (user.following){
        		unf.setVisibility(Button.VISIBLE);
        		f.setVisibility(Button.GONE);
        	}else{
        		f.setVisibility(Button.VISIBLE);
        		unf.setVisibility(Button.GONE);
        	}
        }
    }
    
    private void updateFollowers(){
		ArrayList<String> followers = getUserManager().getFollowers();
		if (followers == null) return;
        
        if (followerAdapter != null){
        	followerAdapter.update(followers);
        }else{
	        followerAdapter = new UserAdapter(this, followers);
	        ((ListView) findViewById(R.id.user_view_followers))
	        		.setAdapter(followerAdapter);
        }
		//Utils.setFooterView(this, lv);
    }
    
    private void updateFollowing(){
		ArrayList<String> following = getUserManager().getFollowing();
		if (following == null) return;
        
        if (followingAdapter != null){
        	followingAdapter.update(following);
        }else{
	        followingAdapter = new UserAdapter(this, following);
	        ((ListView) findViewById(R.id.user_view_following))
    			.setAdapter(followingAdapter);
        }
		//Utils.setFooterView(this, lv);
    }
    
    private void updateEvents(){
		ArrayList<String> revs = new EventManager(this, this, Filter.CREATED, 
				username).getRevisions();
		if (revs == null) return;
		
        if (eventsAdapter != null){
        	eventsAdapter.clear();
        	for (String rev: revs)
        		eventsAdapter.add(rev);
        	eventsAdapter.notifyDataSetChanged();
        }else{
        	eventsAdapter = new EventsAdapter(this, 
        			R.layout.event_list_item, revs);
	        ((ListView) findViewById(R.id.user_view_events))
    		.setAdapter(eventsAdapter);
        }
		//Utils.setFooterView(this, lv);
    }
    
    private void updateTab(String tab){
    	if (tab != null) tabSelected = tab;
    	if (tabSelected.equals("events")){
			findViewById(R.id.user_view_events_button).setSelected(true);
			findViewById(R.id.user_view_followers_button).setSelected(false);
			findViewById(R.id.user_view_following_button).setSelected(false);

			findViewById(R.id.user_view_events).setVisibility(View.VISIBLE);
	        findViewById(R.id.user_view_followers).setVisibility(View.GONE);
	        findViewById(R.id.user_view_following).setVisibility(View.GONE);
    	}else if (tabSelected.equals("followers")){
			findViewById(R.id.user_view_events_button).setSelected(false);
			findViewById(R.id.user_view_followers_button).setSelected(true);
			findViewById(R.id.user_view_following_button).setSelected(false);

			findViewById(R.id.user_view_events).setVisibility(View.GONE);
	        findViewById(R.id.user_view_followers).setVisibility(View.VISIBLE);
	        findViewById(R.id.user_view_following).setVisibility(View.GONE);
    	}else if (tabSelected.equals("following")){
			findViewById(R.id.user_view_events_button).setSelected(false);
			findViewById(R.id.user_view_followers_button).setSelected(false);
			findViewById(R.id.user_view_following_button).setSelected(true);

			findViewById(R.id.user_view_events).setVisibility(View.GONE);
	        findViewById(R.id.user_view_followers).setVisibility(View.GONE);
	        findViewById(R.id.user_view_following).setVisibility(View.VISIBLE);
    	}
    }
    
	private void changeAvatar(){
		startActivityForResult(new Intent(Intent.ACTION_PICK, 
				Images.Media.INTERNAL_CONTENT_URI), SELECT_AVATAR);
	}
	
	private void follow(){
		getUserManager(true).follow(FOLLOW);
//        loadingDialog = ProgressDialog.show(this, "", "Following...", true);
		operationsPending++;
		setRefreshing(true);
	}
	
	private void unfollow(){
		getUserManager(true).unfollow(UNFOLLOW);
//        loadingDialog = ProgressDialog.show(this, "", "Unfollowing...", true);
		operationsPending++;
		setRefreshing(true);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
			Uri selectedImage = data.getData();
			try {
				getUserManager(true).uploadAvatar(selectedImage, UPLOAD_AVATAR);
				operationsPending++;
				setRefreshing(true);
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
    	else if (v.getId() == R.id.ab_user_search) 
    		startActivity(new Intent(this, UserSearch.class));
    	else if (v.getId() == R.id.user_view_avatar) 
    		changeAvatar();
    	else if (v.getId() == R.id.user_view_follow) 
    		follow();
    	else if (v.getId() == R.id.user_view_unfollow) 
    		unfollow();
    	else if (v.getId() == R.id.user_view_events_button) 
    		updateTab("events");
    	else if (v.getId() == R.id.user_view_followers_button) 
    		updateTab("followers");
    	else if (v.getId() == R.id.user_view_following_button) 
    		updateTab("following");
	}
	
	private void refresh(){
		getUserManager(true).refreshUser(REFRESH_USER);
		getUserManager(true).refreshFollowers(REFRESH_FOLLOWERS);
		getUserManager(true).refreshFollowing(REFRESH_FOLLOWING);
		new EventManager(this, this, Filter.CREATED, username)
				.refreshRevisions(REFRESH_EVENTS);
		operationsPending += 4;
		setRefreshing(true);
	}

	public void onDataUpdate(int code, String response) {
		if (code == REFRESH_USER){
			user = getUserManager().getUser();
			updateUser();
		}else if (code == REFRESH_EVENTS){
			updateEvents();
		}else if (code == REFRESH_FOLLOWERS){
			updateFollowers();
		}else if (code == REFRESH_FOLLOWING){
			updateFollowing();
		}else if (code == FOLLOW){
			findViewById(R.id.user_view_follow).setVisibility(View.GONE);
//			loadingDialog.dismiss();
			Toast t = Toast.makeText(this, "Following", 2000);
			t.setGravity(Gravity.TOP, 0, 60);
			t.show();
			getUserManager(true).refreshUser(REFRESH_USER);
			getUserManager(true).refreshFollowers(REFRESH_FOLLOWERS);
			operationsPending += 2;
		}else if (code == UNFOLLOW){
			findViewById(R.id.user_view_unfollow).setVisibility(View.GONE);
//			loadingDialog.dismiss();
			Toast t = Toast.makeText(this, "Not Following", 2000);
			t.setGravity(Gravity.TOP, 0, 60);
			t.show();
			getUserManager(true).refreshUser(REFRESH_USER);
			getUserManager(true).refreshFollowers(REFRESH_FOLLOWERS);
			operationsPending += 2;
		}else if (code == UPLOAD_AVATAR){
	        ImageView avatar = (ImageView)findViewById(R.id.user_view_avatar);
	        AvatarFetcher.download(username, avatar, true);
			new EventManager(this, this, Filter.CREATED, username)
					.refreshRevisions(REFRESH_EVENTS);
			operationsPending++;
		}
		operationsPending--;
		if (operationsPending == 0){
			setRefreshing(false);
		}
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		if (loadingDialog != null) loadingDialog.dismiss();
		operationsPending--;
		if (operationsPending == 0)
			setRefreshing(false);
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

	private UserManager getUserManager(){
		return getUserManager(false); }
	private UserManager getUserManager(boolean forceNew){
		if (userManager == null || forceNew)
			userManager = new UserManager(this, this, username);
		return userManager;
	}
}
