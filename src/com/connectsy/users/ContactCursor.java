/**
 * 
 */
package com.connectsy.users;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactCursor {
	
	public static class Contact{
		public String number;
		public String displayNumber;
		public String displayName;
		public String lookupKey;
		//public Long dbID;
		public int type;
		public String customType;
		public String displayType;
		public boolean stared;
		
		public String normalizedNumber(){
			String cleanNumber = "";
			for (int n=0;n<number.length();n++){
				char c = number.charAt(n);
				if (Character.isDigit(c))
					cleanNumber += c;
			}
			return cleanNumber;
		}
		
		public String toString(){
			return "Contact: "+displayName+" number:"+number;
		}
		
		public static String serializeList(ArrayList<Contact> contacts){
			JSONArray jsonContacts = new JSONArray();
			try {
				for (Contact contact: contacts){
					JSONObject jsonContact = new JSONObject();
					jsonContact.put("key_number", contact.number);
					jsonContact.put("display_number", contact.displayNumber);
					jsonContact.put("display_name", contact.displayName);
					jsonContact.put("lookup_key", contact.lookupKey);
					//jsonContact.put("db_id", contact.dbID);
					jsonContact.put("type", contact.type);
					jsonContact.put("custom_type", contact.customType);
					jsonContact.put("display_type", contact.displayType);
					jsonContact.put("stared", contact.stared);
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
				contact.number = jsonContact.getString("key_number");
				contact.displayNumber = jsonContact.getString("display_number");
				contact.displayName = jsonContact.getString("display_name");
				contact.lookupKey = jsonContact.getString("lookup_key");
				//contact.dbID = jsonContact.getLong("db_id");
				contact.type = jsonContact.getInt("type");
				contact.customType = jsonContact.getString("custom_type");
				contact.displayType = jsonContact.getString("display_type");
				contact.stared = jsonContact.getBoolean("stared");
				contacts.add(contact);
			}
			return contacts;
		}
		
		public static Contact fromCursor(Context context, Cursor cursor){
			Contact contact = new Contact();
	    	contact.displayName = cursor.getString(
	    			cursor.getColumnIndex(Data.DISPLAY_NAME));
	    	contact.stared = (cursor.getInt(
	    			cursor.getColumnIndex(Data.STARRED)) == 1);
	    	contact.lookupKey = cursor.getString(
	    			cursor.getColumnIndex(Data.LOOKUP_KEY));
//	    	contact.dbID = cursor.getLong(
//	    			cursor.getColumnIndex(Data._ID));
	    	contact.number = cursor.getString(
	    			cursor.getColumnIndex(Data.DATA1));
	    	contact.type = cursor.getInt(
	    			cursor.getColumnIndex(Data.DATA2));
	    	contact.customType = cursor.getString(
	    			cursor.getColumnIndex(Data.DATA3));
	    	contact.displayNumber = formatNumber(contact);
	    	contact.displayType = CommonDataKinds.Phone.getTypeLabel(
	    			context.getResources(), contact.type, contact.customType)
	    			.toString();
	    	return contact;
		}
	}
	
	
	@SuppressWarnings("unused")
	private final static String TAG = "ContactCursor";
	protected Cursor cursor;
	protected Context context;

	protected ContactCursor(Activity activity){
		this.context = activity;
		cursor = activity.managedQuery(Data.CONTENT_URI, 
			new String[]{
				Data._ID,
				Data.LOOKUP_KEY,
				Data.DISPLAY_NAME,
				Data.STARRED,
				Data.MIMETYPE,
				Data.DATA1,
				Data.DATA2,
				Data.DATA3,
			},
			Data.MIMETYPE+"='"+Phone.CONTENT_ITEM_TYPE+"'",
			null,
			Data.STARRED +" DESC, "+ Data.DISPLAY_NAME + " ASC");
	}

	public int getCount(){
		return cursor.getCount();
	}
	
	public Contact next(){
		cursor.moveToNext();
		return getCurrentContact();
	}
	
	public Contact getAt(int position){
		if (cursor.getCount() > position){
			int curpos = cursor.getPosition();
			cursor.moveToPosition(position);
			Contact c = getCurrentContact();
			cursor.moveToPosition(curpos);
			return c;
		}else{
			return null;
		}
	}
	
	protected Contact getCurrentContact(){
		return Contact.fromCursor(context, cursor);
	}
	
	public static String formatNumber(Contact contact){
		String resp = contact.normalizedNumber();
		if (resp.length() == 11 && resp.charAt(0) == '1')
			resp = resp.substring(1);
		if (resp.length() == 10)
			resp = new StringBuffer(resp).insert(3, "-").insert(7, "-").toString();
		
		return resp;
	}
}
