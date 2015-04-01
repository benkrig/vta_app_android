package johankrig.hotmail.com;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.AsyncTask;
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


class ResponseObject 
{
	List<PolylineOptions> polyies;
	List<MarkerOptions> markers;
	
	public ResponseObject(List<PolylineOptions> polyies, List<MarkerOptions> markers)
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

public class DecodeRouteJSON extends AsyncTask<Void, Void, ResponseObject> 
{
	private GoogleMap map;
	private String json;
	private int route;
	ResponseObject response = null;
	int lineWidth;
	
	private ProgressBar bar;

	private Button button;
	private FragmentCommunicator comm;
	private LinearLayout bottomBar;

	public DecodeRouteJSON(LinearLayout bottomBar, int px, ProgressBar bar, Button button, FragmentCommunicator comm, GoogleMap map, String json, int route)
	{
		this.bottomBar = bottomBar;
		this.lineWidth = px;
		this.map = map;
		this.json = json;
		this.route = route;
		this.comm = comm;
		this.bar = bar;
		this.button = button;
	}
	
	public void hideBottomBar()
	{
		bottomBar.animate()
        .translationY(bottomBar.getHeight())
        .alpha(0.0f)
        .setDuration(300)
        .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                bottomBar.setVisibility(View.GONE);
            }
        });
	}
	public void showBottomBar()
	{
		bottomBar.animate()
        .translationY(0)
        .alpha(1.0f)
        .setDuration(300)
        .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
        		bottomBar.setVisibility(View.VISIBLE);
            }
        });
	}
	
	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		button.setVisibility(View.VISIBLE);
		bar.setVisibility(View.GONE);
		
		bottomBar.animate().alpha(1.0f).setDuration(2000);
        //bottomBar.setVisibility(View.VISIBLE);
	}
		
	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		button.setVisibility(View.GONE);
		bar.setVisibility(View.VISIBLE);
        comm.updateDirectionsList(json, route);
        
		hideBottomBar();
	}
	
	@Override
	protected ResponseObject doInBackground(Void... params) 
	{	        
		return drawPath(json, route);
	}
	
	@Override
	protected void onPostExecute(ResponseObject result) 
	{
		super.onPostExecute(result);  
		map.clear();
		if(result != null)
		{
			List<MarkerOptions> markers = result.getmarkers();
			Log.d("aaa", markers.get(0).getPosition()+"");
	
			List<PolylineOptions> polyies = result.getpolyies();
	
			for (PolylineOptions temp : polyies) 
	        {
	        	map.addPolyline(temp);
	        }
	        for (MarkerOptions temp : markers)
	        {
	        	map.addMarker(temp);
	        }
		}
        
		button.setVisibility(View.VISIBLE);
		bar.setVisibility(View.GONE);
		
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
	    
	    public ResponseObject drawPath(String result, int routeNumber) 
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
				                .color(Color.argb(190, 121, 14, 189)).geodesic(true));
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
		    {
		    	//Called if no route is found at Index routeNumber
		    	SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
		    }
		    
		    ResponseObject resp = new ResponseObject(polyies, markers);
	        return resp;
		} 
	   
}
