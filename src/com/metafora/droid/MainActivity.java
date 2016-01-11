package com.metafora.droid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;
import com.metafora.droid.Metafora.TrackerName;

public class MainActivity extends FragmentActivity implements FragmentCommunicator
{	  

	
	private static NoSwipeViewPager viewPager;
	private boolean showHelp;
	SharedPreferences sf;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		//Ensure keyboard is not showing on startup
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		//getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color));
		Tracker t = ((Metafora) getApplication()).getTracker(TrackerName.APP_TRACKER);
		t.setScreenName("Home");
		t.send(new HitBuilders.AppViewBuilder().build());
		
		
		//Create no swipe view pager (see NoSwipeViewPager.java)
		viewPager = (NoSwipeViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
		
		viewPager.setOffscreenPageLimit(3);
		viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);	
		
		sf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		showHelp = sf.getBoolean("showHelp", true);
		

		if(showHelp)
		{
			//show popup dialog
		}
	}
	

	@Override
	protected void onStart() 
	{
		super.onStart();
		GoogleAnalytics.getInstance(MainActivity.this).reportActivityStart(this);
	}


	@Override
	protected void onStop() 
	{
		super.onStop();
		GoogleAnalytics.getInstance(MainActivity.this).reportActivityStop(this);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle s)
	{
		super.onRestoreInstanceState(s);
	
		((MyAdapter) viewPager.getAdapter()).locationsearchFrag = (LocationSearchFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:"+R.id.pager+":0");
		((MyAdapter) viewPager.getAdapter()).routeFrag = (RouteSelectionFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:"+R.id.pager+":1");
		((MyAdapter) viewPager.getAdapter()).placeFrag = (PlaceFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:"+R.id.pager+":2");
		
		viewPager.setCurrentItem(0, false);
	}
	@Override
	protected void onSaveInstanceState(Bundle s)
	{
		super.onSaveInstanceState(s);	

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{  
		Intent browserIntent;
        switch (item.getItemId()) 
        {    
            case R.id.menu_about:  
            	browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://metafora.co/about"));
        		startActivity(browserIntent);
            	return true;     
            case R.id.menu_website:
            	 browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://metafora.co/"));
        		startActivity(browserIntent);
            	return true;     
            default:  
                return super.onOptionsItemSelected(item);  
        }  
    }  
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	
	/**
	 * Hardware back-button handles
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	    	if(viewPager.getCurrentItem() == 0)
	    	{
	    		return super.onKeyDown(keyCode, event);
	    	}
	    	if(viewPager.getCurrentItem() == 1)
	    	{
	    		LocationSearchFragment lFrag = ((MyAdapter) viewPager.getAdapter()).locationsearchFrag;
	    		if(!lFrag.dismissNearby())
	    		{
		    		return super.onKeyDown(keyCode, event);
	    		}
	    	}
	    	//route selection fragment, goes to placedetails
	    	else if(viewPager.getCurrentItem() == 1)
	    	{
	    		viewPager.setCurrentItem(0, false);
	    	}
	    	else if(viewPager.getCurrentItem() == 2)
	    	{
	    		viewPager.setCurrentItem(0, false);
	    	}
	      return true;
	    } 
	    else 
	    {
	       return super.onKeyDown(keyCode, event);
	    }
	}


	//Communicator methods
	
	@Override
	public void chooseRoute()
	{
		
	};
	
	@Override
	public void cancelTimers()
	{
		RouteSelectionFragment dFrag = ((MyAdapter) viewPager.getAdapter()).routeFrag;
		dFrag.cancelTimers();
	}
	
	@Override
	public void goToLocationSearch()
	{
		viewPager.setCurrentItem(0, false);
	}
	
	@Override
	public void returnRoutes(LatLng destination)
	{
		RouteSelectionFragment rtFrag = ((MyAdapter) viewPager.getAdapter()).routeFrag;
		
		rtFrag.updateFragment(destination);
		
		viewPager.setCurrentItem(1, false);

	}
	@Override
	public void gotoTextDirections()
	{
		viewPager.setCurrentItem(2, false);
	}
	@Override
	public void getPlaceDetails(LatLng location, String place, String address)
	{	
		PlaceFragment placeFragment = ((MyAdapter) viewPager.getAdapter()).getPlaceFragment();
		placeFragment.initialize(location, place, address);
		viewPager.setCurrentItem(2, false);

	}
	
	@Override
	public void goToStepLocation(LatLng location)
	{
		RouteSelectionFragment rtFrag = ((MyAdapter) viewPager.getAdapter()).getRouteFragment();
		rtFrag.goToLocation(location);
		this.gotoRouteSelection();
	}
	
	@Override 
	public void gotoRouteSelection()
	{
		viewPager.setCurrentItem(1, false);
	}

	@Override
	public void updateDirectionsList(String JSON, int routeNumber) 
	{
		RouteSelectionFragment directionsFragment = ((MyAdapter) viewPager.getAdapter()).routeFrag;
		directionsFragment.updateDirectionsList(JSON, routeNumber);
	}

	@Override
	public void goToPlaceDetails() 
	{
		viewPager.setCurrentItem(2, false);
	}
}

//pager adapter class
class MyAdapter extends FragmentPagerAdapter
{
    private final FragmentManager mFragmentManager;
    
	public MyAdapter(FragmentManager fm) 
	{
		super(fm);
		this.mFragmentManager = fm;
	}
	
	//slick as fuuuuuuuuu
	//create and maintain reference to each fragment dynamically
	public LocationSearchFragment locationsearchFrag;
	public RouteSelectionFragment routeFrag;
	public PlaceFragment placeFrag;
	
    public LocationSearchFragment getMainFragment() 
    {
        return locationsearchFrag;
    }
	public RouteSelectionFragment getRouteFragment()
	{
		return routeFrag;
	}
	public PlaceFragment getPlaceFragment()
	{
		return placeFrag;
	}

    
	@Override
	public Fragment getItem(int arg0) 
	{
		Fragment fragment = null;
		
		if(arg0 == 0)
		{
			fragment = new LocationSearchFragment();
			locationsearchFrag = (LocationSearchFragment) fragment;
		}
		if(arg0 == 1)
		{
			fragment = new RouteSelectionFragment();
			routeFrag = (RouteSelectionFragment) fragment;
		}
		if(arg0 == 2)
		{
			fragment = new PlaceFragment();
			placeFrag = (PlaceFragment) fragment;
		}
		
		return fragment;
	}
	

	@Override
	public int getCount() 
	{
		return 3;
	}
}