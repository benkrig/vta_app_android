package johankrig.hotmail.com;

import java.io.IOException;
import java.util.ArrayList;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;


public class MainFragment extends Fragment implements View.OnClickListener
{
	AutoCompleteTextView searchBar;
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
		
		
		searchBar = (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
		searchBar.setAdapter(new AutoCompleteAdapter(getActivity(), map));
		
		button = (Button) getActivity().findViewById(R.id.routeMenuButton);
		button.setOnClickListener(this);
		comm = (Communicator) getActivity();
	}
	@Override
	public void onClick(View view)
	{
		AutoCompleteTextView searchBar =  (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
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
            // Getting a maximum of 5 Address that match the input text
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


class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable {
 
	private LayoutInflater mInflater;
	private Geocoder mGeocoder;
	private StringBuilder mSb = new StringBuilder();
	GoogleMap map;
	
	public AutoCompleteAdapter(final Context context, GoogleMap map) {
		super(context, -1);
		mInflater = LayoutInflater.from(context);
		mGeocoder = new Geocoder(context);
		this.map = map;
	}
 
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView tv;
		if (convertView != null) {
			tv = (TextView) convertView;
		} else {
			tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
		}
 
		tv.setText(createFormattedAddressFromAddress(getItem(position)));
		return tv;
	}
 
	private String createFormattedAddressFromAddress(final Address address) {
		mSb.setLength(0);
		final int addressLineSize = address.getMaxAddressLineIndex();
		for (int i = 0; i < addressLineSize; i++) {
			mSb.append(address.getAddressLine(i));
			if (i != addressLineSize - 1) {
				mSb.append(", ");
			}
		}
		return mSb.toString();
	}
 
	@Override
	public Filter getFilter() {
		Filter myFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				List<Address> addressList = null;
				if (constraint != null) {
					try {
						//changed this 7/20/2014
						//remove the map part if error
						addressList = mGeocoder.getFromLocationName((String) constraint, 4,
				                map.getMyLocation().getLatitude()-0.1,
				                map.getMyLocation().getLongitude()-0.1,
				                map.getMyLocation().getLatitude()+0.1,
				                map.getMyLocation().getLongitude()+0.1);
					} catch (IOException e) {
					}
				}
				if (addressList == null) {
					addressList = new ArrayList<Address>();
				}
 
				
				final FilterResults filterResults = new FilterResults();
				filterResults.values = addressList;
				filterResults.count = addressList.size();
 
				return filterResults;
			}
 
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence contraint, final FilterResults results) {
				clear();
				for (Address address : (List<Address>) results.values) {
					add(address);
				}
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
 
			@Override
			public CharSequence convertResultToString(final Object resultValue) {
				return resultValue == null ? "" : ((Address) resultValue).getAddressLine(0);
			}
		};
		return myFilter;
	}
}