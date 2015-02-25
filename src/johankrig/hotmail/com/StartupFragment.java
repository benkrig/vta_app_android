package johankrig.hotmail.com;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class StartupFragment extends Fragment 
{
	private View rootView;
	Button letsgo;
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

	    gps = new GPSTracker(getActivity());

	    if(!gps.canGetLocation)
	    {
	    	gps.showSettingsAlert();
	    }

	    
	    
	    comm = (Communicator) getActivity();
	    
	    letsgo = (Button) rootView.findViewById(R.id.letsgoButton); 
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
