


//add custom infowindow on marker click, add more detail to text directions,

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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


class AddressSearchAsyncTask extends AsyncTask<String, Void, List<Address>>{
	
    private final String LOG_TAG = "VTA";
    private final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private final String TYPE_TEXT = "/textsearch";
    private final String OUT_JSON = "/json";
    private final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	private final int MAX_RESULTS = 40;
	//In meters
	private final int RADIUS = 5000;
	

	Context context;
	MarkerOptions markerOptions;
	GoogleMap map;
	String markerString = "\nGet Route";
	GPSTracker gps;
	String searchKeyword;
	ProgressBar searchProgress;
	private Button addressSearchButton;
	
	public AddressSearchAsyncTask(Context context, GoogleMap map, ProgressBar searchProgress, Button addressSearchButton)
	{
		this.map = map;
		this.context = context;
		this.gps = new GPSTracker(context);
		this.searchProgress = searchProgress;
		this.addressSearchButton = addressSearchButton;
	}
	
	public void setLocation(String newKeyword)
	{
		searchKeyword = newKeyword;
	}

    @Override
    protected void onCancelled() 
    {
    	searchProgress.setVisibility(View.GONE);
    	addressSearchButton.setVisibility(View.VISIBLE);
    }
	
	@Override 
	protected void onPreExecute()
	{
		addressSearchButton.setVisibility(View.GONE);
		searchProgress.setVisibility(View.VISIBLE);
	}
	
    @Override
    protected List<Address> doInBackground(String... params)
    {
    	// 1. create HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        
        HttpGet httpPost = new HttpGet("http://metafora.herokuapp.com/search");
        String body = "";
        JSONObject jsonObject = new JSONObject();
        JSONObject userloc = new JSONObject();
        
        try
        {
        	
            userloc.put("lat", gps.getLatitude());
            userloc.put("lng", gps.getLongitude());
            jsonObject.put("userlatlng", userloc);            
            jsonObject.put("searchstring", searchKeyword);
            jsonObject.put("unixtimestamp", System.currentTimeMillis());
            
        }
        catch(Exception e)
        {
        	
        }

        
        body = jsonObject.toString();
        
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("body", body);
        httpPost.setHeader("Content-type", "application/json");

        try 
        {
			httpclient.execute(httpPost);
		} 
        catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
        catch (IOException e) 
		{
			e.printStackTrace();
		}
        httpPost.abort();
        
        
        List<Address> addresses = null;
        if(!searchKeyword.isEmpty())
        {
            addresses = this.getAddresses(searchKeyword, gps);
        }
        
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) 
    {
    	searchProgress.setVisibility(View.GONE);
    	addressSearchButton.setVisibility(View.VISIBLE);
    	
    	if(searchKeyword.isEmpty())
    	{
            Toast.makeText(context, "Enter text to search or touch a location", Toast.LENGTH_SHORT).show();
    	}
    	else
    	{
	        if(addresses == null || addresses.size() == 0)
	        {
	            Toast.makeText(context, "Try a different search!", Toast.LENGTH_SHORT).show();
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
	
	        		markerOptions = new MarkerOptions();
	        		markerOptions.position(markerLatLng);
	        		markerOptions.title("Let's go to " + address.getFeatureName());
	        		markerOptions.snippet(address.getAddressLine(0));
	        		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon));
	           		markerOptions.flat(true);
	        		map.addMarker(markerOptions);
	        		
	        		
	        		//center map on first location
	        		if(index == 0)
	        		{
	        			map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 13));
	        		}	
	        	}
	        }
    	}
    }

    public List<Address> getAddresses(String keyword, GPSTracker gpsTracker) 
    {
    	List<Address> resultList = new ArrayList<Address>();
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        
        //Send request to PLACES API
        try 
        {

            StringBuilder endpointURL = new StringBuilder(PLACES_API_BASE + TYPE_TEXT + OUT_JSON);
            endpointURL.append("?key=" + API_KEY);
            endpointURL.append("&location="+gps.getLatitude()+","+gps.getLongitude());
           // endpointURL.append("&radius="+RADIUS);
            endpointURL.append("&radius="+RADIUS);
            endpointURL.append("&query=" + URLEncoder.encode(keyword, "utf8"));
            
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
            	addr.setAddressLine(0, result.getString("formatted_address"));
            	
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
