package com.connectsy;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.connectsy.LocManager.LocListener;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.events.EventList;
import com.connectsy.events.EventManager;
import com.connectsy.settings.MainMenu;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class Dashboard extends MapActivity implements OnClickListener, LocListener, ApiRequestListener {
	private LocManager loc;
	private double lat;
	private double lng;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        ActionBarHandler abHandler = new ActionBarHandler(this);
        ImageView abNewEvent = (ImageView)findViewById(R.id.ab_new_event);
        abNewEvent.setOnClickListener(abHandler);
        
        Button events_nearby = (Button)findViewById(R.id.dashboard_events_nearby);
        events_nearby.setOnClickListener(this);
        Button events_friends = (Button)findViewById(R.id.dashboard_events_friends);
        events_friends.setOnClickListener(this);
        Button events_category = (Button)findViewById(R.id.dashboard_events_category);
        events_category.setOnClickListener(this);
        Button profile = (Button)findViewById(R.id.dashboard_profile);
        profile.setOnClickListener(this);

        MapView mapView = (MapView) findViewById(R.id.dashboard_map);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(13);
        
        loc = new LocManager(this);
        loc.requestUpdates(this);
        setMapLocation();
        getMapOverlay();
    }
    
	public void onClick(View v) {
		if (v.getId() == R.id.dashboard_profile){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.user");
			SharedPreferences data = getSharedPreferences("consy", 0);
			String username = data.getString("username", null);
			i.putExtra("com.connectsy.user.username", username);
			startActivity(i);
    		return;
		}
		Intent i = new Intent(this, EventList.class);
		if (v.getId() == R.id.dashboard_events_friends){
    		i.putExtra("filter", EventManager.Filter.FRIENDS);
    	}else if (v.getId() == R.id.dashboard_events_category){
    		i.putExtra("filter", EventManager.Filter.CATEGORY);
    		//i.putExtra("category", "all");
    	}
		startActivity(i);
	}
    
    private void setMapLocation(){
        Location l = loc.getLocation();
        MapView mapView = (MapView) findViewById(R.id.dashboard_map);
        if (l != null){
        	lat = l.getLatitude();
        	lng = l.getLongitude();
        }else{
        	lat = 43.652527;
        	lng = -79.381961;
        }
        mapView.getController().animateTo(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6)));
    }
    
    private void getMapOverlay(){
    	ApiRequest getPoints = new ApiRequest(this, this, 
    			Method.GET, "/extras/maps/", true, 0);
    	List<NameValuePair> args = getPoints.getGetArgs();
    	args.add(new BasicNameValuePair("lat", Double.toString(lat)));
    	args.add(new BasicNameValuePair("lng", Double.toString(lng)));
    	getPoints.setGetArgs(args);
    	getPoints.execute();
    	String points = getPoints.getCached();
    	if (points != null)
    		displayMapOverlay(points);
    }
    
    private void displayMapOverlay(String pointsStr){
    	Drawable marker = getResources().getDrawable(R.drawable.map_marker);
    	EventMapOverlay overlay = new EventMapOverlay(marker);
    	try {
			JSONArray points = new JSONArray(pointsStr);
			for (int i=0;i<points.length();i++){
				JSONObject point = points.getJSONObject(i);
				double lat = point.getJSONArray("loc").getDouble(0);
				double lng = point.getJSONArray("loc").getDouble(1);
				overlay.addOverlay(lat, lng, point.getString("rev"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		MapView mapView = (MapView) findViewById(R.id.dashboard_map);
		mapView.getOverlays().add(overlay);
    }
    
    private class EventMapOverlay extends ItemizedOverlay<OverlayItem>{
    	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
    	private ArrayList<String> revs = new ArrayList<String>();
    	
		public EventMapOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}
		
		@Override
		protected boolean onTap(int index) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy.events.revision", revs.get(index));
			startActivity(i);
			return true;
		}
		
		public void addOverlay(double lat, double lng, String rev) {
			GeoPoint point = new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
			OverlayItem overlay = new OverlayItem(point, rev, rev);
		    overlays.add(overlay);
		    revs.add(rev);
		    populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}
    	
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void freshLocation() {
		setMapLocation();
	}

	public void onApiRequestFinish(int status, String response, int code) {
		displayMapOverlay(response);
	}

	public void onApiRequestError(int httpStatus, int retCode) {
		// TODO Auto-generated method stub
		
	}
}
