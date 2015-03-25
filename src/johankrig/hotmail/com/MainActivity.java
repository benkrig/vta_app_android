package johankrig.hotmail.com;


import com.google.android.gms.maps.model.LatLng;
import johankrig.hotmail.com.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements Communicator
{	  
	public NoSwipeViewPager viewPager;
	public String destination;
	private PopupWindow changeStatusPopUp;
	private ImageButton button1;
	
	
	
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
	 	       	return super.onKeyDown(keyCode, event);
	    	}	
	    	//route selection fragment, goes to placedetails
	    	else if(viewPager.getCurrentItem() == 2)
	    	{
	    		viewPager.setCurrentItem(4, false);
	    	}
	    	else if(viewPager.getCurrentItem() == 3)
	    	{
	    		viewPager.setCurrentItem(2, false);
	    	}
	    	else if(viewPager.getCurrentItem() == 4)
	    	{
	    		viewPager.setCurrentItem(1, false);
	    	}
	      return true;
	    } 
	    else 
	    {
	       return super.onKeyDown(keyCode, event);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Ensure keyboard is not showing on startup
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		

		
		//Create no swipe view pager (see NoSwipeViewPager.java)
		viewPager = (NoSwipeViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
		viewPager.setOffscreenPageLimit(3);
		viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
	    super.onRestoreInstanceState(savedInstanceState);
		viewPager.setCurrentItem(1, true);
	}


	//Communicator methods
	@Override
	public void startupSlide()
	{
		viewPager.setCurrentItem(1, true);
	}
	
	@Override
	public void goToLocationSearch()
	{
		viewPager.setCurrentItem(1, false);
	}
	
	@Override
	public void returnRoutes(LatLng destination)
	{
		viewPager.setCurrentItem(2, false);
		RouteSelectionFragment rtFrag = ((MyAdapter) viewPager.getAdapter()).getRouteFragment();
		rtFrag.updateFragment(destination);
	}

	@Override
	public void gotoTextDirections()
	{
		viewPager.setCurrentItem(3, false);
	}
	@Override
	public void getPlaceDetails(LatLng location, String place, String address)
	{	
		PlaceFragment placeFragment = ((MyAdapter) viewPager.getAdapter()).getPlaceFragment();
		placeFragment.initialize(location, place, address);
		viewPager.setCurrentItem(4, false);

	}
	
	@Override 
	public void gotoRouteSelection()
	{
		viewPager.setCurrentItem(2, false);
	}

	@Override
	public void updateDirectionsList(String JSON, int routeNumber) 
	{
		DirectionsFragment directionsFragment = ((MyAdapter) viewPager.getAdapter()).getDirectionsFragment();
		directionsFragment.updateDirectionsList(JSON, routeNumber);
	}

	@Override
	public void goToPlaceDetails() 
	{
		viewPager.setCurrentItem(4, false);
	}
	
	
	
	   // The method that displays the popup.
	private void showStatusPopup(final Activity context, Point p) {

	   // Inflate the popup_layout.xml
	   LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.llSortChangePopup);
	   LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	   View layout = layoutInflater.inflate(R.layout.status_popup_layout, null);

	   // Creating the PopupWindow
	   changeStatusPopUp = new PopupWindow(context);
	   changeStatusPopUp.setContentView(layout);
	   changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
	   changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
	   changeStatusPopUp.setFocusable(true);

	   // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
	   int OFFSET_X = -20;
	   int OFFSET_Y = 50;

	   //Clear the default translucent background
	   changeStatusPopUp.setBackgroundDrawable(new BitmapDrawable());

	   // Displaying the popup at the specified location, + offsets.
	   changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
	}
	
	

}


//pager adapter class
class MyAdapter extends FragmentPagerAdapter
{
	public MyAdapter(FragmentManager fm) 
	{
		super(fm);
	}
	
	//slick as fuuuuuuuuu
	//create and maintain each fragment dynamically
	private LocationSearchFragment locationsearchFrag;
	private RouteSelectionFragment routeFrag;
	private DirectionsFragment directionsFrag;
	private PlaceFragment placeFrag;
	private StartupFragment startFrag;

	
	public StartupFragment getStartFragment()
	{
		return startFrag;
	}
    public LocationSearchFragment getMainFragment() 
    {
        return locationsearchFrag;
    }
	public RouteSelectionFragment getRouteFragment()
	{
		return routeFrag;
	}
	public DirectionsFragment getDirectionsFragment()
	{
		return directionsFrag;
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
			fragment = new StartupFragment();
			startFrag = (StartupFragment) fragment;
		}
		
		if(arg0 == 1)
		{
			fragment = new LocationSearchFragment();
			locationsearchFrag = (LocationSearchFragment) fragment;
		}
		if(arg0 == 2)
		{
			fragment = new RouteSelectionFragment();
			routeFrag = (RouteSelectionFragment) fragment;
		}
		if(arg0 == 3)
		{
			fragment = new DirectionsFragment();
			directionsFrag = (DirectionsFragment) fragment;
		}
		if(arg0 == 4)
		{
			fragment = new PlaceFragment();
			placeFrag = (PlaceFragment) fragment;
		}
		return fragment;
	}

	@Override
	public int getCount() 
	{
		return 5;
	}
}