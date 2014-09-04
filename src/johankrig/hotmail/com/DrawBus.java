/*
	class DrawBus  extends AsyncTask<String, Void, List<MarkerOptions>> 
	{	
		MarkerOptions markerOptions;
		GoogleMap map;
		JSONObject data = null;
		JSONObject json = null;
		JSONArray buses = null;
		JSONObject location = null;
		List<MarkerOptions> buslist = new ArrayList<MarkerOptions>();
		GetBusLocationTask parent;
		
		public DrawBus()
		{
		}

	    @Override
	    protected List<MarkerOptions> doInBackground(String... params) {
	    	buslist.clear();

			String result;
			JSONParser j = new JSONParser();
			result = j.getVehicleJson();
			
			try
			{
			json = new JSONObject(result);
			data = json.getJSONObject("data");

			buses = data.getJSONArray("255");
			
			for(int c = 0; c < buses.length(); c ++)
			{
				//buses.getJSONObject(c);
				//returns the current bus object in the loop
				
				JSONObject location = buses.getJSONObject(c).getJSONObject("location");
				LatLng position = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));
				
				//Create Marker Options for bus location
				markerOptions = new MarkerOptions();
	    		markerOptions.position(position);
	    		markerOptions.title("Bus: " + buses.getJSONObject(c).getString("vehicle_id"));
	    		markerOptions.snippet(buses.getJSONObject(c).getString("last_updated_on"));
	    		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
	    		markerOptions.visible(false);
	    		buslist.add(markerOptions);
	    	
			}

			}
			catch(Exception e)
	    		{       
	    		}
			
			return buslist;
	    }

	    @Override
	    protected void onPostExecute(List<MarkerOptions> busesOptions) 
	    {

	    	for(int c = 0; c < busesOptions.size(); c ++)
	        {
				if(firstRun)
				{
					busesOptions.get(c).visible(true);
					markerList.add(c, map.addMarker(busesOptions.get(c)));
					Log.e("MADE IT BITTCHH", markerList.get(c).getTitle());
	        	}
				else
				{
					if(!markerList.isEmpty()){
					if(markerList.get(c).getTitle() == busesOptions.get(c).getTitle())
					{
						//marker already exists, update location
						animateMarker(markerList.get(c), busesOptions.get(c).getPosition(), busesOptions.get(c).isVisible());
					}
					else
					{
						//new marker, add to MarkerList and update map
						markerList.add(map.addMarker(busesOptions.get(c)));
					}
					}
				}
	        }    
	    }

	    public void animateMarker(final Marker marker, final LatLng toPosition,
	            final boolean hideMarker) {
	        final Handler handler = new Handler();
	        final long start = SystemClock.uptimeMillis();
	        Projection proj = map.getProjection();
	        Point startPoint = proj.toScreenLocation(marker.getPosition());
	        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
	        final long duration = 500;

	        final Interpolator interpolator = new LinearInterpolator();

	        handler.post(new Runnable() {
	            @Override
	            public void run() {
	                long elapsed = SystemClock.uptimeMillis() - start;
	                float t = interpolator.getInterpolation((float) elapsed
	                        / duration);
	                double lng = t * toPosition.longitude + (1 - t)
	                        * startLatLng.longitude;
	                double lat = t * toPosition.latitude + (1 - t)
	                        * startLatLng.latitude;
	                marker.setPosition(new LatLng(lat, lng));

	                if (t < 1.0) {
	                    // Post again 16ms later.
	                    handler.postDelayed(this, 16);
	                } else {
	                    if (hideMarker) {
	                        marker.setVisible(false);
	                    } else {
	                        marker.setVisible(true);
	                    }
	                }
	            }
	        });
	}

	}*/