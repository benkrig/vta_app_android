package johankrig.hotmail.com;

import johankrig.hotmail.com.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.support.v4.view.ViewPager;


public class MainActivity extends FragmentActivity
{
	public ViewPager viewPager;
	public String destination;
	public Bundle bundle = new Bundle();
	boolean bool;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
		bool = false;
		destination = "search";
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public Bundle getBundle()
	
	{
		return bundle;
	}
	
	public void mainMapButton(View view)
	{
		viewPager.setCurrentItem(0, true);
	}
	
	public void routeButton(View view)
	{
		Fragment m_frag = ((MyAdapter) viewPager.getAdapter()).getCurrentFragment();
		destination = ((MainFragment) m_frag).g_Destination();
		
		EditText searchBar = (EditText) findViewById(R.id.searchBar);
		destination = searchBar.getText().toString();
		bundle.putString("destination", destination);
        viewPager.setCurrentItem(1, true);
	}

}

class MyAdapter extends FragmentPagerAdapter
{
	public MyAdapter(FragmentManager fm) 
	{
		super(fm);
	}
	
	//slick as fuuuuuuuuu
	private Fragment mCurrentFragment;

    public Fragment getCurrentFragment() 
    {
        return mCurrentFragment;
    }

	@Override
	public Fragment getItem(int arg0) 
	{
		Fragment fragment = null;
		if(arg0 == 0)
		{
			fragment = new MainFragment();
			mCurrentFragment = fragment;
		}
		
		if(arg0 == 1)
		{
			fragment = new RouteFragment();
			mCurrentFragment = fragment;
		}
		return fragment;
	}

	@Override
	public int getCount() 
	{
		return 2;
	}
}