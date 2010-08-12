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
        
		setUpdater();
		loc = lm.getLastKnownLocation(provider);
	}
	
	private void setUpdater(){
        provider = lm.getBestProvider(criteria, true);
        lm.requestLocationUpdates(lm.getBestProvider(criteria, true), 0, 0, this);
	}
	
	public void onLocationChanged(Location location) {
		loc = location;
		if (listener != null)
			listener.freshLocation();
	}

	public void onProviderDisabled(String provider) {
		setUpdater();
	}
	
	public Location getLocation(){
		return loc;
	}
	
	public void requestUpdates(LocListener l){
		listener = l;
	}
	
	public void onProviderEnabled(String provider) {}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
