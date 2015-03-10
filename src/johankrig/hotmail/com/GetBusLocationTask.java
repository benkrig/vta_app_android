package johankrig.hotmail.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import android.graphics.Point;
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
	public HashMap<String, Bus> bushHashMap = new HashMap<String, Bus>();
	//Markers
	public HashMap<String, Marker> markerList = new HashMap<String, Marker>();
	
	public Boolean firstRun = true;

	
	public GetBusLocationTask(GoogleMap map)
	{
		this.map = map;
		 firstRun = true;
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
		public DrawBus()
		{
		}

	    @Override
	    protected ArrayList<MarkerOptions> doInBackground(String... params) {
	    	buslist.clear();

			String result;
			JSONParser j = new JSONParser();
			result = j.getVehicleJson();
			
			try
			{
			json = new JSONObject(result);
			data = json.getJSONObject("data");

			buses = data.getJSONArray("255");
			
			for(int c = 0; c < buses.length(); c ++)
			{
				//buses.getJSONObject(c);
				//returns the current bus object in the loop
				
				JSONObject location = buses.getJSONObject(c).getJSONObject("location");
				LatLng position = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));
				
				//Create Marker Options for bus location
				markerOptions = new MarkerOptions();
	    		markerOptions.position(position);
	    		markerOptions.title("Bus: " + buses.getJSONObject(c).getString("vehicle_id"));
	    		markerOptions.snippet(buses.getJSONObject(c).getString("last_updated_on"));
	    		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
	    		buslist.add(markerOptions);
	    		
	    		bushHashMap.put(buses.getJSONObject(c).getString("vehicle_id"), new Bus(buses.getJSONObject(c).getString("vehicle_id"), location.getString("lat"), location.getString("lng"), buses.getJSONObject(c).getString("last_updated_on") ));
	    	
			}

			}
			catch(Exception e)
			{      
				SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	    	}
			
			return buslist;
	    }

	    @Override
	    protected void onPostExecute(ArrayList<MarkerOptions> busesOptions) 
	    {
	    	update(busesOptions);
	    }

	}

	public void update(ArrayList<MarkerOptions> busesOptions) 
	{
		if(markerList.isEmpty())
		{
			for(int c = 0; c < busesOptions.size(); c ++)
	        {
				MarkerOptions current = busesOptions.get(c);
				this.markerList.put(current.getTitle(), map.addMarker(current));
	        }
		}
		else
		{
			for(int c = 0; c < busesOptions.size(); c ++)
			{
				//test current.getTitle() against every current marker
				//current markers on map are stored in markerList, a HashMap<String, Marker> markerList()
				//HashMap keys are formatted as follows "Bus: 000000", zeros are the vehicle id
		    	if(markerList.containsKey(busesOptions.get(c).getTitle()))
		    	{
			    		animateMarker(markerList.get(busesOptions.get(c).getTitle()), busesOptions.get(c).getPosition(), busesOptions.get(c).isVisible());
			    }
			    else
			    {
			    	markerList.put(busesOptions.get(c).getTitle(), map.addMarker(busesOptions.get(c)));
			    }
			}
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
        final long duration = 500;

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
                        //marker.setVisible(false);
                    } 
                    else 
                    {
                        marker.setVisible(true);
                    }
                }
            }
        });
}
	

}
