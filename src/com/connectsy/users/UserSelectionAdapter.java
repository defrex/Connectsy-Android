package com.connectsy.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;

public class UserSelectionAdapter extends ArrayAdapter<String> {

	private Context context;
	private HashMap<String, Boolean> selected = new HashMap<String, Boolean>();

	public UserSelectionAdapter(Context context, int textViewResourceId,
			List<String> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
	}
	
	public ArrayList<String> getSelected(){
		ArrayList<String> ret = new ArrayList<String>();
		for (String username: selected.keySet())
			if (selected.get(username)) ret.add(username);
		return ret;
	}
	
	public void setSelected(ArrayList<String> users){
		for (String user: users)
			selected.put(user, true);
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		final String user = getItem(position);
		View view;
		if (convertView != null && convertView.getId() == R.layout.user_list_item)
			view = convertView;
		else
			view = LayoutInflater.from(context).inflate(
					R.layout.user_list_item, parent, false);
		
		CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
		sel.setVisibility(CheckBox.VISIBLE);
		sel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		    	selected.put(user, isChecked);
		    	notifyDataSetChanged();
		    }
		});
		if (selected.containsKey(user))
			sel.setChecked(selected.get(user));
		
        TextView username = (TextView)view.findViewById(
        		R.id.user_list_item_username);
        username.setText(user);
        username.setTextColor(R.color.text_grey);
        username.setClickable(false);
        
        ImageView avatar = (ImageView)view.findViewById(
        		R.id.user_list_item_avatar);
        new AvatarFetcher(user, avatar, false);

        return view;
	}

}