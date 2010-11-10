package com.connectsy;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocManager implements LocationListener {
	public interface LocListener {
		public void freshLocation();
	}
	
	private LocationManager lm;
	private String provider;
	private Criteria criteria;
	private Location loc;
	private LocListener listener;
	
	public LocManager(Context c){
        criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setSpeedRequired(false);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        lm = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
        provider = lm.getBestProvider(criteria, true);
		loc = lm.getLastKnownLocation(provider);
	}

	public String distanceFrom(double lat, double lng){
		if (loc == null) return null;
		float[] result = new float[2];
		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), 
				lat, lng, result);
		String distance;
		if (result[0] > 1000)
			distance = Integer.toString(((int)result[0])/1000)+" kilometres";
		else
			distance = Integer.toString(((int)result[0]))+" metres";
		String direction;
		if (result[1] < 0)
			result[1] = 360+result[1];
		int region = (int) (result[1]/22.5);
		if (region == 16 || region == 0 || region == 1)
			direction = "north";
		else if (region == 2 || region == 3)
			direction = "northeast";
		else if (region == 4 || region == 5)
			direction = "east";
		else if (region == 6 || region == 7)
			direction = "southeast";
		else if (region == 8 || region == 9)
			direction = "south";
		else if (region == 10 || region == 11)
			direction = "southwest";
		else if (region == 12 || region == 13)
			direction = "west";
		else if (region == 14 || region == 15)
			direction = "northwest";
		else
			direction = Integer.toString(region)+" "+Float.toString(result[1]);
		return distance+" "+direction;
	}
	
	public void onLocationChanged(Location location) {
		loc = location;
		if (listener != null)
			listener.freshLocation();
	}

	public void onProviderDisabled(String provider) {
        provider = lm.getBestProvider(criteria, true);
	}
	
	public Location getLocation(){
		return loc;
	}
	
	public void requestUpdates(LocListener l){
        lm.requestLocationUpdates(provider, 0, 0, this);
		listener = l;
	}
	
	public void onProviderEnabled(String provider) {}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
