package com.connectsy.events.attendants;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.events.attendants.AttendantManager.Attendant;

public class AttendantsAdapter extends ArrayAdapter<Attendant> {
	@SuppressWarnings("unused")
	private final String TAG = "AttendantsCursorAdapter";
	
	public AttendantsAdapter(Context context, int viewResourceId,
			ArrayList<Attendant> objects) {
		super(context, viewResourceId, objects);
	}
 
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final Attendant att = getItem(position);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.attendant_list_item, parent, false);
		
		OnClickListener userClick = new View.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", att.username);
	    		context.startActivity(i);
			}
        };
		
        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
        username.setText(att.username);
        username.setOnClickListener(userClick);
        
        Resources r = context.getResources();
        String status_text = r.getString(r.getIdentifier(
        		"string/attendant_status_"+Integer.toString(att.status), null, 
        		context.getPackageName()));
        ((TextView)view.findViewById(R.id.user_list_item_detail)).setText(status_text);
        
        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
        avatar.setOnClickListener(userClick);
        new AvatarFetcher(att.username, avatar, false);
		return view;
	}
}
