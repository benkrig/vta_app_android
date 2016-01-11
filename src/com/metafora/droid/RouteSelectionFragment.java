//RouteFragment.java - Benjamin Krig
//This screen will be used to choose and view information about specific routes
//Fragment locates destination and updates routes upon change of destination.

//TO DO: 


package com.metafora.droid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class RouteSelectionFragment extends Fragment
{
	private LatLng destinationLatLng = new LatLng(0, 0);
	private LatLng userLatLng = new LatLng(0,0);


	private GoogleMap map;

	int[] secondCount = new int[3];
	//Global variables


	Timer myTimer = new Timer();
	GetBusLocationTask myTask = null;
	private ImageButton selectRouteButton = null;
	DisplayMetrics displayMetrics;


	private int MapsPolylineWidth;

	//Interface
	FragmentCommunicator comm;

	//Private variables
	private Button route1Button;
	private Button route2Button;
	private Button route3Button;
	private ImageButton expandTextDirectionsButton;
	private ImageButton fragmentBackButton;
	private DirectionsAsyncTask drawRoute;
	private DrawRouteAsync draw;
	private View rootView;
	String googleDirectionsResultJSON;
	private ImageView myLocationButton;
	private GPSTracker gps;
	private RelativeLayout bottomBar;
	private ImageButton goRouteButton;

	SlidingUpPanelLayout slidingPanel;
	ImageButton directionsBackButton;
	public ListView mainListView;  
	TextDirectionsMobileAdapter directionsAdapter;
	public String DirectionsJSON;
	View headerView;
	View footerView;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		footerView = inflater.inflate(R.layout.directions_footer_row, null, false);
		headerView = inflater.inflate(R.layout.directions_header_row, null, false);
		if (rootView!= null) 
		{
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (parent != null)
				parent.removeView(rootView);
		}
		try 
		{
			rootView = inflater.inflate(R.layout.fragment_route_selection, container, false);
		} 
		catch (InflateException e) 
		{
			/* map is already there, just return view as it is */
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mainListView = (ListView) rootView.findViewById(R.id.textDirectionsListView);  
		slidingPanel = (com.sothree.slidinguppanel.SlidingUpPanelLayout)getActivity().findViewById(R.id.sliding_layout);
		slidingPanel.setDragView(getActivity().findViewById(R.id.routeBottomBar));

		gps = new GPSTracker(getActivity());
		gps.getLocation();

		bottomBar = (RelativeLayout) getActivity().findViewById(R.id.testid);

		comm = (FragmentCommunicator) getActivity();

		displayMetrics = getActivity().getResources().getDisplayMetrics();
		MapsPolylineWidth = Math.round(7 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       

		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.routeselectionmap)).getMap();
		map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
		
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 15));

		route1Button = (Button) getActivity().findViewById(R.id.routebutton1);
		route2Button = (Button) getActivity().findViewById(R.id.routebutton2);
		route3Button = (Button) getActivity().findViewById(R.id.routebutton3);

		expandTextDirectionsButton = (ImageButton) getActivity().findViewById(R.id.textDirectionsButton);
		expandTextDirectionsButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) 
			{

				Log.d("", slidingPanel.getPanelState().toString());
				//default is anchored, so test if it's not expanded instead
				if(slidingPanel.getPanelState() == PanelState.HIDDEN || slidingPanel.getPanelState() == PanelState.COLLAPSED)
				{
					slidingPanel.setPanelState(PanelState.EXPANDED);
				}
				else
				{
					slidingPanel.setPanelState(PanelState.COLLAPSED);
				}
			}
		});

		selectRouteButton = (ImageButton) getActivity().findViewById(R.id.routeProgressButton);

		selectRouteButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				comm.chooseRoute();
			}
		});



		class mytimepicker extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener, android.content.DialogInterface.OnClickListener 
		{
			Button time = null;

			public mytimepicker(Button time)
			{
				this.time = time;
			}
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) 
			{
				final Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int minute = c.get(Calendar.MINUTE);

				TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute,
						DateFormat.is24HourFormat(getActivity()));

				dialog.setTitle("Departure Time");
				dialog.setCancelable(true);
				dialog.setCanceledOnTouchOutside(true);

				return dialog;
			}

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
			{
				if(view.isShown())
				{
					String am_pm = "";

					Calendar datetime = Calendar.getInstance();
					datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
					datetime.set(Calendar.MINUTE, minute);

					if (datetime.get(Calendar.AM_PM) == Calendar.AM)
						am_pm = "AM";
					else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
						am_pm = "PM";

					String strTimeToShow = String.format(Locale.US, "%02d:%02d", (datetime.get(Calendar.HOUR) == 0) ? 12 : datetime.get(Calendar.HOUR), datetime.get(Calendar.MINUTE));;
					time.setText(strTimeToShow + " "+ am_pm);


					map.clear();
					GPSTracker gps = new GPSTracker(getActivity());
					userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());

					map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));

					if(userLatLng != null && destinationLatLng != null)
					{
						if(drawRoute != null)
						{
							drawRoute.cancel(true);
						}

						route1Button.setBackgroundColor(getResources().getColor(R.color.highlight_pink_medium));
						route2Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
						route3Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
						slidingPanel.setPanelState(PanelState.COLLAPSED);

						drawRoute = new DirectionsAsyncTask();
						drawRoute.updateUserLocation(userLatLng, destinationLatLng);
						drawRoute.setTime(datetime.getTimeInMillis()/1000);
						drawRoute.setRouteNumber(0);
						drawRoute.execute();

						Timer myTimer = new Timer();
						if(myTask != null)
						{
							myTask.cancel();
						}
						myTask = new GetBusLocationTask(map, getActivity());
						myTimer.schedule(myTask, 0, 2000);
					}
				}		    	
			}

			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(which == DialogInterface.BUTTON_NEGATIVE)
				{
					dialog.cancel();
					dialog.dismiss();
				}
				if(which == DialogInterface.BUTTON_POSITIVE)
				{
				}

			}
		}

		final Button routeTimeButton = (Button) getActivity().findViewById(R.id.selectRouteDepartTime);
		Calendar datetime = Calendar.getInstance();
		datetime.setTimeInMillis(System.currentTimeMillis());

		String am_pm = null;
		if (datetime.get(Calendar.AM_PM) == Calendar.AM)
			am_pm = "AM";
		else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
			am_pm = "PM";

		final String strTimeToShow = String.format(Locale.US, "%02d:%02d", (datetime.get(Calendar.HOUR) == 0) ? 12 : datetime.get(Calendar.HOUR), datetime.get(Calendar.MINUTE));;
		routeTimeButton.setText(strTimeToShow + " "+ am_pm);

		routeTimeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				DialogFragment newFragment = new mytimepicker(routeTimeButton);
				newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
			}
		});


		//OnClick ROUTEBUTTON1
		//Main route directions will be drawn onto map
		route1Button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{

				int buttonColor = ((ColorDrawable) route1Button.getBackground()).getColor();
				if(buttonColor == getResources().getColor(R.color.greytransparent))
				{
					return;          	
				}
				route1Button.setBackgroundColor(getResources().getColor(R.color.highlight_pink_medium));
				route2Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				route3Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				slidingPanel.setPanelState(PanelState.COLLAPSED);
				
				if(googleDirectionsResultJSON != null)
				{
					if(draw != null)
					{
						draw.cancel(true);
					}
					draw = new DrawRouteAsync(bottomBar, MapsPolylineWidth, selectRouteButton, comm, map, googleDirectionsResultJSON, 0);
					draw.execute();
					if(myTask != null)
					{
						myTask.cancel();
					}

					myTask = new GetBusLocationTask(map, getActivity());
					myTimer.schedule(myTask, 0, 2000);
				}
			}
		});

		//OnClick ROUTEBUTTON2
		//Alternate route 1 directions will be drawn onto map
		route2Button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				int buttonColor = ((ColorDrawable) route2Button.getBackground()).getColor();
				if(buttonColor == getResources().getColor(R.color.greytransparent))
				{
					return;          	
				}
				route2Button.setBackgroundColor(getResources().getColor(R.color.highlight_pink_medium));
				route1Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				route3Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				slidingPanel.setPanelState(PanelState.COLLAPSED);
				
				if(googleDirectionsResultJSON != null)
				{		            
					if(draw != null)
					{
						draw.cancel(true);
					}

					DrawRouteAsync draw = new DrawRouteAsync(bottomBar, MapsPolylineWidth, selectRouteButton, comm, map, googleDirectionsResultJSON, 1);
					draw.execute();
					if(myTask != null)
					{
						myTask.cancel();
					}

					myTask = new GetBusLocationTask(map, getActivity());
					myTimer.schedule(myTask, 0, 2000);
				}
			}
		});
		//OnClick ROUTEBUTTON3
		//Alternate route 2 directions will be drawn onto map
		route3Button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				int buttonColor = ((ColorDrawable) route3Button.getBackground()).getColor();
				if(buttonColor == getResources().getColor(R.color.greytransparent))
				{
					return;          	
				}

				route3Button.setBackgroundColor(getResources().getColor(R.color.highlight_pink_medium));
				route2Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				route1Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				slidingPanel.setPanelState(PanelState.COLLAPSED);
				
				if(googleDirectionsResultJSON != null)
				{
					if(draw != null)
					{
						draw.cancel(true);
					}

					DrawRouteAsync draw = new DrawRouteAsync(bottomBar, MapsPolylineWidth, selectRouteButton, comm, map, googleDirectionsResultJSON, 2);
					draw.execute();
					if(myTask != null)
					{
						myTask.cancel();
					}

					myTask = new GetBusLocationTask(map, getActivity());
					myTimer.schedule(myTask, 0, 2000);

				}
			}
		});

		//This button returns the user to home search screen
		fragmentBackButton = (ImageButton) getActivity().findViewById(R.id.directionsBackButton);
		fragmentBackButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{            	
				//Return to Place Details Screen
				comm.goToLocationSearch();
			}
		});

		myLocationButton = (ImageView) getActivity().findViewById(R.id.routemylocationbutton);
		myLocationButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				gps.getLocation();
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 15));
			}
		});

	}
	//END onActivityCreated



	//decodes google polyline, formula has already been developed online.
	private List<LatLng> decodePoly(String encoded) 
	{
		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) 
		{
			int b, shift = 0, result = 0;
			do 
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do 
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng( (((double) lat / 1E5)),
					(((double) lng / 1E5) ));
			poly.add(p);
		}

		return poly;
	}

	//drawPath - Edited from online code - Benjamin Krig
	//Takes the raw String response from Google Directions API,
	//Transforms it into a JSONObject and selects the polyline from
	//the given routeNumber.
	//Draws polyline onto RouteFragment map


	/*
	 * ResponseObject contains polylines from the do in background of an
	 * async task
	 * 
	 * also contains booleans to determine whether 
	 * */
	private class ResponseObject 
	{
		public boolean hasRoute2 = false;
		public boolean hasRoute3 = false;

		List<PolylineOptions> polyies;
		List<MarkerOptions> markers;
		public ResponseObject(List<PolylineOptions> polyies, List<MarkerOptions> markers)
		{
			this.polyies = polyies;
			this.markers = markers;
		}

		public List<PolylineOptions> getpolyies()
		{
			return polyies;
		}
		public List<MarkerOptions> getmarkers()
		{
			return markers;
		}
		//accessors
	}	
	public ResponseObject drawPath(String result, int routeNumber) 
	{
		List<PolylineOptions> polyies = new ArrayList<PolylineOptions>();
		List<MarkerOptions> markers = new ArrayList<MarkerOptions>();

		try 
		{
			//Tranform the String response RESULT into a JSON object
			final JSONObject json = new JSONObject(result);
			//Get the Routes Array from the JsonObject
			JSONArray routeArray = json.getJSONArray("routes");
			Log.d("Routes: ", ""+routeArray.length());


			//Select which route to draw
			//Throws a JSONException if no route is found at index routeNumber
			JSONObject routes = routeArray.getJSONObject(routeNumber);

			JSONArray legsArray = routes.getJSONArray("legs");
			JSONObject legsObject = legsArray.getJSONObject(0);

			JSONArray stepsArray = legsObject.getJSONArray("steps");

			for(int z = 0; z < stepsArray.length(); z++)
			{
				JSONObject step = stepsArray.getJSONObject(z);

				JSONObject polyline = step.getJSONObject("polyline");   
				if(step.getString("travel_mode").equals("TRANSIT"))
				{	    	           
					String encodedString = polyline.getString("points");
					List<LatLng> list = decodePoly(encodedString);


					for(int x = 0; x < list.size() - 1; x++)
					{
						LatLng src = list.get(x);
						LatLng dest = list.get(x+1);

						polyies
						.add(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
						.width(MapsPolylineWidth)
						.color(Color.argb(190, 121, 14, 189)).geodesic(true));
					}


					if(step.has("transit_details"))
					{
						JSONObject transit_details = step.getJSONObject("transit_details");

						JSONObject arrival_stop = transit_details.getJSONObject("arrival_stop");
						JSONObject arrival_location = arrival_stop.getJSONObject("location");

						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.position(new LatLng(arrival_location.getDouble("lat"), arrival_location.getDouble("lng")));
						markerOptions.title("Stop name: " + arrival_stop.getString("name"));
						String headsign = transit_details.getString("headsign");
						if(Character.isDigit(headsign.charAt(0)))
						{
							markerOptions.snippet("Line: " + headsign);
						}
						else
						{
							try
							{
								JSONObject line = transit_details.getJSONObject("line");
								int shortline = line.getInt("short_name");
								markerOptions.snippet("Line: " + shortline + " " + headsign);
							}
							catch(Exception e)
							{
								markerOptions.snippet("Line: " + headsign);
							}
						}

						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.trial_busstop));
						markerOptions.flat(true);

						markers.add(markerOptions);

						JSONObject departure_stop = transit_details.getJSONObject("departure_stop");
						JSONObject departure_location = departure_stop.getJSONObject("location");

						markerOptions = new MarkerOptions();
						markerOptions.position(new LatLng(departure_location.getDouble("lat"), departure_location.getDouble("lng")));
						markerOptions.title("Stop: " + departure_stop.getString("name"));
						markerOptions.snippet("Line:  " + transit_details.getString("headsign"));
						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.trial_busstop));
						markerOptions.flat(true);
						markers.add(markerOptions);
					}
				}

				if(step.getString("travel_mode").equals("WALKING"))
				{	        		   
					String encodedString = polyline.getString("points");
					List<LatLng> list = decodePoly(encodedString);

					for(int x = 0; x < list.size() - 1; x++)
					{
						LatLng src = list.get(x);
						LatLng dest = list.get(x+1);


						polyies.add(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
						.width(MapsPolylineWidth)
						.color(Color.argb(180, 0, 100, 0)).geodesic(true));		
					}
				}		        
			}
		} 
		catch (JSONException e) 
		{            
			SendErrorAsync log = new SendErrorAsync(e.toString());
			log.execute();
		}

		ResponseObject resp = new ResponseObject(polyies, markers);
		return resp;
	} 

	//
	private class DirectionsAsyncTask extends AsyncTask<Void, Void, ResponseObject>
	{
		private String DAPIKEY = "AIzaSyBoU0I2dTrmBwKvFAtAHY72ZWPjtwE_r-8";
		private long time = System.currentTimeMillis()/1000;
		private LatLng eloc;
		private LatLng uloc;
		private String json;
		private int route;
		private String directionsurl;

		public void setRouteNumber(int num)
		{
			route = num;
		}
		public void updateUserLocation(LatLng uloc, LatLng eloc)
		{
			this.uloc = new LatLng(uloc.latitude, uloc.longitude);
			this.eloc = new LatLng(eloc.latitude, eloc.longitude);
		}
		public void setTime(long time)
		{
			this.time = time;
		}

		@Override
		protected void onPreExecute() 
		{
			hideBottomBar();

			super.onPreExecute();

			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.position(eloc);
			markerOptions.title("Destination");
			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_ic_action_place));
			markerOptions.flat(true);
			map.addMarker(markerOptions);	     

			directionsurl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + uloc.latitude + "," + uloc.longitude + "&destination=" + eloc.latitude + "," + eloc.longitude + "&sensor=true" + "&departure_time=" + time + "&mode=transit&alternatives=true" + "&key=" + DAPIKEY;
			Log.d("url", directionsurl);
		}

		@Override
		protected ResponseObject doInBackground(Void... params) 
		{
			//to be returned
			ResponseObject response = null;


			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpPost = new HttpGet("http://metafora.herokuapp.com/route");
			String body = "";

			JSONObject jsonObject = new JSONObject();

			JSONObject userloc = new JSONObject();
			JSONObject destloc = new JSONObject();

			try
			{
				userloc.put("lat", uloc.latitude);
				userloc.put("lng", uloc.longitude);
				destloc.put("lat", eloc.latitude);
				destloc.put("lng", eloc.longitude);

				jsonObject.put("userlatlng", userloc);
				jsonObject.put("destinationlatlng", destloc);
				jsonObject.put("unixtimestamp", System.currentTimeMillis());
				jsonObject.put("manufacturer", ""+android.os.Build.MANUFACTURER);
				jsonObject.put("brand", ""+android.os.Build.BRAND);
				jsonObject.put("device", ""+android.os.Build.DEVICE);
				jsonObject.put("sdkversion", ""+android.os.Build.VERSION.SDK_INT);
				jsonObject.put("devicemodel", ""+android.os.Build.MODEL);
				jsonObject.put("product", ""+android.os.Build.PRODUCT);

			}
			catch(Exception e)
			{
				SendErrorAsync log = new SendErrorAsync(e.toString());
				log.execute();
			}

			body = jsonObject.toString();
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("body", body);
			httpPost.setHeader("Content-type", "application/json");

			try 
			{
				httpclient.execute(httpPost);
			} 
			catch (ClientProtocolException e) 
			{
				e.printStackTrace();

				SendErrorAsync log = new SendErrorAsync(e.toString());
				log.execute();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();

				SendErrorAsync log = new SendErrorAsync(e.toString());
				log.execute();
			}
			httpPost.abort();

			//Get Google Directions API reponse
			JSONParser jParser = new JSONParser();
			json = jParser.getDirectionApiJsonResponse(directionsurl);

			if(json!=null)
			{
				response = drawPath(json, route);
			}

			//Get Route TIMES to show on route buttons
			try 
			{
				JSONObject j = new JSONObject(json);
				JSONArray routes = j.getJSONArray("routes");

				JSONObject route1 = routes.getJSONObject(0);
				Log.d("rt num", ""+routes.length());

				secondCount[0] = 0;
				JSONArray legs = route1.getJSONArray("legs");
				for(int c = 0; c < legs.length(); c++)
				{
					JSONObject curleg = legs.getJSONObject(c);
					JSONObject duration = curleg.getJSONObject("duration");
					secondCount[0] += duration.getInt("value");
				}


				/*
				 * If there are multiple routes, calculate the route times
				 * 
				 * If there is only one route hide the views of the other route
				 * buttons
				 * 
				 * There will always be at least 1 route from the Google Directions API
				 * */
				if(routes.length() > 1)
				{
					response.hasRoute2 = true;
					secondCount[1] = 0;
					JSONObject route2 = routes.getJSONObject(1);
					legs = route2.getJSONArray("legs");
					for(int c = 0; c < legs.length(); c++)
					{
						JSONObject curleg = legs.getJSONObject(c);
						JSONObject duration = curleg.getJSONObject("duration");
						secondCount[1] += duration.getInt("value");
					}

					if(routes.length() > 2)
					{
						response.hasRoute3 = true;

						secondCount[2] = 0;
						JSONObject route3 = routes.getJSONObject(2);
						legs = route3.getJSONArray("legs");
						for(int c = 0; c < legs.length(); c++)
						{
							JSONObject curleg = legs.getJSONObject(c);
							JSONObject duration = curleg.getJSONObject("duration");
							secondCount[2] += duration.getInt("value");
						}
					}
				}
			} 
			catch (JSONException e) 
			{
				Log.d("Err", e.toString());
			}





			return response;
		}

		@Override
		protected void onPostExecute(ResponseObject result) 
		{
			super.onPostExecute(result);

			if(result.hasRoute2)
				route2Button.setVisibility(View.VISIBLE);
			else
				route2Button.setVisibility(View.GONE);
			if(result.hasRoute3)
				route3Button.setVisibility(View.VISIBLE);
			else
				route3Button.setVisibility(View.GONE);

			googleDirectionsResultJSON = json;

			if(((int)((secondCount[0]/60)/60)) == 0)
			{
				route1Button.setText(Html.fromHtml("<font color=#ffffff>" + ((int)((secondCount[0]/60)%60)) + "</font>" + "<small> min</small>"));
			}
			else
			{
				route1Button.setText(Html.fromHtml("<font color=#ffffff>" + ((int)((secondCount[0]/60)/60)) + "</font>" + "<small> hr </small><font color=#ffffff>" + ((int)((secondCount[0]/60)%60)) + "</font><small> min</small>"));
			}  

			if(((int)((secondCount[1]/60)/60)) == 0)
			{
				route2Button.setText(Html.fromHtml("<font color=#ffffff>" + ((int)((secondCount[1]/60)%60)) + "</font>" + "<small> min</small>"));
			}
			else
			{
				route2Button.setText(Html.fromHtml("<font color=#ffffff>" + ((int)((secondCount[1]/60)/60)) + "</font>" + "<small> hr </small><font color=#ffffff>" + ((int)((secondCount[1]/60)%60)) + "</font><small> min</small>"));
			}

			if(((int)((secondCount[2]/60)/60)) == 0)
			{
				route3Button.setText(Html.fromHtml("<font color=#ffffff>" + ((int)((secondCount[2]/60)%60)) + "</font>" + "<small> min</small>"));
			}
			else
			{
				route3Button.setText(Html.fromHtml("<font color=#ffffff>" + ((int)((secondCount[2]/60)/60)) + "</font>" + "<small> hr </font></small><font color=#ffffff>" + ((int)((secondCount[2]/60)%60)) + "</font><small> min</small>"));
			}  

			/*
			 * Draw first route's polyline
			 * */
			List<PolylineOptions> polyies = result.getpolyies();
			List<MarkerOptions> markers = result.getmarkers();

			PolylineOptions t = new PolylineOptions()
			.width(MapsPolylineWidth)
			.color(Color.argb(255, 80, 73, 137));
			for (PolylineOptions temp : polyies) 
			{
				t.addAll(temp.getPoints());
			}
			for (MarkerOptions temp : markers)
			{
				map.addMarker(temp);
			}
			map.addPolyline(t);


			showBottomBar();

			comm.updateDirectionsList(json, route);

		}
	}

	//updates userLatLng, destinationLatLng
	public void updateFragment(LatLng dest)
	{
		try
		{
			if(!dest.equals(destinationLatLng))
			{
				comm.cancelTimers();

				map.clear();

				GPSTracker gps = new GPSTracker(getActivity());
				userLatLng = new LatLng(gps.getLatitude(), gps.getLongitude());
				destinationLatLng = dest;

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13));

				if(drawRoute != null)
				{
					drawRoute.cancel(true);
				}


				route1Button.setBackgroundColor(getResources().getColor(R.color.highlight_pink_medium));
				route2Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));
				route3Button.setBackgroundColor(getResources().getColor(R.color.highlight_blue_small_transparent));

				drawRoute = new DirectionsAsyncTask();
				drawRoute.updateUserLocation(userLatLng, destinationLatLng);
				drawRoute.setRouteNumber(0);
				drawRoute.execute();


				Timer myTimer = new Timer();
				if(myTask != null)
				{
					myTask.cancel();
				}
				myTask = new GetBusLocationTask(map, getActivity());

				myTimer.schedule(myTask, 0, 2000);
			}
		}
		catch(Exception e)
		{
			SendErrorAsync log = new SendErrorAsync(e+"");
			log.execute();
		}

	}

	public void goToLocation(LatLng location) 
	{
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
	}

	public void hideBottomBar()
	{
		slidingPanel.setPanelState(PanelState.COLLAPSED);
		bottomBar.post(new Runnable()
		{
			@Override
			public void run() 
			{
				bottomBar.animate()
				.translationY(bottomBar.getHeight())
				.alpha(0.0f)
				.setDuration(300)
				.setListener(new AnimatorListenerAdapter() 
				{
					@Override
					public void onAnimationEnd(Animator animation) 
					{
						super.onAnimationEnd(animation);
						bottomBar.setVisibility(View.GONE);
					}
				});		
			}
		});
	}
	public void showBottomBar()
	{
		bottomBar.post(new Runnable()
		{
			@Override
			public void run() 
			{
				bottomBar.animate()
				.translationY(0)
				.alpha(1.0f)
				.setDuration(300)
				.setListener(new AnimatorListenerAdapter() 
				{
					@Override
					public void onAnimationStart(Animator animation) 
					{
						super.onAnimationStart(animation);
						bottomBar.setVisibility(View.VISIBLE);
					}
				});				
			}
		});
	}

	public void updateDirectionsList(String JSON, int routeNumber)
	{
		if(directionsAdapter != null)
			directionsAdapter.stopTimers();

		DirectionsJSON = JSON;   
		try 
		{

			final JSONObject json = new JSONObject(DirectionsJSON);
			JSONArray routes = json.getJSONArray("routes");
			final JSONObject route = routes.getJSONObject(routeNumber);


			JSONArray legs = route.getJSONArray("legs");
			JSONObject leg = legs.getJSONObject(0);

			JSONArray steps = leg.getJSONArray("steps");

			//create header view
			TextView header1 = (TextView) headerView.findViewById(R.id.departTextView);
			if(leg.has("departure_time"))
			{
				JSONObject departure_time = leg.getJSONObject("departure_time");
				String departTime = departure_time.getString("text");

				header1.setText(Html.fromHtml("Depart at: " + "<b><font color=#790ebd>" + departTime + "</font></b>"));				
			}
			else
			{
				header1.setText("Walk to this location.");
			}
			TextView header2 = (TextView) headerView.findViewById(R.id.arriveTextView);
			if(leg.has("arrival_time"))
			{				
				JSONObject arrival_time = leg.getJSONObject("arrival_time");
				String arrivalTime = arrival_time.getString("text");

				header2.setText(Html.fromHtml("Arrive at: " + "<b><font color=#790ebd>" + arrivalTime + "</font></b>"));				
			}
			else
			{
				header2.setText("");
			}
			//end header veiw


			final String[] instructions = new String[steps.length()];
			final String[] travel_modes = new String[steps.length()];
			final String[] distances = new String[steps.length()];
			final String[] durations = new String[steps.length()];
			final String[] transitArrivals = new String[steps.length()];
			final String[] vehicleTypes = new String[steps.length()];
			final LatLng[] locations = new LatLng[steps.length()];

			for(int index = 0; index < steps.length(); index++)
			{	        	
				JSONObject step = steps.getJSONObject(index);
				String html_instructions = step.getString("html_instructions");
				String travel_mode = step.getString("travel_mode");

				JSONObject startLocation = step.getJSONObject("start_location");
				locations[index] = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));

				JSONObject distance = step.getJSONObject("distance");

				String distanceString = distance.getString("text");
				String[] distanceparts = distanceString.split("\\ ");
				String htmlDistanceString = "<b>" + distanceparts[0] +"</b>" + "<small><font color=#212121>" + distanceparts[1] + "</font></small>";

				JSONObject duration = step.getJSONObject("duration");
				int durationSec = duration.getInt("value");
				String durationString = duration.getString("text");
				durationString = (int)(durationSec * 0.0166667) + " min";

				if(travel_mode.equals("TRANSIT"))
				{
					JSONObject transitDetails = step.getJSONObject("transit_details");
					JSONObject departureTime = transitDetails.getJSONObject("departure_time");
					transitArrivals[index] = departureTime.getString("text");

					JSONObject line = transitDetails.getJSONObject("line");
					JSONObject vehicle = line.getJSONObject("vehicle");
					vehicleTypes[index] = vehicle.getString("name");
				}

				distances[index] = htmlDistanceString;
				durations[index] = durationString;
				instructions[index] = html_instructions;
				travel_modes[index] = travel_mode;
			}

			if(directionsAdapter == null)
			{
				directionsAdapter = new TextDirectionsMobileAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes, locations);
			}

			//set footer view
			final JSONObject lastleg = legs.getJSONObject(legs.length()-1);	         
			final TextView footerEndAddressTextView = (TextView) footerView.findViewById(R.id.directionsEndAddress);
			footerEndAddressTextView.post(new Runnable()
			{

				@Override
				public void run() 
				{

					try 
					{
						footerEndAddressTextView.setText(lastleg.getString("end_address"));
					} catch (JSONException e) 
					{
					}

				}

			});
			final TextView footerTotalDistanceTextView = (TextView) footerView.findViewById(R.id.directionsDistance);
			final TextView footerTotalTimeTextView = (TextView) footerView.findViewById(R.id.directionsTime);
			final TextView footerFare = (TextView) footerView.findViewById(R.id.directionsFare);


			//Get total Distance
			int count = 0;
			for(int c = 0; c < legs.length(); c++)
			{
				JSONObject currentLeg = legs.getJSONObject(c);
				JSONObject distance = currentLeg.getJSONObject("distance");
				count += distance.getInt("value");
			}
			final int totalDistanceMeters = count;

			footerTotalDistanceTextView.post(new Runnable()
			{

				@Override
				public void run() 
				{
					footerTotalDistanceTextView.setText(Html.fromHtml("<b><font color=#790ebd>" + (int)(totalDistanceMeters * 0.00062137) + "</font></b>" + "<small><font color=#212121> mi</font></small>"));
				}

			});

			//Get total trip time in seconds
			int secondCount = 0;
			for(int c = 0; c < legs.length(); c++)
			{
				JSONObject current_leg = legs.getJSONObject(c);
				JSONObject duration = current_leg.getJSONObject("duration");
				secondCount += duration.getInt("value");
			}
			final int seconds = secondCount;

			footerTotalTimeTextView.post(new Runnable()
			{
				@Override
				public void run() 
				{
					if(((int)((seconds/60)/60)) == 0)
					{
						footerTotalTimeTextView.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((seconds/60)%60)) + "</font></b>" + "<small><font color=#212121> mins</font></small>"));
					}
					else
					{
						footerTotalTimeTextView.setText(Html.fromHtml("<b><font color=#790ebd>" + ((int)((seconds/60)/60)) + "</font></b>" + "<small><font color=#212121> hrs </font></small>" + "<b><font color=#790ebd>" + ((int)((seconds/60)%60)) + "</font></b>" + "<small><font color=#212121> mins </font></small>"));
					}     					
				}
			});

			footerFare.post(new Runnable()
			{

				@Override
				public void run() 
				{

					//Get route Fare
					if(route.has("fare"))
					{
						String fareCost = null;
						try 
						{
							fareCost = "$"+route.getJSONObject("fare").getDouble("value");
						} 
						catch (JSONException e) 
						{
							footerFare.setText("");
						}
						footerFare.setText(Html.fromHtml("<b><font color=#790ebd>" + fareCost + "</font></b>"));
					}
					else
					{
						footerFare.setText("");
					}					
				}

			});

			//end footer
			if(mainListView.getAdapter() == null)
			{
				mainListView.post(new Runnable()
				{

					@Override
					public void run() 
					{
						mainListView.addHeaderView(headerView);
						mainListView.addFooterView(footerView);
						mainListView.setAdapter(directionsAdapter);	 						
					}

				});
			}
			else
			{
				mainListView.post(new Runnable()
				{

					@Override
					public void run() 
					{
						mainListView.removeHeaderView(headerView);
						mainListView.removeFooterView(footerView);

						mainListView.addHeaderView(headerView);
						mainListView.addFooterView(footerView);

						mainListView.setAdapter(new TextDirectionsMobileAdapter(getActivity(), instructions, travel_modes, distances, durations, transitArrivals, vehicleTypes, locations));
					}
				});
			}
		}
		catch (JSONException e) 
		{
		}
	}
	public void cancelTimers()
	{
		if(directionsAdapter != null)
			directionsAdapter.stopTimers();
	}
}




