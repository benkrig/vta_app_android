package johankrig.hotmail.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import johankrig.hotmail.com.R;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;


public class MainFragment extends Fragment
{
	AutoCompleteTextView searchBar;
	public GoogleMap map;
	View rootView;
	Button addressSearchButton;
	Communicator comm;
	String destination = "";
	Bundle bundle;
	FindLocationsAsync geoTask;
	GPSTracker gps;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
		rootView = inflater.inflate(R.layout.fragment_main, container, false);
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mainmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
        
		return rootView;
    }
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		gps = new GPSTracker(getActivity());
		if(gps.canGetLocation())
		{
			LatLng updateLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
			@SuppressWarnings("unused")
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(updateLatLng, 10);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
		}
		comm = (Communicator) getActivity();
		
		geoTask = new FindLocationsAsync(getActivity(), map);
		
		searchBar = (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
		searchBar.setAdapter(new AutoCompleteAdapter(getActivity(), map));
		
		bundle = new Bundle();
		
		addressSearchButton = (Button) getActivity().findViewById(R.id.routeMenuButton);
		addressSearchButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
        		destination = searchBar.getEditableText().toString();

        		bundle.putString("destination", destination);
                bundle.putInt("fragment", 1);
                if(gps.canGetLocation() == true)
                {
                	bundle.putDouble("latitude", gps.getLatitude());
                	bundle.putDouble("longitude", gps.getLongitude());
                }
                
                //AsyncTask can only execute ONCE
                //geoTask.getStatus()
                //Status.FINISHED, create a new instance : this sets mStatus to PENDING
                //PENDING, execute.
                if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
                {
                	geoTask = new FindLocationsAsync(getActivity(), map);
                }
                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
                {
                	geoTask.execute(destination);
                }
            }
        });
	}
}


class FindLocationsAsync extends AsyncTask<String, Void, List<Address>>{
	
	Context context;
	LatLng latLng;
	MarkerOptions markerOptions;
	GoogleMap map;
	String markerString = "Get Route";
	final int MAX_RESULTS = 5;
	GPSTracker gps;

	
	public FindLocationsAsync(Context c, GoogleMap m)
	{
		map = m;
		context = c;
		gps = new GPSTracker(context);
	}

    @Override
    protected List<Address> doInBackground(String... locationName) {
        
        List<Address> addresses = null;

        try 
        {
        	if(gps.canGetLocation() == true)
        	{        
        		//GEOCODER IS REUTNRING ALL KINDS OF ADDRESSES wtffff
        		Geocoder geocoder = new Geocoder(context, Locale.getDefault()
        				);
        		addresses = geocoder.getFromLocationName(locationName[0], MAX_RESULTS);
        	}
        	else
        	{
                Toast.makeText(context, "GPS Provider not found...", Toast.LENGTH_SHORT).show();
        	}
        	gps.stopUsingGPS();
        } 
        catch (IOException e) 
        {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) 
    {
        if(addresses == null || addresses.size() == 0)
        {
            Toast.makeText(context, "No Location found", Toast.LENGTH_SHORT).show();
        }
        else
        {
        // Clears all the existing markers on the map
        	map.clear();
        	Location uloc = new Location("");
        	uloc.setLatitude(gps.getLatitude());
        	uloc.setLongitude(gps.getLongitude());

        // Adding Markers on Google Map for each matching address
        	for(int i=0;i<MAX_RESULTS;i++)
        	{
        		Address address = (Address) addresses.get(i);
            	Location eloc = new Location("");
            	eloc.setLatitude(address.getLatitude());
            	eloc.setLongitude(address.getLongitude());
            	
            	if(uloc.distanceTo(eloc) < 100000)
            	{

        		// Creating an instance of GeoPoint, to display in Google Map
        		latLng = new LatLng(address.getLatitude(), address.getLongitude());

        		String addressText = String.format("%s, %s",
        				address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
        				address.getCountryName());

        		markerOptions = new MarkerOptions();
        		markerOptions.position(latLng);
        		markerOptions.title(addressText);
        		markerOptions.snippet(markerString);
            
        		map.addMarker(markerOptions);
        		
        		// Locate the first location
        		if(i==0)
        			map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            	}
        	}
        }
    	
    }
}


class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable 
{
 
	private LayoutInflater mInflater;
	private Geocoder mGeocoder;
	private StringBuilder mSb = new StringBuilder();
	GoogleMap map;
	
	public AutoCompleteAdapter(final Context context, GoogleMap googlemap) 
	{
		super(context, -1);
		mInflater = LayoutInflater.from(context);
		mGeocoder = new Geocoder(context);
		map = googlemap;
	}
 
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) 
	{
		final TextView tv;
		if (convertView != null) 
		{
			tv = (TextView) convertView;
		} 
		else 
		{
			tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
		}
 
		tv.setText(createFormattedAddressFromAddress(getItem(position)));
		return tv;
	}
 
	private String createFormattedAddressFromAddress(final Address address) 
	{
		mSb.setLength(0);
		final int addressLineSize = address.getMaxAddressLineIndex();
		for (int i = 0; i < addressLineSize; i++) 
		{
			mSb.append(address.getAddressLine(i));
			if (i != addressLineSize - 1) 
			{
				mSb.append(", ");
			}
		}
		return mSb.toString();
	}
 
	@Override
	public Filter getFilter() 
	{
		Filter myFilter = new Filter() 
		{
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) 
			{
				List<Address> addressList = null;
				if (constraint != null) 
				{
					try 
					{
						addressList = mGeocoder.getFromLocationName((String) constraint, 4);
					} 
					catch (IOException e) 
					{
					}
				}
				if (addressList == null) 
				{
					addressList = new ArrayList<Address>();
				}
 
				
				final FilterResults filterResults = new FilterResults();
				filterResults.values = addressList;
				filterResults.count = addressList.size();
 
				return filterResults;
			}
 
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence contraint, final FilterResults results) 
			{
				clear();
				for (Address address : (List<Address>) results.values) 
				{
					add(address);
				}
				if (results.count > 0) 
				{
					notifyDataSetChanged();
				} 
				else 
				{
					notifyDataSetInvalidated();
				}
			}
 
			@Override
			public CharSequence convertResultToString(final Object resultValue) 
			{
				return resultValue == null ? "" : ((Address) resultValue).getAddressLine(0);
			}
		};
		return myFilter;
	}
}


/*
Timer simple TimerTask Java Android example
Posted on September 26, 2011 by admin	

TimerTask with updating of TextView here

 
package cz.okhelp.timer;
 
import java.util.Timer;
import java.util.TimerTask;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
 
public class TimerActivity extends Activity {
TextView hTextView;
@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        hTextView = (TextView)findViewById(R.id.idTextView);
        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();
//        public void schedule (TimerTask task, long delay, long period) 
//        Schedule a task for repeated fixed-delay execution after a specific delay.
//
//        Parameters
//        task  the task to schedule. 
//        delay  amount of time in milliseconds before first execution. 
//        period  amount of time in milliseconds between subsequent executions. 
 
        myTimer.schedule(myTask, 3000, 1500);        
 
    }
class MyTimerTask extends TimerTask {
	  public void run() {
		  // ERROR
		 hTextView.setText("Impossible");
		 // how update TextView in link below  
                 // http://android.okhelp.cz/timer-task-timertask-run-cancel-android-example/
 
	    System.out.println("");
	  }
	}
 
 
}
*/