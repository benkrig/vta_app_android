package johankrig.hotmail.com;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import johankrig.hotmail.com.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



public class DirectionsFragment extends Fragment 
{
	ImageButton directionsBackButton;
	private View rootView;
    Communicator comm;
	private ListView mainListView;  
	MobileArrayAdapter directionsAdapter;
	private
	String DirectionsJSON;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
        rootView = inflater.inflate(R.layout.fragment_directions, container, false);

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
	           
	           String[] instructions = new String[steps.length()];
	           String[] travel_modes = new String[steps.length()];
	           String[] distances = new String[steps.length()];
	           String[] durations = new String[steps.length()];

	           
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

	        	   distances[index] = distanceString;
	        	   durations[index] = durationString;
	        	   instructions[index] = html_instructions;
	        	   travel_modes[index] = travel_mode;
	           }
	           
	           directionsAdapter = new MobileArrayAdapter(getActivity(), instructions, travel_modes, distances, durations);
	           mainListView.setAdapter(directionsAdapter);
	           

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
 
	public MobileArrayAdapter(Context context, String[] values, String[] modes, String[] distances, String[] durations) 
	{
		super(context, R.layout.directionsrow, values);
		this.context = context;
		this.values = values;
		this.modes = modes;
		this.distances = distances;
		this.durations = durations;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.directionsrow, parent, false);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);

		TextView directionsText = (TextView) rowView.findViewById(R.id.directionsText);
		TextView directionsDetails = (TextView) rowView.findViewById(R.id.directionsDetails);
		
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
