


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


class GeocoderAsyncTask extends AsyncTask<String, Void, List<Address>>{
	
	public String region = " San Jose, CA";
	Context context;
	LatLng latLng;
	MarkerOptions markerOptions;
	GoogleMap map;
	String markerString = "\nGet Route";
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

        MyGeocoder geo = new MyGeocoder();
        addresses = geo.getAddresses(locationName, gps);
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
        	for(int i=0;i<MAX_RESULTS;i++)
        	{
        		Address address = (Address) addresses.get(i);

        		// Creating an instance of GeoPoint, to display in Google Map
        		latLng = new LatLng(address.getLatitude(), address.getLongitude());

        		String addressText = address.getAddressLine(0);
        		markerOptions = new MarkerOptions();
        		markerOptions.position(latLng);
        		markerOptions.title(address.getFeatureName());
        		markerOptions.snippet(addressText + " "
        				+ markerString);
        		
        		map.addMarker(markerOptions);
        		
        		// Locate the first location
        		if(i==0)
        			
        			map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            	
        	}
        }
    }
}


class MyGeocoder {
	

    private static final String LOG_TAG = "ExampleApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_NEARBY = "/nearbysearch";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";

    public List<Address> getAddresses(String input, GPSTracker c) {
        List<Address> resultList = new ArrayList<Address>();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_NEARBY + OUT_JSON);
            sb.append("?key=" + API_KEY);
            GPSTracker gps = c;
            
			sb.append("&location="+gps.getLatitude()+","+gps.getLongitude());
            sb.append("&radius=5000");
            sb.append("&keyword=" + URLEncoder.encode(input, "utf8"));
            Log.e("LEL", sb.toString());
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray results = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            for (int i = 0; i < results.length(); i++) {
            	JSONObject result = results.getJSONObject(i);
            	Address addr = new Address(null);
            	addr.setFeatureName(result.getString("name"));
            	addr.setAddressLine(0, result.getString("vicinity"));
            	JSONObject geo = result.getJSONObject("geometry");
            	JSONObject location = geo.getJSONObject("location");
            	addr.setLatitude(Double.parseDouble(location.getString("lat")));
            	addr.setLongitude(Double.parseDouble(location.getString("lng")));

            	
                resultList.add(addr);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}