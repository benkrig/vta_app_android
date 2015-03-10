package johankrig.hotmail.com;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

 class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable 
 {
    private ArrayList<String> resultList;
    private GPSTracker gps;
    private static final String LOG_TAG = "VTA";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
    
    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) 
    {
        super(context, textViewResourceId);
        gps = new GPSTracker(context);
    }
    
    @Override
    public int getCount() 
    {
    	if(resultList != null)
    	{
    		return resultList.size();
    	}
    	else
    	{
    		return 0;
    	}
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() 
    {
        Filter filter = new Filter() 
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) 
            {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) 
                {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) 
            {
                if (results != null && results.count > 0) 
                {
                    notifyDataSetChanged();
                }
                else 
                {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private ArrayList<String> autocomplete(String input) 
    {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try 
        {
            StringBuilder endpointURL = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            endpointURL.append("?key=" + API_KEY);
            endpointURL.append("&components=country:us");
            endpointURL.append("&location="+gps.getLatitude()+","+gps.getLongitude());
            endpointURL.append("&radius=500");
            endpointURL.append("&types=establishment");

            endpointURL.append("&input=" + URLEncoder.encode(input, "utf8"));

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
            
            SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
        	
            return resultList;
        } 
        catch (IOException e) 
        {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            
            SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
        	
            return resultList;
        } 
        finally 
        {
            if (conn != null) 
            {
                conn.disconnect();
            }
        }

        try 
        {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) 
            {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } 
        catch (JSONException e) 
        {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
            
            SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
        	
        }

        return resultList;
    }
}
