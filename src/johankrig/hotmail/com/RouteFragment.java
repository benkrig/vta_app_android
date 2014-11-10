//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.

//TO DO: 


package johankrig.hotmail.com;

import johankrig.hotmail.com.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import android.widget.ImageButton;
import android.widget.Toast;

public class RouteFragment extends Fragment
{
	//Global variables
	public GoogleMap map;
    public LatLng destinationLatLng = new LatLng(0, 0);
    public LatLng userLatLng = new LatLng(0,0);
    Timer myTimer = new Timer();
    GetBusLocationTask myTask = null;
    
    //Interface
    Communicator comm;
	
	//Private variables
	private Button routebtn1;
	private Button routebtn2;
    private Button routebtn3;
    private ImageButton textDirectionsButton;
    ImageButton mainMapButton;
    GeoAsyncTask findDestination = new GeoAsyncTask();
    DirectionsAsyncTask drawRoute;
    GetBusLocationTask bus;
    GPSTracker gps;
    

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);
        comm = (Communicator) getActivity();
    	
        gps = new GPSTracker(getActivity());
        
    	//Set up map for RouteFragment
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routemap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        //This button returns the user to home search screen
        
        mainMapButton = (ImageButton) rootView.findViewById(R.id.directionsBackButton);
        mainMapButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	
            	//Erase variables
            	map.clear();
            	
            	//Return to home search screen
                comm.respond();
                
            }
        });
        
        
        //could be error here
        //updateLatLng = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
        //CameraUpdate update = CameraUpdateFactory.newLatLngZoom(updateLatLng, 10);
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
        

    	routebtn1 = (Button) rootView.findViewById(R.id.routebutton1);
    	//OnClick ROUTEBUTTON1
    	//Main route directions will be drawn onto map
        routebtn1.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	//Route number is hard coded based on button number

            	if(drawRoute.getStatus() == AsyncTask.Status.FINISHED)
                {
                	drawRoute = new DirectionsAsyncTask();
                	drawRoute.setRouteNumber(0);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
                }
                if(drawRoute.getStatus() == AsyncTask.Status.PENDING)
                {
                	map.clear();
                	drawRoute.setRouteNumber(0);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
                	drawRoute.execute(); 
                }
                
                Timer myTimer = new Timer();
                if(myTask !=null){
                myTask.cancel();}
            	myTask = new GetBusLocationTask(map);
                myTimer.schedule(myTask, 3000, 1000); 
            }
        });
        
        routebtn2 = (Button) rootView.findViewById(R.id.routebutton2);
    	//OnClick ROUTEBUTTON2
    	//Alternate route 1 directions will be drawn onto map
        routebtn2.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	if(drawRoute.getStatus() == AsyncTask.Status.FINISHED)
                {
                	drawRoute = new DirectionsAsyncTask();
                	drawRoute.setRouteNumber(1);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
                }
                if(drawRoute.getStatus() == AsyncTask.Status.PENDING)
                {
                	map.clear();
                	drawRoute.setRouteNumber(1);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
                	drawRoute.execute();
                }

                Timer myTimer = new Timer();
                if(myTask !=null){
                myTask.cancel();}
            	myTask = new GetBusLocationTask(map);
                myTimer.schedule(myTask, 3000, 1000); 

            }
        });
        
        routebtn3 = (Button) rootView.findViewById(R.id.routebutton3);
    	//OnClick ROUTEBUTTON3
    	//Alternate route 2 directions will be drawn onto map
        routebtn3.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	//Route number is hard coded based on button number
            	if(drawRoute.getStatus() == AsyncTask.Status.FINISHED)
                {
                	drawRoute = new DirectionsAsyncTask();
                	drawRoute.setRouteNumber(2);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
                }
                if(drawRoute.getStatus() == AsyncTask.Status.PENDING)
                {
                	map.clear();
                	drawRoute.setRouteNumber(2);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
                	drawRoute.execute();
                }

                Timer myTimer = new Timer();
                if(myTask !=null){
                myTask.cancel();}
            	myTask = new GetBusLocationTask(map);
                myTimer.schedule(myTask, 3000, 1000); 
            }
        });
        textDirectionsButton = (ImageButton) rootView.findViewById(R.id.textDirectionsButton);
        textDirectionsButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	comm.gotoTextDirections();
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
	           //Get the Routes Array from the JsonObject
	           JSONArray routeArray = json.getJSONArray("routes");
	           
	           //Select which route to draw
	           //Throws a JSONException if no route is found at index routeNumber
	           JSONObject routes = routeArray.getJSONObject(routeNumber);
	           
	           //Select overview_polyline
	           JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
	           String encodedString = overviewPolylines.getString("points");
	           List<LatLng> list = decodePoly(encodedString);

	           //Draw lines between ALL points from LatLng List list onto map
	           for(int z = 0; z < list.size() - 1; z++)
	           {
	                LatLng src = list.get(z);
	                LatLng dest = list.get(z+1);
	                @SuppressWarnings("unused")
					Polyline line = map.addPolyline(new PolylineOptions()
	                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
	                .width(3)
	                .color(Color.BLUE).geodesic(true));
	           }
	           //routes -> legs -> steps -> transit_details -> arrival/location_stop -> location latlng
	           JSONArray legs = routes.getJSONArray("legs");
	           JSONObject leg = legs.getJSONObject(0);
	           JSONArray steps = leg.getJSONArray("steps");
	           //for each step, draw bus stops on map
	           for (int z = 0; z < steps.length(); z ++)
	           {
		           	JSONObject step = steps.getJSONObject(z);
		           	if(step.has("transit_details"))
		           	{
		           		JSONObject transit_details = step.getJSONObject("transit_details");
		           
		           		JSONObject arrival_stop = transit_details.getJSONObject("arrival_stop");
		           		JSONObject arrival_location = arrival_stop.getJSONObject("location");

		           		MarkerOptions markerOptions = new MarkerOptions();
		           		markerOptions.position(new LatLng(arrival_location.getDouble("lat"), arrival_location.getDouble("lng")));
		           		markerOptions.title(transit_details.getString("headsign"));
		           		markerOptions.snippet(arrival_stop.getString("name"));
		           		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
		           		map.addMarker(markerOptions);
		           		
		           		JSONObject departure_stop = transit_details.getJSONObject("departure_stop");
		           		JSONObject departure_location = departure_stop.getJSONObject("location");
		           		
		           		markerOptions = new MarkerOptions();
		           		markerOptions.position(new LatLng(departure_location.getDouble("lat"), departure_location.getDouble("lng")));
		           		markerOptions.title(transit_details.getString("headsign"));
		           		markerOptions.snippet(departure_stop.getString("name"));
		           		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
		           		map.addMarker(markerOptions);
		    		}
		    		//add markers to map
		           
	           }


	           //Draw ALL stops along route

	    } 
	    catch (JSONException e) 
	    {
	    	//Called if no route is found at Index routeNumber
            Toast.makeText(getActivity(), "No route found :: E: "+e.toString(), Toast.LENGTH_LONG).show();
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

		public void setRouteNumber(int num)
		{
			route = num;
		}
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
	        String json = jParser.getDirectionApiJsonResponse(directionsurl);
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
	            comm.updateDirectionsList(result, route);
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
	
//updates userLatLng, destinationLatLng
	public void updateFragment(LatLng dest)
	{
		GPSTracker gps = new GPSTracker(getActivity());
		userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
		destinationLatLng = dest;
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10));
		
		
		drawRoute = new DirectionsAsyncTask();
		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
    	drawRoute.setRouteNumber(0);
		drawRoute.execute();
	}
	
}