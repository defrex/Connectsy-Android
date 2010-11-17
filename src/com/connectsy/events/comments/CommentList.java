package com.connectsy.events.comments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager;
import com.connectsy.events.EventManager.Event;
import com.connectsy.events.comments.CommentManager.Comment;

public class CommentList extends Activity implements OnClickListener, 
		DataUpdateListener {
    private String eventRev;
    private Event event;
	private CommentAdapter adapter;
	private CommentManager commentManager;
	private int pendingOperations = 0;
	private static int NEW_COMMENT = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_list);

        findViewById(R.id.ab_refresh).setOnClickListener(this);
        findViewById(R.id.ab_new_comment).setOnClickListener(this);
        
        Intent i = getIntent();
		eventRev = i.getExtras().getString("com.connectsy.events.revision");
		// Event should always be cached when this Activity is loaded.
		event = new EventManager(this, null, null, null).getEvent(eventRev);
        
        updateData();
        
        ListView lv = (ListView)findViewById(R.id.comments_list);
        lv.setAdapter(adapter);
        refresh();
    }

	private void updateData() {
		ListView comments = (ListView) findViewById(R.id.comments_list);

//		if (findViewById(R.id.comment_list_item_new) == null) {
//			LayoutInflater inflater = LayoutInflater.from(this);
//			View add_comment = inflater.inflate(
//					R.layout.comment_list_item_new, comments, false);
//			comments.addHeaderView(add_comment);
//			add_comment.setOnClickListener(this);
//		}

		if (adapter == null) {
			adapter = new CommentAdapter(this, R.layout.comment_list_item, 
					getCommentManager().getComments());
		} else {
			adapter.clear();
			for (Comment c : getCommentManager().getComments())
				adapter.add(c);
			adapter.notifyDataSetChanged();
		}
		comments.setAdapter(adapter);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.ab_refresh) refresh();
		else if (v.getId() == R.id.ab_new_comment) {
			Intent i = new Intent(Intent.ACTION_INSERT);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event.comment");
			startActivityForResult(i, NEW_COMMENT);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == NEW_COMMENT) {
			getCommentManager().createComment(
					data.getStringExtra("com.connectsy.event.comment"),
					NEW_COMMENT);
			pendingOperations++;
			setRefreshing(true);
		}
	}
	
	private void refresh(){
		getCommentManager().refreshComments(0);
		setRefreshing(true);
		pendingOperations++;
	}

	public void onDataUpdate(int code, String response) {
		if (code == NEW_COMMENT)
			refresh();
		else
			updateData();
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		pendingOperations--;
		if (pendingOperations == 0)
			setRefreshing(false);
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
	
	private CommentManager getCommentManager() {
		return getCommentManager(false);
	}
	private CommentManager getCommentManager(boolean forceNew) {
		if ((commentManager == null || forceNew) && event != null)
			commentManager = new CommentManager(this, this, event);
		return commentManager;
	}

}
