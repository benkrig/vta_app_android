package johankrig.hotmail.com;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StartupFragment extends Fragment 
{
	private View rootView;
	Button letsgo;
	TextView loadingText;
	ProgressBar loadingBar;
	Communicator comm;
	GPSTracker gps;
	

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
	        rootView = inflater.inflate(R.layout.startuppage, container, false);
	    } 
	    catch (InflateException e) 
	    {
	        /* map is already there, just return view as it is */
	    }
	    loadingText = (TextView) rootView.findViewById(R.id.startScreenLoadingText);
	    loadingBar = (ProgressBar) rootView.findViewById(R.id.startScreenProgressBar);
	    letsgo = (Button) rootView.findViewById(R.id.letsgoButton); 

	    gps = new GPSTracker(getActivity());

	    if(!gps.canGetLocation)
	    {
	    	gps.showSettingsAlert();
	    }
	    else
    	{
    		loadingText.setVisibility(View.GONE);
    		loadingBar.setVisibility(View.GONE);
    		letsgo.setVisibility(View.VISIBLE);
    	}
	    
	    
	    comm = (Communicator) getActivity();
	    
	    
	    letsgo.setOnClickListener(new OnClickListener()
	    {

			@Override
			public void onClick(View v) 
			{
				comm.startupSlide();
				rootView.destroyDrawingCache();
			}
	    	
	    });


        
	    
	    
	    
        return rootView;
	}
}
