package com.connectsy.users;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;

public class UserAdapter extends ArrayAdapter<String> {
	private int viewId;
	
	public UserAdapter(Context context, ArrayList<String> usernames) {
		super(context, R.layout.user_list_item, usernames);
		viewId = R.layout.user_list_item;
	}

	public UserAdapter(Context context, int textViewResourceId, 
			ArrayList<String> usernames) {
		super(context, textViewResourceId, usernames);
		viewId = textViewResourceId;
	}
	
	public void update(ArrayList<String> usernames){
		setNotifyOnChange(false);
		clear();
		for (String username: usernames) 
			add(username);
		notifyDataSetChanged();
	}
	
	@Override
	public View getView (final int position, View view, ViewGroup parent) {
		final Context context = getContext();
		final String username = getItem(position);
		
		if (view == null){
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(viewId, parent, false);
		}
		
        TextView usernameField = (TextView)view.findViewById(
        		R.id.user_list_item_username);
        usernameField.setText(username);
        usernameField.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", username);
	    		context.startActivity(i);
			}
        });
        
        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
        avatar.setImageResource(R.drawable.avatar_default);
        AvatarFetcher.download(username, avatar, false);
        
        return view;
	}
}
