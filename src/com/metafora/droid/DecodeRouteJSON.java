package com.metafora.droid;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


class RouteDetails
{
	private List<PolylineOptions> polyies;
	private List<MarkerOptions> markers;

	public RouteDetails(List<PolylineOptions> polyies, List<MarkerOptions> markers)
	{
		this.polyies = polyies;
		this.markers = markers;
	}

	public List<PolylineOptions> getpolyies()
	{
		return polyies;
	}
	public List<MarkerOptions> getmarkers()
	{
		return markers;
	}
}

public class DecodeRouteJSON extends AsyncTask<Void, Void, RouteDetails> 
{
	private GoogleMap map;

	private ProgressBar routeFragmentProgressBar;
	private Button routeFragmentProgressPlaceholder;
	private LinearLayout routeFragmentBottomBar;
	
	private String json;
	private int jsonArrayIndex;
	private int lineWidth;

	private FragmentCommunicator comm;

	public DecodeRouteJSON(LinearLayout bottomBar, int px, ProgressBar bar, Button button, FragmentCommunicator comm, GoogleMap map, String json, int route)
	{		
		this.routeFragmentBottomBar = bottomBar;
		this.lineWidth = px;
		this.map = map;
		this.comm = comm;
		this.json = json;
		this.jsonArrayIndex = route;
		this.routeFragmentProgressBar = bar;
		this.routeFragmentProgressPlaceholder = button;
	}

	
	public void toggleProgressBar()
	{
		if(routeFragmentProgressBar.getVisibility() == View.VISIBLE)
		{
			routeFragmentProgressBar.post(new Runnable()
			{
				@Override
				public void run() {
					routeFragmentProgressBar.setVisibility(View.GONE);
					
				}
			});
			routeFragmentProgressPlaceholder.post(new Runnable()
			{
				@Override
				public void run() 
				{
					routeFragmentProgressPlaceholder.setVisibility(View.VISIBLE);
				
				}

			});
		}
		else
		{
			routeFragmentProgressBar.post(new Runnable()
			{
				@Override
				public void run() 
				{
					routeFragmentProgressBar.setVisibility(View.VISIBLE);
					
				}
			});
			routeFragmentProgressPlaceholder.post(new Runnable()
			{
				@Override
				public void run() 
				{
					routeFragmentProgressPlaceholder.setVisibility(View.GONE);
				
				}

			});
		}
	}
	public void hideBottomBar()
	{
		routeFragmentBottomBar.post(new Runnable()
		{
			@Override
			public void run()
			{
				routeFragmentBottomBar.animate()
				.translationY(routeFragmentBottomBar.getHeight())
				.alpha(0.0f)
				.setDuration(300)
				.setListener(new AnimatorListenerAdapter() 
				{
					@Override
					public void onAnimationEnd(Animator animation) 
					{
						super.onAnimationEnd(animation);
						routeFragmentBottomBar.setVisibility(View.GONE);
					}
				});
			}
		});
		
	}
	public void showBottomBar()
	{
		routeFragmentBottomBar.post(new Runnable()
		{
			@Override
			public void run()
			{
				routeFragmentBottomBar.animate()
				.translationY(0)
				.alpha(1.0f)
				.setDuration(300)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						super.onAnimationStart(animation);
						routeFragmentBottomBar.setVisibility(View.VISIBLE);
					}
				});
			}
		});
		
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		toggleProgressBar();
		showBottomBar();
	}

	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		toggleProgressBar();
		hideBottomBar();
	}

	@Override
	protected RouteDetails doInBackground(Void... params) 
	{	        
		return getPolyline(json, jsonArrayIndex);
	}

	@Override
	protected void onPostExecute(RouteDetails route) 
	{
		super.onPostExecute(route); 
		comm.updateDirectionsList(json, jsonArrayIndex);

		map.clear();
		if(route != null)
		{
			final List<MarkerOptions> markers = route.getmarkers();
			final List<PolylineOptions> polyies = route.getpolyies();
			
			new Handler().postAtFrontOfQueue(new Runnable()
			{

				@Override
				public void run() 
				{
					PolylineOptions t= new PolylineOptions()
			        .width(lineWidth)
			        .color(Color.argb(255, 80, 73, 137));
					for (PolylineOptions temp : polyies) 
					{
						t.addAll(temp.getPoints());
					}
					for (MarkerOptions temp : markers)
					{
						map.addMarker(temp);
					}
					map.addPolyline(t);
				}
				
			});
			
		}

		toggleProgressBar();
		showBottomBar();
	}
	
	
	private List<LatLng> decodePoly(String encoded) 
	{
		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) 
		{
			int b, shift = 0, result = 0;
			do 
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);

			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do 
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);

			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng( (((double) lat / 1E5)),
					(((double) lng / 1E5) ));
			poly.add(p);
		}
		return poly;
	}

	public RouteDetails getPolyline(String result, int routeNumber) 
	{
		List<PolylineOptions> polyies = new ArrayList<PolylineOptions>();
		List<MarkerOptions> markers = new ArrayList<MarkerOptions>();

		try 
		{
			//Tranform the String response RESULT into a JSON object
			final JSONObject json = new JSONObject(result);
			//Get the Routes Array from the JsonObject
			JSONArray routeArray = json.getJSONArray("routes");
			Log.d("Routes: ", ""+routeArray.length());

			//Select which route to draw
			//Throws a JSONException if no route is found at index routeNumber
			JSONObject routes = routeArray.getJSONObject(routeNumber);

			JSONArray legsArray = routes.getJSONArray("legs");
			JSONObject legsObject = legsArray.getJSONObject(0);
			//Get total Seconds
	    	int secondCount = 0;
	    	for(int c = 0; c < legsArray.length(); c++)
	    	{
	    		JSONObject curleg = legsArray.getJSONObject(c);
	    		JSONObject duration = curleg.getJSONObject("duration");
	    		secondCount += duration.getInt("value");
	    	}

			JSONArray stepsArray = legsObject.getJSONArray("steps");

			for(int z = 0; z < stepsArray.length(); z++)
			{
				JSONObject step = stepsArray.getJSONObject(z);

				JSONObject polyline = step.getJSONObject("polyline");   
				if(step.getString("travel_mode").equals("TRANSIT"))
				{	    	           
					String encodedString = polyline.getString("points");
					List<LatLng> list = decodePoly(encodedString);

					for(int x = 0; x < list.size() - 1; x++)
					{
						LatLng src = list.get(x);
						LatLng dest = list.get(x+1);

						polyies.add(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
						.width(lineWidth)
						.color(Color.argb(255, 121, 14, 189)).geodesic(true));
					}

					if(step.has("transit_details"))
					{
						JSONObject transit_details = step.getJSONObject("transit_details");

						JSONObject arrival_stop = transit_details.getJSONObject("arrival_stop");
						JSONObject arrival_location = arrival_stop.getJSONObject("location");



						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.position(new LatLng(arrival_location.getDouble("lat"), arrival_location.getDouble("lng")));
						markerOptions.title("Stop name: " + arrival_stop.getString("name"));
						markerOptions.snippet("Line: " + transit_details.getString("headsign"));
						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
						markerOptions.flat(true);

						markers.add(markerOptions);



						JSONObject departure_stop = transit_details.getJSONObject("departure_stop");
						JSONObject departure_location = departure_stop.getJSONObject("location");

						markerOptions = new MarkerOptions();
						markerOptions.position(new LatLng(departure_location.getDouble("lat"), departure_location.getDouble("lng")));
						markerOptions.title("Stop name: " + departure_stop.getString("name"));
						markerOptions.snippet("Line:  " + transit_details.getString("headsign"));
						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
						markerOptions.flat(true);

						markers.add(markerOptions);
					}
				}

				if(step.getString("travel_mode").equals("WALKING"))
				{	        		   
					String encodedString = polyline.getString("points");
					List<LatLng> list = decodePoly(encodedString);

					for(int x = 0; x < list.size() - 1; x++)
					{
						LatLng src = list.get(x);
						LatLng dest = list.get(x+1);

						polyies.add(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
						.width(lineWidth)
						.color(Color.argb(150, 0, 100, 0)).geodesic(true));

					}
				}		        
			}
		} 
		catch (JSONException e) 
		{}

		RouteDetails resp = new RouteDetails(polyies, markers);
		return resp;
	}
}
