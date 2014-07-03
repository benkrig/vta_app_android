package johankrig.hotmail.com;

import com.google.android.gms.maps.GoogleMap;
import android.widget.Button;


//this class will make calls out to the GoogleDirectionsAPI.java class


//parameters: GoogleMap, end latitude, end longitude, route number to draw

    public class DrawDirections
    {
    	
    	private GoogleMap map = null;
    	private double elat = 0;
    	private double elon = 0;
    	private int routeNum = 0;
    	Button btn = null;
    	GoogleDirectionsAPI directions = new GoogleDirectionsAPI();

    	public DrawDirections(GoogleMap newmap, double ela, double elo, int route, Button btn1)
    	{
    		btn = btn1;
    		map = newmap;
    		elat = ela;
    		elon = elo;
    		routeNum = route;
    	}
   
    	public void drawRoute()
    	{
            try 
            {
            	directions.getDirections(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude(), elat, elon);
            } 
            catch (Exception e) 
			{
            	btn.setText(e.getMessage());

				e.printStackTrace();
			}
            try 
        	{
				directions.drawPath(routeNum, map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude(), elat, elon, map);
			} 
        	catch (Exception e) 
        	{
				e.printStackTrace();
			}
    	}
        

    }
