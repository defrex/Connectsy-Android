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
import com.connectsy.settings.Settings;
import com.connectsy.users.UserManager.User;
import com.wilson.android.library.DrawableManager;

public class UserAdapter extends ArrayAdapter<User> {
	
	public UserAdapter(Context context, int viewResourceId,
			ArrayList<User> users) {
		super(context, viewResourceId, users);
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final User user = getItem(position);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.user_list_item, parent, false);
        
        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
        username.setText(user.username);
        username.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", user.username);
	    		context.startActivity(i);
			}
        });
        
        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
        DrawableManager dm = new DrawableManager();
        String avyUrl = Settings.API_DOMAIN+"/users/"+user.username+"/avatar/";
        dm.fetchDrawableOnThread(avyUrl, avatar);
        
        return view;
	}
}
