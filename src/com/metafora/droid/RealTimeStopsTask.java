package com.metafora.droid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;



public class RealTimeStopsTask extends TimerTask 
{
	private View row;
	private Activity main;
	Stop stop;

	
	public RealTimeStopsTask(View row, Activity main)
	{
		this.row = row;
		this.main = main;
	}
	
	
	public void init(LatLng stopLocations) throws JSONException
	{
		stop = null;
		Init initializer = new Init(stopLocations);
		initializer.execute();
	}
	

	class Init extends AsyncTask<String, Void, String>
	{
		LatLng stopLocations;
	
		public Init (LatLng stopLocations)
		{
			this.stopLocations = stopLocations;
		}
		
		@Override
		protected String doInBackground(String... params)
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
	    protected void onPostExecute(String b) 
	    {
	    	stop = null;
			JSONObject stopsObj = null;
			JSONArray jstops = null;
			
			try 
			{
				stopsObj = new JSONObject(b);
				
				jstops = stopsObj.getJSONArray("data");
			} 
			catch (JSONException e) 
			{}
			if(stopLocations != null)
			{	
				for(int i = 0; i < jstops.length(); i++)
				{
					try
					{
						JSONObject ite = jstops.getJSONObject(i);
						JSONObject iteloc = ite.getJSONObject("location");
						LatLng itell =  new LatLng(iteloc.getDouble("lat"), iteloc.getDouble("lng"));
		
						Location iterationStop = new Location("");
						iterationStop.setLatitude(itell.latitude);
						iterationStop.setLongitude(itell.longitude);
						
						Location directionStop = new Location("");
						directionStop.setLatitude(stopLocations.latitude);
						directionStop.setLongitude(stopLocations.longitude);
						
						
						float d = iterationStop.distanceTo(directionStop);
							
						if(d < 5)
						{
							stop = new Stop(ite.getString("stop_id"), itell);
						}
					}
					catch(Exception e)
					{
							
					}
				}
			}
	    } 
	}
	
	@Override 
	public void run()
	{
		main.runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				//update routes
				if(stop == null)
					return;
				
				Update u = new Update(stop);
				u.execute();
			}
		});
	}
	
	class Update extends AsyncTask<String, Void, String>
	{
		Stop stops;

		public Update (Stop stop)
		{
			this.stops = stop;
		}
		
		@Override
		protected String doInBackground(String... params)
	    {
			return getArrivalEstimates(stop);
			
	    }

	    @Override
	    protected void onPostExecute(String b) 
	    {
	    	try 
	    	{
				updateView(b);
			} catch (JSONException e) 
			{}
	    }
	}
	
	
	private void updateView(String arrivalEstimates) throws JSONException 
	{
    	String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    	
    	JSONObject estimates = new JSONObject(arrivalEstimates);
    	JSONArray data = estimates.getJSONArray("data");
    	
    	JSONObject dataObj = data.getJSONObject(0);
    	
    	JSONArray arrivalsArray = dataObj.getJSONArray("arrivals");
    	
    	Date current = new Date();
		TextView directionsText = (TextView) row.findViewById(R.id.nextarrivalsid);

    	Long times[] = new Long[arrivalsArray.length()];
    	
    	for(int i = 0; i < arrivalsArray.length(); i++)
    	{
    		

    		JSONObject arrival = arrivalsArray.getJSONObject(i);
    		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
    		DateFormat df = sdf;
    		Date date = null;
    		try 
    		{
				 date = df.parse(arrival.getString("arrival_at"));
				 
			} catch (ParseException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if(date.after(current))
    		{
    			Log.d("date","success");
    			Log.d("current", current.toString());
    			Log.d("test", date.toString());
    			long diff = date.getTime() - current.getTime();
			    long diffMinutes = diff / (60 * 1000) % 60;
    			times[i] = diffMinutes;
    		}
    		else
    		{
    			Log.d("date","badie");
    		}
    	}
    	Arrays.sort(times);
    	String text = "";
    	for(int i = 0; i < 3 && i < times.length; i ++)
    	{
    		if(i == (times.length - 1))
    		{
    			text += times[i]+"";
    		}
    		else
    		{
    			text += times[i]+", ";
    		}
    	}
    	
    	directionsText.setVisibility(View.VISIBLE);
    	directionsText.setText(Html.fromHtml("Arrivals here in: " + "<font color=#790ebd>" + text + "</font>" +  " minutes"));
	}

	private String getArrivalEstimates(Stop stop)
    {
    	String urlString = "https://transloc-api-1-2.p.mashape.com/arrival-estimates.json?agencies=255&stops=";
    	String requestHeader = "X-Mashape-Key";
    	String value = "y28dPGGYKPmshBzOH0xTKSodfOLYp1bJUc2jsnG4TPIT9jW4TZ";
    	String json = "";

    	urlString += (stop.id);

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








