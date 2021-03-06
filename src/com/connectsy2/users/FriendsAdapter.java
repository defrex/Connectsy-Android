package com.connectsy2.users;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.connectsy2.R;
import com.connectsy2.data.AvatarFetcher;
import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.users.UserManager.User;

public class FriendsAdapter extends BaseAdapter implements ListAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = "FriendsAdapter";
	Context context;
	ArrayList<Object> friends;
	DataUpdateListener listener;
	int returnCode;
	
	public FriendsAdapter(Context context,  DataUpdateListener listener, 
			ArrayList<Object> friends, int returnCode){
		this.context = context;
		this.friends = friends;
		this.listener = listener;
		this.returnCode = returnCode;
	}
	
	public int getCount() {
		return friends.size();
	}

	public Object getItem(int position) {
		return friends.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		
		if (obj instanceof String){
			String header = (String) obj;
			View view = inflater.inflate(R.layout.user_list_header, parent, false);
			((TextView)view.findViewById(R.id.user_list_header_text))
					.setText(header);
			return view;
		}
		
		View view = inflater.inflate(R.layout.user_list_item, parent, false);
		final User user = (User) obj;
		
        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
        username.setText(user.username);
        username.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy2.user.username", user.username);
	    		context.startActivity(i);
			}
        });
        
        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
        AvatarFetcher.download(user.username, avatar, false);
        
        return view;
	}

	public void update(ArrayList<Object> friends) {
		this.friends = friends;
	}

}
