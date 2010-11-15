package com.connectsy.events;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.utils.DateUtils;

public class EventRenderer implements DataUpdateListener {
	@SuppressWarnings("unused")
	private static final String TAG = "EventRenderer";
	private EventManager evMan;
	Context context;
	View view;
	String rev;
	boolean truncate;
	Event event;
	
	public EventRenderer(Context context, View view, String rev, boolean truncate){
		evMan = new EventManager(context, this, null, null);
		this.context = context;
		this.view = view;
		this.rev = rev;
		this.truncate = truncate;
		event = evMan.getEvent(rev);
		if (event == null){
			evMan.refreshEvent(rev, 0);
		}else{
			render();
		}
	}
	
	private void render(){

		final OnClickListener userClick = new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", event.creator);
				context.startActivity(i);
			}
		};

		View loading = view.findViewById(R.id.event_loading);
		if (loading != null) loading.setVisibility(View.GONE);
		
		ImageView avatar = (ImageView) view.findViewById(R.id.event_avatar);
		avatar.setOnClickListener(userClick);
		new AvatarFetcher(event.creator, avatar, false);

		TextView username = (TextView) view.findViewById(R.id.event_username);
		username.setText(event.creator);
		username.setOnClickListener(userClick);

		TextView what = (TextView) view.findViewById(R.id.event_what);
		if (event.what == null)
			Log.d(TAG, "event.what null");
		else if (what == null)
			Log.d(TAG, "wat textview null");
		else
			what.setText(event.what);
		
		if (!truncate){
			TextView where = (TextView) view.findViewById(R.id.event_where);
			TextView when = (TextView) view.findViewById(R.id.event_when);
			if (event.where != null)
				where.setText(Html.fromHtml("<b>where:</b> "+ event.where));
//				where.setText("where: "+ event.where);
			else
				where.setVisibility(TextView.GONE);
			if (event.when != 0)
//				when.setText("when: "+ DateUtils.formatTimestamp(event.when));
				when.setText(Html.fromHtml("<b>when:</b> "
						+ DateUtils.formatTimestamp(event.when)));
			else
				when.setVisibility(TextView.GONE);
		}
		TextView created = (TextView) view.findViewById(R.id.event_created);
		created.setText("Created " + DateUtils.formatTimestamp(event.created));
	}

	public void onDataUpdate(int code, String response) {
		event = evMan.getEvent(rev);
		render();
	}

	public void onRemoteError(int httpStatus, String response, int code) {}
}
