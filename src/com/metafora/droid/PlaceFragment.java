package com.metafora.droid;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class PlaceFragment extends Fragment 
{
	FragmentCommunicator comm;
	private View rootView;
	private ProgressBar loadProgressBar = null;
	private Button loadProgressButton = null;
	private RelativeLayout placeLayout = null;
	private RelativeLayout placeNoInformationLayout = null;
	
	private String name;
	private String address;
	private String phone;
	private String web;
	private String openNowString;
	private boolean openNow;
	private int numberOfRatings;
	
	private float rating;
	private PlaceMobileArrayAdapter placeAdapter;
	private ListView placeReviewListView;

	TextView placeName;
	TextView placeAddress;
	TextView placePhone;
	TextView placeWeb;
	TextView openNowText;
	TextView placeNumberOfRatings;
	RatingBar placeRating;
	TextView placeNoInformationAddress;
	
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
	    loadProgressBar = (ProgressBar) rootView.findViewById(R.id.placeLoadBar);
	    loadProgressButton = (Button) rootView.findViewById(R.id.placeLoadButton);
	    placeLayout = (RelativeLayout) rootView.findViewById(R.id.placeRelativeLayout);
	    placeNoInformationLayout = (RelativeLayout) rootView.findViewById(R.id.placeNoInformationLayout);
        
	    return rootView;
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
    	super.onActivityCreated(savedInstanceState);
    	
        comm = (FragmentCommunicator) getActivity();
        
        placeReviewListView = (ListView) rootView.findViewById(R.id.placeReviewList);  


    	placeName = (TextView) rootView.findViewById(R.id.name);
        placeAddress = (TextView) rootView.findViewById(R.id.address);
        placePhone = (TextView) rootView.findViewById(R.id.phone);
    	placeWeb = (TextView) rootView.findViewById(R.id.website);
        placeRating = (RatingBar) rootView.findViewById(R.id.placeRating);
        openNowText = (TextView) rootView.findViewById((R.id.placeCurrentStatus));
    	placeNumberOfRatings = (TextView) rootView.findViewById(R.id.placeNumberOfRatings);
        placeNoInformationAddress = (TextView) rootView.findViewById(R.id.placeNoInformationAddress);
        
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
        
        placePhone.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View arg0)
        	{
        		if(phone != null)
        		{
        			Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)); 
        			startActivity(callIntent);
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

    
    public void initialize(LatLng location, String keyword, String address)
    {
    	//initialize new place if it is not already initialized
    	if(location.equals(placeLoc))
    	{
        	//place already loaded
    		placeName.setText(name);
    		placeAddress.setText(address);		
    		//placePhone.setText(phone);
    		//placeWeb.setText(web);		
    		placeRating.setRating(rating);
    		openNowText.setText(openNowString);
    		placeNumberOfRatings.setText(numberOfRatings + " reviews");
    		placeNoInformationAddress.setText(address);
    	}
    	else
    	{
    		placeNoInformationLayout.setVisibility(View.GONE);
        	placeLayout.setVisibility(View.INVISIBLE);
    		
        	//clear and get new place
    		clearPlace();
    		
    		
    		placeNoInformationAddress.setText(address);
    		this.address = address;
        	
    		placeLoc = new LatLng(location.latitude, location.longitude);
        	
    	    GetPlacesIDTask getPlace = new GetPlacesIDTask(location, keyword.substring(11));
    	    getPlace.execute();
    	}
    }
    
	private void clearPlace() 
	{
		
		placeName.setText("");
		placeAddress.setText("");		
		//placePhone.setText("");
		//placeWeb.setText("");		
		placeRating.setRating(0);
		openNowText.setText("");
		placeNumberOfRatings.setText("");
	}

	public void updatePlace(JSONObject result) 
	{
		String[] placeReviewsDetails = {};
		String[] reviewerNames = {};
		int[] reviewDates = {};
		float[] reviewRatings = {};
		
	    try
	    {
	    	//if google has reviews for this location
	    	if(result != null)
	    	{
				JSONObject detailsJSON = new JSONObject(result.toString());
		    	
				if(detailsJSON.has("name"))
				{
					name = detailsJSON.getString("name");
					placeName.setText(name);
	
				}
				
				if(detailsJSON.has("formatted_address"))
				{
					address = detailsJSON.getString("formatted_address");
					placeAddress.setText(address);
	
				}
				if(detailsJSON.has("user_ratings_total"))
				{
					numberOfRatings = detailsJSON.getInt("user_ratings_total");
					placeNumberOfRatings.setText(numberOfRatings + " reviews");
				}
		    	
				if(detailsJSON.has("formatted_phone_number"))
				{
			    	phone = detailsJSON.getString("formatted_phone_number");
					//placePhone.setText(phone);
					placePhone.setVisibility(View.VISIBLE);
				}
				else
				{
					placePhone.setVisibility(View.INVISIBLE);
				}
				
				if(detailsJSON.has("website"))
				{
			    	web = detailsJSON.getString("website");
					//String link = "<a href="+ web +">"+detailsJSON.getString("website")+"</a>";
					//placeWeb.setMovementMethod(LinkMovementMethod.getInstance());
					//placeWeb.setText(Html.fromHtml(link));
					placeWeb.setVisibility(View.VISIBLE);
				}
				else
				{
					placeWeb.setVisibility(View.INVISIBLE);
				}
				
				if(detailsJSON.has("opening_hours"))
				{
					JSONObject openingHours = detailsJSON.getJSONObject("opening_hours");
					if(openingHours.has("open_now"))
					{
						openNow = openingHours.getBoolean("open_now");
						if(openNow)
						{
							openNowString = "Open";
							openNowText.setTextColor(getResources().getColor(R.color.openred));
							openNowText.setText(openNowString);
						}
						else
						{
							openNowString = "Closed";
							openNowText.setTextColor(getResources().getColor(R.color.closedgrey));
							openNowText.setText(openNowString);
						}
					}
				}
				else
				{
					openNowString = "unknown";
					openNowText.setTextColor(getResources().getColor(R.color.closedgrey));
					openNowText.setText(openNowString);
				}
				
				if(detailsJSON.has("rating"))
				{
					rating = (float) detailsJSON.getDouble("rating");
					placeRating.setStepSize((float) 0.25);
					placeRating.setRating((float) detailsJSON.getDouble("rating"));
				}
				
				if(detailsJSON.has("reviews"))
				{
					JSONArray reviews = detailsJSON.getJSONArray("reviews");
					if(!reviews.isNull(0))
					{
						placeReviewsDetails = new String[reviews.length()];
						reviewerNames = new String[reviews.length()];
						reviewDates = new int[reviews.length()];
						reviewRatings = new float[reviews.length()];
						
						for(int i = 0; i < reviews.length(); i++)
						{
							JSONObject tmp = reviews.getJSONObject(i);
							placeReviewsDetails[i] = tmp.getString("text");
							reviewerNames[i] = tmp.getString("author_name");
							reviewDates[i] = tmp.getInt("time");
							JSONArray rating = tmp.getJSONArray("aspects");
				        	JSONObject rating1 = rating.getJSONObject(0);
							reviewRatings[i] = (float) (rating1.getDouble("rating") + (3*0.66));
						}
					}
				}
			} 
	    }
	    
	    catch (JSONException e) 
	    {
        	Log.e("details ", "101", e);
		}
	    
        placeAdapter = new PlaceMobileArrayAdapter(getActivity(), placeReviewsDetails, reviewerNames, reviewDates, reviewRatings);
        placeReviewListView.setAdapter(placeAdapter);
	}

	//working
	private JSONObject getDetails(String place_id) 
	{
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
	            
	            Log.d("placedets", endpointURL.toString());
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
	            
	            //IF THIS RETURNS NULL PUT UP A NO PLACE INFO PAGE INSTEAD
	            if(jsonObj.has("result"))
	            {	
	            result = jsonObj.getJSONObject("result");
	            }
	            else
	            {
	            	result = null;
	            }
	        } 
	        catch (JSONException e) 
	        {
	            Log.e(LOG_TAG, "No results for this place", e);
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
		private JSONObject place_details;

		GetPlacesIDTask(LatLng location, String keyword)
		{
			this.location = location;
			this.keyword = keyword;
		}
		@Override
	    protected void onPreExecute() 
	    {
	        super.onPreExecute();
	        loadProgressButton.setVisibility(View.GONE);
	        loadProgressBar.setVisibility(View.VISIBLE);
	        
	    }
		
	    protected JSONObject doInBackground(Void... params) 
	    {
		    String place_id = getPlaceID(location, keyword);
		    place_details = getDetails(place_id);
	        return place_details;
	    }
	    protected void onPostExecute(JSONObject result) 
	    {
	    	if(result == null)
	    	{
		    	placeNoInformationLayout.setVisibility(View.VISIBLE);
		        loadProgressButton.setVisibility(View.VISIBLE);
		        loadProgressBar.setVisibility(View.GONE);
	    	}
	    	else
	    	{
		    	placeLayout.setVisibility(View.VISIBLE);
		        loadProgressButton.setVisibility(View.VISIBLE);
		        loadProgressBar.setVisibility(View.GONE);
			    updatePlace(result);
	    	}
	    }
	}
}