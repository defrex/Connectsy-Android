package com.connectsy.events;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;
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
		public String eventID;
		public int status;
		
		public Attendant(){}
		
		public Attendant(JSONObject jsonAttendant) throws JSONException{
			username = jsonAttendant.getString("user");
			eventID = jsonAttendant.getString("event");
			status = jsonAttendant.getInt("status");
		}
		
		public static ArrayList<Attendant> deserializeList(String attsString) throws JSONException{
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
	
	public Attendant getAttendant(String username){
		if (attendants == null) getAttendants(true);
		for (Attendant a: attendants)
			if (a.username == username) return a;
		return null;
	}
	
	public Integer getCurrentUserStatus(){
		String username = DataManager.getCache(context).getString("username", null);
		Attendant att = getAttendant(username);
		if (att != null) return att.status;
		else return null;
	}
	
	public boolean isUserAttending(String username){
		Attendant a = getAttendant(username);
		if (a == null) return false;
		return (a.status == Status.ATTENDING);
	}
	
	public ArrayList<Attendant> getAttendants(boolean force){
		if (attendants != null && !force) return attendants;
		String attsString = getRequest().getCached();
		if (attsString == null) 
			if (attendants == null)
				return new ArrayList<Attendant>();
			else
				return attendants;
		attendants = new ArrayList<Attendant>();
		try {
			JSONObject json = new JSONObject(attsString);
			DataManager.getCache(context).edit().putInt("attendants.timestamp", 
					json.getInt("timestamp")).commit();
			JSONObject attsJSON = json.getJSONObject("attendants");
			@SuppressWarnings("unchecked")
			Iterator<String> keys = attsJSON.keys();
			while (keys.hasNext()){
				Attendant att = new Attendant();
				att.username = keys.next();
				att.status = attsJSON.getInt(att.username);
				att.eventID = eventID;
				attendants.add(att);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return attendants;
	}
	
	public void refreshAttendants(int sentReturnCode){
		returnCode = sentReturnCode;
		ApiRequest apiRequest = getRequest();
//		int timestamp = DataManager.getCache(context).getInt("attendants.timestamp", 0);
//		if (timestamp != 0)
//			apiRequest.addGetArg("timestamp", Integer.toString(timestamp));
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
	
	public void bulkInvite(ArrayList<User> users, int passedReturnCode){
		returnCode = passedReturnCode;
		ApiRequest r = new ApiRequest(this, context, Method.POST, 
				"/events/"+eventID+"/invites/", true, SET_ATTS);
		try {
			JSONObject kwargs = new JSONObject();
			if (users != null){
				JSONArray usersJSON = new JSONArray();
				for (int i=0;i<users.size();i++)
					usersJSON.put(users.get(i).username);
				kwargs.put("users", usersJSON);
			}else{
				kwargs.put("users", "friends");
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
