package com.metafora.droid;

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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
	private FragmentCommunicator comm;
	private String searchString = "";
	private AddressSearchAsyncTask geoTask;
	private GPSTracker gps;
	private ImageButton clearSearchBarButton;
	private ProgressBar searchProgress;
	private PopupWindow changeStatusPopUp;
	private RelativeLayout touchLayout;
	private LinearLayout hiddenSearchBar;
	private Handler runnableHandler;
	private ImageView myLocationButton;
	TextView go;
	TextView near;


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
	    
	    gps = new GPSTracker(getActivity());
	    
	    if(!gps.canGetLocation)
	    {
	    	gps.showSettingsAlert();
	    }
	    
        return rootView;
    }
	
	//hide keyboard
	public void hideKeyBoard()
	{
	    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    searchBar.clearFocus();
	    imm.hideSoftInputFromWindow(hiddenSearchBar.getWindowToken(), 0);
	}
	
	
	public static int convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    int dp = (int) (px / (metrics.densityDpi / 160f));
	    return dp;
	}
	
	public int dpToPx(int dp) 
	{
	    DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		/*
		 * Prompt User to turn on GPS
		 * */
		
		
		
		comm = (FragmentCommunicator) getActivity();
		runnableHandler = new Handler();			
		hiddenSearchBar = (LinearLayout) getActivity().findViewById(R.id.barid);
		touchLayout = (RelativeLayout) getActivity().findViewById(R.id.touchlayout);
		go = (TextView)getActivity().findViewById(R.id.goTextView);
		near = (TextView)getActivity().findViewById(R.id.nearTextView);
		
		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mainmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
        
        map.setOnInfoWindowClickListener(new InfoWindowClickAdapter(getActivity(), comm));
        
        //center map on user
        if(gps.canGetLocation())
        {
        	gps.getLocation();
        	LatLng updateLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 15));
        }
        else
        {
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
        			37.3333, -121.9000), 12.0f));
        }


        
        
		myLocationButton = (ImageView) getActivity().findViewById(R.id.locmylocationbutton);
		myLocationButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{
				gps.getLocation();
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 15));				
			}
			
		});
		
		final RelativeLayout re = (RelativeLayout) rootView.findViewById(R.id.startupid);

		Button letsgo = (Button) rootView.findViewById(R.id.letsgoButton); 
	    letsgo.setOnClickListener(new OnClickListener()
	    {

			@Override
			public void onClick(final View v) 
			{
	            Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
	            slideDown.setAnimationListener(new AnimationListener()
	            {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						re.setVisibility(View.GONE);
						v.setVisibility(View.GONE);
						touchLayout.setVisibility(View.VISIBLE);
						myLocationButton.setVisibility(View.VISIBLE);						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
	            	
	            });
                re.startAnimation(slideDown);
	            

				
			}
	    	
	    });
	    
		
		
		touchLayout.setOnTouchListener(new OnTouchListener()
		{            
			private MotionEvent event;
			private View v;
			private RelativeLayout purplelayout = new RelativeLayout(getActivity());
			private RelativeLayout bluelayout = new RelativeLayout(getActivity());
			private boolean purple = false;
			private boolean blue = false;
			
			private Runnable r = new Runnable()
			{
			    public void run()
			    {
			    	((RelativeLayout)v).setBackgroundColor(0xAA000000);
			    	map.getUiSettings().setScrollGesturesEnabled(false);
			    	
					purplelayout.setBackgroundResource(R.drawable.touchgobackground);
					bluelayout.setBackgroundResource(R.drawable.touchnearbybackground);
					
					int oh = ((RelativeLayout)v).getHeight() / 2;
					int ow = ((RelativeLayout)v).getWidth() / 2;
					
					float purpleLayoutX = 0;
					float purpleLayoutY = 0;
					float blueLayoutX = 0;
					float blueLayoutY = 0;

					if(event.getY()*event.getYPrecision() >= oh)
					{
						//lower
						
						if(event.getX()*event.getXPrecision() > ow)
						{
							//right
							purpleLayoutX = event.getX()*event.getXPrecision() - dpToPx(30);
				            purpleLayoutY = event.getY()*event.getYPrecision() - dpToPx(175);

				            blueLayoutX = event.getX()*event.getXPrecision() - dpToPx(90);
				            blueLayoutY = event.getY()*event.getYPrecision() - dpToPx(130);
						}
						else
						{
							//left
							
							purpleLayoutX = event.getX()*event.getXPrecision() - dpToPx(30);
				            purpleLayoutY = event.getY()*event.getYPrecision() - dpToPx(175);

				            blueLayoutX = event.getX()*event.getXPrecision() + dpToPx(40);
				            blueLayoutY = event.getY()*event.getYPrecision() - dpToPx(130);
						}
					}
					else
					{
						//upper				
						
						if(event.getX()*event.getXPrecision() > ow)
						{
							//right
							
							//p
							purpleLayoutX = event.getX()*event.getXPrecision() - dpToPx(120);
				            purpleLayoutY = event.getY()*event.getYPrecision() - dpToPx(5);

				            //b
				            blueLayoutX = event.getX()*event.getXPrecision() - dpToPx(20);
				            blueLayoutY = event.getY()*event.getYPrecision() + dpToPx(30);

						}
						else
						{
							//left
							
							//p
							purpleLayoutX = event.getX()*event.getXPrecision() + dpToPx(65);
				            purpleLayoutY = event.getY()*event.getYPrecision() - dpToPx(5);

				            //b
				            blueLayoutX = event.getX()*event.getXPrecision() - dpToPx(20);
				            blueLayoutY = event.getY()*event.getYPrecision() + dpToPx(30);

						}
					}
					
		            
		            RelativeLayout.LayoutParams purpleLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            purpleLayoutParams.leftMargin = (int) purpleLayoutX;
		            purpleLayoutParams.topMargin = (int) purpleLayoutY;
		            purpleLayoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
		            purpleLayoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		            purplelayout.setLayoutParams(purpleLayoutParams);
		            
		            RelativeLayout.LayoutParams blueLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		            blueLayoutParams.leftMargin = (int) blueLayoutX;
		            blueLayoutParams.topMargin = (int) blueLayoutY;
		            blueLayoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
		            blueLayoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		            bluelayout.setLayoutParams(blueLayoutParams);
		            		            		            
		            ((RelativeLayout)v).addView(purplelayout);
		            ((RelativeLayout)v).addView(bluelayout);
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
					purple = false;
					blue = false;
					
					
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
					
					if(isPointInsideView(event.getRawX(), event.getRawY(), purplelayout))
					{
						if(purple)
							return false;
						purplelayout.setBackgroundResource(R.drawable.touchgobackgroundsel);
						
						go.setVisibility(View.VISIBLE);
						
						purple = true;
						blue = false;
						return false;
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), bluelayout))
					{
						if(blue)
							return false;
						
						bluelayout.setBackgroundResource(R.drawable.touchnearbybackgroundsel);
						near.setVisibility(View.VISIBLE);
						
						purple = false;
						blue = true;
			            return false;
					}
					blue = false;
					purple = false;
					go.setVisibility(View.GONE);
					near.setVisibility(View.GONE);
					
					purplelayout.setBackgroundResource(R.drawable.touchgobackground);
					bluelayout.setBackgroundResource(R.drawable.touchnearbybackground);

					return false;
			    }
				
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
		            ((RelativeLayout)v).setBackgroundResource(0);

					if(isPointInsideView(event.getRawX(), event.getRawY(), purplelayout))
					{
						//Convert touch location to LatLng
						
						LatLng point = map.getProjection().fromScreenLocation(new Point((int)mDownX, (int)mDownY));
						go.setVisibility(View.GONE);

						GetMarkerFromTouch goTo = new GetMarkerFromTouch(getActivity(), point);
						goTo.execute();
						comm.returnRoutes(point);
					}
					else if(isPointInsideView(event.getRawX(), event.getRawY(), bluelayout))
					{
						int[] location = new int[2];
						v.getLocationOnScreen(location);
						Point p = new Point();
						p.x = location[0];
						p.y = location[1];

						near.setVisibility(View.GONE);
						nearbyPopUp(getActivity(), p);
					}
					
					((RelativeLayout)v).removeView(purplelayout);
					((RelativeLayout)v).removeView(bluelayout);
					
			    	map.getUiSettings().setScrollGesturesEnabled(true);
					runnableHandler.removeCallbacks(r);
					
					return true;
				}
				return false;
			}
		});
		
		
		
		
		map.setOnMapClickListener(new OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng point) 
			{
				Projection projection = map.getProjection();
				
				//Convert Points to on screen location
				Point p1 = new Point();
				p1 = projection.toScreenLocation(point);
				
				if(isPointInsideView(p1.x, p1.y, hiddenSearchBar))
				{}
				else
				{
					hideKeyBoard();
				}
			}	
		});

		
	    searchProgress = (ProgressBar) getActivity().findViewById(R.id.locationSearchProgressBar);

		
    	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		
		searchBar = (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
		searchBar.setDropDownBackgroundResource(R.drawable.white_search_button);
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
		searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
			{
				if(actionId == EditorInfo.IME_ACTION_SEARCH)
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
	                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
	                }
	                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
	                {
	                	geoTask.setSearchString(searchString);
	                	geoTask.execute();
	                }  
					return true;
				}
				return false;
			}
		});
		
		searchBar.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				searchString = (String) ((TextView)view).getText();
				hideKeyBoard();
				//AsyncTask can only execute ONCE
                //Status.FINISHED, create a new instance : this sets mStatus to PENDING
                //Status.PENDING, execute.
                if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
                {
                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
                }
                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
                {
                	geoTask.setSearchString(searchString);
                	geoTask.execute();
                }  
			}
			
		});
		
		
		clearSearchBarButton = (ImageButton) getActivity().findViewById(R.id.clearSearchBarButton);
		clearSearchBarButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				map.clear();
				
				if(geoTask != null)
					geoTask.cancel(true);
				
				hideKeyBoard();
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
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("asian food nearby");
		                	geoTask.execute();
		                }
		                map.clear();
						changeStatusPopUp.dismiss();
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeFastFood)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("fast food nearby");
		                	geoTask.execute();
		                }
		                map.clear();
						changeStatusPopUp.dismiss();
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.foodTypeThai)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("thai food nearby");
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
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("clothes shopping nearby");
		                	geoTask.execute();
		                }
		                map.clear();
		                changeStatusPopUp.dismiss();
					}
					if(isPointInsideView(event.getRawX(), event.getRawY(), layout.findViewById(R.id.shoppingTypeShoes)))
					{
						if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
		                {
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("shoes shopping nearby");
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
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("cinema nearby");
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
		                	geoTask = new AddressSearchAsyncTask(getActivity(), map, searchProgress);
		                }
		                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
		                {
		                	geoTask.setSearchString("family fun nearby");
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
	    
	    int h = getStatusBarHeight();
	    
	    //Creating the PopupWindow
	    changeStatusPopUp = new PopupWindow(context);
	    changeStatusPopUp.setContentView(layout);
	    changeStatusPopUp.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
	    changeStatusPopUp.setHeight(touchLayout.getHeight());
	    changeStatusPopUp.setFocusable(true);
	
	    //Clear the default translucent background
	    changeStatusPopUp.setBackgroundDrawable(new ColorDrawable());
	    
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
		markerOptions.title("Go here!");
		markerOptions.snippet(address.getAddressLine(0));
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_ic_action_place));
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
	public boolean dismissNearby()
	{
		if(changeStatusPopUp == null)
			return false;
		
		changeStatusPopUp.dismiss();
		return changeStatusPopUp.isShowing();
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


