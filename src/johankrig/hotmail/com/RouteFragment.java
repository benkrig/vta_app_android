//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.

//TO DO: 
//1. add button for route selection
//2. add text based directions option


package johankrig.hotmail.com;

import johankrig.hotmail.com.R;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class RouteFragment extends Fragment
{
	//Global variables
	public GoogleMap map;
    public LatLng destinationLatLng = new LatLng(0, 0);
    public LatLng userLatLng = new LatLng(0,0);
    LatLng updateLatLng;
    
    //Interface
    Communicator comm;
    
    //Holds the String value of EditText searchBar field from MainFragment.java
	private String destination;
	
	//Private variables
	private Button routebtn1;
	private Button routebtn2;
    private Button routebtn3;
    Button mainMapButton;
    Button addressNameButton;
    GeoAsyncTask findDestination = new GeoAsyncTask();
    DirectionsAsyncTask drawRoute = new DirectionsAsyncTask();
    
	

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
        final View rootView = inflater.inflate(R.layout.fragment_route, container, false);
        comm = (Communicator) getActivity();
    	
    	//SET A BUTTON OR TEXT FIELD TO SHOW CURRENT DESTINATION  
        addressNameButton = (Button) rootView.findViewById(R.id.addressNameButton);
    	mainMapButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
                comm.respond();
            }
        });
        
    	//Set up map for RouteFragment
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routemap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
        //could be error here
        updateLatLng = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
        //CameraUpdate update = CameraUpdateFactory.newLatLngZoom(updateLatLng, 10);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
        
    	

    	routebtn1 = (Button) rootView.findViewById(R.id.button1);
    	//OnClick ROUTEBUTTON1
    	//Main route directions will be drawn onto map
        routebtn1.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	//Route number is hard coded based on button number
             	drawRoute.execute();
                //could be error here
                updateLatLng = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
                //CameraUpdate update = CameraUpdateFactory.newLatLngZoom(updateLatLng, 10);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
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
            	drawRoute.execute();		
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
            	drawRoute.execute();
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
	            //Tranform the String response RESULT into a JSON object
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
	                @SuppressWarnings("unused")
					Polyline line = map.addPolyline(new PolylineOptions()
	                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
	                .width(2)
	                .color(Color.BLUE).geodesic(true));
	           }

	    } 
	    catch (JSONException e) 
	    {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();

	    }
	} 
	
	//
	private class DirectionsAsyncTask extends AsyncTask<Void, Void, String>
	{
	    private ProgressDialog progressDialog;
		private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    private long time = System.currentTimeMillis()/1000;
	    private LatLng eloc;
	    private LatLng uloc;

	    //HARDCODED FOR NOW CHANGE THIS
	    private int route = 1;
		private String directionsurl;

		//takes a LatLng for the start point and a string for the endpoint
		//using google geocode api finds the LatLng of the String Adress
		//then uses google directions api to get directions from the points
		//if directions are received it will return a TRUE boolean, otherwise it will return a FALSE
		
		public void updateUserLocation(LatLng uloc, LatLng eloc)
		{
			this.uloc = new LatLng(uloc.latitude, uloc.longitude);
			this.eloc = new LatLng(eloc.latitude, eloc.longitude);
		}
		
	    @Override
	    protected void onPreExecute() 
	    {
	        super.onPreExecute(); 
	        progressDialog = new ProgressDialog(getActivity());
	        progressDialog.setMessage("Fetching route, Please wait...");
	        progressDialog.setIndeterminate(true);
	        progressDialog.show();
			directionsurl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + uloc.latitude + "," + uloc.longitude + "&destination=" + eloc.latitude + "," + eloc.longitude + "&sensor=true" + "&departure_time=" + time + "&mode=transit&alternatives=true" + "&key=" + DAPIKEY;
	        
	    }
	    @Override
	    protected String doInBackground(Void... params) 
	    {
	        JSONParser jParser = new JSONParser();
	        //1 for directions api
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
		public void updateDestination(String destination)
		{
			dest = destination;
			geocodingurl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + dest + "&components=administrative_area:CA|country:US&key=" + DAPIKEY + "&sensor=true";
		}
		
	    @Override
	    protected void onPreExecute() 
	    {
	        super.onPreExecute();
	        progressDialog = new ProgressDialog(getActivity());
	        progressDialog.setMessage("Finding destination, Please wait...");
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
	           //Transform the string into a json object
	           try 
	           {
	        	   JSONObject json = new JSONObject(result);
	        	   JSONArray addresses = json.getJSONArray("results");
	        	   JSONObject address = addresses.getJSONObject(0);
	        	   JSONObject geometry = address.getJSONObject("geometry");
	        	   JSONObject location = geometry.getJSONObject("location");
	         	   location.get("lat");
	        	   //Public LatLng in RouteFragment.java
	         	   //Something to do with JSON formatting, have to parse double from string
	        	   destinationLatLng = new LatLng ((Double.parseDouble(location.getString("lat"))),(Double.parseDouble(location.getString("lng"))));
	           } 
	           catch (JSONException e) 
	           {
		            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
	           }
	        }
	        else
	        {
	            Toast.makeText(getActivity(), "No Location found", Toast.LENGTH_SHORT).show();
	        }
	    }
	}

	//gets user location and destination.
	//recalls GeoAsyncTask and updates DrawPath variables
	public void update(String destination, double latitude, double longitude) 
	{
		this.destination = destination;
		addressNameButton.setText(this.destination);
		
		GeoAsyncTask getDestination = new GeoAsyncTask();
		//Send destination string to GeoAsyncTask and get the LatLng for the string
		getDestination.updateDestination(this.destination);
		getDestination.execute();
		
		//Get current user location
		userLatLng = new LatLng(latitude, longitude);
		
		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
	}
	
}