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

import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;



public class PlaceFragment extends Fragment 
{
	Communicator comm;
	private View rootView;
	
	
	private String name;
	private String address;
	private String phone;
	private String web;
	private float rating;
	private PlaceMobileArrayAdapter placeAdapter;
	private ListView placeReviewListView;

	TextView placeName;
	TextView placeAddress;
	TextView placePhone;
	TextView placeWeb;
	RatingBar placeRating;
	
	LatLng placeLoc;

	ImageButton locationSearchButton;
	Button getRoutesButton;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
    	if (rootView != null) 
		{
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try 
	    {
	        rootView = inflater.inflate(R.layout.place, container, false);
	    } 
	    catch (InflateException e) 
	    {
	        /* map is already there, just return view as it is */
	    }

        
        return rootView;
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
    	super.onActivityCreated(savedInstanceState);
    	
        comm = (Communicator) getActivity();
        
        placeReviewListView = (ListView) rootView.findViewById(R.id.placeReviewList);  


    	placeName = (TextView) rootView.findViewById(R.id.name);
        placeAddress = (TextView) rootView.findViewById(R.id.address);
        placePhone = (TextView) rootView.findViewById(R.id.phone);
    	placeWeb = (TextView) rootView.findViewById(R.id.website);
        placeRating = (RatingBar) rootView.findViewById(R.id.placeRating);
    	
        placeWeb.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View arg0) 
            {
            	if(web != null)
            	{
            		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(web));
            		startActivity(browserIntent);
            	}
            }
        });
        
        locationSearchButton = (ImageButton) rootView.findViewById(R.id.placeInfoBackButton);
        locationSearchButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	//Return to home search screen
                comm.goToLocationSearch();
            }
        });
        
        getRoutesButton = (Button) rootView.findViewById(R.id.placeGetRoutesButton);
        getRoutesButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
            	//Return to home search screen
            	comm.returnRoutes(placeLoc);
            }
        });
        
	}

    
    public void initialize(LatLng location, String keyword)
    {
    	//initialize new place if it is not already initialized
    	if(location.equals(placeLoc))
    	{
        	//place already loaded
    		placeName.setText(name);
    		placeAddress.setText(address);		
    		placePhone.setText(phone);
    		placeWeb.setText(web);		
    		placeRating.setRating(rating);
    	}
    	else
    	{
    		//clear and get new place
        	placeLoc = new LatLng(location.latitude, location.longitude);
    	    GetPlacesIDTask getPlace = new GetPlacesIDTask(location, keyword);
    	    getPlace.execute();
    	}
    }
    
	public void updatePlace(JSONObject result) 
	{
		String[] details = null;
		int[] dates = null;
	    try 
	    {
	    	
			JSONObject detailsJSON = new JSONObject(result.toString());
	    	
			name = detailsJSON.getString("name");
	    	address = detailsJSON.getString("formatted_address");
	    	phone = detailsJSON.getString("formatted_phone_number");
	    	web = detailsJSON.getString("website");
	    	rating = (float) detailsJSON.getDouble("rating");

			placeName.setText(detailsJSON.getString("name"));
			placeAddress.setText(detailsJSON.getString("formatted_address"));
			//placeAddress.setLines(1);
			
			float size = placeAddress.getTextSize();
			
			
			//placePhone.setTextSize((size/2));
			placePhone.setText(detailsJSON.getString("formatted_phone_number"));
			
			//placeWeb.setTextSize((size/2));
			//placeWeb.setLines(1);

			String link = "<a href="+detailsJSON.getString("website")+">"+detailsJSON.getString("website")+"</a>";
			placeWeb.setMovementMethod(LinkMovementMethod.getInstance());
			placeWeb.setText(Html.fromHtml(link));
			
			
			JSONArray reviews = detailsJSON.getJSONArray("reviews");
			details = new String[reviews.length()];
			dates = new int[reviews.length()];
			for(int i = 0; i < reviews.length(); i++)
			{
				JSONObject tmp = reviews.getJSONObject(i);
				details[i] = tmp.getString("text");
				dates[i] = tmp.getInt("time");
			}
			placeRating.setStepSize((float) 2.0);
			placeRating.setRating((float) detailsJSON.getDouble("rating"));
		} 
	    
	    catch (JSONException e) 
	    {
        	Log.e("details ", "101", e);
		}
	    
	    String[] test = {"test", "123"};

        placeAdapter = new PlaceMobileArrayAdapter(getActivity(), details, dates);
        placeReviewListView.setAdapter(placeAdapter);
	}

//working
	private JSONObject getDetails(String place_id) {
		final String LOG_TAG = "VTA";
	    final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	    final String TYPE_DETAILS = "/details";
	    final String OUT_JSON = "/json";
	    final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    JSONObject jsonObj = null;
	    JSONObject result = null;
	    HttpURLConnection conn = null;
	    StringBuilder jsonResults = new StringBuilder();
	        
	        //Send request to PLACES API
	        try 
	        {
	            StringBuilder endpointURL = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
	            endpointURL.append("?key=" + API_KEY);
	            endpointURL.append("&placeid="+place_id);
	            
	            URL url = new URL(endpointURL.toString());
	            conn = (HttpURLConnection) url.openConnection();
	            InputStreamReader in = new InputStreamReader(conn.getInputStream());

	            // Load the results into a StringBuilder
	            int read;
	            char[] buff = new char[10000];
	            while ((read = in.read(buff)) != -1) 
	            {
	                jsonResults.append(buff, 0, read);
	            }
	            in.close();
	        } 
	        catch (MalformedURLException e) 
	        {
	            Log.e(LOG_TAG, "Error processing Places API URL", e);
	        } 
	        catch (IOException e) 
	        {
	            Log.e(LOG_TAG, "Error connecting to Places API", e);
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
	            jsonObj = new JSONObject(jsonResults.toString());
	            result = jsonObj.getJSONObject("result");
	        } 
	        catch (JSONException e) 
	        {
	            Log.e(LOG_TAG, "Cannot process JSON results", e);
	        }
	    return result;
	}
	//working
	public String getPlaceID(LatLng location, String name)
	{
		final String LOG_TAG = "VTA";
	    final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	    final String TYPE_NEARBY = "/nearbysearch";
	    final String OUT_JSON = "/json";
	    final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
	    String place_id = null;
		//In meters
		final int RADIUS = 10;

	    HttpURLConnection conn = null;
	    StringBuilder jsonResults = new StringBuilder();
	        
	        //Send request to PLACES API
	        try 
	        {
	            StringBuilder endpointURL = new StringBuilder(PLACES_API_BASE + TYPE_NEARBY + OUT_JSON);
	            endpointURL.append("?key=" + API_KEY);
	            endpointURL.append("&location="+location.latitude+","+location.longitude);
	            endpointURL.append("&radius="+RADIUS);
	            endpointURL.append("&keyword=" + URLEncoder.encode(name, "utf8"));

	            URL url = new URL(endpointURL.toString());
	            conn = (HttpURLConnection) url.openConnection();
	            InputStreamReader in = new InputStreamReader(conn.getInputStream());
	            // Load the results into a StringBuilder
	            int read;
	            char[] buff = new char[10000];
	            while ((read = in.read(buff)) != -1) 
	            {
	                jsonResults.append(buff, 0, read);
	            }
	            in.close();
	        } 
	        catch (MalformedURLException e) 
	        {
	            Log.e(LOG_TAG, "Error processing Places API URL", e);
	        } 
	        catch (IOException e) 
	        {
	            Log.e(LOG_TAG, "Error connecting to Places API", e);
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

	            	JSONObject result = results.getJSONObject(0);
	            	place_id = result.getString("place_id");
	            
	        } catch (JSONException e) 
	        {
	            Log.d(LOG_TAG, "Cannot process JSON results", e);
	        }
		return place_id;
	}

	
	//working
	class GetPlacesIDTask extends AsyncTask<Void, Void, JSONObject> 
	{
		private LatLng location;
		private String keyword;
		private String place_id;
		private JSONObject place_details;

		GetPlacesIDTask(LatLng location, String keyword)
		{
			this.location = location;
			this.keyword = keyword;
		}
	    protected JSONObject doInBackground(Void... params) 
	    {

		    String placeid = getPlaceID(location, keyword);
		    place_id = placeid;
		    place_details = getDetails(place_id);
	        return place_details;
	    }
	    protected void onPostExecute(JSONObject result) 
	    {
		    updatePlace(result);
	    }
	}
}