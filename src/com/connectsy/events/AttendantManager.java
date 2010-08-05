package com.connectsy.events;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;

public class AttendantManager extends DataManager implements ApiRequestListener {
	public static final String TAG = "AttendantManager";
	private final int GET_ATTS = 0;
	private final int SET_ATTS = 1;
	private String eventID;
	private ApiRequest apiRequest;
	private ArrayList<Attendant> attendants;
	private SharedPreferences data;
	
    public static final class Status{
		public static final int INVITED = 0;
		public static final int ATTENDING = 1;
		public static final int MAYBE = 2;
		public static final int NOT_ATTENDING = 3;
		public static final int LATE = 4;
    }
	
	public class Attendant{
		public String username;
		public String eventID;
		public int status;
		
		public Attendant(){}
		
		public Attendant(JSONObject jsonAttendant) throws JSONException{
			username = jsonAttendant.getString("user");
			eventID = jsonAttendant.getString("event");
			status = jsonAttendant.getInt("status");
		}
	}
	
	public AttendantManager(Context c, DataUpdateListener l, String passedEventID) {
			super(c, l);
			eventID = passedEventID;
			data = DataManager.getCache(c);
			createRequest();
	}
	
	private void createRequest(){
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
		if (data.contains("attendants.timestamp"))
			args.add(new BasicNameValuePair("timestamp", 
					Integer.toString(data.getInt("attendants.timestamp", 0))));
		apiRequest = new ApiRequest(this, context, Method.GET, 
				"/events/"+eventID+"/attendants/", null, args, true, GET_ATTS);
	}
	
	public Attendant getAttendant(String username){
		if (attendants == null) getAttendants();
		for (Attendant a: attendants)
			if (a.username == username) return a;
		return null;
	}
	
	public int getCurrentUserStatus(){
		String username = data.getString("username", null);
		Attendant att = getAttendant(username);
		if (att != null){
			return att.status;
		}else{
			// TODO: change this once invitations are in place
			return Status.INVITED;
		}
	}
	
	public ArrayList<Attendant> getAttendants(){
		attendants = new ArrayList<Attendant>();
		String attsString = apiRequest.getCached();
		if (attsString == null) return attendants;
		try {
			JSONObject json = new JSONObject(attsString);
			data.edit().putInt("attendants.timestamp", json.getInt("timestamp"));
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
		int timestamp = data.getInt("attendants.timestamp", 0);
		if (timestamp != 0){
			try {
				apiRequest.addGetArg("timestamp", Integer.toString(timestamp));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		apiRequest.execute();
		createRequest();
	}
	
	public void setStatus(int status, int passedReturnCode){
		returnCode = passedReturnCode;
		try {
			JSONObject kwargs = new JSONObject();
			kwargs.put("status", status);
			new ApiRequest(this, context, Method.POST, 
					"/events/"+eventID+"/attendants/", kwargs.toString(), 
					null, true, SET_ATTS).execute();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		listener.onDataUpdate(returnCode);
	}
}
