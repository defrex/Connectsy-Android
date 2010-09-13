package com.connectsy.events;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.connectsy.ActionBarHandler;
import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.AttendantManager.Attendant;
import com.connectsy.events.AttendantManager.Status;
import com.connectsy.events.EventManager.Event;
import com.connectsy.settings.MainMenu;
import com.connectsy.settings.Settings;
import com.wilson.android.library.DrawableManager;

public class EventView extends Activity implements DataUpdateListener, 
		OnClickListener, OnItemClickListener {
    @SuppressWarnings("unused")
	private static final String TAG = "EventView";
    
    private Event event;
    private String eventRev;
    private EventManager eventManager;
    private ArrayList<Attendant> attendants;
    private CommentManager commentManager;
    private AttendantManager attManager;
    private AttendantsAdapter attAdapter;
    // This needs to change once invitations are in place.
    private int curUserStatus = Status.NOT_ATTENDING;
    
    private int pendingOperations = 0;
    
    private static final int REFRESH_EVENT = 0;
    private static final int REFRESH_ATTENDANTS = 1;
    private static final int ATT_SET = 2;
    private static final int REFRESH_COMMENTS = 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        ActionBarHandler abHandler = new ActionBarHandler(this);
        ImageView abProfile = (ImageView)findViewById(R.id.ab_profile);
        abProfile.setOnClickListener(abHandler);
        ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
        abNewEvent.setOnClickListener(abHandler);
        
        ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
        abRefresh.setOnClickListener(this);
        
        Intent i = getIntent();
        eventRev = i.getExtras().getString("com.connectsy.events.revision");
        
        updateData();
        refresh();
    }
    
    private void updateData(){
        if (eventManager == null) {
        	eventManager = new EventManager(this, this, null, null);
        	event = eventManager.getEvent(eventRev);
        }
        if (commentManager == null && event != null) {
        	commentManager = new CommentManager(this, this, event);
        }
        if (event != null){
	        if (attManager == null)
	        	attManager = new AttendantManager(this, this, event.ID);
	        attendants = attManager.getAttendants();
	        curUserStatus = attManager.getCurrentUserStatus();
	        if (attAdapter == null){
		        attAdapter = new AttendantsAdapter(this, R.layout.attendant_list_item, 
		        		attendants);
	        }
        }
    }
	
    private void updateDisplay(){
        TextView attend = (TextView)findViewById(R.id.event_view_attend);
        if (curUserStatus == Status.NOT_ATTENDING){
        	attend.setOnClickListener(this);
        }else if (curUserStatus == Status.ATTENDING){
        	attend.setText("I'm running late!");
        	attend.setOnClickListener(this);
        }else if (curUserStatus == Status.LATE){
        	attend.setText("Okay, I'm back on time.");
        	attend.setOnClickListener(this);
        }else{
        	attend.setVisibility(0);
        }
        
        if (event != null){
	        TextView where = (TextView)findViewById(R.id.event_view_where);
	        where.setText(event.where);
	        TextView when = (TextView)findViewById(R.id.event_view_when);
	        when.setText(Integer.toString(event.when));
	        TextView what = (TextView)findViewById(R.id.event_view_what);
	        what.setText(event.description);
	        
	        ImageView avatar = (ImageView)findViewById(R.id.event_view_avatar);
	        String avyUrl = Settings.API_DOMAIN+"/users/"+event.creator+"/avatar/";
	        new DrawableManager().fetchDrawableOnThread(avyUrl, avatar);
        
	        //reload comments
	        LinearLayout comments = (LinearLayout)findViewById(R.id.event_view_comments);
	        Collection<CommentManager.Comment> commentList = commentManager.getComments();
	        LayoutInflater inflater = LayoutInflater.from(this);
	        //only add after the last comment
	        View lastComment = comments.getChildCount() > 0 ? comments.getChildAt(comments.getChildCount()) : null;
	        boolean canAdd = false;
	        for (CommentManager.Comment comment: commentList) {
	        	//skip to the end of the list
	        	if (!canAdd && lastComment == null || lastComment.getTag().equals(comment.getId())) {
	        		canAdd = true;
	        		if (lastComment != null)
	        			continue;
	        	}
	        	
	        	View view = inflater.inflate(R.layout.event_comment, comments);
	        	((TextView)view.findViewById(R.id.comment_text)).setText(comment.getComment());
	        }
        }
    }

	public void onClick(View v) {
    	if (v.getId() == R.id.ab_refresh){ 
    		refresh();
    	}else if (v.getId() == R.id.event_view_attend){
    		if (curUserStatus == Status.NOT_ATTENDING)
    			attManager.setStatus(Status.ATTENDING, ATT_SET);
    		else if (curUserStatus == Status.ATTENDING)
    			attManager.setStatus(Status.LATE, ATT_SET);
    		else if (curUserStatus == Status.LATE)
    			attManager.setStatus(Status.ATTENDING, ATT_SET);
    		pendingOperations++;
    		setRefreshing(true);
    	}
	}
	
	private void refresh(){
		if (eventManager != null){
			eventManager.refreshEvent(eventRev, REFRESH_EVENT);
			pendingOperations++;
		}
		if (attManager != null){
			attManager.refreshAttendants(REFRESH_ATTENDANTS);
			pendingOperations++;
		}
		if (commentManager != null) {
			commentManager.refreshComments(REFRESH_COMMENTS);
		}
		if (pendingOperations > 0)
			setRefreshing(true);
	}

	public void onDataUpdate(int code, String response) {
//		if (code == REFRESH_EVENT)
//		else if (code == REFRESH_ATTENDANTS)
//		else  if (code == ATT_SET)
		updateData();
		updateDisplay();
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
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
}
