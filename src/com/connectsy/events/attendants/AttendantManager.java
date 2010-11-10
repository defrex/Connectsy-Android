package com.connectsy.events.attendants;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.DataManager;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.users.ContactCursor.Contact;
import com.connectsy.users.UserManager.User;

public class AttendantManager extends DataManager implements ApiRequestListener {
	public static final String TAG = "AttendantManager";
	private final int GET_ATTS = 0;
	private final int SET_ATTS = 1;
	private String eventID;
	private ArrayList<Attendant> attendants;
	
    public static final class Status{
		public static final int INVITED = 0;
		public static final int ATTENDING = 1;
		public static final int MAYBE = 2;
		public static final int NOT_ATTENDING = 3;
		public static final int LATE = 4;
    }
	
	public static class Attendant{
		public String username;
		public String display_name;
		public String userID;
		public String eventID;
		public int status;
		
		public Attendant(){}
		
		public Attendant(JSONObject jsonAttendant) throws JSONException{
			status = jsonAttendant.getInt("status");
			userID = jsonAttendant.getString("user");

			if (jsonAttendant.has("username"))
				username = jsonAttendant.getString("username");
			if (jsonAttendant.has("display_name"))
				display_name = jsonAttendant.getString("display_name");
		}
		
		public static ArrayList<Attendant> deserializeList(String attsString) 
				throws JSONException{
			ArrayList<Attendant> atts = new ArrayList<Attendant>();
			JSONArray attsJSON = new JSONArray(attsString);
			for (int i=0;i<attsJSON.length();i++)
				atts.add(new Attendant(attsJSON.getJSONObject(i)));
			return atts;
		}
	}
	
	public AttendantManager(Context c, DataUpdateListener l, String eventID) {
			super(c, l);
			this.eventID = eventID;
	}
	
	public AttendantManager(Context c, DataUpdateListener l, String eventID, 
				ArrayList<Attendant> attendants) {
			super(c, l);
			this.eventID = eventID;
			this.attendants = attendants;
	}
	
	private ApiRequest getRequest(){
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
		if (DataManager.getCache(context).contains("attendants.timestamp"))
			args.add(new BasicNameValuePair("timestamp", 
					Integer.toString(DataManager.getCache(context)
							.getInt("attendants.timestamp", 0))));
		ApiRequest apiRequest = new ApiRequest(this, context, Method.GET, 
				"/events/"+eventID+"/attendants/", true, GET_ATTS);
		apiRequest.setGetArgs(args);
		return apiRequest;
	}
	
	public Attendant getAttendant(String userID){
		return getAttendant(userID, false);}
	public Attendant getAttendant(String userID, boolean force){
		getAttendants(force);
		for (Attendant a: attendants)
			if (a.userID.equals(userID)) return a;
		return null;
	}
	
	public Integer getCurrentUserStatus(){
		return getCurrentUserStatus(false);}
	public Integer getCurrentUserStatus(boolean force){
		String username = DataManager.getCache(context).getString("username", null);
		getAttendants(force);
		for (Attendant a: attendants)
			if (a.username != null && a.username.equals(username)) 
				return a.status;
		return null;
	}
	
	public boolean isUserAttending(String userID){
		Attendant a = getAttendant(userID);
		if (a == null) return false;
		return (a.status == Status.ATTENDING);
	}
	
	public ArrayList<Attendant> getAttendants(){
		return getAttendants(false);}
	public ArrayList<Attendant> getAttendants(boolean force){
		String attsString = getRequest().getCached();
		if (attsString == null) 
			if (attendants == null)
				return new ArrayList<Attendant>();
			else
				return attendants;
		try {
			attendants = Attendant.deserializeList(
					new JSONObject(attsString).getString("attendants"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (attendants == null)
			return new ArrayList<Attendant>();
		else
			return attendants;
	}
	
	public void refreshAttendants(int sentReturnCode){
		returnCode = sentReturnCode;
		ApiRequest apiRequest = getRequest();
		apiRequest.execute();
	}
	
	public void setStatus(int status, int passedReturnCode){
		returnCode = passedReturnCode;
		try {
			JSONObject kwargs = new JSONObject();
			kwargs.put("status", status);
			ApiRequest r = new ApiRequest(this, context, Method.POST, 
					"/events/"+eventID+"/attendants/", true, SET_ATTS);
			r.setBodyString(kwargs.toString());
			r.execute();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void bulkInvite(ArrayList<User> users, ArrayList<Contact> contacts, 
			int returnCode){
		this.returnCode = returnCode;
		ApiRequest r = new ApiRequest(this, context, Method.POST, 
				"/events/"+eventID+"/invites/", true, SET_ATTS);
		try {
			JSONObject kwargs = new JSONObject();
			if (users != null){
				if (users.size() > 0){
					JSONArray usersJSON = new JSONArray();
					for (int i=0;i<users.size();i++)
						usersJSON.put(users.get(i).username);
					kwargs.put("users", usersJSON);
				}
			}else{
				kwargs.put("users", "friends");
			}
			if (contacts != null && contacts.size() > 0){
				JSONArray jsonContacts = new JSONArray();
				for (int i=0;i<contacts.size();i++){
					JSONObject c = new JSONObject();
					c.put("number", contacts.get(i).normalizedNumber());
					c.put("name", contacts.get(i).displayName);
					jsonContacts.put(c);
				}
				Log.d(TAG, "sending contacts: "+jsonContacts);
				kwargs.put("contacts", jsonContacts);
			}
			r.setBodyString(kwargs.toString());
			r.execute();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		getAttendants(true);
		listener.onDataUpdate(returnCode, response);
	}
}
