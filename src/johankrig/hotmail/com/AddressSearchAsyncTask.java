


//add custom infowindo on marker click, add more detail to text directions,

//smooth transitions between all screens

package johankrig.hotmail.com;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


class AddressSearchAsyncTask extends AsyncTask<String, Void, List<Address>>{
	
    private final String LOG_TAG = "VTA";
    private final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private final String TYPE_NEARBY = "/nearbysearch";
    private final String OUT_JSON = "/json";
    private final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	private final int MAX_RESULTS = 7;

	Context context;
	MarkerOptions markerOptions;
	GoogleMap map;
	String markerString = "\nGet Route";
	GPSTracker gps;
	String searchKeyword;
	
	public AddressSearchAsyncTask(Context context, GoogleMap map)
	{
		this.map = map;
		this.context = context;
		this.gps = new GPSTracker(context);
	}
	public void setLocation(String newKeyword)
	{
		searchKeyword = newKeyword;
	}

    @Override
    protected List<Address> doInBackground(String... params)
    {
        
        List<Address> addresses = null;
        addresses = this.getAddresses(searchKeyword, gps);
        
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) 
    {
        if(addresses == null || addresses.size() == 0)
        {
            Toast.makeText(context, "No addresses found", Toast.LENGTH_SHORT).show();
        }
        else
        {
        	//Clears all the existing markers on the map
        	map.clear();
        	//Adding Markers on Google Map for each matching address
        	for(int index = 0; index < MAX_RESULTS && index < addresses.size(); index++)
        	{
        		final Address address = (Address) addresses.get(index);

        		//Creating an instance of GeoPoint, to display in Google Map
        		final LatLng markerLatLng = new LatLng(address.getLatitude(), address.getLongitude());

        		String addressText = address.getAddressLine(0);
        		markerOptions = new MarkerOptions();
        		markerOptions.position(markerLatLng);
        		markerOptions.title(address.getFeatureName());
        		markerOptions.snippet(addressText + " "
        				+ markerString);
        		
        		map.addMarker(markerOptions);
        		
        		//center map on first location
        		if(index == 0)
        		{
        			map.animateCamera(CameraUpdateFactory.newLatLng(markerLatLng));
        			map.animateCamera(CameraUpdateFactory.zoomTo(12));
        		}	
        	}
        }
    }

    public List<Address> getAddresses(String keyword, GPSTracker gpsTracker) 
    {
        GPSTracker gps = gpsTracker;
    	List<Address> resultList = new ArrayList<Address>();
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        
        //Send request to PLACES API
        try 
        {

            StringBuilder endpointURL = new StringBuilder(PLACES_API_BASE + TYPE_NEARBY + OUT_JSON);
            endpointURL.append("?key=" + API_KEY);
            endpointURL.append("&location="+gps.getLatitude()+","+gps.getLongitude());
            endpointURL.append("&radius=5000");
            endpointURL.append("&keyword=" + URLEncoder.encode(keyword, "utf8"));
            
            Log.e(LOG_TAG, "endpointURL: " + endpointURL);

            URL url = new URL(endpointURL.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) 
            {
                jsonResults.append(buff, 0, read);
            }
        } 
        catch (MalformedURLException e) 
        {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } 
        catch (IOException e) 
        {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } 
        finally 
        {
            if (conn != null) 
            {
                conn.disconnect();
            }
        }

        //process PLACES API response
        try 
        {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray results = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            for (int index = 0; index < results.length(); index++) 
            {
            	JSONObject result = results.getJSONObject(index);
            	Address addr = new Address(null);
            	addr.setFeatureName(result.getString("name"));
            	addr.setAddressLine(0, result.getString("vicinity"));
            	
            	JSONObject geo = result.getJSONObject("geometry");
            	JSONObject location = geo.getJSONObject("location");
            	addr.setLatitude(Double.parseDouble(location.getString("lat")));
            	addr.setLongitude(Double.parseDouble(location.getString("lng")));

                resultList.add(addr);
            }
            
        } catch (JSONException e) 
        {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }
}