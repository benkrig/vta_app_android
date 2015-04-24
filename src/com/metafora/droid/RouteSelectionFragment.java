//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.

//TO DO: 


package com.metafora.droid;

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
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;

public class RouteSelectionFragment extends Fragment
{
	int[] secondCount = new int[3];
	//Global variables
	public GoogleMap map;
    public LatLng destinationLatLng = new LatLng(0, 0);
    public LatLng userLatLng = new LatLng(0,0);
    Timer myTimer = new Timer();
    GetBusLocationTask myTask = null;
    public ProgressBar loadProgress = null;
    private Button loadProgressButton = null;
    
    DisplayMetrics displayMetrics;
    int px;
	
    //Interface
    FragmentCommunicator comm;
	
	//Private variables
	private Button route1Button;
	private Button route2Button;
    private Button route3Button;
    private ImageButton textDirectionsButton;
    private ImageButton routeBackButton;
    private DirectionsAsyncTask drawRoute;
    private DecodeRouteJSON draw;
	private View rootView;
    String googleDirectionsResultJSON;
	private ImageView myLocationButton;
	private GPSTracker gps;
	private LinearLayout bottomBar;

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
    	    	
        return rootView;
    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
				
    	bottomBar = (LinearLayout) getActivity().findViewById(R.id.routeBottomBar);
    	gps = new GPSTracker(getActivity());
        comm = (FragmentCommunicator) getActivity();

        displayMetrics = getActivity().getResources().getDisplayMetrics();
        px = Math.round(7 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
        
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routeselectionmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
    	
        gps.getLocation();
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 15));
		
    	route1Button = (Button) getActivity().findViewById(R.id.routebutton1);
        route2Button = (Button) getActivity().findViewById(R.id.routebutton2);
        route3Button = (Button) getActivity().findViewById(R.id.routebutton3);

        textDirectionsButton = (ImageButton) getActivity().findViewById(R.id.textDirectionsButton);
        
    	loadProgress = (ProgressBar) getActivity().findViewById(R.id.routeProgressBar);
        loadProgressButton = (Button) getActivity().findViewById(R.id.routeProgressButton);
    	
    	        
        
        
        
        class mytimepicker extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener, android.content.DialogInterface.OnClickListener 
        {
        	Button time = null;
        	
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
		        
		        return dialog;
		    }
		
		    @Override
		    public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		    {
		    	Log.d("lll", ""+view.isShown());
		    	if(view.isShown())
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
						

						route1Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
			        	route2Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
			        	route3Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
			        	
						
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
		            	myTask = new GetBusLocationTask(map, getActivity());
		                myTimer.schedule(myTask, 0, 2000);
					}
		    	}		    	
		    }
		    
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(which == DialogInterface.BUTTON_NEGATIVE)
				{
			    	Log.d("lll", "lklklk00");

					dialog.cancel();
					dialog.dismiss();
				}
				if(which == DialogInterface.BUTTON_POSITIVE)
				{
			    	Log.d("lll", "popopopop");

				}
				
			}
		}
        
        final Button routeTimeButton = (Button) getActivity().findViewById(R.id.selectRouteDepartTime);
        Calendar datetime = Calendar.getInstance();
        datetime.setTimeInMillis(System.currentTimeMillis());

        String am_pm = null;
        if (datetime.get(Calendar.AM_PM) == Calendar.AM)
            am_pm = "AM";
        else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
            am_pm = "PM";
        
    	final String strTimeToShow = String.format(Locale.US, "%02d:%02d", (datetime.get(Calendar.HOUR) == 0) ? 12 : datetime.get(Calendar.HOUR), datetime.get(Calendar.MINUTE));;
    	routeTimeButton.setText(strTimeToShow + " "+ am_pm);

        routeTimeButton.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				DialogFragment newFragment = new mytimepicker(routeTimeButton);
                newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
			}
        });
        
        
    	//OnClick ROUTEBUTTON1
    	//Main route directions will be drawn onto map
        route1Button.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	
            	int buttonColor = ((ColorDrawable) route1Button.getBackground()).getColor();
            	if(buttonColor == getResources().getColor(R.color.greytransparent))
            	{
            		return;          	
            	}
            	route1Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
            	route2Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	route3Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	if(googleDirectionsResultJSON != null)
            	{
		            
                	if(draw != null)
                	{
                		draw.cancel(true);
                	}
                	draw = new DecodeRouteJSON(bottomBar, px, loadProgress, loadProgressButton, comm, map, googleDirectionsResultJSON, 0);
                	draw.execute();
                	if(myTask != null)
    	            {
    	            	myTask.cancel();
    	            }
    	            
    	        	myTask = new GetBusLocationTask(map, getActivity());
    	            myTimer.schedule(myTask, 0, 2000);
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
            	int buttonColor = ((ColorDrawable) route2Button.getBackground()).getColor();
            	if(buttonColor == getResources().getColor(R.color.greytransparent))
            	{
            		return;          	
            	}
            	route2Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
            	route1Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	route3Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));

            	if(googleDirectionsResultJSON != null)
            	{		            

                	if(draw != null)
                	{
                		draw.cancel(true);
                	}
                	
                	DecodeRouteJSON draw = new DecodeRouteJSON(bottomBar, px, loadProgress, loadProgressButton, comm, map, googleDirectionsResultJSON, 1);
                	draw.execute();
                	if(myTask != null)
    	            {
    	            	myTask.cancel();
    	            }
    	            
    	        	myTask = new GetBusLocationTask(map, getActivity());
    	            myTimer.schedule(myTask, 0, 2000);
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
            	int buttonColor = ((ColorDrawable) route3Button.getBackground()).getColor();
            	if(buttonColor == getResources().getColor(R.color.greytransparent))
            	{
            		return;          	
            	}
            	route3Button.setBackgroundColor(getResources().getColor(R.color.greytransparent));
            	route2Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));
            	route1Button.setBackgroundColor(getResources().getColor(R.color.whitetransparent));

            	if(googleDirectionsResultJSON != null)
            	{
                	if(draw != null)
                	{
                		draw.cancel(true);
                	}
                	
                	DecodeRouteJSON draw = new DecodeRouteJSON(bottomBar, px, loadProgress, loadProgressButton, comm, map, googleDirectionsResultJSON, 2);
                	draw.execute();
                	if(myTask != null)
    	            {
    	            	myTask.cancel();
    	            }
    	            
    	        	myTask = new GetBusLocationTask(map, getActivity());
    	            myTimer.schedule(myTask, 0, 2000);
                	  	
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
        routeBackButton = (ImageButton) getActivity().findViewById(R.id.directionsBackButton);
        routeBackButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {            	
            	//Return to Place Details Screen
                comm.goToLocationSearch();
            }
        });
        
        myLocationButton = (ImageView) getActivity().findViewById(R.id.routemylocationbutton);
		myLocationButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				gps.getLocation();
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 15));

			}
		});
    	
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
			                .width(px)
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
		        		
		        		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_sign_blue_dropshadow));
		        		markerOptions.flat(true);
		        		
		        		markers.add(markerOptions);
			         
		        		JSONObject departure_stop = transit_details.getJSONObject("departure_stop");
		        		JSONObject departure_location = departure_stop.getJSONObject("location");
			           	
		        		markerOptions = new MarkerOptions();
		        		markerOptions.position(new LatLng(departure_location.getDouble("lat"), departure_location.getDouble("lng")));
		        		markerOptions.title("Stop: " + departure_stop.getString("name"));
		        		markerOptions.snippet("Line:  " + transit_details.getString("headsign"));
		        		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_sign_blue_dropshadow));
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
        				.width(px)
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
	    	hideBottomBar();
	    	
	        super.onPreExecute();
	        loadProgressButton.setVisibility(View.GONE);
	        loadProgress.setVisibility(View.VISIBLE);
			
	        MarkerOptions markerOptions = new MarkerOptions();
    		markerOptions.position(eloc);
    		markerOptions.title("Destination");
    		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_ic_action_place_dropshadow));
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
	        
	        try {
				JSONObject j = new JSONObject(json);
				JSONArray routes = j.getJSONArray("routes");
				
				JSONObject route1 = routes.getJSONObject(0);
				JSONArray legs = route1.getJSONArray("legs");
				//Get total Seconds
				secondCount[0] = 0;
				for(int c = 0; c < legs.length(); c++)
				{
					JSONObject curleg = legs.getJSONObject(c);
					JSONObject duration = curleg.getJSONObject("duration");
					secondCount[0] += duration.getInt("value");
				}
				
				Log.d("", ""+secondCount[0]);

				
				secondCount[1] = 0;
				JSONObject route2 = routes.getJSONObject(1);
				legs = route2.getJSONArray("legs");
				for(int c = 0; c < legs.length(); c++)
				{
					JSONObject curleg = legs.getJSONObject(c);
					JSONObject duration = curleg.getJSONObject("duration");
					secondCount[1] += duration.getInt("value");
				}

				secondCount[2] = 0;
				JSONObject route3 = routes.getJSONObject(2);
				legs = route3.getJSONArray("legs");
				for(int c = 0; c < legs.length(); c++)
				{
					JSONObject curleg = legs.getJSONObject(c);
					JSONObject duration = curleg.getJSONObject("duration");
					secondCount[2] += duration.getInt("value");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	           
	        
	        
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
	        if(((int)((secondCount[0]/60)/60)) == 0)
	    	{
	        	route1Button.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((secondCount[0]/60)%60)) + "</font></b>" + "<small><font color=#212121> mins</font></small>"));
	    	}
	    	else
	    	{
	        	route1Button.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((secondCount[0]/60)/60)) + "</font></b>" + "<small><font color=#212121> hrs </font></small>" + "<b><font color=#790ebd>" + ((int)((secondCount[0]/60)%60)) + "</font></b>" + "<small><font color=#212121> mins </font></small>"));
	    	}  
	        
	        if(((int)((secondCount[1]/60)/60)) == 0)
	    	{
	        	route2Button.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((secondCount[1]/60)%60)) + "</font></b>" + "<small><font color=#212121> mins</font></small>"));
	    	}
	    	else
	    	{
	        	route2Button.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((secondCount[1]/60)/60)) + "</font></b>" + "<small><font color=#212121> hrs </font></small>" + "<b><font color=#790ebd>" + ((int)((secondCount[1]/60)%60)) + "</font></b>" + "<small><font color=#212121> mins </font></small>"));
	    	}
	        
	        if(((int)((secondCount[2]/60)/60)) == 0)
	    	{
	        	route3Button.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((secondCount[2]/60)%60)) + "</font></b>" + "<small><font color=#212121> mins</font></small>"));
	    	}
	    	else
	    	{
	        	route3Button.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((secondCount[2]/60)/60)) + "</font></b>" + "<small><font color=#212121> hrs </font></small>" + "<b><font color=#790ebd>" + ((int)((secondCount[2]/60)%60)) + "</font></b>" + "<small><font color=#212121> mins </font></small>"));
	    	}  
	        

	        showBottomBar();
	        loadProgressButton.setVisibility(View.VISIBLE);
	        loadProgress.setVisibility(View.GONE);
	        
	        googleDirectionsResultJSON = json;
	        
            comm.updateDirectionsList(json, route);
	        
	        List<PolylineOptions> polyies = result.getpolyies();
	        List<MarkerOptions> markers = result.getmarkers();
	        
	        PolylineOptions t= new PolylineOptions()
	        .width(px)
	        .color(Color.argb(255, 80, 73, 137));
	        for (PolylineOptions temp : polyies) 
	        {
	        		t.addAll(temp.getPoints());
	        }
	        for (MarkerOptions temp : markers)
	        {
	        	map.addMarker(temp);
	        }
        	map.addPolyline(t);

	    }
	}
	
	//updates userLatLng, destinationLatLng
	public void updateFragment(LatLng dest)
	{
		try
		{
			if(!dest.equals(destinationLatLng))
			{
				comm.cancelTimers();
				
				map.clear();
				
				GPSTracker gps = new GPSTracker(getActivity());
				userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
				destinationLatLng = dest;
				
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
				
				if(drawRoute != null)
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
	        	myTask = new GetBusLocationTask(map, getActivity());

	            myTimer.schedule(myTask, 0, 2000);
			}
		}
		catch(Exception e)
		{
	    	SendErrorAsync log = new SendErrorAsync(e+"");
        	log.execute();
		}

	}

	public void goToLocation(LatLng location) 
	{
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
	}
	
	public void hideBottomBar()
	{
		bottomBar.post(new Runnable()
		{
			@Override
			public void run() 
			{
				bottomBar.animate()
		        .translationY(bottomBar.getHeight())
		        .alpha(0.0f)
		        .setDuration(300)
		        .setListener(new AnimatorListenerAdapter() 
		        {
		            @Override
		            public void onAnimationEnd(Animator animation) 
		            {
		                super.onAnimationEnd(animation);
		                bottomBar.setVisibility(View.GONE);
		            }
		        });		
			}
		});
	}
	public void showBottomBar()
	{
		bottomBar.post(new Runnable()
		{
			@Override
			public void run() 
			{
				bottomBar.animate()
		        .translationY(0)
		        .alpha(1.0f)
		        .setDuration(300)
		        .setListener(new AnimatorListenerAdapter() 
		        {
		            @Override
		            public void onAnimationStart(Animator animation) 
		            {
		                super.onAnimationStart(animation);
		        		bottomBar.setVisibility(View.VISIBLE);
		            }
		        });				
			}
		});
	}
}