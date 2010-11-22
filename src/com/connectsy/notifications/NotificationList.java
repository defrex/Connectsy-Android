package com.connectsy.notifications;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.data.DataManager;
import com.connectsy.notifications.NotificationHandler.NotificationContent;
import com.connectsy.notifications.NotificationHandler.NotificationContentListener;
import com.connectsy.utils.Utils;

public class NotificationList extends Activity {
	@SuppressWarnings("unused")
	private final String TAG = "NotificationList";
	private NotificationServiceBinder binder;
	private ArrayList<JSONObject> nots;
    
    private class NotificationAdapter extends ArrayAdapter<JSONObject>{

		private Context context;
		private int viewId;
		private HashMap<Integer, Intent> intents = new HashMap<Integer, Intent>();

		public NotificationAdapter(Context context, int textViewResourceId,
				ArrayList<JSONObject> notifications) {
			super(context, textViewResourceId, notifications);
			this.context = context;
			this.viewId = textViewResourceId;
		}

		@Override
		public View getView(final int position, View cView, ViewGroup parent) {
			final View view;
			if (cView == null){
				LayoutInflater inflater = LayoutInflater.from(context);
				view = inflater.inflate(viewId, parent, false);
			}else{
				view = cView;
			}
			
			try {
				binder.getContentForNotification(context, getItem(position), 
						new NotificationContentListener(){
					public void sendNotification(NotificationContent not) {
						((TextView) view.findViewById(
								R.id.notification_list_item_title))
								.setText(not.title);
						((TextView) view.findViewById(
								R.id.notification_list_item_body))
								.setText(Utils.maybeTruncate(not.body, 30));

				        ImageView avatar = (ImageView)view.findViewById(
				        		R.id.notification_list_item_avatar);
				        avatar.setImageResource(R.drawable.avatar_default);
				        if (not.username != null)
				        	AvatarFetcher.download(not.username, avatar, false);
				        
				        if (not.intent != null)
				        	intents.put(position, not.intent);
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return view;
		}
		
		public Intent getIntent(int position){
			if (intents.containsKey(position))
				return intents.get(position);
			else
				return null;
		}
    }
    
    private final Context context = this;
    private ServiceConnection serviceCon = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, 
        		IBinder service) {
            binder = (NotificationServiceBinder) service;
            
            nots.addAll(binder.getPendingNotifications());
            binder.clearPendingNotifications();
            
            ListView lv = (ListView) findViewById(R.id.notification_list);
            lv.setAdapter(new NotificationAdapter(context, 
            		R.layout.notification_list_item, nots));
            lv.setOnItemClickListener(new OnItemClickListener(){
    			public void onItemClick(AdapterView<?> adapterView, 
    					View view, int position, long id) {
    				Intent i = ((NotificationAdapter) adapterView.getAdapter())
    						.getIntent(position);
    				if (i != null)
    					startActivity(i);
    			}
            });
        }

		public void onServiceDisconnected(ComponentName name) {}
    };
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_list);
        
        nots = new ArrayList<JSONObject>();
        try {
			JSONArray jsonNots = new JSONArray(DataManager.getCache(this)
					.getString("notifications", "[]"));
			int num = 10;
			if (jsonNots.length() < num) num = jsonNots.length();
			for (int i=0;i<num;i++) nots.add(jsonNots.getJSONObject(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        bindService(new Intent(this, NotificationService.class),
        		serviceCon, BIND_AUTO_CREATE);
    }
    
    @Override
	protected void onDestroy() {
		unbindService(serviceCon);
//		DataManager.getCache(this).edit().putString("notifications", 
//				new JSONArray(nots).toString());
		super.onDestroy();
	}
}
