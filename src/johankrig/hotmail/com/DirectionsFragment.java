package johankrig.hotmail.com;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import johankrig.hotmail.com.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
	private View rootView;
    Communicator comm;
	private ListView mainListView;  
	MobileArrayAdapter directionsAdapter;
	private String DirectionsJSON;
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
        comm = (Communicator) getActivity();
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
	           
	           JSONObject departure_time = leg.getJSONObject("departure_time");
	           JSONObject arrival_time = leg.getJSONObject("arrival_time");
	           
	           String departTime = departure_time.getString("text");
	           String arrivalTime = arrival_time.getString("text");


	           
	           String[] instructions = new String[steps.length()];
	           String[] travel_modes = new String[steps.length()];
	           String[] distances = new String[steps.length()];
	           String[] durations = new String[steps.length()];
	           String[] transitArrivals = new String[steps.length()];
	           String[] vehicleTypes = new String[steps.length()];
	           

	           
	           for(int index = 0; index < steps.length(); index++)
	           {
	        	   //consider adding transit details in here as well.
	        	   
	        	   JSONObject step = steps.getJSONObject(index);
	        	   String html_instructions = step.getString("html_instructions");
	        	   String travel_mode = step.getString("travel_mode");
	        	   
	        	   JSONObject distance = step.getJSONObject("distance");
	        	   String distanceString = distance.getString("text");
	        	   
	        	   JSONObject duration = step.getJSONObject("duration");
	        	   String durationString = duration.getString("text");

	        	   if(travel_mode.equals("TRANSIT"))
	        	   {
	        		   JSONObject transitDetails = step.getJSONObject("transit_details");
	        		   JSONObject departureTime = transitDetails.getJSONObject("departure_time");
	        		   transitArrivals[index] = departureTime.getString("text");
	        		   
	        		   JSONObject line = transitDetails.getJSONObject("line");
	        		   JSONObject vehicle = line.getJSONObject("vehicle");
	        		   vehicleTypes[index] = vehicle.getString("name");
	        	   }
	        	   distances[index] = distanceString;
	        	   durations[index] = durationString;
	        	   instructions[index] = html_instructions;
	        	   travel_modes[index] = travel_mode;
	           }
	           if(directionsAdapter == null)
	           {
	        	   directionsAdapter = new MobileArrayAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes);
	           }
	           //create header view
	           TextView header1 = (TextView) headerView.findViewById(R.id.departTextView);
	           header1.setText("Depart at: " + departTime);
	           TextView header2 = (TextView) headerView.findViewById(R.id.arriveTextView);
	           header2.setText("Arrive at: " + arrivalTime);
	           //end header view
	           
	           //set footer view
	           TextView footer1 = (TextView) footerView.findViewById(R.id.directionsLocation);
	           JSONObject firstleg = legs.getJSONObject(0);
	           JSONObject lastleg = legs.getJSONObject(legs.length()-1);
	           Log.d("firstlegs", firstleg.getString("start_address"));
	           Log.d("lastleg", lastleg.getString("end_address"));
	           Log.d("footerview", footerView.toString());
	           Log.d("footerviewtext", footer1.toString());


	           footer1.setText(firstleg.getString("start_address") + " to " + lastleg.getString("end_address"));
	           
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
	           String fareCost = "$"+route.getJSONObject("fare").getInt("value");
	           
	           footerFare.setText(fareCost);
	           
	           //end footer
	           if(mainListView.getAdapter() == null)
	           {
		           mainListView.setAdapter(directionsAdapter);	 

		           mainListView.addHeaderView(headerView);
		           mainListView.addFooterView(footerView);
	           }
	           else
	           {	           
	        	   mainListView.removeHeaderView(headerView);
	        	   mainListView.removeFooterView(footerView);

		           mainListView.addHeaderView(headerView);
		           mainListView.addFooterView(footerView);
		           
		           mainListView.setAdapter(new MobileArrayAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes)
);
	           }

 	   	}
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}

 class MobileArrayAdapter extends ArrayAdapter<String> 
 {
	private final Context context;
	private final String[] values;
	String[] modes;
	private String[] distances;
	private String[] durations;
	private String[] transitArrivals;
	private String[] vehicleTypes;
 
	public MobileArrayAdapter(Context context, String[] values, String[] modes, String[] distances, String[] durations, String[] transitArrivals, String[] vehicleTypes) 
	{
		super(context, R.layout.directionsrow, values);
		this.context = context;
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
		
		View rowView = inflater.inflate(R.layout.directionsrow, parent, false);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.reviewerName);

		TextView directionsText = (TextView) rowView.findViewById(R.id.directionsText);
		TextView directionsDetails = (TextView) rowView.findViewById(R.id.directionsDetails);
		
		if(transitArrivals[position] != null)
		{
			LinearLayout detailsLayout = (LinearLayout) rowView.findViewById(R.id.transitDetailsLinearLayout);
			detailsLayout.setVisibility(View.VISIBLE);
			TextView transitDetails = (TextView) rowView.findViewById(R.id.transitTextView);
			transitDetails.setText(vehicleTypes[position] + " arrives here at " + transitArrivals[position]);
		}
		
		directionsDetails.setText("Dist: " + distances[position]
				+ "\n"
				+ durations[position]);
		
		directionsText.setText(values[position]);
 
		// Change icon based on name
		String imageType = modes[position];
 
		if (imageType.equals("TRANSIT")) 
		{
			imageView.setImageResource(R.drawable.bus);
		} 
		else if (imageType.equals("WALKING")) 
		{
			imageView.setImageResource(R.drawable.walking_man);
		}
 
		return rowView;
	}
}

