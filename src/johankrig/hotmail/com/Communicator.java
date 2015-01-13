package johankrig.hotmail.com;

import com.google.android.gms.maps.model.LatLng;

public interface Communicator 
{
	public void respond();
	
	public void returnRoutes(LatLng destination);
	
	public void updateDirectionsList(String JSON, int routeNumber);

	public void gotoTextDirections();

	public void gotoRouteSelection();
	
	public void getPlaceDetails(LatLng location, String place);
	
}
