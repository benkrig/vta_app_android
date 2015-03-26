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
import com.google.android.gms.maps.Projection;
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
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LocationSearchFragment extends Fragment
{
	private AutoCompleteTextView searchBar;
	private GoogleMap map;
	private View rootView;
	private ImageButton addressSearchButton;
	private FragmentCommunicator comm;
	private String searchString = "";
	private AddressSearchAsyncTask geoTask;
	private GPSTracker gps;
	private Button clearSearchBarButton;
	private ProgressBar searchProgress;
	private PopupWindow changeStatusPopUp;
	private RelativeLayout touchLayout;
	private LinearLayout hiddenSearchBar;
	private LinearLayout topScreenBar;
	private Handler runnableHandler;

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

        return rootView;
    }
	
	//hide keyboard
	public void hideKeyBoard()
	{
	    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(hiddenSearchBar.getWindowToken(), 0);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		comm = (FragmentCommunicator) getActivity();
		runnableHandler = new Handler();			
		hiddenSearchBar = (LinearLayout) getActivity().findViewById(R.id.barid);
		topScreenBar = (LinearLayout) getActivity().findViewById(R.id.locationfragmenttopbar);
		touchLayout = (RelativeLayout) getActivity().findViewById(R.id.touchlayout);
		
		touchLayout.setOnTouchListener(new OnTouchListener()
		{
			private Vibrator vb = (Vibrator)   getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            
			private MotionEvent event;
			private View v;
			private RelativeLayout purplelayout = new RelativeLayout(getActivity());
			private RelativeLayout greylayout = new RelativeLayout(getActivity());
			private RelativeLayout bluelayout = new RelativeLayout(getActivity());
			private boolean purple = false;
			private boolean blue = false;
			private boolean grey = false;
			
			private Runnable r = new Runnable()
			{
			    public void run()
			    {
			    	((RelativeLayout)v).setBackgroundColor(0xAA000000);
		            vb.vibrate(50);
			    	map.getUiSettings().setScrollGesturesEnabled(false);
			    	
					purplelayout.setBackgroundResource(R.drawable.purplebg_ic_action_place);
					greylayout.setBackgroundResource(R.drawable.black_ic_action_search);
					bluelayout.setBackgroundResource(R.drawable.blue_ic_action_locate);
					
					int oh = ((RelativeLayout)v).getHeight() / 2;
					int ow = ((RelativeLayout)v).getWidth() / 2;
					
					float purpleLayoutX = 0;
					float purpleLayoutY = 0;
					float blueLayoutX = 0;
					float blueLayoutY = 0;
					float greyLayoutX = 0;
					float greyLayoutY = 0;

					if(event.getY()*event.getYPrecision() >= oh)
					{
						//lower
						
						if(event.getX()*event.getXPrecision() > ow)
						{
							//right
							purpleLayoutX = event.getX()*event.getXPrecision()- 10;
				            purpleLayoutY = event.getY()*event.getYPrecision() - 230;

				            blueLayoutX = event.getX()*event.getXPrecision() - 180;
				            blueLayoutY = event.getY()*event.getYPrecision() - 180;

				            greyLayoutX = event.getX()*event.getXPrecision() - 180;
				            greyLayoutY = event.getY()*event.getYPrecision();
						}
						else
						{
							//left
							
							purpleLayoutX = event.getX()*event.getXPrecision() - 56;
				            purpleLayoutY = event.getY()*event.getYPrecision() - 250;

				            blueLayoutX = event.getX()*event.getXPrecision() + 100;
				            blueLayoutY = event.getY()*event.getYPrecision() - 200;

				            greyLayoutX = event.getX()*event.getXPrecision() + 100;
				            greyLayoutY = event.getY()*event.getYPrecision() - 44;
						}
					}
					else
					{
						//upper				
						
						if(event.getX()*event.getXPrecision() > ow)
						{
							//right
							
							//p
							purpleLayoutX = event.getX()*event.getXPrecision() - 190;
				            purpleLayoutY = event.getY()*event.getYPrecision() - 144;

				            //b
				            blueLayoutX = event.getX()*event.getXPrecision() - 190;
				            blueLayoutY = event.getY()*event.getYPrecision();

				            //g
				            greyLayoutX = event.getX()*event.getXPrecision() - 44;
				            greyLayoutY = event.getY()*event.getYPrecision() + 64;
						}
						else
						{
							//left
							
							//p
							purpleLayoutX = event.getX()*event.getXPrecision() + 90;
				            purpleLayoutY = event.getY()*event.getYPrecision() - 144;

				            //b
				            blueLayoutX = event.getX()*event.getXPrecision() + 90;
				            blueLayoutY = event.getY()*event.getYPrecision();

				            //g
				            greyLayoutX = event.getX()*event.getXPrecision() - 44;
				            greyLayoutY = event.getY()*event.getYPrecision() + 44;
						}
					}
					
		            RelativeLayout.LayoutParams greyLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            greyLayoutParams.leftMargin = (int) greyLayoutX;
		            greyLayoutParams.topMargin = (int) greyLayoutY;
		            greyLayoutParams.height = 64;
		            greyLayoutParams.width = 64;
		            greylayout.setLayoutParams(greyLayoutParams);
		            
		            RelativeLayout.LayoutParams purpleLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            purpleLayoutParams.leftMargin = (int) purpleLayoutX;
		            purpleLayoutParams.topMargin = (int) purpleLayoutY;
		            purpleLayoutParams.height = 64;
		            purpleLayoutParams.width = 64;
		            purplelayout.setLayoutParams(purpleLayoutParams);
		            
		            RelativeLayout.LayoutParams blueLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            blueLayoutParams.leftMargin = (int) blueLayoutX;
		            blueLayoutParams.topMargin = (int) blueLayoutY;
		            blueLayoutParams.height = 64;
		            blueLayoutParams.width = 64;
		            bluelayout.setLayoutParams(blueLayoutParams);
		            		            		            
		            ((RelativeLayout)v).addView(purplelayout);
		            ((RelativeLayout)v).addView(bluelayout);
		            ((RelativeLayout)v).addView(greylayout);
			    }
			};

			private int TOUCH_WAIT = 250;
			private float mDownX;
			private float mDownY;
			private final float SCROLL_THRESHOLD = 15;
			private boolean isOnClick;

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					this.v = v;
					this.event = event;	
					
					if(!isPointInsideView(event.getRawX(), event.getRawY(), hiddenSearchBar))
					{
						hiddenSearchBar.setVisibility(View.GONE);
						addressSearchButton.setClickable(false);
					}
					
					runnableHandler.removeCallbacks(r);
					runnableHandler.postDelayed(r, TOUCH_WAIT);
					
					mDownX = event.getX();
		            mDownY = event.getY();
		            isOnClick = true;
					
					return true;
				}
				
				if(event.getAction() == MotionEvent.ACTION_MOVE)
			    {
					if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) 
					{
		                runnableHandler.removeCallbacks(r);
		                isOnClick = false;
		            }
					if(isPointInsideView(event.getRawX(), event.getRawY(), purplelayout) && !purple)
					{
						purple = true;
						grey = false;
						blue = false;
			            vb.vibrate(50);
					}
					else if(isPointInsideView(event.getRawX(), event.getRawY(), greylayout) && !grey)
					{
						purple = false;
						grey = true;
						blue = false;
			            vb.vibrate(50);
					}
					else if(isPointInsideView(event.getRawX(), event.getRawY(), bluelayout) && !blue)
					{
						purple = false;
						grey = false;
						blue = true;
			            vb.vibrate(50);
					}
					return false;
			    }
				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
		            ((RelativeLayout)v).setBackgroundResource(0);

					if(isPointInsideView(event.getRawX(), event.getRawY(), purplelayout))
					{
						//Convert touch location to LatLng
						LatLng point = map.getProjection().fromScreenLocation(new Point((int)event.getX(), (int)event.getY()));
						comm.returnRoutes(point);
					}
					else if(isPointInsideView(event.getRawX(), event.getRawY(), bluelayout))
					{
						int[] location = new int[2];
						v.getLocationOnScreen(location);
						Point p = new Point();
						p.x = location[0];
						p.y = location[1];
						
						nearbyPopUp(getActivity(), p);
					}
					else if(isPointInsideView(event.getRawX(), event.getRawY(), greylayout))
					{
						hiddenSearchBar.setVisibility(View.VISIBLE);
		            	addressSearchButton.setClickable(true);
					}
					
					((RelativeLayout)v).removeView(purplelayout);
					((RelativeLayout)v).removeView(greylayout);
					((RelativeLayout)v).removeView(bluelayout);
					
			    	map.getUiSettings().setScrollGesturesEnabled(true);
					runnableHandler.removeCallbacks(r);
					
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
		
		map.setOnMapClickListener(new OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng point) 
			{
				Projection projection = map.getProjection();
				
				//Convert Points to on screen location
				Point p1 = new Point();
				p1 = projection.toScreenLocation(point);
				
				if(isPointInsideView(p1.x, (p1.y + topScreenBar.getHeight() + (topScreenBar.getHeight()/2)), hiddenSearchBar))
				{
				}
				else
				{
					hideKeyBoard();
				}
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
            	//searchBar.dismissDropDown();
            	hideKeyBoard();
            	hiddenSearchBar.setVisibility(View.GONE);
            	addressSearchButton.setClickable(false);
		
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
				
				if(geoTask != null)
					geoTask.cancel(true);
				
				searchBar.setText("");
			}
		});
	}
	
	public int getStatusBarHeight() 
	{
	    int result = 0;
	    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	    if (resourceId > 0) 
	    {
	        result = getResources().getDimensionPixelSize(resourceId);
	    }
	    return result;
	}
	
    // The method that displays the popup.
	private void nearbyPopUp(final Activity context, Point p) 
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
		                map.clear();
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
		                map.clear();
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
		                map.clear();
						changeStatusPopUp.dismiss();
					}
					
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					foodSub.setVisibility(View.INVISIBLE);
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
				
				LinearLayout shopSub = (LinearLayout) layout.findViewById(R.id.shoppingSubLayout);
			    
			    shopSub.setVisibility(View.VISIBLE);
			    
			    if(event.getAction() == MotionEvent.ACTION_MOVE)
			    {
			    	TextView shopClothes = (TextView) layout.findViewById(R.id.shoppingTypeClothes);
					TextView shopShoes = (TextView) layout.findViewById(R.id.shoppingTypeShoes);

					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.shoppingTypeClothes)))
					{
						shopClothes.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						shopClothes.setTextColor(context.getResources().getColor(R.color.purple));
					}
					
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.shoppingTypeShoes)))
					{
						shopShoes.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						shopShoes.setTextColor(context.getResources().getColor(R.color.purple));
					}
					
					return false;
			    }
				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.shoppingTypeClothes)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("clothes shopping nearby");
		                	geoTask.execute();
		                }
		                map.clear();
		                changeStatusPopUp.dismiss();
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.shoppingTypeShoes)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress, addressSearchButton);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setLocation("shoes shopping nearby");
		                	geoTask.execute();
		                }
		                map.clear();
		                changeStatusPopUp.dismiss();
					}

					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					shopSub.setVisibility(View.INVISIBLE);

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
		                map.clear();
						changeStatusPopUp.dismiss();
					}
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					entertainmentSub.setVisibility(View.INVISIBLE);
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
				
				LinearLayout recSub = (LinearLayout) layout.findViewById(R.id.recreationSubLayout);
				recSub.setVisibility(View.VISIBLE);
				
				
				if(event.getAction() == MotionEvent.ACTION_MOVE)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.recreationTypeParks)))
					{
						TextView cinema = (TextView) layout.findViewById(R.id.recreationTypeParks);
						cinema.setTextColor(context.getResources().getColor(R.color.backgroundblue));
					}
					else
					{
						TextView cinema = (TextView) layout.findViewById(R.id.recreationTypeParks);
						cinema.setTextColor(context.getResources().getColor(R.color.purple));
					}
					return false;
				}
				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.recreationTypeParks)))
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
		                map.clear();
		                changeStatusPopUp.dismiss();
					}
					subTextView.setTextColor(context.getResources().getColor(R.color.purple));				
					recSub.setVisibility(View.INVISIBLE);
					return true;
				}
				return false;
			}
	    });
	    
	    int h = getStatusBarHeight() + topScreenBar.getHeight();
	    
	    //popup is being created at too high of a coord
	    //Creating the PopupWindow
	    changeStatusPopUp = new PopupWindow(context);
	    changeStatusPopUp.setContentView(layout);
	    changeStatusPopUp.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
	    changeStatusPopUp.setHeight(touchLayout.getHeight());
	    changeStatusPopUp.setFocusable(true);
	
	    //Clear the default translucent background
	    changeStatusPopUp.setBackgroundDrawable(new BitmapDrawable());
	    //Displaying the popup at the specified location, + offsets.
	    int[] p1 = new int[2];
	    touchLayout.getLocationOnScreen(p1);
	    changeStatusPopUp.showAtLocation(touchLayout, Gravity.NO_GRAVITY, 0, h);
	}
	
	
	public static boolean isPointInsideView(float x, float y, View view)
	{
	    int location[] = new int[2];
	    view.getLocationOnScreen(location);
	    int viewX = location[0];
	    int viewY = location[1];

	    //point is inside view bounds
	    if(( x > viewX && x < (viewX + view.getWidth())) &&
	            ( y > viewY && y < (viewY + view.getHeight())))
	    {
	        return true;
	    }
	    else 
	    {
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
	FragmentCommunicator comm;
	
	public InfoWindowClickAdapter(Context context, FragmentCommunicator comm)
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


