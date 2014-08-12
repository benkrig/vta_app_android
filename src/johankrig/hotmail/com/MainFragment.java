package johankrig.hotmail.com;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import johankrig.hotmail.com.R;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
	public Communicator comm;
	private String destination = "";
	private Bundle data = new Bundle();
	GeocoderAsyncTask geoTask;
	GPSTracker gps;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
		rootView = inflater.inflate(R.layout.fragment_main, container, false);
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
			@SuppressWarnings("unused")
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(updateLatLng, 10);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLatLng, 10));
		}
		
		
    	geoTask = new GeocoderAsyncTask(getActivity(), map);

		
		searchBar = (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
		searchBar.setAdapter(new AutoCompleteAdapter(getActivity(), map));
		
		addressSearchButton = (Button) getActivity().findViewById(R.id.routeMenuButton);
		addressSearchButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
        		destination = searchBar.getEditableText().toString();

        		Bundle newData = new Bundle();
        		
        		newData.putString("destination", destination);
                newData.putInt("fragment", 1);
                if(gps.canGetLocation() == true)
                {
                	newData.putDouble("latitude", gps.getLatitude());
                	newData.putDouble("longitude", gps.getLongitude());
                }
                setData(newData);
                
                //AsyncTask can only execute ONCE
                //Status.FINISHED, create a new instance : this sets mStatus to PENDING
                //Status.PENDING, execute.
                if(geoTask.getStatus() == AsyncTask.Status.FINISHED)
                {
                	//geoTask = new FindLocationsAsync(getActivity(), map);
                	geoTask = new GeocoderAsyncTask(getActivity(), map);
                }
                if(geoTask.getStatus() == AsyncTask.Status.PENDING)
                {
                	geoTask.execute(destination);
                }
                
            }
        });
	}
	public void setData(Bundle newData)
	{
		data = newData;
	}
	public Bundle getData()
	{
		return data;
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
		//put data in bundle
		
		//send bundle through interface to RouteFragment and get dem routes boiiii
		comm.returnRoutes(marker.getPosition());
	}
	
}

/*
Timer simple TimerTask Java Android example
Posted on September 26, 2011 by admin	

TimerTask with updating of TextView here

 
package cz.okhelp.timer;
 
import java.util.Timer;
import java.util.TimerTask;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
 
public class TimerActivity extends Activity {
TextView hTextView;
@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        hTextView = (TextView)findViewById(R.id.idTextView);
        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();
//        public void schedule (TimerTask task, long delay, long period) 
//        Schedule a task for repeated fixed-delay execution after a specific delay.
//
//        Parameters
//        task  the task to schedule. 
//        delay  amount of time in milliseconds before first execution. 
//        period  amount of time in milliseconds between subsequent executions. 
 
        myTimer.schedule(myTask, 3000, 1500);        
 
    }
class MyTimerTask extends TimerTask {
	  public void run() {
		  // ERROR
		 hTextView.setText("Impossible");
		 // how update TextView in link below  
                 // http://android.okhelp.cz/timer-task-timertask-run-cancel-android-example/
 
	    System.out.println("");
	  }
	}
 
 
}
*/