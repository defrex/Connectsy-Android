package com.connectsy.events;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.events.AttendantManager.Attendant;
import com.connectsy.settings.Settings;
import com.wilson.android.library.DrawableManager;

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
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.attendant_list_item, parent, false);
		
        TextView username = (TextView)view.findViewById(R.id.attendant_list_item_username);
        username.setText(att.username);
        username.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", att.username);
	    		context.startActivity(i);
			}
        });
        
        Resources r = context.getResources();
        String status_text = r.getString(r.getIdentifier(
        		"string/attendant_status_"+Integer.toString(att.status), null, 
        		context.getPackageName()));
        TextView status = (TextView)view.findViewById(R.id.attendant_list_item_status);
        status.setText(status_text);
        
        ImageView avatar = (ImageView)view.findViewById(R.id.attendant_list_item_avatar);
        DrawableManager dm = new DrawableManager();
        String avyUrl = Settings.API_DOMAIN+"/users/"+att.username+"/avatar/";
        dm.fetchDrawableOnThread(avyUrl, avatar);
		return view;
	}

}
