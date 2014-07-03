//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.
//

package johankrig.hotmail.com;

import johankrig.hotmail.com.R;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;

public class RouteFragment extends Fragment
{
	//Global variables
	public GoogleMap map;
    public LatLng destinationLatLng = new LatLng(0, 0);
    
    //Holds the String value of EditText searchBar field from MainFragment.java
	private String destination;
	
	//Private variables
	private Button routebtn1;
	private Button routebtn2;
    private Button routebtn3;
    Button btn;
    private LatLng userloc = new LatLng(0, 0);
    private double[] userlocation;
	

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
        final View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        //Get String value of EditText searchBar field from MainFragment.java
        
        //Bundle mainActivityBundle = ((MainActivity) getActivity()).getBundle();
    	//destination = mainActivityBundle.getString("destination");
    	
    	//SET A BUTTON OR TEXT FIELD TO SHOW CURRENT DESTINATION
    	btn = (Button) rootView.findViewById(R.id.mainMapButton);
    	btn.setText(destination);
        
    	//Set up map for RouteFragment
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routemap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
        /*ZOOM TO USER LOCATION AUTO
        LatLng latLng = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        map.moveCamera(update);*/ 
        

    	//SOME PROBLEM WITH THIS LINE
        //userlocation[0] = map.getMyLocation().getLatitude();
        //userlocation[1] = map.getMyLocation().getLongitude();
        
        //IT IS CALLING THIS CODE ON APP STARTUP, CALL IT ONLY WHEN ROUTEBUTTON IS PRESSED
        /* testing for error if(mainActivityBundle.getBoolean("update") == true)
        {            	
        	destination = mainActivityBundle.getString("destination");
        	GeoAsyncTask findDestination = new GeoAsyncTask(destination);
        	findDestination.execute();
        }*/
    	

    	routebtn1 = (Button) rootView.findViewById(R.id.button1);
    	//OnClick ROUTEBUTTON1
    	//Main route directions will be drawn onto map
        routebtn1.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	Bundle newBundle = ((MainActivity) getActivity()).getBundle();
            	if(newBundle.getBoolean("update") == true);
                {            	
                	destination = newBundle.getString("destination");
                	GeoAsyncTask findDestination = new GeoAsyncTask(destination);
                	findDestination.execute();
                }
            	//Route number is hard coded based on button number
            	DirectionsAsyncTask route = new DirectionsAsyncTask(userlocation, destinationLatLng, 0);
            	route.execute();
                
            }
        });
        
        routebtn2 = (Button) rootView.findViewById(R.id.button2);
    	//OnClick ROUTEBUTTON2
    	//Alternate route 1 directions will be drawn onto map
        routebtn2.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	//Route number is hard coded based on button number
            	DirectionsAsyncTask route = new DirectionsAsyncTask(userlocation, destinationLatLng, 1);
            	route.execute();
				
            }
        });
        routebtn3 = (Button) rootView.findViewById(R.id.button3);
    	//OnClick ROUTEBUTTON3
    	//Alternate route 2 directions will be drawn onto map
        routebtn3.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	//Route number is hard coded based on button number
				DirectionsAsyncTask route = new DirectionsAsyncTask(userlocation, destinationLatLng, 2);
            	route.execute();
            }
        });
        
        return rootView;
        
    }
	//decodes google polyline, formula has already been developed online.
	private List<LatLng> decodePoly(String encoded) 
	{

	    List<LatLng> poly = new ArrayList<LatLng>();
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
	        do 
	        {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        LatLng p = new LatLng( (((double) lat / 1E5)),
	                 (((double) lng / 1E5) ));
	        poly.add(p);
	    }

	    return poly;
	}
	
	//drawPath - Edited from online code - Benjamin Krig
	//Takes the raw String response from Google Directions API,
	//Transforms it into a JSONObject and selects the polyline from
	//the given routeNumber.
	//Draws polyline onto RouteFragment map
	public void drawPath(String result, int routeNumber) 
	{
	    try 
	    {
	            //Tranform the reponse string into a JSON object
	           final JSONObject json = new JSONObject(result);
	           JSONArray routeArray = json.getJSONArray("routes");
	           
	           //Select which route to draw
	           //test to see if route exists... add this feature...
	           JSONObject routes = routeArray.getJSONObject(routeNumber);
	           
	           //Select overview_polyline
	           JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
	           String encodedString = overviewPolylines.getString("points");
	           List<LatLng> list = decodePoly(encodedString);

	           //Draw ALL the points in the LatLng List onto map
	           for(int z = 0; z < list.size() - 1; z++)
	           {
	                LatLng src = list.get(z);
	                LatLng dest = list.get(z+1);
	                Polyline line = map.addPolyline(new PolylineOptions()
	                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
	                .width(2)
	                .color(Color.BLUE).geodesic(true));
	           }

	    } 
	    catch (JSONException e) 
	    {

	    }
	} 
	
	//
	private class DirectionsAsyncTask extends AsyncTask<Void, Void, String>
	{
	    private ProgressDialog progressDialog;
		private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    private long time = System.currentTimeMillis()/1000;
	    private double[] uloc;
	    private LatLng eloc;

	    private int route;
		private String directionsurl;

		//takes a LatLng for the start point and a string for the endpoint
		//using google geocode api finds the LatLng of the String Adress
		//then uses google directions api to get directions from the points
		//if directions are received it will return a TRUE boolean, otherwise it will return a FALSE
		
		//Constructor
		//Creates API call from Starting location and destinationLatLng
		DirectionsAsyncTask( double[] userlocation , LatLng destinationLatLng, int routeNum)
	    {
			uloc = userlocation;
	    	eloc = destinationLatLng;
			directionsurl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + uloc[0] + "," + uloc[1] + "&destination=" + eloc.latitude + "," + eloc.longitude + "&sensor=true" + "&departure_time=" + time + "&mode=transit&alternatives=true" + "&key=" + DAPIKEY;
			route = routeNum;
	    }
		
	    @Override
	    protected void onPreExecute() 
	    {
	        super.onPreExecute(); 
	        progressDialog = new ProgressDialog(getActivity());
	        progressDialog.setMessage("Fetching route, Please wait...");
	        progressDialog.setIndeterminate(true);
	        progressDialog.show();
	    }
	    @Override
	    protected String doInBackground(Void... params) 
	    {
	        JSONParser jParser = new JSONParser();
	        String json = jParser.getJSONFromUrl(directionsurl, 1);
	        return json;
	    }
	    @Override
	    protected void onPostExecute(String result) 
	    {
	        super.onPostExecute(result);   
	        progressDialog.hide();        
	        
	        //BAD TEST, RESULT WILL NEVER RETURN A NULL, FIX THIS
	        if(result!=null)
	        {
	            drawPath(result, route);
	        }
	    }
	}

	private class GeoAsyncTask extends AsyncTask<Void, Void, String>
	{
	    private ProgressDialog progressDialog;
		private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    private String dest;
	    private String geocodingurl;

		//takes a LatLng for the start point and a string for the endpoint
		//using google geocode api finds the LatLng of the String Adress
		//then uses google directions api to get directions from the points
		GeoAsyncTask(String destination)
	    {
			dest = destination;
			geocodingurl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + dest + "&components=administrative_area:CA|country:US&key=" + DAPIKEY + "&sensor=true";
	    }
		
	    @Override
	    protected void onPreExecute() 
	    {
	        super.onPreExecute();
	        progressDialog = new ProgressDialog(getActivity());
	        progressDialog.setMessage("Finding route, Please wait...");
	        progressDialog.setIndeterminate(true);
	        progressDialog.show();
	    }
	    
	    @Override
	    protected String doInBackground(Void... params) 
	    {
	        JSONParser jParser = new JSONParser();
	        String json = jParser.getJSONFromUrl(geocodingurl, 0);
	        return json;
	    }
	    
	    @Override
	    protected void onPostExecute(String result) 
	    {
	        super.onPostExecute(result);   
	        progressDialog.hide(); 
	        
	        if(result!=null)
	        {
	           //Tranform the string into a json object
	           try 
	           {
	        	   JSONObject json = new JSONObject(result);
	        	   JSONArray addresses = json.getJSONArray("results");
	        	   JSONObject address = addresses.getJSONObject(0);
	        	   JSONObject geometry = address.getJSONObject("geometry");
	        	   JSONObject location = geometry.getJSONObject("location");
	         	   
	        	   //Public LatLng in RouteFragment.java
	        	   destinationLatLng = new LatLng ((location.getDouble("lat")), ((location.getDouble("lng"))));
	        	   
	        	   //then call directions async task with destination as param
	           } 
	           catch (JSONException e) 
	           {
	        	  
	           }
	        }
	        else
	        {
	        	Builder alert = new AlertDialog.Builder(getActivity().getApplicationContext());
	            alert.setTitle("Error");
	            alert.setMessage("Can't locate destination, please choose a new one!");
	            alert.setPositiveButton("OK", null);
	            alert.show();
	        }
	    }
	}

	public void updateText(String dest) 
	{
		destination = dest;
		btn.setText(dest);
	}
	
}