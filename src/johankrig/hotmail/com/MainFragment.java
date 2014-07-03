package johankrig.hotmail.com;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import johankrig.hotmail.com.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class MainFragment extends Fragment
{
	EditText s;
	public GoogleMap map;
	View rootView;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
		s = (EditText) getActivity().findViewById(R.id.searchBar);
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mainmap)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
        
        return rootView;
    }

    public String g_Destination()
    {
    	return s.getText().toString();
    }
	
	

}
