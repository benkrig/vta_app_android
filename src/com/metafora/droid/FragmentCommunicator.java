package com.metafora.droid;

import com.google.android.gms.maps.model.LatLng;

public interface FragmentCommunicator 
{
	public void goToLocationSearch();
	
	public void returnRoutes(LatLng destination);
	
	public void updateDirectionsList(String JSON, int routeNumber);

	public void gotoTextDirections();

	public void getPlaceDetails(LatLng location, String place, String address);

	public void gotoRouteSelection();

	public void goToPlaceDetails();

	public void goToStepLocation(LatLng latLng);

	public void cancelTimers();

}