package com.connectsy.users;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.users.UserManager.User;

public class UserSelector extends Activity implements OnItemClickListener, 
		OnClickListener, DataUpdateListener {
	private static String TAG = "UserSelector";
	UserSelectionAdapter adapter;
	UserManager manager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selector);
        
        manager = new UserManager(this, this, UserManager.currentUsername(this));
        Button done = (Button)findViewById(R.id.user_select_done);
        done.setOnClickListener(this);
        
        update();
    }

	public void onItemClick(AdapterView<?> adapterView, View itemView, int position, long id) {
		
	}

	private void update(){
        ArrayList<User> users = manager.getFriends(false, true);
        if (users != null){
            adapter = new UserSelectionAdapter(this, users);
            ListView lv = (ListView)findViewById(R.id.user_list);
            lv.setOnItemClickListener(this);
            lv.setAdapter(adapter);
        }else{ 
        	manager.refreshFriends(false, 0);
        	findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
        }
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.user_select_done){
			String users = User.serializeList(adapter.getSelectedFriends());
			String contacts = Contact.serializeList(adapter.getSelectedContacts());
			Intent i = new Intent();
			i.putExtra("com.connectsy.users", users);
			i.putExtra("com.connectsy.contacts",contacts);
			setResult(RESULT_OK, i);
			finish();
		}
	}

	public String serializeContacts(
			ArrayList<HashMap<String, Object>> contacts) {
		JSONArray jsonContacts = new JSONArray();
		for (HashMap<String, Object> contact: contacts)
			jsonContacts.put(new JSONObject(contact));
		return jsonContacts.toString();
	}

	public void onDataUpdate(int code, String response) {
		update();
    	findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
	}

	public void onRemoteError(int httpStatus, String response, int code) {}
	
	public static class Contact{
		public String keyNumber;
		public String displayNumber;
		public String displayName;
		public Long personID;
		
		public String toString(){
			return "Contact: "+displayName+" k:"+keyNumber+" d:"+displayNumber;
		}
		
		public static String serializeList(ArrayList<Contact> contacts){
			JSONArray jsonContacts = new JSONArray();
			try {
				for (Contact contact: contacts){
					JSONObject jsonContact = new JSONObject();
					jsonContact.put("key_number", contact.keyNumber);
					jsonContact.put("display_number", contact.displayNumber);
					jsonContact.put("display_name", contact.displayName);
					jsonContact.put("person_id", contact.personID);
					jsonContacts.put(jsonContact);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonContacts.toString();
		}
		
		public static ArrayList<Contact> deserializeList(String sContacts) 
				throws JSONException{
			ArrayList<Contact> contacts = new ArrayList<Contact>();
			JSONArray jsonContacts = new JSONArray(sContacts);
			for(int i=0;i<jsonContacts.length();i++){
				JSONObject jsonContact = jsonContacts.getJSONObject(i);
				Contact contact = new Contact();
				contact.keyNumber = jsonContact.getString("key_number");
				contact.displayNumber = jsonContact.getString("display_number");
				contact.displayName = jsonContact.getString("display_name");
				contact.personID = jsonContact.getLong("person_id");
				contacts.add(contact);
			}
			return contacts;
		}
	}
	
	private class UserSelectionAdapter extends BaseAdapter implements ListAdapter {
		Context context;
		ArrayList<Object> objects = new ArrayList<Object>();
		ArrayList<User> friends;
		ArrayList<Contact> contacts;

		private HashMap<String, Boolean> friendsSelected = new HashMap<String, Boolean>();
		private HashMap<String, Boolean> contactsSelected = new HashMap<String, Boolean>();

		public static final int FRIENDS = 1;
		public static final int CONTACTS = 1;
		
		public UserSelectionAdapter(Context context, ArrayList<User> friends){
			this.context = context;
			update(friends);
		}

		public void update(ArrayList<User> friends) {
			this.friends = friends;
			if (contacts == null) getContacts();
			objects.clear();
			// Nothing in 0 since it'll be "select all friends".
			objects.add(null);
			objects.addAll(friends);
			objects.add("Select From Contacts");
			objects.addAll(contacts);
		}
		
		private void getContacts(){
			contacts = new ArrayList<Contact>();
			
			ContentResolver cr = getContentResolver();
	        Cursor c = cr.query(People.CONTENT_URI, 
	        		new String[]{People.DISPLAY_NAME, People._ID},
	        		null, null, null);
	        while (c.moveToNext()){
	        	Cursor numbers = cr.query(Contacts.Phones.CONTENT_URI, 
	        			new String[]{
	        				Contacts.Phones.NUMBER_KEY,
	        				Contacts.Phones.NUMBER, 
	        				Contacts.Phones.ISPRIMARY
	        			}, 
	        			Contacts.Phones.PERSON_ID+" == "+c.getString(
	        					c.getColumnIndex(People._ID)), 
						null, null);
	        	if (numbers.getCount() > 0){
		        	numbers.moveToFirst();
		        	Contact contact = new Contact();
		        	contact.keyNumber = numbers.getString(numbers.getColumnIndex(
		        			Contacts.Phones.NUMBER_KEY));
		        	contact.displayNumber = numbers.getString(numbers.getColumnIndex(
		        			Contacts.Phones.NUMBER));
		        	contact.displayName = c.getString(c.getColumnIndex(
		        			People.DISPLAY_NAME));
		        	contact.personID = c.getLong(c.getColumnIndex(
		        			People._ID));
		        	
		        	contacts.add(contact);
	        	}
	        }
		}
		
		public int getCount() {
			return objects.size();
		}

		public Object getItem(int position) {
			return objects.get(position);
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
					for (Contact contact: contacts)
						if (contact.keyNumber.equals(entry.getKey()))
							selContacts.add(contact);
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
				    	contactsSelected.put(contact.keyNumber, isChecked);
				    }
				});
				if (contactsSelected.containsKey(contact.keyNumber))
					sel.setChecked(contactsSelected.get(contact.keyNumber));
				
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
}
