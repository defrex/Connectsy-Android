package com.connectsy.events;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.connectsy.ActionBarHandler;
import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.events.attendants.AttendantManager;
import com.connectsy.events.attendants.AttendantsAdapter;
import com.connectsy.events.attendants.AttendantManager.Attendant;
import com.connectsy.events.attendants.AttendantManager.Status;
import com.connectsy.events.comments.CommentAdapter;
import com.connectsy.events.comments.CommentManager;
import com.connectsy.events.comments.CommentManager.Comment;
import com.connectsy.settings.MainMenu;
import com.connectsy.users.UserManager;
import com.connectsy.users.UserManager.User;

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
	private String tabSelected = "comments";
	private Integer curUserStatus;

	private int pendingOperations = 0;

	private static final int REFRESH_EVENT = 0;
	private static final int REFRESH_ATTENDANTS = 1;
	private static final int ATT_SET = 2;
	private static final int REFRESH_COMMENTS = 3;
	private static final int NEW_COMMENT = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_view);

		//set up logo clicks
        new ActionBarHandler(this);
        
		ImageView abRefresh = (ImageView) findViewById(R.id.ab_refresh);
		abRefresh.setOnClickListener(this);
		Button comments = (Button) findViewById(R.id.event_view_tab_comments);
		comments.setOnClickListener(this);
		comments.setSelected(true);
		Button atts = (Button) findViewById(R.id.event_view_tab_atts);
		atts.setOnClickListener(this);

		Intent i = getIntent();
		eventRev = i.getExtras().getString("com.connectsy.events.revision");
		event = getEventManager().getEvent(eventRev);
		update();
		refresh();
	}

	private void update() {
		if (event != null) {
			setUserStatus(null, false);
			new EventRenderer(this, findViewById(R.id.event), eventRev, false);
			User curUser = UserManager.currentUser(this);
			if (!event.creator.equals(curUser.username)) {
				findViewById(R.id.event_view_ab_in).setVisibility(View.VISIBLE);
				findViewById(R.id.event_view_ab_in_seperator).setVisibility(
						View.VISIBLE);

				ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
				in.setOnClickListener(this);
				if (getAttManager().isUserAttending(curUser.id)) {
					in.setSelected(true);
					in.setImageDrawable(getResources().getDrawable(
							R.drawable.icon_check_selected));
				} else {
					in.setSelected(false);
					in.setImageDrawable(getResources().getDrawable(
							R.drawable.icon_check));
				}
			}
		}
	}
	
	private void updateCommentsTab(){
		setTabSelected(null);
		ListView comments = (ListView) findViewById(R.id.comments_list);

		if (findViewById(R.id.comment_list_item_new) == null) {
			LayoutInflater inflater = LayoutInflater.from(this);
			View add_comment = inflater.inflate(
					R.layout.comment_list_item_new, comments, false);
			comments.addHeaderView(add_comment);
			add_comment.setOnClickListener(this);
		}

		if (commentAdapter == null) {
			commentAdapter = new CommentAdapter(this,
					R.layout.comment_list_item, getCommentManager()
							.getComments());
		} else {
			commentAdapter.clear();
			for (Comment c : getCommentManager().getComments())
				commentAdapter.add(c);
			commentAdapter.notifyDataSetChanged();
		}
		comments.setAdapter(commentAdapter);
	}
	
	private void updateAttTab(){
		setTabSelected(null);
		ArrayList<Attendant> atts = getAttManager().getAttendants();
		if (attAdapter == null) {
			attAdapter = new AttendantsAdapter(this,
					R.layout.attendant_list_item, atts);
		} else {
			attAdapter.clear();
			for (Attendant a : atts)
				attAdapter.add(a);
			attAdapter.notifyDataSetChanged();
		}
		((ListView) findViewById(R.id.attendants_list)).setAdapter(attAdapter);
		Log.d(TAG, "updated atts adapter "+atts.size());
	}

	private void setTabSelected(String tab) {
		if (tab != null) tabSelected = tab;
		if (tabSelected.equals("comments")) {
			findViewById(R.id.event_view_tab_comments).setSelected(true);
			findViewById(R.id.event_view_tab_atts).setSelected(false);
			findViewById(R.id.attendants_list).setVisibility(ListView.GONE);
			findViewById(R.id.comments_list).setVisibility(ListView.VISIBLE);
		} else if (tabSelected.equals("atts")) {
			findViewById(R.id.event_view_tab_comments).setSelected(false);
			findViewById(R.id.event_view_tab_atts).setSelected(true);
			findViewById(R.id.attendants_list).setVisibility(ListView.VISIBLE);
			findViewById(R.id.comments_list).setVisibility(ListView.GONE);
		}
	}

	private void setUserStatus(Integer status) {
		setUserStatus(status, true);
	}
	private void setUserStatus(Integer status, boolean doRequest) {
		if (status != null)
			curUserStatus = status;
		else if (curUserStatus == null)
			return;

		ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
		if (curUserStatus == Status.ATTENDING) {
			in.setSelected(true);
			in.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_check_selected));
		} else {
			in.setSelected(false);
			in.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_check));
		}
		if (doRequest) {
			getAttManager().setStatus(curUserStatus, ATT_SET);
			pendingOperations++;
			setRefreshing(true);
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.ab_refresh) {
			refresh();
		} else if (v.getId() == R.id.event_view_ab_in) {
			ImageView in = (ImageView) findViewById(R.id.event_view_ab_in);
			if (in.isSelected())
				setUserStatus(Status.NOT_ATTENDING);
			else
				setUserStatus(Status.ATTENDING);
		} else if (v.getId() == R.id.event_view_tab_comments) {
			setTabSelected("comments");
		} else if (v.getId() == R.id.event_view_tab_atts) {
			setTabSelected("atts");
		} else if (v.getId() == R.id.comment_list_item_new) {
			Intent i = new Intent(Intent.ACTION_INSERT);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event.comment");
			startActivityForResult(i, NEW_COMMENT);
		}
	}

	private void refresh() {
		if (event == null) {
			getEventManager().refreshEvent(eventRev, REFRESH_EVENT);
			pendingOperations++;
		}else{
			getAttManager().refreshAttendants(REFRESH_ATTENDANTS);
			getCommentManager().refreshComments(REFRESH_COMMENTS);
			pendingOperations += 2;
		}
		setRefreshing(true);
	}

	public void onDataUpdate(int code, String response) {
		if (code == REFRESH_EVENT){
			event = getEventManager().getEvent(eventRev);
			update();
			refresh();
		}else if (code == NEW_COMMENT || code == ATT_SET) {
			refresh();
		} else if (code == REFRESH_ATTENDANTS){
			curUserStatus = getAttManager().getCurrentUserStatus(true);
			updateAttTab();
		}else if (code == REFRESH_COMMENTS){
			updateCommentsTab();
		}else{
			update();
		}
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, String response, int returnCode) {
		if (httpStatus == 403 && returnCode == ATT_SET) {
			Toast.makeText(this, "You are not invited.", 500).show();
			setUserStatus(Status.NOT_ATTENDING, false);
		}
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == NEW_COMMENT) {
			getCommentManager().createComment(
					data.getStringExtra("com.connectsy.event.comment"),
					NEW_COMMENT);
			pendingOperations++;
			setRefreshing(true);
		}
	}

	private void setRefreshing(boolean on) {
		if (on) {
			findViewById(R.id.ab_refresh).setVisibility(View.GONE);
			findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
		} else {
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

	private AttendantManager getAttManager() {
		return getAttManager(false);
	}

	private AttendantManager getAttManager(boolean forceNew) {
		if ((attManager == null || forceNew) && event != null)
			attManager = new AttendantManager(this, this, event.ID,
					event.attendants);
		return attManager;
	}

	private CommentManager getCommentManager() {
		return getCommentManager(false);
	}

	private CommentManager getCommentManager(boolean forceNew) {
		if ((commentManager == null || forceNew) && event != null)
			commentManager = new CommentManager(this, this, event);
		return commentManager;
	}

	private EventManager getEventManager() {
		return getEventManager(false);
	}

	private EventManager getEventManager(boolean forceNew) {
		if (eventManager == null || forceNew)
			eventManager = new EventManager(this, this, null, null);
		return eventManager;
	}

}
