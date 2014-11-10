package johankrig.hotmail.com;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import johankrig.hotmail.com.R;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;


public class MainFragment extends Fragment
{
	private AutoCompleteTextView searchBar;
	private GoogleMap map;
	private View rootView;
	private Button addressSearchButton;
	private Communicator comm;
	private String searchString = "";
	private AddressSearchAsyncTask geoTask;
	private GPSTracker gps;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
		if (rootView!= null) 
		{
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try 
	    {
	        rootView= inflater.inflate(R.layout.fragment_main, container, false);
	    } 
	    catch (InflateException e) 
	    {
	        /* map is already there, just return view as it is */
	    }
        
		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mainmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
		return rootView;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		comm = (Communicator) getActivity();
		
        map.setOnInfoWindowClickListener(new InfoWindowClickAdapter(getActivity(), comm));

		//center map on user
		gps = new GPSTracker(getActivity());
		if(gps.canGetLocation())
		{
			LatLng updateLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
		}
		
    	geoTask = new AddressSearchAsyncTask(getActivity(), map);
		
		searchBar = (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
		searchBar.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line));
		
		addressSearchButton = (Button) getActivity().findViewById(R.id.routeMenuButton);
		addressSearchButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
        		searchString = searchBar.getEditableText().toString();
                
                //AsyncTask can only execute ONCE
                //Status.FINISHED, create a new instance : this sets mStatus to PENDING
                //Status.PENDING, execute.
                if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
                {
                	geoTask = new AddressSearchAsyncTask(getActivity(), map);
                }
                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
                {
                	geoTask.setLocation(searchString);
                	geoTask.execute();
                }
                else{}
                
            }
        });
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
		Toast.makeText(context, 
				marker.getSnippet() + marker.getId(), 
				Toast.LENGTH_SHORT).show();
		comm.returnRoutes(marker.getPosition());
	}
	
}