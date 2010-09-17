package com.connectsy.events;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.events.CommentManager.Comment;

public class CommentAdapter extends ArrayAdapter<Comment> {
	@SuppressWarnings("unused")
	private final String TAG = "AttendantsCursorAdapter";
	
	public CommentAdapter(Context context, int viewResourceId,
			ArrayList<Comment> objects) {
		super(context, viewResourceId, objects);
	}
 
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final Comment comment = getItem(position);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.comment_list_item, parent, false);
		
		OnClickListener userClick = new View.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", comment.getUsername());
	    		context.startActivity(i);
			}
        };
		
        TextView username = (TextView)view.findViewById(R.id.comment_username);
        username.setText(comment.getUsername());
        username.setOnClickListener(userClick);
        
        ImageView avatar = (ImageView)view.findViewById(R.id.comment_avatar);
        avatar.setOnClickListener(userClick);
        new AvatarFetcher(context, comment.getUsername(), avatar);

        TextView body = (TextView)view.findViewById(R.id.comment_text);
        body.setText(comment.getComment());

		return view;
	}
}
