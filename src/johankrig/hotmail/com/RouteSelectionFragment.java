//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.

//TO DO: 


package johankrig.hotmail.com;

import johankrig.hotmail.com.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

public class RouteSelectionFragment extends Fragment
{
	//Global variables
	public GoogleMap map;
    public LatLng destinationLatLng = new LatLng(0, 0);
    public LatLng userLatLng = new LatLng(0,0);
    Timer myTimer = new Timer();
    GetBusLocationTask myTask = null;
    public ProgressBar loadProgress = null;
    
    //Interface
    Communicator comm;
	
	//Private variables
	private Button routebtn1;
	private Button routebtn2;
    private Button routebtn3;
    private ImageButton textDirectionsButton;
    ImageButton mainMapButton;
    DirectionsAsyncTask drawRoute;
    GetBusLocationTask bus;
    GPSTracker gps;
	private View rootView;
    String googleDirectionsResultJSON;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
    	if (rootView!= null) 
    	{
    		ViewGroup parent = (ViewGroup) rootView.getParent();
    		if (parent != null)
    			parent.removeView(rootView);
    	}
    	try 
    	{
    		rootView = inflater.inflate(R.layout.fragment_route_selection, container, false);
    	} 
    	catch (InflateException e) 
    	{
    		/* map is already there, just return view as it is */
    	}
    	
    	loadProgress = (ProgressBar) rootView.findViewById(R.id.routeProgressBar);
        comm = (Communicator) getActivity();
    	
        gps = new GPSTracker(getActivity());
        
    	//Set up map for RouteFragment
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routeselectionmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
        
        //This button returns the user to home search screen
        
        mainMapButton = (ImageButton) rootView.findViewById(R.id.directionsBackButton);
        mainMapButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {            	
            	//Return to Place Details Screen
                comm.goToPlaceDetails();
                
            }
        });
        
        class mytimepicker extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener, android.content.DialogInterface.OnClickListener 
        {
        	Button time = null;
        	int callCount = 0;
        	
        	public mytimepicker(Button time)
        	{
        		this.time = time;
        	}
		    @Override
		    public Dialog onCreateDialog(Bundle savedInstanceState) 
		    {
		    	
		        final Calendar c = Calendar.getInstance();
		        int hour = c.get(Calendar.HOUR_OF_DAY);
		        int minute = c.get(Calendar.MINUTE);
		        
		        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute,
		                DateFormat.is24HourFormat(getActivity()));
		        
		        dialog.setTitle("Departure Time");
		        dialog.setCancelable(true);
		        dialog.setCanceledOnTouchOutside(true);
		        dialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "Cancel", this);
		        return dialog;
		    }
		
		    public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		    {
		    	if(callCount==1)
		    	{
			    	String am_pm = "";
	
			        Calendar datetime = Calendar.getInstance();
			        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			        datetime.set(Calendar.MINUTE, minute);
	
			        if (datetime.get(Calendar.AM_PM) == Calendar.AM)
			            am_pm = "AM";
			        else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
			            am_pm = "PM";
			        
			    	String strTimeToShow = String.format(Locale.US, "%02d:%02d", datetime.get(Calendar.HOUR), datetime.get(Calendar.MINUTE));;
			        time.setText(strTimeToShow + " "+ am_pm);
			        
	
			        map.clear();
					GPSTracker gps = new GPSTracker(getActivity());
					userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
					
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
					
					if(drawRoute != null)
					{
						drawRoute.cancel(true);
					}
					drawRoute = new DirectionsAsyncTask();
					drawRoute.updateUserLocation(userLatLng, destinationLatLng);
			    	drawRoute.setTime(datetime.getTimeInMillis()/1000);
					drawRoute.setRouteNumber(0);
					drawRoute.execute();
		    	}
		    	callCount++;
		    }
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(which == DialogInterface.BUTTON_NEGATIVE)
				{
					dialog.cancel();
					dialog.dismiss();
				}
			}
		}
        
        final Button time = (Button) rootView.findViewById(R.id.selectRouteDepartTime);
        time.setOnClickListener(new OnClickListener()
        {

			@Override
			public void onClick(View v) 
			{
				DialogFragment newFragment = new mytimepicker(time);
                newFragment.show(getFragmentManager(), "timePicker");
			}
        	
        });
        
        
    	routebtn1 = (Button) rootView.findViewById(R.id.routebutton1);
    	//OnClick ROUTEBUTTON1
    	//Main route directions will be drawn onto map
        routebtn1.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	//Route number is hard coded based on button number
            	/*
            	if(drawRoute.getStatus() == AsyncTask.Status.RUNNING)
            	{
            		drawRoute.cancel(true);
                	drawRoute = new DirectionsAsyncTask();
            	}
            	if(drawRoute.getStatus() == AsyncTask.Status.FINISHED)
                {
                	drawRoute = new DirectionsAsyncTask();
                }
                if(drawRoute.getStatus() == AsyncTask.Status.PENDING)
                {
                	map.clear();
                	drawRoute.setRouteNumber(0);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
    		    	drawRoute.setTime(System.currentTimeMillis()/1000);
                	drawRoute.execute(); 
                }*/
            	map.clear();
	            drawPath(googleDirectionsResultJSON, 0);
	            
	            Log.d("json", googleDirectionsResultJSON+"");
	            comm.updateDirectionsList(googleDirectionsResultJSON, 0);
                
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
            	/*if(drawRoute.getStatus() == AsyncTask.Status.RUNNING)
            	{
            		drawRoute.cancel(true);
                	drawRoute = new DirectionsAsyncTask();
            	}
            	if(drawRoute.getStatus() == AsyncTask.Status.FINISHED)
                {
                	drawRoute = new DirectionsAsyncTask();
                }
                if(drawRoute.getStatus() == AsyncTask.Status.PENDING)
                {
                	map.clear();
                	drawRoute.setRouteNumber(1);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
    		    	drawRoute.setTime(System.currentTimeMillis()/1000);
                	drawRoute.execute();
                }*/

            	map.clear();
	            drawPath(googleDirectionsResultJSON, 1);
	            
	            Log.d("json", googleDirectionsResultJSON+"");
	            comm.updateDirectionsList(googleDirectionsResultJSON, 1);

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
            	/*if(drawRoute.getStatus() == AsyncTask.Status.RUNNING)
            	{
                	drawRoute = new DirectionsAsyncTask();
            	}
            	if(drawRoute.getStatus() == AsyncTask.Status.FINISHED)
                {
                	drawRoute = new DirectionsAsyncTask();
                }
                if(drawRoute.getStatus() == AsyncTask.Status.PENDING)
                {
                	
                	map.clear();
                	drawRoute.setRouteNumber(2);
            		drawRoute.updateUserLocation(userLatLng, destinationLatLng);
    		    	drawRoute.setTime(System.currentTimeMillis()/1000);
                	drawRoute.execute();
                }*/
            	

            	map.clear();
	            drawPath(googleDirectionsResultJSON, 2);
	            
	            Log.d("json", googleDirectionsResultJSON+"");
	            comm.updateDirectionsList(googleDirectionsResultJSON, 2);

                Timer myTimer = new Timer();
                if(myTask !=null)
                {
                	myTask.cancel();
                }
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
	           Log.d("Routes: ", ""+routeArray.length());

	           
	           //Select which route to draw
	           //Throws a JSONException if no route is found at index routeNumber
	           JSONObject routes = routeArray.getJSONObject(routeNumber);
	           	           
	           JSONArray legsArray = routes.getJSONArray("legs");
	           JSONObject legsObject = legsArray.getJSONObject(0);
	           
	           JSONArray stepsArray = legsObject.getJSONArray("steps");
	           
	           for(int z = 0; z < stepsArray.length(); z++)
	           {

	        	   JSONObject step = stepsArray.getJSONObject(z);
	        	   
        		   JSONObject polyline = step.getJSONObject("polyline");
        		   
	        	   if(step.getString("travel_mode").equals("TRANSIT"))
	        	   {	    	           
	    	           String encodedString = polyline.getString("points");
	        		   List<LatLng> list = decodePoly(encodedString);
	        		   
	        		   for(int x = 0; x < list.size() - 1; x++)
	    	           {
		        		   LatLng src = list.get(x);
			               LatLng dest = list.get(x+1);
		        		   @SuppressWarnings("unused")
							Polyline line = map.addPolyline(new PolylineOptions()
			                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
			                .width(9)
			                
			                .color(Color.argb(190, 255, 102, 126)).geodesic(true));
	    	           }
	        	   }
	        	   
	        	   if(step.getString("travel_mode").equals("WALKING"))
	        	   {	        		   

	    	           String encodedString = polyline.getString("points");
	        		   List<LatLng> list = decodePoly(encodedString);
	        		   
	        		   for(int x = 0; x < list.size() - 1; x++)
	    	           {
		        		   LatLng src = list.get(x);
			               LatLng dest = list.get(x+1);
		        		   @SuppressWarnings("unused")
		        		   Polyline line = map.addPolyline(new PolylineOptions()
			                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
			                .width(9)
			                .color(Color.argb(150, 0, 0, 255)).geodesic(true)
			                );
		        		   
	    	           }
	        	   }
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
		           		markerOptions.title("Stop name: " + arrival_stop.getString("name"));
		           		markerOptions.snippet("Line: " + transit_details.getString("headsign"));
		           		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
		           		markerOptions.flat(true);
		           		map.addMarker(markerOptions);
		           		
		         
		           		JSONObject departure_stop = transit_details.getJSONObject("departure_stop");
		           		JSONObject departure_location = departure_stop.getJSONObject("location");
		           		
		           		markerOptions = new MarkerOptions();
		           		markerOptions.position(new LatLng(departure_location.getDouble("lat"), departure_location.getDouble("lng")));
		           		markerOptions.title("Stop name: " + departure_stop.getString("name"));
		           		markerOptions.snippet("Line:  " + transit_details.getString("headsign"));
		           		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
		           		markerOptions.flat(true);
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
		private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    private long time = System.currentTimeMillis()/1000;
	    private LatLng eloc;
	    private LatLng uloc;

	    //HARDCODED FOR NOW CHANGE THIS
	    private int route;
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
		public void setTime(long time)
		{
			this.time = time;
		}
		
	    @Override
	    protected void onPreExecute() 
	    {
	        super.onPreExecute();
	        loadProgress.setVisibility(View.VISIBLE);
			directionsurl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + uloc.latitude + "," + uloc.longitude + "&destination=" + eloc.latitude + "," + eloc.longitude + "&sensor=true" + "&departure_time=" + time + "&mode=transit&alternatives=true" + "&key=" + DAPIKEY;
			Log.d("url", directionsurl);
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
	        loadProgress.setVisibility(View.GONE);
	        
	        googleDirectionsResultJSON = result;
	        //BAD TEST, RESULT WILL NEVER RETURN A NULL, FIX THIS
	        if(result!=null)
	        {
	            drawPath(result, route);
	            Log.d("json", result+"");
	            comm.updateDirectionsList(result, route);
	        }
	    }
	}
	
//updates userLatLng, destinationLatLng
	public void updateFragment(LatLng dest)
	{
		if(!dest.equals(destinationLatLng))
		{
			map.clear();
			
			GPSTracker gps = new GPSTracker(getActivity());
			userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
			destinationLatLng = dest;
			
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
			
			if(drawRoute !=null)
			{
				drawRoute.cancel(true);
			}
			drawRoute = new DirectionsAsyncTask();
			drawRoute.updateUserLocation(userLatLng, destinationLatLng);
	    	drawRoute.setRouteNumber(0);
			drawRoute.execute();
		}
		else
		{
		}
	}
	
}