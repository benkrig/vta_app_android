package johankrig.hotmail.com;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import johankrig.hotmail.com.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;



public class DirectionsFragment extends Fragment 
{
	private View rootView;
    Communicator comm;
	private ListView mainListView;  
	private ArrayAdapter<String> listAdapter;  
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
        	    
        // Create and populate a List of planet names.  
        /*String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",  
        		"Jupiter", "Saturn", "Uranus", "Neptune"};    
        ArrayList<String> planetList = new ArrayList<String>();  
        planetList.addAll( Arrays.asList(planets) );  
        	    
        // Create ArrayAdapter using the planet list.  
        listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simplerow, planetList);  
        	    
        // Add more planets. If you passed a String[] instead of a List<String>   
        // into the ArrayAdapter constructor, you must not add more items.   
        // Otherwise an exception will occur.  
        listAdapter.add( "Ceres" );  
        listAdapter.add( "Pluto" );  
        listAdapter.add( "Haumea" );  
        listAdapter.add( "Makemake" );  
        listAdapter.add( "Eris" );  
        // Set the ArrayAdapter as the ListView's adapter.  
         * 
         */
        listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simplerow);  
        mainListView.setAdapter( listAdapter );       	
	}
    
    //TODO change routes jsonobject to have variable named Routenumber
    public void updateDirectionsList(String JSON)
    {
        listAdapter.clear();
        DirectionsJSON = JSON;
        
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
	           for(int c = 0; c < steps.length(); c++)
	           {
	        	   //consider adding transit details in here as well.
	        	   
	        	   JSONObject step = steps.getJSONObject(c);
	        	   String html_instructions = step.getString("html_instructions");
	        	   String travel_mode = step.getString("travel_mode");
	        	   listAdapter.add(html_instructions + " | " + travel_mode);
	           }

 	   	}
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
