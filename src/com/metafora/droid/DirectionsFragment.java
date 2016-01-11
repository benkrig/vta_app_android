package com.metafora.droid;

import java.util.ArrayList;
import java.util.Timer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.model.LatLng;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;



public class DirectionsFragment extends Fragment 
{
	ImageButton directionsBackButton;
	public View rootView;
    public FragmentCommunicator comm;
	public ListView mainListView;  
	TextDirectionsMobileAdapter directionsAdapter;
	public String DirectionsJSON;
	View headerView;
	View footerView;
	public void cancelTimers()
	{
		if(directionsAdapter != null)
			directionsAdapter.stopTimers();
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
        rootView = inflater.inflate(R.layout.fragment_directions, container, false);
        footerView = inflater.inflate(R.layout.directions_footer_row, null, false);
        headerView = inflater.inflate(R.layout.directions_header_row, null, false);
        
        directionsBackButton = (ImageButton) rootView.findViewById(R.id.directionsBackButton);
        directionsBackButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {            	
            	//Return to home search screen
                comm.gotoRouteSelection();
            }
        });
		return rootView;
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
    	super.onActivityCreated(savedInstanceState);
        mainListView = (ListView) rootView.findViewById(R.id.textDirectionsListView);  
        comm = (FragmentCommunicator) getActivity();
	}
    
    public void updateDirectionsList(String JSON, int routeNumber)
    {
    	if(directionsAdapter != null)
    			directionsAdapter.stopTimers();
    	
        DirectionsJSON = JSON;   
		try 
		{
		
			final JSONObject json = new JSONObject(DirectionsJSON);
			JSONArray routes = json.getJSONArray("routes");
			final JSONObject route = routes.getJSONObject(routeNumber);
	           
	           
			JSONArray legs = route.getJSONArray("legs");
			JSONObject leg = legs.getJSONObject(0);
	           
			JSONArray steps = leg.getJSONArray("steps");
	        
			//create header view
			if(leg.has("departure_time"))
			{
				JSONObject departure_time = leg.getJSONObject("departure_time");
				String departTime = departure_time.getString("text");

				TextView header1 = (TextView) headerView.findViewById(R.id.departTextView);
				
				header1.setText(Html.fromHtml("Depart at: " + "<b><font color=#790ebd>" + departTime + "</font></b>"));				
			}
			if(leg.has("arrival_time"))
			{				
				JSONObject arrival_time = leg.getJSONObject("arrival_time");
				String arrivalTime = arrival_time.getString("text");

				TextView header2 = (TextView) headerView.findViewById(R.id.arriveTextView);
				header2.setText(Html.fromHtml("Arrive at: " + "<b><font color=#790ebd>" + arrivalTime + "</font></b>"));				
			}
			//end header veiw

	           
			final String[] instructions = new String[steps.length()];
			final String[] travel_modes = new String[steps.length()];
			final String[] distances = new String[steps.length()];
			final String[] durations = new String[steps.length()];
			final String[] transitArrivals = new String[steps.length()];
			final String[] vehicleTypes = new String[steps.length()];
			final LatLng[] locations = new LatLng[steps.length()];
	           
			for(int index = 0; index < steps.length(); index++)
			{	        	
				JSONObject step = steps.getJSONObject(index);
				String html_instructions = step.getString("html_instructions");
				String travel_mode = step.getString("travel_mode");
	        	   
				JSONObject startLocation = step.getJSONObject("start_location");
				locations[index] = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
				
				
				JSONObject distance = step.getJSONObject("distance");
				
				String distanceString = distance.getString("text");
				String[] distanceparts = distanceString.split("\\ ");
				String htmlDistanceString = "<b>" + distanceparts[0] +"</b>" + "<small><font color=#212121>" + distanceparts[1] + "</font></small>";
				
				JSONObject duration = step.getJSONObject("duration");
				int durationSec = duration.getInt("value");
				String durationString = duration.getString("text");
				durationString = (int)(durationSec * 0.0166667) + " min";

				if(travel_mode.equals("TRANSIT"))
				{
					JSONObject transitDetails = step.getJSONObject("transit_details");
					JSONObject departureTime = transitDetails.getJSONObject("departure_time");
					transitArrivals[index] = departureTime.getString("text");
	        		   
					JSONObject line = transitDetails.getJSONObject("line");
					JSONObject vehicle = line.getJSONObject("vehicle");
					vehicleTypes[index] = vehicle.getString("name");
				}
				
				distances[index] = htmlDistanceString;
				durations[index] = durationString;
				instructions[index] = html_instructions;
				travel_modes[index] = travel_mode;
			}
			
			if(directionsAdapter == null)
			{
				directionsAdapter = new TextDirectionsMobileAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes, locations);
			}
	           
	    	//set footer view
	    	final JSONObject lastleg = legs.getJSONObject(legs.length()-1);	         
	    	final TextView footerEndAddressTextView = (TextView) footerView.findViewById(R.id.directionsEndAddress);
	    	footerEndAddressTextView.post(new Runnable()
	    	{

				@Override
				public void run() 
				{
					
			    	try 
			    	{
						footerEndAddressTextView.setText(lastleg.getString("end_address"));
					} catch (JSONException e) 
					{
					}

				}
	    		
	    	});
	    	final TextView footerTotalDistanceTextView = (TextView) footerView.findViewById(R.id.directionsDistance);
	    	final TextView footerTotalTimeTextView = (TextView) footerView.findViewById(R.id.directionsTime);
	    	final TextView footerFare = (TextView) footerView.findViewById(R.id.directionsFare);

	    	
	    	//Get total Distance
	    	int count = 0;
	    	for(int c = 0; c < legs.length(); c++)
	    	{
	    		JSONObject currentLeg = legs.getJSONObject(c);
	    		JSONObject distance = currentLeg.getJSONObject("distance");
	    		count += distance.getInt("value");
	    	}
	    	final int totalDistanceMeters = count;
	    	
	    	footerTotalDistanceTextView.post(new Runnable()
	    	{

				@Override
				public void run() 
				{
			    	footerTotalDistanceTextView.setText(Html.fromHtml("<b><font color=#790ebd>" + (int)(totalDistanceMeters * 0.00062137) + "</font></b>" + "<small><font color=#212121> mi</font></small>"));
				}
	    		
	    	});

	    	//Get total trip time in seconds
	    	int secondCount = 0;
	    	for(int c = 0; c < legs.length(); c++)
	    	{
	    		JSONObject current_leg = legs.getJSONObject(c);
	    		JSONObject duration = current_leg.getJSONObject("duration");
	    		secondCount += duration.getInt("value");
	    	}
	    	final int seconds = secondCount;

	    	footerTotalTimeTextView.post(new Runnable()
	    	{
				@Override
				public void run() 
				{
			    	if(((int)((seconds/60)/60)) == 0)
			    	{
			    		footerTotalTimeTextView.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((seconds/60)%60)) + "</font></b>" + "<small><font color=#212121> mins</font></small>"));
			    	}
			    	else
			    	{
			    		footerTotalTimeTextView.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((seconds/60)/60)) + "</font></b>" + "<small><font color=#212121> hrs </font></small>" + "<b><font color=#790ebd>" + ((int)((seconds/60)%60)) + "</font></b>" + "<small><font color=#212121> mins </font></small>"));
			    	}     					
				}
	    	});
	    	
	    	footerFare.post(new Runnable()
	    	{

				@Override
				public void run() 
				{

			    	//Get route Fare
			    	if(route.has("fare"))
			    	{
			    		String fareCost = null;
						try 
						{
							fareCost = "$"+route.getJSONObject("fare").getDouble("value");
						} 
						catch (JSONException e) 
						{
				     		footerFare.setText("");
						}
			    		footerFare.setText(Html.fromHtml("<b><font color=#790ebd>" + fareCost + "</font></b>"));
			    	}
			     	else
			     	{
			     		footerFare.setText("");
			     	}					
				}
	    		
	    	});
	           
	    	//end footer
	    	if(mainListView.getAdapter() == null)
	    	{
	    		mainListView.post(new Runnable()
	    		{

					@Override
					public void run() 
					{
			    		mainListView.addHeaderView(headerView);
			    		mainListView.addFooterView(footerView);
			    		mainListView.setAdapter(directionsAdapter);	 						
					}
	    			
	    		});
	    	}
	    	else
	    	{
	    		mainListView.post(new Runnable()
	    		{

					@Override
					public void run() 
					{
						mainListView.removeHeaderView(headerView);
			    		mainListView.removeFooterView(footerView);

			    		mainListView.addHeaderView(headerView);
			    		mainListView.addFooterView(footerView);
				           
			    		mainListView.setAdapter(new TextDirectionsMobileAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes, locations));
					}
	    		});
	    	}
 	   	}
		catch (JSONException e) 
		{
		}
    }

    
}


