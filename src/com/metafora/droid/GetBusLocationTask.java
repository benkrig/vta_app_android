package com.metafora.droid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class GetBusLocationTask extends TimerTask 
{
	GoogleMap map;
	public HashMap<String, Vehicle> vehicleHashMap = new HashMap<String, Vehicle>();
	//Markers
	public HashMap<String, Marker> markerList = new HashMap<String, Marker>();
	public Boolean firstRun = true;
	public Context context;
	public GPSTracker gps;
	JSONParser j;
	public int DISTANCE_LIMIT = 5000;
	
	public GetBusLocationTask(GoogleMap map, Context context)
	{
		this.map = map;
		this.context = context;
		gps = new GPSTracker(context);
		gps.getLocation();
		firstRun = true;
		j = new JSONParser();

	}

	public void run() 
	{
		if(firstRun == true)
		{
			DrawBus draw = new DrawBus();
			draw.execute();
			firstRun = false;
		}
		else
		{
			DrawBus draw = new DrawBus();
			draw.execute();
		}
		
	}
	
	class DrawBus  extends AsyncTask<String, Void, ArrayList<MarkerOptions>> 
	{	
		MarkerOptions markerOptions;
		GoogleMap map;
		JSONObject data = null;
		JSONObject json = null;
		JSONArray buses = null;
		JSONObject location = null;
		ArrayList<MarkerOptions> buslist = new ArrayList<MarkerOptions>();	
		String[] vehicleIdArray = null;

		public DrawBus()
		{
		}

	    @Override
	    protected ArrayList<MarkerOptions> doInBackground(String... params) 
	    {
	    	buslist.clear();

			String result;
			
			result = j.getVehicleJson();
			try
			{
				json = new JSONObject(result);
				data = json.getJSONObject("data");
	
				buses = data.getJSONArray("255");
				vehicleIdArray = new String[buses.length()];
				gps.getLocation();
				for(int c = 0; c < buses.length(); c ++)
				{	
					JSONObject location = buses.getJSONObject(c).getJSONObject("location");
					LatLng position = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));
					
					Location loc = new Location("Loc");
					loc.setLatitude(position.latitude);
					loc.setLongitude(position.longitude);
					
					
					if(gps.location.distanceTo(loc) < DISTANCE_LIMIT)
					{
						//Create Marker Options for bus location
						markerOptions = new MarkerOptions();
			    		markerOptions.position(position);
			    		markerOptions.rotation((float)buses.getJSONObject(c).getInt("heading"));
			    		markerOptions.title("Lightrail " +buses.getJSONObject(c).getString("call_name"));
			    		markerOptions.snippet("Speed: " + (int) buses.getJSONObject(c).getDouble("speed") + " mph");
			    		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.transit_icon_dropshadow));
			    		buslist.add(markerOptions);
			    		
			    		vehicleHashMap.put(buses.getJSONObject(c).getString("vehicle_id"), new Vehicle(buses.getJSONObject(c).getString("vehicle_id"), location.getString("lat"), location.getString("lng"), buses.getJSONObject(c).getString("last_updated_on"), markerOptions ));
					}
					else
					{
						if(markerList.containsKey(buses.getJSONObject(c).getString("vehicle_id")))
						{
							markerList.get(buses.getJSONObject(c).getString("vehicle_id")).remove();
							markerList.remove(buses.getJSONObject(c).getString("vehicle_id"));
						}
						vehicleHashMap.remove(buses.getJSONObject(c).getString("vehicle_id"));

					}
				}

			}
			catch(Exception e)
			{}
			
			return buslist;
	    }

	    @Override
	    protected void onPostExecute(ArrayList<MarkerOptions> buslist) 
	    {
	    	update(buslist, vehicleHashMap);
	    }

	}

	/**
	 * @param busesOptions
	 * @param bushHashMap2
	 * 
	 * 
	 */
	public void update(ArrayList<MarkerOptions> busesOptions, final HashMap<String, Vehicle> bushHashMap2) 
	{
		if(markerList.isEmpty())
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run() 
				{
					for (Vehicle value : bushHashMap2.values()) 
					{
						markerList.put(value.getVehicleID(), map.addMarker(value.getMarkerOptions()));
					}					
				}
			});	
		}
		else
		{		
			new Handler().post(new Runnable()
			{

				@Override
				public void run() 
				{
					for (Vehicle value : bushHashMap2.values()) 
					{
						if(markerList.containsKey(value.getVehicleID()))
				    	{
							markerList.get(value.getVehicleID()).setSnippet(value.getMarkerOptions().getSnippet());
					   		animateMarker(markerList.get(value.getVehicleID()), value.getMarkerOptions().getPosition(), false);
					    }
					    else
					    {
					    	markerList.put(value.getVehicleID(), map.addMarker(value.getMarkerOptions()));
					    }
					}
				}
			});
        }  
	}
	
    public void animateMarker(final Marker marker, final LatLng toPosition,
            final boolean hideMarker) 
    {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        
        //Animation duration
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() 
        {
            @Override
            public void run() 
            {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) 
                {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
                else 
                {
                    if (hideMarker) 
                    {
                        marker.setVisible(false);
                    } 
                    else 
                    {
                        marker.setVisible(true);
                        /*
                         * InfoWindows are static views rendered on top of 
                         * the map canvas. To show a change to the window 
                         * we have to hideInfoWindow() and call
                         * showInfoWindow()
                         * */
                        if(marker.isInfoWindowShown())
                        {
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        }
                    }
                }
            }
        });
    }
}
