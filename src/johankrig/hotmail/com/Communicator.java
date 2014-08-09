package johankrig.hotmail.com;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;

public interface Communicator {
	public void respond();

	public void getRoutes(Bundle bundle);
	
	public void returnRoutes(LatLng destination);
}
