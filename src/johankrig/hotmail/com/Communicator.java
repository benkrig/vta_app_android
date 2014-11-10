package johankrig.hotmail.com;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;

public interface Communicator {
	public void respond();
	
	public void returnRoutes(LatLng destination);
	
	public void updateDirectionsList(String JSON);
	
}
