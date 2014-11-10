package johankrig.hotmail.com;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import johankrig.hotmail.com.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



public class DirectionsFragment extends Fragment 
{
	private View rootView;
    Communicator comm;
	private ListView mainListView;  
	private ArrayAdapter<String> textListAdapter; 
	MobileArrayAdapter custAdapter;
	private
	String DirectionsJSON;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
        rootView = inflater.inflate(R.layout.fragment_directions, container, false);

		return rootView;
		
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
    	super.onActivityCreated(savedInstanceState);
        mainListView = (ListView) rootView.findViewById(R.id.mainListViewID);  
        comm = (Communicator) getActivity();
        	    
        //change to directionsrow.xml and test this out. if travelmode is walking show a stick guy
        
        textListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simplerow);  
        mainListView.setAdapter( textListAdapter );       	
	}
    
    //TODO change routes jsonobject to have variable named Routenumber
    public void updateDirectionsList(String JSON)
    {
        DirectionsJSON = JSON;
        textListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simplerow); 
        
		try 
		{
		
			//Tranform the String response RESULT into a JSON object
	           final JSONObject json = new JSONObject(DirectionsJSON);
	           //Get the Routes Array from the JsonObject
	           JSONArray routeArray = json.getJSONArray("routes");
	           
	           //todo
	           JSONObject routes = routeArray.getJSONObject(0);
	           JSONArray legs = routes.getJSONArray("legs");
	           JSONObject leg = legs.getJSONObject(0);
	           JSONArray steps = leg.getJSONArray("steps");
	           
	           String[] instructions = new String[steps.length()];
	           String[] travel_modes = new String[steps.length()];
	           for(int c = 0; c < steps.length(); c++)
	           {
	        	   //consider adding transit details in here as well.
	        	   
	        	   JSONObject step = steps.getJSONObject(c);
	        	   String html_instructions = step.getString("html_instructions");
	        	   String travel_mode = step.getString("travel_mode");
	        	   instructions[c] = html_instructions;
	        	   travel_modes[c] = travel_mode;
	        	   textListAdapter.add(html_instructions + " | " + travel_mode);
	           }
	           custAdapter = new MobileArrayAdapter(getActivity(), instructions, travel_modes);
	           mainListView.setAdapter(custAdapter);
	           

 	   	}
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}

 class MobileArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	String[] modes;
 
	public MobileArrayAdapter(Context context, String[] values, String[] modes) {
		super(context, R.layout.directionsrow, values);
		this.context = context;
		this.values = values;
		this.modes = modes;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.directionsrow, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(values[position]);
 
		// Change icon based on name
		String s = modes[position];
 
 
		if (s.equals("TRANSIT")) {
			imageView.setImageResource(R.drawable.bus);
		} else if (s.equals("WALKING")) {
			imageView.setImageResource(R.drawable.walking_man);
		}
 
		return rowView;
	}
}

