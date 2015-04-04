package com.metafora.droid;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 *	Vehicle is an object for working with the TransLoc openAPI
 *	
 *
 */

public class Vehicle 
{
	String vehicleID;
	String type;
	String lat;
	String lng;
	String timestamp;
	MarkerOptions markerOptions;
	String heading;
	
	public MarkerOptions getMarkerOptions() 
	{
		return markerOptions;
	}

	public void setMarkerOptions(MarkerOptions markerOptions) 
	{
		this.markerOptions = markerOptions;
	}

	public Vehicle()
	{}

	public Vehicle(String vehicleID, String lat, String lng, String timestamp, MarkerOptions marker) 
	{
		this.vehicleID = vehicleID;
		this.lat = lat;
		this.lng = lng;
		this.timestamp = timestamp;
		this.markerOptions = marker;
	}
	
	public Vehicle(String vehicleID, String lat, String lng, String timestamp, MarkerOptions marker, String type)
	{
		this.vehicleID = vehicleID;
		this.type = type;
		this.lat = lat;
		this.lng = lng;
		this.timestamp = timestamp;
		this.markerOptions = marker;
	}

	public String getVehicleID() 
	{
		return vehicleID;
	}

	public void setVehicleID(String vehicleID) 
	{
		this.vehicleID = vehicleID;
	}

	public String getLat() 
	{
		return lat;
	}

	public void setLat(String lat) 
	{
		this.lat = lat;
	}

	public String getLng() 
	{
		return lng;
	}

	public void setLng(String lng) 
	{
		this.lng = lng;
	}

	public String getHeading() 
	{
		return heading;
	}

	public void setHeading(String heading) 
	{
		this.heading = heading;
	}

	public String getTimestamp() 
	{
		return timestamp;
	}

	public void setTimestamp(String timestamp) 
	{
		this.timestamp = timestamp;
	}

}
