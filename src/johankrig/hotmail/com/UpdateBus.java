package johankrig.hotmail.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


class MyTimerTask extends TimerTask 
{
	GoogleMap map;
	HashMap<String, Bus> busses = new HashMap<String, Bus>();

	
	MarkerOptions markerOptions;
	HashMap<String, Marker> markers = new HashMap<String, Marker>();
	private Context c;
	
	public MyTimerTask(Context c, GoogleMap map)
	{
		this.c = c;
		this.map = map;


	}
	public void run() 
	{/*
		String result;
		JSONParser j = new JSONParser();
		result = j.getVehicleJson();
		JSONObject json = null;
		try {
			json = new JSONObject(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray buses = null;
		JSONArray data = null;
		try {
			 data = json.getJSONArray("data");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//0 is 255
			//changed data to json array
			buses = data.getJSONArray(0);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(int c = 0; c < buses.length(); c ++)
		{
			Log.println(0, "JSON", ""+c);
			JSONObject bus = null;
			try {
				bus = buses.getJSONObject(c);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject location = null;
			try {
				location = bus.getJSONObject("location");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				busses.put(bus.getString("vehicle_id"), new Bus(bus.getString("vehicle_id"), location.getString("lat"), location.getString("lng")));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		DrawBus draw = new DrawBus(c, map);
		draw.execute();
	 }
	private void drawBus(final HashMap<String, Bus> busses2) 
	{
		
		Iterator<String> keySetIterator = busses2.keySet().iterator();
		while(keySetIterator.hasNext())
		{

			final String key = keySetIterator.next();
			markerAsync m = new markerAsync(markerOptions, busses2, key, map);
			m.execute();
			/*LatLng position = new LatLng(Double.parseDouble(busses2.get(key).getLat()),Double.parseDouble(busses2.get(key).getLng()));

			markerOptions = new MarkerOptions();
			markerOptions.position(position);
			markerOptions.title("Bus"+keySetIterator);
			
			markerOptions.snippet(busses2.get(key).getVehicleID());
			map.addMarker(markerOptions);*/
			
			//loop through and test every marker with every other marker.
			//if marker with vehicle id exists, delete it
			/*for(int c = 0; c < busses2.size(); c++)
			{
				if(markers.get(key).getSnippet() == busses2.get(key).getVehicleID() )
				{
					markers.get(key).remove();
					
					LatLng position = new LatLng(Double.parseDouble(busses2.get(key).getLat()),Double.parseDouble(busses2.get(key).getLng()));

					markerOptions = new MarkerOptions();
					markerOptions.position(position);
					markerOptions.title("Bus");
					markerOptions.snippet(busses2.get(key).getVehicleID());
					
					markers.put(busses2.get(key).getVehicleID(), map.addMarker(markerOptions));
				}
				else
				{
					LatLng position = new LatLng(Double.parseDouble(busses2.get(key).getLat()),Double.parseDouble(busses2.get(key).getLng()));

					markerOptions = new MarkerOptions();
					markerOptions.position(position);
					markerOptions.title("Bus");
					markerOptions.snippet(busses2.get(key).getVehicleID());
					
					markers.put(busses2.get(key).getVehicleID(), map.addMarker(markerOptions));
				}
				
			}*/
			
		}

	}
	
}
 
class markerAsync extends AsyncTask<Void, Void, Void> {
    
	MarkerOptions markerOptions = new MarkerOptions();
	HashMap<String, Bus> busses2;
	String key;
	GoogleMap map;
	JSONArray buses = null;

	
	public markerAsync(MarkerOptions op, HashMap<String, Bus> j, String k, GoogleMap m)
	{
		markerOptions = op;
		busses2= j;
		key = k;
		map = m;
	}
	
	protected void onPreExecute() {
        // perhaps show a dialog 
        // with a progress bar
        // to let your users know
        // something is happening
    }
	
    protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
    	

		String result;
		JSONParser j = new JSONParser();
		result = j.getVehicleJson();
		JSONObject json = null;
		JSONArray buses = null;
		JSONArray data = null;
		try {
			json = new JSONObject(result);
		
			 data = json.getJSONArray("data");
		
			//0 is 255
			//changed data to json array
			buses = data.getJSONArray(0);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(int c = 0; c < buses.length(); c ++)
		{
			Log.println(0, "JSON", ""+c);
			JSONObject bus = null;
			try
			{
				bus = buses.getJSONObject(c);
			
				JSONObject location = null;
			
				location = bus.getJSONObject("location");
			
				busses2.put(bus.getString("vehicle_id"), new Bus(bus.getString("vehicle_id"), location.getString("lat"), location.getString("lng")));
			}
			 catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    	LatLng position = new LatLng(Double.parseDouble(busses2.get(key).getLat()),Double.parseDouble(busses2.get(key).getLng()));


		markerOptions.position(position);
		markerOptions.title("Bus"+key);
		return null;
		}
		return null;
	}


    
    protected void onPostExecute() {

		for(int c = 0; c < buses.length(); c ++)
		{
			Log.println(0, "JSON", ""+c);
			JSONObject bus = null;
			try
			{
				bus = buses.getJSONObject(c);
			
				JSONObject location = null;
			
				location = bus.getJSONObject("location");
			
				busses2.put(bus.getString("vehicle_id"), new Bus(bus.getString("vehicle_id"), location.getString("lat"), location.getString("lng")));
			}
			 catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    	LatLng position = new LatLng(Double.parseDouble(busses2.get(key).getLat()),Double.parseDouble(busses2.get(key).getLng()));


		markerOptions.position(position);
		markerOptions.title("Bus"+key);

		map.addMarker(markerOptions);

    }


};
}