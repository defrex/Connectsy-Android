package com.connectsy.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
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

import com.connectsy.LocManager;
import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.data.DataManager;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.events.attendants.AttendantManager;
import com.connectsy.events.attendants.AttendantManager.Attendant;
import com.connectsy.events.attendants.AttendantManager.Status;
import com.connectsy.events.attendants.AttendantsAdapter;
import com.connectsy.events.comments.CommentAdapter;
import com.connectsy.events.comments.CommentManager;
import com.connectsy.events.comments.CommentManager.Comment;
import com.connectsy.settings.MainMenu;
import com.connectsy.utils.DateUtils;
import com.connectsy.utils.Utils;

public class EventView extends Activity implements DataUpdateListener, 
		OnClickListener, OnItemClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "EventView";
    
    private Event event;
    private String eventRev;
    private EventManager eventManager;
    private CommentManager commentManager;
    private CommentAdapter commentAdapter;
    private AttendantManager attManager;
    private AttendantsAdapter attAdapter;
    private String tabSelected;
    private Integer curUserStatus;
    private boolean pendingStatusChange = false;
    
    private int pendingOperations = 0;
    
    private static final int REFRESH_EVENT = 0;
    private static final int REFRESH_ATTENDANTS = 1;
    private static final int ATT_SET = 2;
    private static final int REFRESH_COMMENTS = 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
        abRefresh.setOnClickListener(this);
    	Button comments = (Button)findViewById(R.id.event_view_tab_comments);
    	comments.setOnClickListener(this);
    	comments.setSelected(true);
    	Button atts = (Button)findViewById(R.id.event_view_tab_atts);
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
        	String curUser = DataManager.getCache(this).getString("username", null);
        	if (!event.creator.equals(curUser)){
        		findViewById(R.id.event_view_ab_in).setVisibility(View.VISIBLE);
        		findViewById(R.id.event_view_ab_in_seperator).setVisibility(View.VISIBLE);
        		
            	ImageView in = (ImageView)findViewById(R.id.event_view_ab_in);
            	in.setOnClickListener(this);
                if (getAttManager(false).isUserAttending(curUser)){
                	in.setSelected(true);
                }else{
                	in.setSelected(false);
                }
        	}
        	
	        EventView.renderView(this, findViewById(R.id.event), event, false);
        }
    }

    private void setTabSelected(String tab){
    	if (tab != null) tabSelected = tab;
		ListView attsList = (ListView)findViewById(R.id.attendants_list);
        ListView comments = (ListView)findViewById(R.id.comments_list);
    	if (tabSelected == "comments"){
    		findViewById(R.id.event_view_tab_comments).setSelected(true);
    		findViewById(R.id.event_view_tab_atts).setSelected(false);
    		attsList.setVisibility(ListView.GONE);
    		comments.setVisibility(ListView.VISIBLE);
    		
    		if (findViewById(R.id.comment_list_item_new) == null){
    			LayoutInflater inflater = LayoutInflater.from(this);
    			View add_comment = inflater.inflate(R.layout.comment_list_item_new, 
    					comments, false);
    			comments.addHeaderView(add_comment);
    		}
    		
            if (event != null){
    	        if (commentAdapter == null){
    	        	commentAdapter = new CommentAdapter(this, R.layout.comment_list_item, 
    		        		getCommentManager(false).getComments());
    	        }else{
    	        	commentAdapter.clear();
    	        	for (Comment c: getCommentManager(false).getComments())
    	        		commentAdapter.add(c);
    	        	commentAdapter.notifyDataSetChanged();
    	        }
    	        comments.setAdapter(commentAdapter);
            }
    		
    	}else if(tabSelected == "atts"){
    		findViewById(R.id.event_view_tab_comments).setSelected(false);
    		findViewById(R.id.event_view_tab_atts).setSelected(true);
    		attsList.setVisibility(ListView.VISIBLE);
    		comments.setVisibility(ListView.GONE);
            if (event != null){
    	        if (attAdapter == null){
    		        attAdapter = new AttendantsAdapter(this, R.layout.attendant_list_item, 
    		        		getAttManager(false).getAttendants(false));
    	        }else{
    	        	attAdapter.clear();
    	        	for (Attendant a: getAttManager(false).getAttendants(false))
    	        		attAdapter.add(a);
    	        	attAdapter.notifyDataSetChanged();
    	        }
    	        attsList.setAdapter(attAdapter);
            }
    	}
    }
    
    private void setUserStatus(Integer status){
		if (status != null) curUserStatus = status;
		else if (curUserStatus == null) return;
		
		ImageView in = (ImageView)findViewById(R.id.event_view_ab_in);
		if (curUserStatus == Status.ATTENDING){
    		in.setSelected(true);
    		in.setImageDrawable(getResources().getDrawable(R.drawable.icon_check_black));
		}else{
    		in.setSelected(false);
    		in.setImageDrawable(getResources().getDrawable(R.drawable.icon_check_white));
		}
		
		if (status != null && event == null){
			pendingStatusChange = true;
			return;
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
    	}else if (v.getId() == R.id.event_view_ab_in){
    		ImageView in = (ImageView)findViewById(R.id.event_view_ab_in);
    		if (in.isSelected())
        		setUserStatus(Status.NOT_ATTENDING);
    		else
        		setUserStatus(Status.ATTENDING);
    	}else if (v.getId() == R.id.event_view_tab_comments){
    		setTabSelected("comments");
    	}else if (v.getId() == R.id.event_view_tab_atts){
    		setTabSelected("atts");
    	}
	}
	
	private void refresh(){
		if (getEventManager(false).getEvent(eventRev) == null){
			getEventManager(false).refreshEvent(eventRev, REFRESH_EVENT);
			pendingOperations++;
		}
		if (event != null){ 
			getAttManager(false).refreshAttendants(REFRESH_ATTENDANTS);
			getCommentManager(false).refreshComments(REFRESH_COMMENTS);
			pendingOperations+=2;
		}
		setRefreshing(true);
	}

	public void onDataUpdate(int code, String response) {
		if (code == REFRESH_ATTENDANTS)
			getAttManager(false).getAttendants(true);
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
		if ((attManager == null || forceNew) && event != null)
			attManager = new AttendantManager(this, this, event.ID, event.attendants);
		return attManager;
	}
	
	private CommentManager getCommentManager(boolean forceNew){
		if ((commentManager == null || forceNew) && event != null)
			commentManager = new CommentManager(this, this, event);
		return commentManager;
	}
	
	private EventManager getEventManager(boolean forceNew){
		if (eventManager == null || forceNew)
			eventManager = new EventManager(this, this, null, null);
		return eventManager;
	}
	
	static View renderView(final Context context, View view, final Event event, boolean truncate){
		OnClickListener userClick = new View.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", event.creator);
	    		context.startActivity(i);
			}
        };
        
        ImageView avatar = (ImageView)view.findViewById(R.id.event_avatar);
        avatar.setOnClickListener(userClick);
        new AvatarFetcher(context, event.creator, avatar);
		
        TextView username = (TextView)view.findViewById(R.id.event_username);
        username.setText(event.creator);
        username.setOnClickListener(userClick);
        
        if (event.category != null && !event.category.equals("")){
        	view.findViewById(R.id.event_pipe).setVisibility(View.VISIBLE);
	        TextView category = (TextView)view.findViewById(R.id.event_category);
	        category.setVisibility(View.VISIBLE);
	        category.setText(event.category);
	        category.setOnClickListener(new TextView.OnClickListener(){
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setType("vnd.android.cursor.dir/vnd.connectsy.event");
		    		i.putExtra("filter", EventManager.Filter.CATEGORY);
		    		i.putExtra("category", event.category);
		    		context.startActivity(i);
				}
	        });
        }
        
        TextView where = (TextView)view.findViewById(R.id.event_where);
        where.setText(Html.fromHtml("<b>where:</b> "+
        		Utils.maybeTruncate(event.where, 25, truncate)));
        TextView what = (TextView)view.findViewById(R.id.event_what);
        what.setText(Html.fromHtml("<b>what:</b> "+
        		Utils.maybeTruncate(event.description, 25, truncate)));
        TextView when = (TextView)view.findViewById(R.id.event_when);
        when.setText(Html.fromHtml("<b>when:</b> "+DateUtils.formatTimestamp(event.when)));

        TextView distance = (TextView)view.findViewById(R.id.event_distance);
        String distanceText = new LocManager(context).distanceFrom(event.posted_from[0], 
        		event.posted_from[1]);
        if (distanceText != null)
        	distance.setText(distanceText);
        else
        	distance.setVisibility(View.VISIBLE);
        TextView created = (TextView)view.findViewById(R.id.event_created);
        created.setText("created "+DateUtils.formatTimestamp(event.created));
        
        return view;
	}
}
