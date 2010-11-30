package com.connectsy2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.connectsy2.R;
import com.connectsy2.data.Analytics;
import com.connectsy2.events.EventList;
import com.connectsy2.events.EventNew;
import com.connectsy2.settings.MainMenu;
import com.connectsy2.users.UserManager;
import com.connectsy2.users.UserSearch;

//public class Dashboard extends MapActivity implements OnClickListener, 
//		LocListener, ApiRequestListener {
public class Dashboard extends Activity implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "Dashboard";

//	private LocManager loc;
//	private double lat;
//	private double lng;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        Analytics.pageView(this, this.getClass().getName());
        
//        new NotificationBarManager(this);

        findViewById(R.id.dashboard_connections).setOnClickListener(this);
        findViewById(R.id.dashboard_profile).setOnClickListener(this);
        findViewById(R.id.dashboard_search).setOnClickListener(this);
        findViewById(R.id.dashboard_new_event).setOnClickListener(this);
        
//        MapView mapView = (MapView) findViewById(R.id.dashboard_map);
//        mapView.setBuiltInZoomControls(true);
//        mapView.getController().setZoom(13);
//        loc = new LocManager(this);
//        loc.requestUpdates(this);
//        setMapLocation();
//        getMapOverlay();
    }
    
	public void onClick(View v) {
		if (v.getId() == R.id.dashboard_profile){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.user");
			i.putExtra("com.connectsy2.user.username", 
					UserManager.currentUsername(this));
			startActivity(i);
    		return;
		}else if (v.getId() == R.id.dashboard_search){
			startActivity(new Intent(this, UserSearch.class));
    		return;
		}else if (v.getId() == R.id.dashboard_new_event){
    		startActivity(new Intent(this, EventNew.class));
    		return;
		}
		Intent i = new Intent(this, EventList.class);
		startActivity(i);
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }
//    
//    private void setMapLocation(){
//        Location l = loc.getLocation();
//        MapView mapView = (MapView) findViewById(R.id.dashboard_map);
//        if (l != null){
//        	lat = l.getLatitude();
//        	lng = l.getLongitude();
//        }else{
//        	lat = 43.652527;
//        	lng = -79.381961;
//        }
//        mapView.getController().animateTo(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6)));
//    }
//    
//    private void getMapOverlay(){
//    	ApiRequest getPoints = new ApiRequest(this, this, 
//    			Method.GET, "/extras/maps/", true, 0);
//    	List<NameValuePair> args = getPoints.getGetArgs();
//    	args.add(new BasicNameValuePair("lat", Double.toString(lat)));
//    	args.add(new BasicNameValuePair("lng", Double.toString(lng)));
//    	getPoints.setGetArgs(args);
//    	getPoints.execute();
//    	String points = getPoints.getCached();
//    	if (points != null)
//    		displayMapOverlay(points);
//    }
//    
//    private void displayMapOverlay(String pointsStr){
//    	Drawable marker = getResources().getDrawable(R.drawable.map_marker);
//    	EventMapOverlay overlay = new EventMapOverlay(marker);
//    	try {
//			JSONArray points = new JSONArray(pointsStr);
//			for (int i=0;i<points.length();i++){
//				JSONObject point = points.getJSONObject(i);
//				double lat = point.getJSONArray("loc").getDouble(0);
//				double lng = point.getJSONArray("loc").getDouble(1);
//				overlay.addOverlay(lat, lng, point.getString("rev"));
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		MapView mapView = (MapView) findViewById(R.id.dashboard_map);
//		mapView.getOverlays().add(overlay);
//    }
//    
//    private class EventMapOverlay extends ItemizedOverlay<OverlayItem>{
//    	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
//    	private ArrayList<String> revs = new ArrayList<String>();
//    	
//		public EventMapOverlay(Drawable defaultMarker) {
//			super(boundCenterBottom(defaultMarker));
//		}
//		
//		@Override
//		protected boolean onTap(int index) {
//			Intent i = new Intent(Intent.ACTION_VIEW);
//			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
//			i.putExtra("com.connectsy2.events.revision", revs.get(index));
//			startActivity(i);
//			return true;
//		}
//		
//		public void addOverlay(double lat, double lng, String rev) {
//			GeoPoint point = new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
//			OverlayItem overlay = new OverlayItem(point, rev, rev);
//		    overlays.add(overlay);
//		    revs.add(rev);
//		    populate();
//		}
//		
//		@Override
//		protected OverlayItem createItem(int i) {
//			return overlays.get(i);
//		}
//
//		@Override
//		public int size() {
//			return overlays.size();
//		}
//    	
//    }
//
//	@Override
//	protected boolean isRouteDisplayed() {
//		return false;
//	}
//
//	public void freshLocation() {
//		setMapLocation();
//	}
//
//	public void onApiRequestFinish(int status, String response, int code) {
//		displayMapOverlay(response);
//	}
//
//	public void onApiRequestError(int httpStatus, String response, int retCode) {}
}
