package johankrig.hotmail.com;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import johankrig.hotmail.com.R;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainFragment extends Fragment implements View.OnClickListener
{
	EditText searchBar;
	public GoogleMap map;
	View rootView;
	Button button;
	Communicator comm;
	String destination = "";
	
	
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
		
		searchBar = (EditText) getActivity().findViewById(R.id.searchBar);
		button = (Button) getActivity().findViewById(R.id.routeMenuButton);
		button.setOnClickListener(this);
		comm = (Communicator) getActivity();
	}
	@Override
	public void onClick(View view)
	{
		EditText searchBar =  (EditText) getActivity().findViewById(R.id.searchBar);
		destination = searchBar.getText().toString();
		
		Bundle bundle = new Bundle();
		bundle.putString("destination", destination);
        bundle.putInt("fragment", 1);
        bundle.putDouble("latitude", map.getMyLocation().getLatitude());
        bundle.putDouble("longitude", map.getMyLocation().getLongitude());
        
		GeocoderTask task = new GeocoderTask(getActivity(), map);
        task.execute(destination);
        
        comm.getRoutes(bundle);      
	}
}
//
class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
	
	Context context;
	LatLng latLng;
	MarkerOptions markerOptions;
	GoogleMap map;
	public GeocoderTask(Context c, GoogleMap m)
	{
		map = m;
		context = c;
	}

    @Override
    protected List<Address> doInBackground(String... locationName) {
        // Creating an instance of Geocoder class
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;

        try 
        {
            // Getting a maximum of 5 Address that matches the input text
            addresses = geocoder.getFromLocationName(locationName[0], 5);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) 
    {
        if(addresses==null || addresses.size()==0)
        {
            Toast.makeText(context, "No Location found", Toast.LENGTH_SHORT).show();
        }

        // Clears all the existing markers on the map
        map.clear();

        // Adding Markers on Google Map for each matching address
        for(int i=0;i<addresses.size();i++)
        {

            Address address = (Address) addresses.get(i);

            // Creating an instance of GeoPoint, to display in Google Map
            latLng = new LatLng(address.getLatitude(), address.getLongitude());

            String addressText = String.format("%s, %s",
            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
            address.getCountryName());

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(addressText);

            map.addMarker(markerOptions);

            // Locate the first location
            if(i==0)
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}