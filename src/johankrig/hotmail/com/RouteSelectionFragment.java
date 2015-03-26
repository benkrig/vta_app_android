//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.

//TO DO: 


package johankrig.hotmail.com;

import johankrig.hotmail.com.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private Button loadProgressButton = null;
    
    //Interface
    Communicator comm;
	
	//Private variables
	private Button route1Button;
	private Button route2Button;
    private Button route3Button;
    private ImageButton textDirectionsButton;
    private ImageButton gotoPlaceDetailsButton;
    private DirectionsAsyncTask drawRoute;
    private DecodeRouteJSON draw;
	private View rootView;
    String googleDirectionsResultJSON;
	private ImageView routeZoomInButton;
	private TextView routeZoomOutButton;

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
    		
    		SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
    	}

        comm = (Communicator) getActivity();

        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routeselectionmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    	
    	route1Button = (Button) rootView.findViewById(R.id.routebutton1);
        route2Button = (Button) rootView.findViewById(R.id.routebutton2);
        route3Button = (Button) rootView.findViewById(R.id.routebutton3);

        textDirectionsButton = (ImageButton) rootView.findViewById(R.id.textDirectionsButton);
        
    	loadProgress = (ProgressBar) rootView.findViewById(R.id.routeProgressBar);
        loadProgressButton = (Button) rootView.findViewById(R.id.routeProgressButton);
    	
    	        
        
        
        
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
			        
			    	String strTimeToShow = String.format(Locale.US, "%02d:%02d", (datetime.get(Calendar.HOUR) == 0) ? 12 : datetime.get(Calendar.HOUR), datetime.get(Calendar.MINUTE));;
			        time.setText(strTimeToShow + " "+ am_pm);
			        
	
			        map.clear();
					GPSTracker gps = new GPSTracker(getActivity());
					userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
					
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
					
					if(userLatLng != null && destinationLatLng != null)
					{
						if(drawRoute != null)
						{
							drawRoute.cancel(true);
						}
						
						drawRoute = new DirectionsAsyncTask();
						drawRoute.updateUserLocation(userLatLng, destinationLatLng);
				    	drawRoute.setTime(datetime.getTimeInMillis()/1000);
						drawRoute.setRouteNumber(0);
						drawRoute.execute();
						
						Timer myTimer = new Timer();
		                if(myTask != null)
		                {
		                	myTask.cancel();
		                }
		            	myTask = new GetBusLocationTask(map);
		                myTimer.schedule(myTask, 3000, 1000);
					}
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
        
        final Button routeTimeButton = (Button) rootView.findViewById(R.id.selectRouteDepartTime);
        Calendar datetime = Calendar.getInstance();
        datetime.setTimeInMillis(System.currentTimeMillis());

        String am_pm = null;
        if (datetime.get(Calendar.AM_PM) == Calendar.AM)
            am_pm = "AM";
        else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
            am_pm = "PM";
        
    	String strTimeToShow = String.format(Locale.US, "%02d:%02d", (datetime.get(Calendar.HOUR) == 0) ? 12 : datetime.get(Calendar.HOUR), datetime.get(Calendar.MINUTE));;
        routeTimeButton.setText(strTimeToShow + " "+ am_pm);
        
        routeTimeButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				DialogFragment newFragment = new mytimepicker(routeTimeButton);
                newFragment.show(getFragmentManager(), "timePicker");
			}
        });
        
        
    	//OnClick ROUTEBUTTON1
    	//Main route directions will be drawn onto map
        route1Button.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	route1Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
            	route2Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	route3Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));

            	if(googleDirectionsResultJSON != null)
            	{
		            
                	if(draw != null)
                	{
                		draw.cancel(true);
                	}
                	draw = new DecodeRouteJSON(loadProgress, loadProgressButton, comm, map, googleDirectionsResultJSON, 0);
                	draw.execute();
                	  	
		            Timer myTimer = new Timer();
	                if(myTask != null)
	                {
	                	myTask.cancel();
	                }
	            	myTask = new GetBusLocationTask(map);
	                myTimer.schedule(myTask, 3000, 1000);
            	}
                
                
            }
        });
        
    	//OnClick ROUTEBUTTON2
    	//Alternate route 1 directions will be drawn onto map
        route2Button.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	route2Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
            	route1Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	route3Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));

            	if(googleDirectionsResultJSON != null)
            	{		            

                	if(draw != null)
                	{
                		draw.cancel(true);
                	}
                	
                	DecodeRouteJSON draw = new DecodeRouteJSON(loadProgress, loadProgressButton, comm, map, googleDirectionsResultJSON, 1);
                	draw.execute();
                	  	
		            Timer myTimer = new Timer();
	                if(myTask != null)
	                {
	                	myTask.cancel();
	                }
	            	myTask = new GetBusLocationTask(map);
	                myTimer.schedule(myTask, 3000, 1000);
            	}
            }
        });
        
    	//OnClick ROUTEBUTTON3
    	//Alternate route 2 directions will be drawn onto map
        route3Button.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	route3Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
            	route2Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	route1Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));

            	if(googleDirectionsResultJSON != null)
            	{
                	if(draw != null)
                	{
                		draw.cancel(true);
                	}
                	
                	DecodeRouteJSON draw = new DecodeRouteJSON(loadProgress, loadProgressButton, comm, map, googleDirectionsResultJSON, 2);
                	draw.execute();
                	  	
                	Timer myTimer = new Timer();
	                if(myTask != null)
	                {
	                	myTask.cancel();
	                }
	            	myTask = new GetBusLocationTask(map);
	                myTimer.schedule(myTask, 3000, 1000);
            	}
            }
        });
        
        //takes user to place details fragment
        textDirectionsButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	comm.gotoTextDirections();
            }
        });

        //This button returns the user to home search screen
        gotoPlaceDetailsButton = (ImageButton) rootView.findViewById(R.id.directionsBackButton);
        gotoPlaceDetailsButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {            	
            	//Return to Place Details Screen
                comm.goToPlaceDetails();
                
            }
        });
        
        routeZoomInButton = (ImageView) rootView.findViewById(R.id.routeZoomInButton);
        routeZoomInButton.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		map.animateCamera(CameraUpdateFactory.zoomTo(map.getCameraPosition().zoom+1), 1000, null);
        	}
        	
        });
        routeZoomOutButton = (TextView) rootView.findViewById(R.id.routeZoomOutButton);
        routeZoomOutButton.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		map.animateCamera(CameraUpdateFactory.zoomTo(map.getCameraPosition().zoom-1), 1000, null);
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
	
	private class ResponseObject 
	{
		List<PolylineOptions> polyies;
		List<MarkerOptions> markers;
		public ResponseObject(List<PolylineOptions> polyies, List<MarkerOptions> markers)
		{
			this.polyies = polyies;
			this.markers = markers;
		}
		
		public List<PolylineOptions> getpolyies()
		{
			return polyies;
		}
		public List<MarkerOptions> getmarkers()
		{
			return markers;
		}
	    //accessors
	}	
	public ResponseObject drawPath(String result, int routeNumber) 
	{
		List<PolylineOptions> polyies = new ArrayList<PolylineOptions>();
		List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
		
		
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
		       		   	
		       		   	polyies.add(new PolylineOptions()
		       		   		.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
			                .width(9)
			                .color(Color.argb(190, 121, 14, 189)).geodesic(true));
	    	        }
	        		

		        	if(step.has("transit_details"))
		        	{
		        		JSONObject transit_details = step.getJSONObject("transit_details");
			           
		        		JSONObject arrival_stop = transit_details.getJSONObject("arrival_stop");
		        		JSONObject arrival_location = arrival_stop.getJSONObject("location");
		        		
		        		MarkerOptions markerOptions = new MarkerOptions();
		        		markerOptions.position(new LatLng(arrival_location.getDouble("lat"), arrival_location.getDouble("lng")));
		        		markerOptions.title("Stop name: " + arrival_stop.getString("name"));
		        		String headsign = transit_details.getString("headsign");
		        		if(Character.isDigit(headsign.charAt(0)))
		        		{
			        		markerOptions.snippet("Line: " + headsign);
		        		}
		        		else
		        		{
		        			try
		        			{
			        			JSONObject line = transit_details.getJSONObject("line");
			        			int shortline = line.getInt("short_name");
				        		markerOptions.snippet("Line: " + shortline + " " + headsign);
		        			}
		        			catch(Exception e)
		        			{
				        		markerOptions.snippet("Line: " + headsign);
		        			}
		        		}
		        		
		        		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
		        		markerOptions.flat(true);
		        		
		        		markers.add(markerOptions);
			         
		        		JSONObject departure_stop = transit_details.getJSONObject("departure_stop");
		        		JSONObject departure_location = departure_stop.getJSONObject("location");
			           		
		        		markerOptions = new MarkerOptions();
		        		markerOptions.position(new LatLng(departure_location.getDouble("lat"), departure_location.getDouble("lng")));
		        		markerOptions.title("Stop: " + departure_stop.getString("name"));
		        		markerOptions.snippet("Line:  " + transit_details.getString("headsign"));
		        		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
		        		markerOptions.flat(true);
		        		markers.add(markerOptions);
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
	        			
	        			polyies.add(new PolylineOptions()
        				.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
        				.width(9)
        				.color(Color.argb(180, 0, 100, 0)).geodesic(true));		
	        		}
	        	}		        
	        }
	    } 
	    catch (JSONException e) 
	    {            
            SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
	    }
	    
	    ResponseObject resp = new ResponseObject(polyies, markers);
        return resp;
	} 
	
	//
	private class DirectionsAsyncTask extends AsyncTask<Void, Void, ResponseObject>
	{
		private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    private long time = System.currentTimeMillis()/1000;
	    private LatLng eloc;
	    private LatLng uloc;
	    private String json;
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
	        loadProgressButton.setVisibility(View.GONE);
	        loadProgress.setVisibility(View.VISIBLE);
			
	        MarkerOptions markerOptions = new MarkerOptions();
    		markerOptions.position(eloc);
    		markerOptions.flat(true);
    		markerOptions.title("Destination");
    		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_place));
    		markerOptions.flat(true);
    		map.addMarker(markerOptions);	     
    		
	        directionsurl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + uloc.latitude + "," + uloc.longitude + "&destination=" + eloc.latitude + "," + eloc.longitude + "&sensor=true" + "&departure_time=" + time + "&mode=transit&alternatives=true" + "&key=" + DAPIKEY;
			Log.d("url", directionsurl);
	    }
	    @Override
	    protected ResponseObject doInBackground(Void... params) 
	    {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet("http://metafora.herokuapp.com/route");
            String body = "";
 
            JSONObject jsonObject = new JSONObject();
            
            JSONObject userloc = new JSONObject();
            JSONObject destloc = new JSONObject();

            try
            {
	            userloc.put("lat", uloc.latitude);
	            userloc.put("lng", uloc.longitude);
	            destloc.put("lat", eloc.latitude);
	            destloc.put("lng", eloc.longitude);
	            
	            jsonObject.put("userlatlng", userloc);
	            jsonObject.put("destinationlatlng", destloc);
	            jsonObject.put("unixtimestamp", System.currentTimeMillis());
	            jsonObject.put("manufacturer", ""+android.os.Build.MANUFACTURER);
	            jsonObject.put("brand", ""+android.os.Build.BRAND);
	            jsonObject.put("device", ""+android.os.Build.DEVICE);
	            jsonObject.put("sdkversion", ""+android.os.Build.VERSION.SDK_INT);
	            jsonObject.put("devicemodel", ""+android.os.Build.MODEL);
	            jsonObject.put("product", ""+android.os.Build.PRODUCT);
	            
            }
            catch(Exception e)
            {
            	SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
            }

            body = jsonObject.toString();
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("body", body);
            httpPost.setHeader("Content-type", "application/json");
 
            try 
            {
				httpclient.execute(httpPost);
			} 
            catch (ClientProtocolException e) 
			{
				e.printStackTrace();
				
				SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
			} 
            catch (IOException e) 
			{
				e.printStackTrace();
				
				SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
			}
            httpPost.abort();
            
            
	        JSONParser jParser = new JSONParser();
	        //1 for directions api
	        json = jParser.getDirectionApiJsonResponse(directionsurl);
	        ResponseObject response = null;
	        
	        if(json!=null)
	        {
	            response = drawPath(json, route);
	            Log.d("json", json+"");
	        }
	        
	        return response;
	    }
	    @Override
	    protected void onPostExecute(ResponseObject result) 
	    {
	        super.onPostExecute(result);  
	        loadProgressButton.setVisibility(View.VISIBLE);
	        loadProgress.setVisibility(View.GONE);
	        
	        googleDirectionsResultJSON = json;
	        
            comm.updateDirectionsList(json, route);
	        
	        List<PolylineOptions> polyies = result.getpolyies();
	        List<MarkerOptions> markers = result.getmarkers();
	        
	        for (PolylineOptions temp : polyies) 
	        {
	        	map.addPolyline(temp);
	        }
	        for (MarkerOptions temp : markers)
	        {
	        	map.addMarker(temp);
	        }
	    }
	}
	
	//updates userLatLng, destinationLatLng
	public void updateFragment(LatLng dest)
	{
		try
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
				
				route1Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
	        	route2Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
	        	route3Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
	        	
				drawRoute = new DirectionsAsyncTask();
				drawRoute.updateUserLocation(userLatLng, destinationLatLng);
		    	drawRoute.setRouteNumber(0);
				drawRoute.execute();
				
				Timer myTimer = new Timer();
	            if(myTask != null)
	            {
	            	myTask.cancel();
	            }
	        	myTask = new GetBusLocationTask(map);
	            myTimer.schedule(myTask, 3000, 1000);
			}
		}
		catch(Exception e)
		{
	    	SendErrorAsync log = new SendErrorAsync(e+"");
        	log.execute();
		}

	}
	
}