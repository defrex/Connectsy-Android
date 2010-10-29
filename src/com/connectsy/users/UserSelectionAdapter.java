package com.connectsy.users;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.users.UserManager.Contact;
import com.connectsy.users.UserManager.User;

public class UserSelectionAdapter extends BaseAdapter implements ListAdapter {
	
	private class ContactCursor{
		Cursor numbers;
		Activity activity;
		
		public ContactCursor(Activity activity){
			this.activity = activity;
			numbers = activity.managedQuery(Contacts.Phones.CONTENT_URI, 
        			new String[]{
						Contacts.Phones.PERSON_ID,
        				Contacts.Phones.NUMBER_KEY,
        				Contacts.Phones.NUMBER, 
        				Contacts.Phones.ISPRIMARY,
        			},
        			null, null, null);
        			//Contacts.Phones.ISPRIMARY+" IS 1", null, null);
			numbers.moveToFirst();
		}
		
		public int getCount(){
			return numbers.getCount();
		}
		
		public Contact next(){
			numbers.moveToNext();
			return getCurrentContact();
		}
		
		public Contact getAt(int position){
			if (numbers.getCount() > position){
				int curpos = numbers.getPosition();
				numbers.moveToPosition(position);
				Contact c = getCurrentContact();
				numbers.moveToPosition(curpos);
				return c;
			}else{
				return null;
			}
		}
		
		private Contact getCurrentContact(){
			Uri personUri = ContentUris.withAppendedId(People.CONTENT_URI, 
					numbers.getLong(numbers.getColumnIndex(
							Contacts.Phones.PERSON_ID)));
			Cursor person = activity.managedQuery(personUri,
	        		new String[]{People.DISPLAY_NAME, People._ID},
	        		null, null, null);
			person.moveToFirst();
			
			Contact contact = new Contact();
        	contact.keyNumber = numbers.getString(numbers.getColumnIndex(
        			Contacts.Phones.NUMBER_KEY));
        	contact.displayNumber = numbers.getString(numbers.getColumnIndex(
        			Contacts.Phones.NUMBER));
        	contact.displayName = person.getString(person.getColumnIndex(
        			People.DISPLAY_NAME));
        	contact.personID = person.getLong(person.getColumnIndex(
        			People._ID));
        	return contact;
		}
	}
	
	Activity context;
	ArrayList<Object> objects = new ArrayList<Object>();
	ArrayList<User> friends;
	ArrayList<Contact> contacts = new ArrayList<Contact>();
	ContactCursor contactsCursor;

	private HashMap<String, Boolean> friendsSelected = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> contactsSelected = new HashMap<String, Boolean>();

	public static final int FRIENDS = 1;
	public static final int CONTACTS = 1;
	
	public UserSelectionAdapter(Activity activity, ArrayList<User> friends){
		this.context = activity;
		this.contactsCursor = new ContactCursor(context);
		update(friends);
	}

	public void update(ArrayList<User> friends) {
		this.friends = friends;
		objects.clear();
		// Nothing in 0 since it'll be "select all friends".
		objects.add(null);
		objects.addAll(friends);
		if (contactsCursor.getCount() > 0)
			objects.add("Select From Contacts");
	}
	
	public int getCount() {
		return objects.size()+contactsCursor.getCount();
	}

	public Object getItem(int position) {
		if (objects.size() <= position){
			int contactPos = position - objects.size();
			return contactsCursor.getAt(contactPos);
		}else{
			return objects.get(position);
		}
	}

	public long getItemId(int position) {
		return position;
	}
	
	public boolean isAllSelected(int type){
		if (type == FRIENDS){
			if (friends.size() == friendsSelected.size()){
				int selected = 0;
				for (HashMap.Entry<String, Boolean> entry : friendsSelected.entrySet())
					if (entry.getValue()) selected++;
				if (selected == friends.size())
					return true;
			}
		}else if (type == CONTACTS){
		}
		return false;
	}
	
	public void selectAll(int type){
		if (type == FRIENDS){
			for (User user: friends)
				friendsSelected.put(user.username, true);
		}else if (type == CONTACTS){
			for (Contact contact: contacts)
				contactsSelected.put(contact.keyNumber, true);
		}
		this.notifyDataSetChanged();
	}
	
	public void deselectAll(int type){
		if (type == FRIENDS){
			for (User user: friends)
				friendsSelected.put(user.username, false);
		}else if (type == CONTACTS){
			for (Contact contact: contacts)
				contactsSelected.put((String) contact.keyNumber, false);
		}
		this.notifyDataSetChanged();
	}
	
	public ArrayList<User> getSelectedFriends(){
		ArrayList<User> users = new ArrayList<User>();
		for (HashMap.Entry<String, Boolean> entry : friendsSelected.entrySet())
			if (entry.getValue())
				for (User user: friends)
					if (user.username == entry.getKey())
						users.add(user);
		return users;
	}
	
	public ArrayList<Contact> getSelectedContacts(){
		ArrayList<Contact> selContacts = new ArrayList<Contact>();
		for (HashMap.Entry<String, Boolean> entry : contactsSelected.entrySet())
			if (entry.getValue())
				selContacts.add((Contact) getItem(
						Integer.parseInt(entry.getKey())));
		return selContacts;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		
		if (position == 0){
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_item)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_item, parent, false);

			CheckBox check = (CheckBox)view.findViewById(R.id.user_list_item_select);
			check.setVisibility(CheckBox.VISIBLE);
			check.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			    	if (isChecked) selectAll(FRIENDS);
			    	else deselectAll(FRIENDS);
			    }
			});
			check.setChecked(isAllSelected(FRIENDS));
			
	        TextView name = (TextView)view.findViewById(R.id.user_list_item_username);
	        name.setText("All Friends");
	        name.setTextColor(context.getResources().getColor(R.color.text_grey));

	        view.findViewById(R.id.user_list_item_avatar).setVisibility(View.GONE);
	        
			return view;
		}else if (obj instanceof String){
			String header = (String) obj;
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_header)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_header, parent, false);
			((TextView)view.findViewById(R.id.user_list_header_text))
					.setText(header);
			return view;
		}else if (obj instanceof Contact){
			final Contact contact = (Contact) obj;
			
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_item)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_item, parent, false);
			
			CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
			sel.setVisibility(CheckBox.VISIBLE);
			sel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			    	contactsSelected.put(Integer.toString(position), isChecked);
			    }
			});
			if (contactsSelected.containsKey(Integer.toString(position)))
				sel.setChecked(contactsSelected.get(Integer.toString(position)));
			
	        TextView name = (TextView)view.findViewById(R.id.user_list_item_username);
	        name.setText(contact.displayName);
	        name.setTextColor(context.getResources().getColor(R.color.text_grey));

	        TextView number = (TextView)view.findViewById(R.id.user_list_item_detail);
	        number.setText(contact.displayNumber);
	        number.setVisibility(TextView.VISIBLE);
	        
	        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
	        Uri avyUri = ContentUris.withAppendedId(People.CONTENT_URI, contact.personID);
	        avatar.setImageBitmap(People.loadContactPhoto(context, 
	        		avyUri, R.drawable.avatar_default, null));

	        return view;
		}else if (obj instanceof User){
			final User user = (User) obj;
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_item)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_item, parent, false);
			
			CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
			sel.setVisibility(CheckBox.VISIBLE);
			sel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			    	friendsSelected.put(user.username, isChecked);
			    	notifyDataSetChanged();
			    }
			});
			if (friendsSelected.containsKey(user.username))
				sel.setChecked(friendsSelected.get(user.username));
			
	        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
	        username.setText(user.username);
	        username.setOnClickListener(new TextView.OnClickListener(){
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setType("vnd.android.cursor.item/vnd.connectsy.user");
					i.putExtra("com.connectsy.user.username", user.username);
		    		context.startActivity(i);
				}
	        });
	        
	        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
	        new AvatarFetcher(user.username, avatar, false);

	        return view;
		}else{
			// This should never happen!
			// If it does, it means you've added a bad Object type to the 
			// adapter. User, String (for a heading), and Contact are acceptable.
			return null;
		}
	}

}