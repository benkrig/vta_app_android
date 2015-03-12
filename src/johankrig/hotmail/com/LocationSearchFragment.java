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
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnTouchListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class LocationSearchFragment extends Fragment
{
	private AutoCompleteTextView searchBar;
	private GoogleMap map;
	private View rootView;
	private ImageButton addressSearchButton;
	private Communicator comm;
	private String searchString = "";
	private AddressSearchAsyncTask geoTask;
	private GPSTracker gps;
	private Button clearSearchBarButton;
	private ProgressBar searchProgress;
	private ImageButton button1;
	private PopupWindow changeStatusPopUp;
	
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
		
		
		RelativeLayout touch = (RelativeLayout) getActivity().findViewById(R.id.touchLayout);
		
		touch.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				LinearLayout purplelayout = new LinearLayout(getActivity());
				LinearLayout greylayout = new LinearLayout(getActivity());
				LinearLayout bluelayout = new LinearLayout(getActivity());
				
				purplelayout.setBackgroundColor(getActivity().getResources().getColor(R.color.purple));
				greylayout.setBackgroundColor(getActivity().getResources().getColor(R.color.buttondarkgrey));
				bluelayout.setBackgroundColor(getActivity().getResources().getColor(R.color.exitblue));

				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
		            float x = event.getX()*event.getXPrecision() - 44;
		            float y = event.getY()*event.getYPrecision() - 130;


		            float x1 = event.getX()*event.getXPrecision() + 90;
		            float y1 = event.getY()*event.getYPrecision() - 130;


		            float x2 = event.getX()*event.getXPrecision() + 90;
		            float y2 = event.getY()*event.getYPrecision() - 30;
		            
		            
		            RelativeLayout.LayoutParams bp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            bp2.leftMargin = (int) x2;
		            bp2.topMargin = (int) y2;
		            bp2.height = 88;
		            bp2.width = 88;
		            greylayout.setLayoutParams(bp2);
		            
		            RelativeLayout.LayoutParams bp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            bp.leftMargin = (int) x;
		            bp.topMargin = (int) y;
		            bp.height = 88;
		            bp.width = 88;
		            purplelayout.setLayoutParams(bp);

		            
		            RelativeLayout.LayoutParams bp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            bp1.leftMargin = (int) x1;
		            bp1.topMargin = (int) y1;
		            bp1.height = 88;
		            bp1.width = 88;
		            bluelayout.setLayoutParams(bp1);
		            ((RelativeLayout)v.getParent()).addView(purplelayout);
		            ((RelativeLayout)v.getParent()).addView(bluelayout);
		            ((RelativeLayout)v.getParent()).addView(greylayout);
		            
		            return true;
				}
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					Toast.makeText(getActivity(), ""+((RelativeLayout)v.getParent()).getChildCount(), Toast.LENGTH_SHORT).show();
					((RelativeLayout)v.getParent()).removeAllViews();
		            rootView.invalidate();
					Toast.makeText(getActivity(), ""+((RelativeLayout)v.getParent()).getChildCount(), Toast.LENGTH_SHORT).show();


					return true;
				}
	            
	            
				return false;
			}
			
			
		});
		
		
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
		
		
		button1 = (ImageButton) getActivity().findViewById(R.id.nearbyMenuButton);
		button1.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View v) 
            {
                 int[] location = new int[2];
   
                 // Get the x, y location and store it in the location[] array
                 // location[0] = x, location[1] = y.
                 v.getLocationOnScreen(location);

                 //Initialize the Point with x, and y positions
                 Point point = new Point();
                 point.x = location[0];
                 point.y = location[1];
                 showStatusPopup(getActivity(), point);
            }
        });

		
		
        

		
	    searchProgress = (ProgressBar) getActivity().findViewById(R.id.locationSearchProgressBar);
		addressSearchButton = (ImageButton) getActivity().findViewById(R.id.routeMenuButton);

		
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
	
	
    // The method that displays the popup.
	private void showStatusPopup(final Activity context, Point p) 
	{

	    // Inflate the popup_layout.xml
	    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    final View layout = layoutInflater.inflate(R.layout.status_popup_layout, null);
	    
	    TextView food = (TextView) layout.findViewById(R.id.foodTextView);
	    
	    food.setOnTouchListener(new OnTouchListener()
	    {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				
				TextView subTextView = (TextView) layout.findViewById(R.id.foodTextView);
				subTextView.setTextColor(context.getResources().getColor(R.color.backgroundblue));				
			    LinearLayout foodSub = (LinearLayout) layout.findViewById(R.id.foodSubLayout);
			    
			    foodSub.setVisibility(View.VISIBLE);
			    
			    if(event.getAction() == MotionEvent.ACTION_MOVE)
			    {
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeAsian)))
					{
						TextView fooda = (TextView) layout.findViewById(R.id.foodTypeAsian);
						fooda.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						TextView fooda = (TextView) layout.findViewById(R.id.foodTypeAsian);
						fooda.setTextColor(context.getResources().getColor(R.color.purple));
					}
					
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeFastFood)))
					{
						TextView fooda = (TextView) layout.findViewById(R.id.foodTypeFastFood);
						fooda.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						TextView fooda = (TextView) layout.findViewById(R.id.foodTypeFastFood);
						fooda.setTextColor(context.getResources().getColor(R.color.purple));
					}
					
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeThai)))
					{
						TextView fooda = (TextView) layout.findViewById(R.id.foodTypeThai);
						fooda.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						TextView fooda = (TextView) layout.findViewById(R.id.foodTypeThai);
						fooda.setTextColor(context.getResources().getColor(R.color.purple));
					}
					
					return false;
			    }
			    
			    if(event.getAction() == MotionEvent.ACTION_UP)
			    {
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeAsian)))
					{
						Log.i("", "IN ASIAN");
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("asian food nearby");
		                	geoTask.execute();
		                }
						changeStatusPopUp.dismiss();
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeFastFood)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("fast food nearby");
		                	geoTask.execute();
		                }
						changeStatusPopUp.dismiss();
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeThai)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("thai food nearby");
		                	geoTask.execute();
		                }
						changeStatusPopUp.dismiss();
					}
					
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					foodSub.setVisibility(View.GONE);
					return true;
					
		        }
			    

				return false;			    
			}
	    	
	    });

	    
	    TextView shopping = (TextView) layout.findViewById(R.id.shoppingTextView);
	    shopping.setOnTouchListener(new OnTouchListener()
	    {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{			
				TextView subTextView = (TextView) layout.findViewById(R.id.shoppingTextView);
				subTextView.setTextColor(context.getResources().getColor(R.color.backgroundblue));				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.shoppingTextView)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("shopping nearby");
		                	geoTask.execute();
		                }
		                changeStatusPopUp.dismiss();
					}
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					return true;
				}
				return false;
			}
	    });
	    
	    TextView entertainment = (TextView) layout.findViewById(R.id.entertainmentTextView);
	    entertainment.setOnTouchListener(new OnTouchListener()
	    {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{			
				TextView subTextView = (TextView) layout.findViewById(R.id.entertainmentTextView);
				subTextView.setTextColor(context.getResources().getColor(R.color.backgroundblue));				
				
				LinearLayout entertainmentSub = (LinearLayout) layout.findViewById(R.id.entertainmentSubLayout);
				entertainmentSub.setVisibility(View.VISIBLE);
				
				if(event.getAction() == MotionEvent.ACTION_MOVE)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.entertainmentTypeMovies)))
					{
						TextView cinema = (TextView) layout.findViewById(R.id.entertainmentTypeMovies);
						cinema.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						TextView cinema = (TextView) layout.findViewById(R.id.entertainmentTypeMovies);
						cinema.setTextColor(context.getResources().getColor(R.color.purple));
					}
					return false;
				}
				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.entertainmentTypeMovies)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("cinema nearby");
		                	geoTask.execute();
		                }
						changeStatusPopUp.dismiss();
					}
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					entertainmentSub.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
	    });
	    
	    TextView recreation = (TextView) layout.findViewById(R.id.recreationTextView);
	    recreation.setOnTouchListener(new OnTouchListener()
	    {
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{				
				
				TextView subTextView = (TextView) layout.findViewById(R.id.recreationTextView);
				subTextView.setTextColor(context.getResources().getColor(R.color.backgroundblue));				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.recreationTextView)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("family fun nearby");
		                	geoTask.execute();
		                }
		                changeStatusPopUp.dismiss();
					}
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					return true;
				}
				return false;
			}
	    });
	    
	    
	    // Creating the PopupWindow
	    changeStatusPopUp = new PopupWindow(context);
	    changeStatusPopUp.setContentView(layout);
	    changeStatusPopUp.setWidth(LinearLayout.LayoutParams.FILL_PARENT);
	    changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
	    changeStatusPopUp.setFocusable(true);
	
	    // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
	    int OFFSET_X = -20;
	    int OFFSET_Y = button1.getHeight();
	
	    //Clear the default translucent background
	    changeStatusPopUp.setBackgroundDrawable(new BitmapDrawable());
	
	    // Displaying the popup at the specified location, + offsets.
	    changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
	}
	public static boolean isPointInsideView(float x, float y, View view){
	    int location[] = new int[2];
	    view.getLocationOnScreen(location);
	    int viewX = location[0];
	    int viewY = location[1];

	    //point is inside view bounds
	    if(( x > viewX && x < (viewX + view.getWidth())) &&
	            ( y > viewY && y < (viewY + view.getHeight()))){
	        return true;
	    } else {
	        return false;
	    }
	}
	
	public void createMarkerFromTouch(Address address)
	{
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(new LatLng(address.getLatitude(), address.getLongitude()));
		markerOptions.title("Let's go here!");
		markerOptions.snippet(address.getAddressLine(0));
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_place));
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
	        	
	            return addressresult;
	        } 
	        catch (IOException e) 
	        {
	            Log.e(LOG_TAG, "Error connecting to Places API", e);
	            
	            SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	        	
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
	            
	            SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	            
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


