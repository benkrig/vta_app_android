package johankrig.hotmail.com;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

//Directions Class
//constructed with route nu



public class GoogleDirectionsAPI 
{
	public GoogleDirectionsAPI() 
	{
		
	}
	private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	private JSONObject json;
	HttpURLConnection connection = null;  
	
	//requests a json response from google directions api and stores in json
	public void getDirections(double slat, double slon, double elat, double elon) throws Exception
	{
		long time = System.currentTimeMillis()/1000;
		String endpoint = "https://maps.googleapis.com/maps/api/directions/json?origin=" + slat + "," + slon + "&destination=" + elat + "," + elon+ "&sensor=true" + "&departure_time=" + time + "&mode=transit&alternatives=true" + "&key=" + DAPIKEY;
		URL url = new URL(endpoint);
		try 
		{
			Scanner scan = new Scanner(url.openStream());
		    String response = new String();
		    while (scan.hasNext())
		        response += scan.nextLine();
		    scan.close();
		    System.out.println(response);
		    
		    // build a JSON object
		    try
		    {
		    	json = new JSONObject(response);
		    }
		    catch(JSONException e)
		    {
		    	e.printStackTrace();
	    	}
		 } 
		 catch (IOException e) 
		 {
		 	e.printStackTrace();
		 }
	}
	
	
	//calls google api, puts directions into 
	private JSONObject getJson(int routeNum, double slat, double slon, double elat, double elon) throws Exception
	{
		JSONArray routeArray = json.getJSONArray("routes");
        JSONObject route = routeArray.getJSONObject(routeNum);
        return route;
	}
	private List<LatLng> decodePoly(String encoded) 
	{

		Log.i("Location", "String received: "+encoded);
	    ArrayList<LatLng> poly = new ArrayList<LatLng>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    while (index < len) 
	    {
	        int b, shift = 0, result = 0;
	        do 
	        {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        LatLng p = new LatLng((int) (((double) lat /1E5)* 1E6), (int) (((double) lng/1E5   * 1E6)));
	        poly.add(p);
	    }

	   for(int i=0;i<poly.size();i++){
	       Log.i("Location", "Point sent: Latitude: "+poly.get(i).latitude+" Longitude: "+poly.get(i).longitude);
	   }
	    return poly;
	}

	//draws routeNum onto map.
	public void drawPath(int routeNum, double slat, double slon, double elat, double elon, GoogleMap map) throws Exception 
	{
	    try
	    {
	    	//gets json route object from Directions class, contains a route from
	    	//the google maps api call, which can contain 1-3 routes.
	    	
	    	JSONObject routes = getJson(routeNum, slat, slon, elat, elon);
	    	JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
	    	String encodedString = overviewPolylines.getString("points");
	    	List<LatLng> list = decodePoly(encodedString);
	    	
	    	map.clear();
	    	for(int z = 0; z<list.size()-1;z++)
	    	{
	    		LatLng src= list.get(z);
	    		
	    		LatLng dest= list.get(z+1);
	    		Polyline line = map.addPolyline(new PolylineOptions()
	    		.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
	    		.width(5)
	    		.visible(true)
	    		.color(Color.BLUE).geodesic(true));
	    	}
	    
	    }
	    catch (JSONException e) 
	    {
	    	e.printStackTrace();
	    }
	} 
	public void getCurrentLocation(Context context, GoogleMap map) 
	{
	    Location myLocation  = map.getMyLocation();
	    if(myLocation!=null)
	    {
	        double dLatitude = myLocation.getLatitude();
	        double dLongitude = myLocation.getLongitude();
	        Log.i("APPLICATION"," : "+dLatitude);
	        Log.i("APPLICATION"," : "+dLongitude);
	        
	        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 8));

	    }
	    else
	    {
	        
	        
	    }

	}


}
