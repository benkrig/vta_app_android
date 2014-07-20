package johankrig.hotmail.com;

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
	public MainFragment mFrag;
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

	//onClick method for mainMapButton found in fragment_route.xml
	public void mainMapButton(View view)
	{
		viewPager.setCurrentItem(0, true);
	}
	@Override
	public void getRoutes(Bundle bundle)
	{
		viewPager.setCurrentItem(1, true);
		RouteFragment rtFrag = ((MyAdapter) viewPager.getAdapter()).getRouteFragment();
		rtFrag.update(bundle.getString("destination"), bundle.getDouble("latitude"), bundle.getDouble("longitude"));
	}
	@Override
	public void respond()
	{
		viewPager.setCurrentItem(0, true);
	}
}

class MyAdapter extends FragmentPagerAdapter
{
	public MyAdapter(FragmentManager fm) 
	{
		super(fm);
	}
	
	//slick as fuuuuuuuuu
	private MainFragment mainFrag;
	private RouteFragment routeFrag;

    public MainFragment getMainFragment() 
    {
        return mainFrag;
    }
	public RouteFragment getRouteFragment()
	{
		return routeFrag;
	}
    
	@Override
	public Fragment getItem(int arg0) 
	{
		Fragment fragment = null;
		if(arg0 == 0)
		{
			fragment = new MainFragment();
			mainFrag = (MainFragment) fragment;
		}
		
		if(arg0 == 1)
		{
			fragment = new RouteFragment();
			routeFrag = (RouteFragment) fragment;
		}
		return fragment;
	}

	@Override
	public int getCount() 
	{
		return 2;
	}
}