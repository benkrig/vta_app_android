package johankrig.hotmail.com;

import com.google.android.gms.maps.model.LatLng;

public interface Communicator 
{
	public void goToLocationSearch();
	
	public void returnRoutes(LatLng destination);
	
	public void updateDirectionsList(String JSON, int routeNumber);

	public void gotoTextDirections();
	
	public void getPlaceDetails(LatLng location, String place);

	public void startupSlide();

	public void gotoRouteSelection();
	
}
