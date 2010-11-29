package com.connectsy2.events.attendants;

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

import com.connectsy2.R;
import com.connectsy2.data.AvatarFetcher;
import com.connectsy2.events.attendants.AttendantManager.Attendant;

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
				i.putExtra("com.connectsy2.user.username", att.username);
	    		context.startActivity(i);
			}
        };
		
        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
        if (att.username != null){
	        username.setText(att.username);
	        username.setOnClickListener(userClick);
        }else if (att.display_name != null){
	        username.setText(att.display_name);
        }
        
        Resources r = context.getResources();
        String status_text = r.getString(r.getIdentifier(
        		"string/attendant_status_"+Integer.toString(att.status), null, 
        		context.getPackageName()));
        ((TextView)view.findViewById(R.id.user_list_item_detail)).setText(status_text);
        
        if (att.username != null){
	        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
	        avatar.setOnClickListener(userClick);
	        AvatarFetcher.download(att.username, avatar, false);
        }
		return view;
	}
}
