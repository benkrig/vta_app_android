

package johankrig.hotmail.com;

import java.util.ArrayList;
import java.util.List;

import org.json.*;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


class DrawBus  extends AsyncTask<String, Void, List<MarkerOptions>> {
	

	MarkerOptions markerOptions;
	GoogleMap map;
	Context context;
	JSONObject data = null;
	JSONObject json = null;
	JSONArray buses = null;
	JSONObject location = null;
	List<MarkerOptions> buslist = new ArrayList<MarkerOptions>();




	
	public DrawBus(Context c, GoogleMap m)
	{
		this.context = c;
		map = m;
	}

    @Override
    protected List<MarkerOptions> doInBackground(String... params) {
    	buslist.clear();

        
		String result;
		JSONParser j = new JSONParser();
		result = j.getVehicleJson();
		
		try{
		

		json = new JSONObject(result);
		data = json.getJSONObject("data");

		buses = data.getJSONArray("255");
		
		for(int c = 0; c < buses.length(); c ++)
		{
			
			//buses.getJSONObject(c);

	        

			
			JSONObject location = null;

			location = buses.getJSONObject(c).getJSONObject("location");
			LatLng position = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));


			
			markerOptions = new MarkerOptions();
    		markerOptions.position(position);
    		markerOptions.title(buses.getJSONObject(c).getString("vehicle_id"));
    		markerOptions.snippet(buses.getJSONObject(c).getString("last_updated_on"));
    		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));

    		buslist.add(markerOptions);
    	
		}

		}catch(Exception e)
    		{       
}
    		
		
		return buslist;
    }

    @Override
    protected void onPostExecute(List<MarkerOptions> buses2) 
    {
    	map.clear();
        for(int c = 0; c < buslist.size(); c ++)
        {
        	
        	map.addMarker(buslist.get(c));
        }
    	
    	//map.addMarker(buses.get(0));
		

    }
}