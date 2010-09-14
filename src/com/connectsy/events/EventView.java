package com.connectsy.events;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.connectsy.data.AvatarFetcher;
import com.connectsy.data.DataManager;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.AttendantManager.Attendant;
import com.connectsy.events.AttendantManager.Status;
import com.connectsy.events.EventManager.Event;
import com.connectsy.settings.MainMenu;
import com.connectsy.utils.DateUtils;

public class EventView extends Activity implements DataUpdateListener, 
		OnClickListener, OnItemClickListener {
    @SuppressWarnings("unused")
	private static final String TAG = "EventView";
    
    private Event event;
    private String eventRev;
    private EventManager eventManager;
    private AttendantManager attManager;
    private AttendantsAdapter attAdapter;
    private String tabSelected;
    private Integer curUserStatus;
    private boolean pendingStatusChange = false;
    
    private int pendingOperations = 0;
    
    private final int REFRESH_EVENT = 0;
    private final int ATT_SET = 1;
    private final int REFRESH_ATTS = 2;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        ActionBarHandler abHandler = new ActionBarHandler(this);
        ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
        abNewEvent.setOnClickListener(abHandler);
        
        ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
        abRefresh.setOnClickListener(this);
    	Button comments = (Button)findViewById(R.id.event_view_comments);
    	comments.setOnClickListener(this);
    	comments.setSelected(true);
    	Button atts = (Button)findViewById(R.id.event_view_atts);
    	atts.setOnClickListener(this);
        
        Intent i = getIntent();
        eventRev = i.getExtras().getString("com.connectsy.events.revision");

        event = getEventManager(false).getEvent(eventRev);
        
        refresh();
        update();
        setTabSelected("comments");
    }
	
    private void update(){
		setUserStatus(null);
		setTabSelected(null);
        event = getEventManager(false).getEvent(eventRev);
        if (event != null){
        	setTabSelected(null);
        	
        	if (!event.creator.equals(DataManager.getCache(this).getString("username", null))){
        		findViewById(R.id.event_view_att_toggle).setVisibility(View.VISIBLE);
        		
            	Button out = (Button)findViewById(R.id.event_view_attend_out);
            	out.setOnClickListener(this);
                Button in = (Button)findViewById(R.id.event_view_attend_in);
            	in.setOnClickListener(this);
                if (curUserStatus != null && curUserStatus == Status.ATTENDING){
                	in.setSelected(true);
                	out.setSelected(false);
                }else{
                	out.setSelected(true);
                	in.setSelected(false);
                }
        	}
        	
	        TextView where = (TextView)findViewById(R.id.event_view_where);
	        where.setText(Html.fromHtml("<b>Where:</b> "+event.where));
	        TextView what = (TextView)findViewById(R.id.event_view_what);
	        what.setText(Html.fromHtml("<b>What:</b> "+event.description));
	        TextView when = (TextView)findViewById(R.id.event_view_when);
	        when.setText(Html.fromHtml("<b>When:</b> "+DateUtils.formatTimestamp(event.when)));
	        
	        ImageView avatar = (ImageView)findViewById(R.id.event_view_avatar);
	        new AvatarFetcher(this, event.creator, avatar);
        }
    }

    private void setTabSelected(String tab){
    	if (tab != null) tabSelected = tab;
		ListView commentsList = (ListView)findViewById(R.id.comments_list);
		ListView attsList = (ListView)findViewById(R.id.attendants_list);
    	if (tabSelected == "comments"){
    		findViewById(R.id.event_view_comments).setSelected(true);
    		findViewById(R.id.event_view_atts).setSelected(false);
    		attsList.setVisibility(ListView.GONE);
    		commentsList.setVisibility(ListView.VISIBLE);

            if (event != null){
            	//TODO: implement comments
            }
    		
    	}else if(tabSelected == "atts"){
    		findViewById(R.id.event_view_comments).setSelected(false);
    		findViewById(R.id.event_view_atts).setSelected(true);
    		attsList.setVisibility(ListView.VISIBLE);
    		commentsList.setVisibility(ListView.GONE);
            if (event != null){
	        	Log.d(TAG, "using atts: "+getAttManager(false).getAttendants());
    	        if (attAdapter == null){
    		        attAdapter = new AttendantsAdapter(this, R.layout.attendant_list_item, 
    		        		getAttManager(false).getAttendants());
    	        }else{
    	        	attAdapter.clear();
    	        	for (Attendant a: getAttManager(false).getAttendants())
    	        		attAdapter.add(a);
    	        	attAdapter.notifyDataSetChanged();
    	        }
    	        attsList.setAdapter(attAdapter);
            }
    	}
    }
    
    private void setUserStatus(Integer status){
		if (status != null){
			curUserStatus = status;
			if (event == null){
				pendingStatusChange = true;
				return;
			}
		}else if (!pendingStatusChange){
			return;
		}
		pendingStatusChange = false;
		getAttManager(false).setStatus(curUserStatus, ATT_SET);
		pendingOperations++;
		setRefreshing(true);
    }
    
	public void onClick(View v) {
    	if (v.getId() == R.id.ab_refresh){ 
    		refresh();
    	}else if (v.getId() == R.id.event_view_attend_in){
    		findViewById(R.id.event_view_attend_in).setSelected(true);
    		findViewById(R.id.event_view_attend_out).setSelected(false);
    		setUserStatus(Status.ATTENDING);
    	}else if (v.getId() == R.id.event_view_attend_out){
    		findViewById(R.id.event_view_attend_out).setSelected(true);
    		findViewById(R.id.event_view_attend_in).setSelected(false);
    		setUserStatus(Status.NOT_ATTENDING);
    	}else if (v.getId() == R.id.event_view_comments){
    		setTabSelected("comments");
    	}else if (v.getId() == R.id.event_view_atts){
    		setTabSelected("atts");
    	}
	}
	
	private void refresh(){
		if (getEventManager(false).getEvent(eventRev) == null){
			getEventManager(false).refreshEvent(eventRev, REFRESH_EVENT);
			pendingOperations++;
		}
		getAttManager(false).refreshAttendants(REFRESH_ATTS);
		pendingOperations++;
		setRefreshing(true);
	}

	public void onDataUpdate(int code, String response) {
		update();
		pendingOperations--;
		if (pendingOperations == 0) setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, int code) {
		pendingOperations--;
		if (pendingOperations == 0) setRefreshing(false);
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
    
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }
    
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	private AttendantManager getAttManager(boolean forceNew){
		if (attManager == null || forceNew)
			attManager = new AttendantManager(this, this, event.ID, event.attendants);
		return attManager;
	}
	
	private EventManager getEventManager(boolean forceNew){
		if (eventManager == null || forceNew)
			eventManager = new EventManager(this, this, null, null);
		return eventManager;
	}
}
