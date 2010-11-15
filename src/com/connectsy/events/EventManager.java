package com.connectsy.events;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

import com.connectsy.LocManager;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.DataManager;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.events.attendants.AttendantManager.Attendant;
import com.connectsy.users.ContactCursor.Contact;
import com.connectsy.users.UserManager.User;

public class EventManager extends DataManager {
	@SuppressWarnings("unused")
	private static final String TAG = "EventManager";
	
	private static final int CREATE_EVENT = 0;
	private static final int GET_EVENTS = 1;
	private static final int GET_EVENT = 2;
	
	private String extra;
	private Filter filter;
	private LocManager locManager;
	
	public enum Filter{INVITED, CREATED, PUBLIC}
	
	public class Event{
		public String ID;
		public String revision;
		public String creator;
		public String what;
		public String where;
		public long when;
		public String category;
		public String location;
		public boolean broadcast;
		public boolean friends;
		public ArrayList<User> someFriends;
		public ArrayList<Attendant> attendants;
		public long created;
		public float[] posted_from = new float[2];

		public Event(){}
		
		public Event(JSONObject response) throws JSONException{
			JSONObject event = response.getJSONObject("event");
			ID = event.getString("id");
			revision = event.getString("revision");
			created = event.getLong("created");
			creator = event.getString("creator");
			what = event.getString("what");
			broadcast = event.getBoolean("broadcast");
			if (event.has("where"))
				where = event.getString("where");
			if (event.has("when"))
				when = event.getLong("when");
			if (event.has("category"))
				category = event.getString("category");
			if (event.has("location"))
				location = event.getString("location");
			if (response.has("attendants"))
				attendants = Attendant.deserializeList(response.getString("attendants"));
			if (event.has("posted_from")){
				posted_from[0] = (float) event.getJSONArray("posted_from").getDouble(0);
				posted_from[1] = (float) event.getJSONArray("posted_from").getDouble(1);
			}
		}
	}

	public EventManager(Context c, DataUpdateListener l, 
			Filter filter, String extra) {
		super(c, l);
		this.filter = filter;
		this.extra = extra;
		locManager = new LocManager(c);
	}

	private ApiRequest getEventsRequest(){
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(); 
		if (filter == Filter.INVITED){
			args.add(new BasicNameValuePair("filter", "invited"));
		}else if (filter == Filter.PUBLIC){
			args.add(new BasicNameValuePair("filter", "public"));
			if (extra != null)
				args.add(new BasicNameValuePair("category", extra));
		}else if (filter == Filter.CREATED){
			args.add(new BasicNameValuePair("filter", "creator"));
			if (extra != null)
				args.add(new BasicNameValuePair("username", extra));
		}
		
		Location loc = locManager.getLocation();
		if (loc != null){
			args.add(new BasicNameValuePair("lat", 
					Double.toString(loc.getLatitude())));
			args.add(new BasicNameValuePair("lng", 
					Double.toString(loc.getLongitude())));
		}
		
		args.add(new BasicNameValuePair("sort", "created"));
		
		ApiRequest request = new ApiRequest(this, context, Method.GET, 
				"/events/", true, GET_EVENTS);
		request.setGetArgs(args);
		return request;
	}
	
	public ArrayList<String> getRevisions(){
		ArrayList<String> revs = new ArrayList<String>();
		String eventsCache = getEventsRequest().getCached();
		if (eventsCache == null) return revs;
		try {
			JSONArray revisions = new JSONObject(eventsCache)
					.getJSONArray("events");
			for(int i=0;i<revisions.length();i++)
				revs.add(revisions.getString(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return revs;
	}
	
	public void refreshRevisions(int returnCode){
		this.returnCode = returnCode;
		getEventsRequest().execute();
	}
	
	private ApiRequest getEventRequest(String revision){
		ApiRequest r = new ApiRequest(this, context, Method.GET, 
				"/events/"+revision+"/", true, GET_EVENT);
		r.addGetArg("attendants", "true");
		return r;
	}
	
	public Event getEvent(String revision){
		ApiRequest eventRequest = getEventRequest(revision);
		String eventString = eventRequest.getCached();
		if (eventString == null) return null;
		try {
			return new Event(new JSONObject(eventString));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void refreshEvent(String revision, int passedReturnCode){
		getEventRequest(revision).execute();
	}

	public void createEvent(Event event, ArrayList<User> users, 
				ArrayList<Contact> contacts, int returnCode) {
		this.returnCode = returnCode;
		
		JSONObject json = new JSONObject();
		try {
			json.put("what", event.what);
			json.put("broadcast", event.broadcast);
			json.put("where", event.where);
			json.put("when", event.when);
			json.put("category", event.category);
			json.put("client", "Connectsy for Android");
			Location loc = locManager.getLocation();
			if (loc != null){
				JSONArray jLoc = new JSONArray();
				jLoc.put(loc.getLatitude());
				jLoc.put(loc.getLongitude());
				json.put("posted_from", jLoc);
			}

			if (users != null && !event.broadcast){
				if (users.size() > 0){
					JSONArray usersJSON = new JSONArray();
					for (int i=0;i<users.size();i++)
						usersJSON.put(users.get(i).username);
					json.put("users", usersJSON);
				}
			}
			if (contacts != null && contacts.size() > 0){
				JSONArray jsonContacts = new JSONArray();
				for (int i=0;i<contacts.size();i++){
					JSONObject c = new JSONObject();
					c.put("number", contacts.get(i).normalizedNumber());
					c.put("name", contacts.get(i).displayName);
					jsonContacts.put(c);
				}
				json.put("contacts", jsonContacts);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ApiRequest r = new ApiRequest(this, context, Method.POST, 
				"/events/", true, CREATE_EVENT);
		r.setBodyString(json.toString());
		r.execute();
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		listener.onDataUpdate(returnCode, response);
	}
}
