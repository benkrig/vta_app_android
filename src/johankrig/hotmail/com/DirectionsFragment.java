package johankrig.hotmail.com;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import johankrig.hotmail.com.R;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
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
	MobileArrayAdapter directionsAdapter;
	public String DirectionsJSON;
	View headerView;
	View footerView;

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
    
    //TODO change routes jsonobject to have variable named Routenumber
    public void updateDirectionsList(String JSON, int routeNumber)
    {
        DirectionsJSON = JSON;   
		try 
		{
		
			final JSONObject json = new JSONObject(DirectionsJSON);
	           
			JSONArray routes = json.getJSONArray("routes");
			JSONObject route = routes.getJSONObject(routeNumber);
	           
	           
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

				//header2.setText("Arrive at: " + arrivalTime);
			}
			//end header veiw

	           
			String[] instructions = new String[steps.length()];
			String[] travel_modes = new String[steps.length()];
			String[] distances = new String[steps.length()];
			String[] durations = new String[steps.length()];
			String[] transitArrivals = new String[steps.length()];
			String[] vehicleTypes = new String[steps.length()];
			LatLng[] locations = new LatLng[steps.length()];
	           

	           
			for(int index = 0; index < steps.length(); index++)
			{
				//consider adding transit details in here as well.
	        	   
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
				directionsAdapter = new MobileArrayAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes, locations);
			}
	           
	    	//set footer view
	    	TextView footer1 = (TextView) footerView.findViewById(R.id.directionsLocation);
	    	JSONObject firstleg = legs.getJSONObject(0);
	    	JSONObject lastleg = legs.getJSONObject(legs.length()-1);
	    	Log.d("firstlegs", firstleg.getString("start_address"));
	    	Log.d("lastleg", lastleg.getString("end_address"));
	    	Log.d("footerview", footerView.toString());
	    	Log.d("footerviewtext", footer1.toString());


	    	footer1.setText(firstleg.getString("start_address"));
	         
	    	TextView footerEndAddress = (TextView) footerView.findViewById(R.id.directionsEndAddress);
	    	footerEndAddress.setText(lastleg.getString("end_address"));
	    	TextView footer2 = (TextView) footerView.findViewById(R.id.directionsDistance);
	    	int distanceMeters = 0;
	    	for(int c = 0; c < legs.length(); c++)
	    	{
	    		JSONObject curleg = legs.getJSONObject(c);
	    		JSONObject distance = curleg.getJSONObject("distance");
	    		distanceMeters += distance.getInt("value");
	    	}
	    	footer2.setText((int)(distanceMeters * 0.00062137) + " mi");

	    	TextView footer3 = (TextView) footerView.findViewById(R.id.directionsTime);
	    	int seconds = 0;
	    	for(int c = 0; c < legs.length(); c++)
	    	{
	    		JSONObject curleg = legs.getJSONObject(c);
	    		JSONObject duration = curleg.getJSONObject("duration");
	    		seconds += duration.getInt("value");
	    	}
	    	if(((int)((seconds/60)/60)) == 0)
	    	{
	    		footer3.setText(((int)((seconds/60)%60)) + " mins");
	    	}
	    	else
	    	{
	    		footer3.setText(((int)((seconds/60)/60)) + " hrs " + ((int)((seconds/60)%60)) + " mins");
	    	}
	           
	    	TextView footerFare = (TextView) footerView.findViewById(R.id.directionsFare);
	    	if(route.has("fare"))
	    	{
	    		String fareCost = "$"+route.getJSONObject("fare").getDouble("value");
	    		footerFare.setText(fareCost);
	    	}
	     	else
	     	{
	     		footerFare.setText("unknown");
	     	}
	           
	    	//end footer
	    	if(mainListView.getAdapter() == null)
	    	{

	    		mainListView.addHeaderView(headerView);
	    		mainListView.addFooterView(footerView);
	    		
	    		mainListView.setAdapter(directionsAdapter);	 
	    	}
	    	else
	    	{
	    		mainListView.removeHeaderView(headerView);
	    		mainListView.removeFooterView(footerView);

	    		mainListView.addHeaderView(headerView);
	    		mainListView.addFooterView(footerView);
		           
	    		mainListView.setAdapter(new MobileArrayAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes, locations));
	    	}
 	   	}
		catch (JSONException e) 
		{			
			SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
        	
			e.printStackTrace();
		}
    }

    
}

 class MobileArrayAdapter extends ArrayAdapter<String> 
 {
	private final Context context;
	private final FragmentCommunicator comm;
	private final String[] values;
	String[] modes;
	private String[] distances;
	private String[] durations;
	private String[] transitArrivals;
	private String[] vehicleTypes;
	private LatLng[] locations;
 
	public MobileArrayAdapter(Context context, String[] values, String[] modes, String[] distances, String[] durations, String[] transitArrivals, String[] vehicleTypes, LatLng[] location) 
	{
		super(context, R.layout.directionsrow, values);
		this.context = context;
		this.comm = (FragmentCommunicator) context;
		this.locations = location;
		this.values = values;
		this.modes = modes;
		this.distances = distances;
		this.durations = durations;
		this.transitArrivals = transitArrivals;
		this.vehicleTypes = vehicleTypes;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View rowView = inflater.inflate(R.layout.directionsrow, parent, false);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.vehicleTypeImage);

		TextView directionsText = (TextView) rowView.findViewById(R.id.directionsText);
		TextView directionsDetails = (TextView) rowView.findViewById(R.id.directionsDetails);
		TextView directionsRowTimeText = (TextView) rowView.findViewById(R.id.directionsRowTimeTextView);
		LinearLayout touchLayout = (LinearLayout) rowView.findViewById(R.id.clickableDirectionLayout);
		
		final int p = position;
		
		touchLayout.setOnTouchListener( new OnTouchListener()
	    {
			public boolean isPointInsideView(float x, float y, View view)
			{
			    int location[] = new int[2];
			    view.getLocationOnScreen(location);
			    int viewX = location[0];
			    int viewY = location[1];

			    //point is inside view bounds
			    if(( x > viewX && x < (viewX + view.getWidth())) &&
			            ( y > viewY && y < (viewY + view.getHeight())))
			    {
			        return true;
			    }
			    else 
			    {
			        return false;
			    }
			}
			
	        @Override
	        public boolean onTouch(View v, MotionEvent event) 
	        {
	            if(event.getAction() == MotionEvent.ACTION_DOWN)
	            {
	            	rowView.setBackgroundColor(Color.LTGRAY);
	            	return true;
	            }
	            else if(event.getAction() == MotionEvent.ACTION_UP)
	            {
	            	if(isPointInsideView(event.getRawX(), event.getRawY(), v))
		        	{
		        		comm.goToStepLocation(locations[p]);
		        	}	
	            	
	            	rowView.setBackgroundColor(Color.WHITE); 
	            	return true;

	            }
	            else if(event.getAction() == MotionEvent.ACTION_CANCEL)
	            {
	            	rowView.setBackgroundColor(Color.WHITE); 
	            	return true;
	            }
	            
	            return false;
	        }
	    });
		
		
		if(position+1 < transitArrivals.length)
		{
			if(transitArrivals[position+1] != null)
			{
				TextView transitDetails = (TextView) rowView.findViewById(R.id.transitTextView);
				transitDetails.setVisibility(View.VISIBLE);
				transitDetails.setText(vehicleTypes[position+1] + " arrives here at " + transitArrivals[position+1]);
			}
		}
		
		directionsDetails.setText(Html.fromHtml(distances[position]));
		
		String[] parts = durations[position].split("\\ ");
		directionsRowTimeText.setText(Html.fromHtml("<b>" + parts[0] +"</b>" + "<small><font color=#212121>" + parts[1] + "</font></small>"));
	
		directionsText.setText(values[position]);
 
		// Change icon based on name
		String imageType = modes[position];
 
		if (imageType.equals("TRANSIT")) 
		{
			if(vehicleTypes[position].equals("Light rail"))
			{
				imageView.setImageResource(R.drawable.smalllightrail);
			}
			else if(vehicleTypes[position].equals("Bus"))
			{
				imageView.setImageResource(R.drawable.bus);
			}
		} 
		else if (imageType.equals("WALKING")) 
		{
			imageView.setImageResource(R.drawable.walking_man);
		}
 
		return rowView;
	}
}


