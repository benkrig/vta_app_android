package johankrig.hotmail.com;


import com.google.android.gms.maps.model.LatLng;

import johankrig.hotmail.com.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.View;
import android.support.v4.view.ViewPager;


public class MainActivity extends FragmentActivity implements Communicator
{	  
	public ViewPager viewPager;
	public String destination;
	public Bundle bundle = new Bundle();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	@Override
	public void respond()
	{
		viewPager.setCurrentItem(0);
	}
	
	@Override
	public void returnRoutes(LatLng destination)
	{
		viewPager.setCurrentItem(1);
		RouteSelectionFragment rtFrag = ((MyAdapter) viewPager.getAdapter()).getRouteFragment();
		rtFrag.updateFragment(destination);
	}

	@Override
	public void updateDirectionsList(String JSON, int routeNumber) 
	{
		DirectionsFragment directionsFragment = ((MyAdapter) viewPager.getAdapter()).getDirectionsFragment();
		directionsFragment.updateDirectionsList(JSON, routeNumber);
	}
	@Override
	public void gotoTextDirections()
	{
		viewPager.setCurrentItem(2);
	}
	@Override
	public void gotoRouteSelection()
	{
		viewPager.setCurrentItem(1);
	}
	@Override
	public void getPlaceDetails(LatLng location, String place)
	{
		viewPager.setCurrentItem(3);
		PlaceFragment placeFragment = ((MyAdapter) viewPager.getAdapter()).getPlaceFragment();
		placeFragment.initialize(location, place);
	}

}

class MyAdapter extends FragmentPagerAdapter
{
	public MyAdapter(FragmentManager fm) 
	{
		super(fm);
	}
	
	//slick as fuuuuuuuuu
	private LocationSearchFragment mainFrag;
	private RouteSelectionFragment routeFrag;
	private DirectionsFragment directionsFrag;
	private PlaceFragment placeFrag;

    public LocationSearchFragment getMainFragment() 
    {
        return mainFrag;
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
			fragment = new LocationSearchFragment();
			mainFrag = (LocationSearchFragment) fragment;
		}
		
		if(arg0 == 1)
		{
			fragment = new RouteSelectionFragment();
			routeFrag = (RouteSelectionFragment) fragment;
		}
		if(arg0 == 2)
		{
			fragment = new DirectionsFragment();
			directionsFrag = (DirectionsFragment) fragment;
		}
		if(arg0 == 3)
		{
			fragment = new PlaceFragment();
			placeFrag = (PlaceFragment) fragment;
		}
		return fragment;
	}

	@Override
	public int getCount() 
	{
		return 4;
	}
}