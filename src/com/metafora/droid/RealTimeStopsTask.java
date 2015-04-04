package com.metafora.droid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class RealTimeStopsTask extends TimerTask 
{
	private ArrayList<Stop> stops;
	
	public void init(LatLng[] stopLocations) throws JSONException
	{
    	Log.w("in init", "in init");

		stops = new ArrayList<Stop>();
		JSONObject obj;
		JSONArray jstops = null;
		
		try 
		{
			obj = new JSONObject(getAllStops());
			jstops = obj.getJSONArray("data");

		} 
		catch (JSONException e) 
		{}
		
		for(LatLng stop : stopLocations)
		{
			if(stop != null)
			{
				for(int i = 0; i < jstops.length(); i++)
				{
					JSONObject ite = jstops.getJSONObject(i);
					JSONObject iteloc = ite.getJSONObject("location");
					LatLng itell =  new LatLng(iteloc.getDouble("lat"), iteloc.getDouble("lng"));

					Location l1 = new Location("");
					l1.setLatitude(itell.latitude);
					l1.setLongitude(itell.longitude);
					
					Location l2 = new Location("");
					l2.setLatitude(stop.latitude);
					l2.setLongitude(stop.longitude);
					
					
					float d = l1.distanceTo(l2);
					
					if(d < 50)
					{
						stops.add(new Stop(ite.getString("stop_id"), itell));
				    	Log.w("has", "stop");
					}
					else
					{}
				}
			}
		}
	}
	
	private String getAllStops()
    {
    	String urlString = "https://transloc-api-1-2.p.mashape.com/stops.json?agencies=255";
    	String requestHeader = "X-Mashape-Key";
    	String value = "y28dPGGYKPmshBzOH0xTKSodfOLYp1bJUc2jsnG4TPIT9jW4TZ";
    	String json = "";
    	try
    	{
    		URL url = new URL(urlString);
		
    		URLConnection urlConnection = url.openConnection();
    		urlConnection.setRequestProperty(requestHeader, value);
    		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    		String inputLine;

    		while ((inputLine = in.readLine()) != null) 
    		{
    			json = json + inputLine;
    		}
    		in.close();
    	}
    	catch(Exception e)
    	{}
    	
    	return json;
    }
	
	@Override
	public void run() 
	{
		//update routes
		if(stops.isEmpty())
			return;
		
		getArrivalEstimates(stops);

	}
	private String getArrivalEstimates(ArrayList<Stop> stops)
    {
    	String urlString = "https://transloc-api-1-2.p.mashape.com/arrival-estimates.json?agencies=255&stops=";
    	String requestHeader = "X-Mashape-Key";
    	String value = "y28dPGGYKPmshBzOH0xTKSodfOLYp1bJUc2jsnG4TPIT9jW4TZ";
    	String json = "";
    	
    	for(int i = 0; i < stops.size(); i++)
    	{
    		if(i == (stops.size() - 1))
    		{
    			urlString += (stops.get(i).id);
    		}
    		else
    		{
    			urlString.concat(stops.get(i)+",");
    	    	Log.w("", urlString);
    		}
    	}
    	try
    	{
    		URL url = new URL(urlString);
		
    		URLConnection urlConnection = url.openConnection();
    		urlConnection.setRequestProperty(requestHeader, value);
    		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    		String inputLine;

    		while ((inputLine = in.readLine()) != null) 
    		{
    			json = json + inputLine;
    		}
    		in.close();
    	}
    	catch(Exception e)
    	{}
    	return json;
    }

}

class Stop 
{
	public String id;
	public LatLng location;
	
	public Stop(String id, LatLng location)
	{
		this.id = id;
		this.location = location;
	}
}
