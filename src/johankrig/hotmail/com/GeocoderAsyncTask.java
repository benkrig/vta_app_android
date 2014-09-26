

package johankrig.hotmail.com;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


class GeocoderAsyncTask extends AsyncTask<String, Void, List<Address>>{
	
	private String region = " San Jose, CA";
	Context context;
	LatLng latLng;
	MarkerOptions markerOptions;
	GoogleMap map;
	String markerString = "Get Route";
	final int MAX_RESULTS = 7;
	GPSTracker gps;
	String locationName;

	
	public GeocoderAsyncTask(Context c, GoogleMap m)
	{
		map = m;
		context = c;
		gps = new GPSTracker(context);
	}
	public void setLocation(String newLocation)
	{
		locationName = newLocation;
	}

    @Override
    protected List<Address> doInBackground(String... params) {
        
        List<Address> addresses = null;

    	if(gps.canGetLocation() == true)
    	{      
    		try 
    		{
    			//right here you will make a call to JSONParser to get the json response
    			//from the google places api.
    			//parse the data returned from the response and take the top 5
    			//results and turn them into addresses
        		//GEOCODER IS REUTNRING ALL KINDS OF ADDRESSES wtffff
        		Geocoder geocoder = new Geocoder(context);
        		
        		addresses = geocoder.getFromLocationName(locationName + region, MAX_RESULTS);
        	}
        	catch (Exception e) 
        	{
        		Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        	}
        	gps.stopUsingGPS();
    	}
    	else
    	{
            Toast.makeText(context, "GPS Provider not found...", Toast.LENGTH_SHORT).show();
    	}
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) 
    {
        if(addresses == null || addresses.size() == 0)
        {
            Toast.makeText(context, "No address found", Toast.LENGTH_SHORT).show();
        }
        else
        {
        	// Clears all the existing markers on the map
        	map.clear();
        	// Adding Markers on Google Map for each matching address
        	for(int i=0;i<addresses.size();i++)
        	{
        		Address address = (Address) addresses.get(i);

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