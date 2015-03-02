package johankrig.hotmail.com;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import johankrig.hotmail.com.R;
import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;


public class LocationSearchFragment extends Fragment
{
	private AutoCompleteTextView searchBar;
	private GoogleMap map;
	private View rootView;
	private Button addressSearchButton;
	private Communicator comm;
	private String searchString = "";
	private AddressSearchAsyncTask geoTask;
	private GPSTracker gps;
	private Button clearSearchBarButton;
	private ProgressBar searchProgress;
	
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
	        rootView = inflater.inflate(R.layout.fragment_main, container, false);
	    } 
	    catch (InflateException e) 
	    {
	        //map is already there, just return view as it is
	    }
        Log.d("pro", ""+android.os.Build.MODEL);
        Log.d("pro", ""+android.os.Build.PRODUCT);
        Log.d("pro", ""+android.os.Build.VERSION.SDK_INT);
        Log.d("pro", ""+android.os.Build.MANUFACTURER);
        Log.d("pro", ""+android.os.Build.BRAND);


        Log.d("pro", ""+android.os.Build.DEVICE);

		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mainmap)).getMap();
        map.setMyLocationEnabled(true);
        
        map.getUiSettings().setMyLocationButtonEnabled(true);
        //Hide keyboard on map click
        map.setOnMapClickListener(new OnMapClickListener()
        {
			@Override
			public void onMapClick(LatLng point) 
			{
				hideKeyBoard();
			}
        	
        });
        
        map.setOnMapLongClickListener(new OnMapLongClickListener()
        {
			@Override
			public void onMapLongClick(LatLng point) 
			{
				//GeoTask get location at Marker
				GetMarkerFromTouch getad = new GetMarkerFromTouch(getActivity(), point);
				getad.execute();
			}
        	
        });
                
		return rootView;
    }
	
	//hide keyboard
	public void hideKeyBoard()
	{
		InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		View focusedView = getActivity().getCurrentFocus();
	    
		//Checks for NullPointerException
	    if (focusedView != null) 
	    {
	        inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
	                InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		comm = (Communicator) getActivity();
		
        map.setOnInfoWindowClickListener(new InfoWindowClickAdapter(getActivity(), comm));
        
        
		//center map on user
		gps = new GPSTracker(getActivity());
		if(gps.canGetLocation())
		{
			LatLng updateLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
		}
		else
		{
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					37.3333, -121.9000), 12.0f));
		}
		
	    searchProgress = (ProgressBar) getActivity().findViewById(R.id.locationSearchProgressBar);
		addressSearchButton = (Button) getActivity().findViewById(R.id.routeMenuButton);

		
    	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		
		searchBar = (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
		searchBar.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line));
		//hide keyboard once user selects item
		searchBar.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				hideKeyBoard();
			}
			
		});
		
		addressSearchButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {            	
            	//hide keyboard and search bar
            	searchBar.dismissDropDown();
            	hideKeyBoard();
            	
        		searchString = searchBar.getEditableText().toString();
                
                //AsyncTask can only execute ONCE
                //Status.FINISHED, create a new instance : this sets mStatus to PENDING
                //Status.PENDING, execute.
                if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
                {
                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
                }
                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
                {
                	geoTask.setLocation(searchString);
                	geoTask.execute();
                }                
            }
        });
		
		clearSearchBarButton = (Button) getActivity().findViewById(R.id.clearSearchBarButton);
		clearSearchBarButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				map.clear();
				geoTask.cancel(true);
				searchBar.setText("");
			}
			
		});
	}
	
	public void createMarkerFromTouch(Address address)
	{
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(new LatLng(address.getLatitude(), address.getLongitude()));
		markerOptions.title("Let's go here!");
		markerOptions.snippet(address.getAddressLine(0));
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon));
   		markerOptions.flat(true);
		map.addMarker(markerOptions);
	}
	
	class GetMarkerFromTouch extends AsyncTask<String, Address, Address>
	{
		
	    private final String LOG_TAG = "VTA";
	    //https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=API_KEY
	    private final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/geocode";
	    private final String OUT_JSON = "/json?";
	    private final String API_KEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
		//In meters

		Context context;
		LatLng latlng;
		
		public GetMarkerFromTouch(Context context, LatLng latlng)
		{
			this.latlng = latlng;
			this.context = context;
		}

	    @Override
	    protected Address doInBackground(String... params)
	    {
	        Address address = null;
	        address = this.getAddresses(latlng);
	        
	        return address;
	    }

	    @Override
	    protected void onPostExecute(Address address) 
	    {
	    	//do stuff with address
	    	createMarkerFromTouch(address);
	    }
	    

	    public Address getAddresses(LatLng latlng) 
	    {
	    	Address addressresult = new Address(Locale.US);
	        HttpURLConnection conn = null;
	        StringBuilder jsonResults = new StringBuilder();
	        
	        //Send request to PLACES API
	        try 
	        {

	            StringBuilder endpointURL = new StringBuilder(PLACES_API_BASE + OUT_JSON);
	            endpointURL.append("latlng="+this.latlng.latitude+","+this.latlng.longitude);
	            endpointURL.append("&key=" + API_KEY);

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
	            return addressresult;
	        } 
	        catch (IOException e) 
	        {
	            Log.e(LOG_TAG, "Error connecting to Places API", e);
	            return addressresult;
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

	            //get formatted address of first result
	            JSONObject result = results.getJSONObject(0);
	            addressresult.setLatitude(latlng.latitude);
	            addressresult.setLongitude(latlng.longitude);

	            addressresult.setAddressLine(0, result.getString("formatted_address"));
	        }
	        catch (JSONException e) 
	        {
	            Log.e(LOG_TAG, "Cannot process JSON results", e);
	        }
	        return addressresult;
	    }
	}
}


class InfoWindowClickAdapter implements OnInfoWindowClickListener
{
	Context context;
	Communicator comm;
	
	public InfoWindowClickAdapter(Context context, Communicator comm)
	{
		this.context = context;
		this.comm = comm;
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) 
	{
		comm.getPlaceDetails(marker.getPosition(), marker.getTitle(), marker.getSnippet());
	}
	
}


