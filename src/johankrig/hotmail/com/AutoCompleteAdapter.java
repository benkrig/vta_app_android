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
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

/*
class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable 
{
	private final String region = " San Jose, CA";
	private LayoutInflater mInflater;
	private Geocoder mGeocoder;
	private StringBuilder mSb = new StringBuilder();
	GoogleMap map;
	private final int numberofDropDownAddresses = 4;
	
	public AutoCompleteAdapter(final Context context, GoogleMap googlemap) 
	{
		super(context, -1);
		mInflater = LayoutInflater.from(context);
		mGeocoder = new Geocoder(context);
		map = googlemap;
	}
 
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) 
	{
		final TextView tv;
		if (convertView != null) 
		{
			tv = (TextView) convertView;
		} 
		else 
		{
			tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
		}
 
		tv.setText(createFormattedAddressFromAddress(getItem(position)));
		return tv;
	}
 
	private String createFormattedAddressFromAddress(final Address address) 
	{
		mSb.setLength(0);
		final int addressLineSize = address.getMaxAddressLineIndex();
		for (int i = 0; i < addressLineSize; i++) 
		{
			mSb.append(address.getAddressLine(i));
			if (i != addressLineSize - 1) 
			{
				mSb.append(", ");
			}
		}
		return mSb.toString();
	}
 
	@Override
	public Filter getFilter() 
	{
		Filter myFilter = new Filter() 
		{
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) 
			{
				List<Address> addressList = null;
				if (constraint != null) 
				{
					try 
					{
						addressList = mGeocoder.getFromLocationName((String) constraint + region, numberofDropDownAddresses);
					} 
					catch (IOException e) 
					{
					}
				}
				if (addressList == null) 
				{
					addressList = new ArrayList<Address>();
				}
 
				
				final FilterResults filterResults = new FilterResults();
				filterResults.values = addressList;
				filterResults.count = addressList.size();
 
				return filterResults;
			}
 
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence contraint, final FilterResults results) 
			{
				clear();
				for (Address address : (List<Address>) results.values) 
				{
					add(address);
				}
				if (results.count > 0) 
				{
					notifyDataSetChanged();
				} 
				else 
				{
					notifyDataSetInvalidated();
				}
			}
 
			@Override
			public CharSequence convertResultToString(final Object resultValue) 
			{
				return resultValue == null ? "" : ((Address) resultValue).getAddressLine(0);
			}
		};
		return myFilter;
	}
}
*/

 class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> resultList;
    Context context;
    GPSTracker gps;
    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
        gps = new GPSTracker(context);

    }
    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
    
    private static final String LOG_TAG = "ExampleApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:us");
            sb.append("&location="+gps.getLatitude()+","+gps.getLongitude());
            sb.append("&radius=5000");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

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
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}
