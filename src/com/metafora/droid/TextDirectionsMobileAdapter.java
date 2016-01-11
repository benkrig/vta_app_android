package com.metafora.droid;

import java.util.ArrayList;
import java.util.Timer;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

class TextDirectionsMobileAdapter extends ArrayAdapter<String> 
{
	private ArrayList<Timer> timers;
	private final Context context;
	private final FragmentCommunicator comm;
	private final String[] values;
	String[] modes;
	private String[] distances;
	private String[] durations;
	private String[] transitArrivals;
	private String[] vehicleTypes;
	private LatLng[] locations;
	Activity main;

	public TextDirectionsMobileAdapter(Context context, String[] values, String[] modes, String[] distances, String[] durations, String[] transitArrivals, String[] vehicleTypes, LatLng[] location) 
	{
		super(context, R.layout.directionsrow, values);
		timers = new ArrayList<Timer>();
		this.context = context;
		this.comm = (FragmentCommunicator) context;
		this.locations = location;
		this.values = values;
		this.modes = modes;
		this.distances = distances;
		this.durations = durations;
		this.transitArrivals = transitArrivals;
		this.vehicleTypes = vehicleTypes;
		this.main = (Activity) context;
	}
	public void stopTimers()
	{
		for(Timer timerIterator : timers)
		{
			timerIterator.cancel();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View rowView = inflater.inflate(R.layout.directionsrow, parent, false);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.vehicleTypeImage);

		TextView directionsText = (TextView) rowView.findViewById(R.id.directionsText);
		TextView directionsDetails = (TextView) rowView.findViewById(R.id.directionsDetails);
		TextView directionsRowTimeText = (TextView) rowView.findViewById(R.id.directionsRowTimeTextView);
		LinearLayout touchLayout = (LinearLayout) rowView.findViewById(R.id.clickableDirectionLayout);




		final int p = position;

		touchLayout.setOnTouchListener( new OnTouchListener()
		{
			public boolean isPointInsideView(float x, float y, View view)
			{
				int location[] = new int[2];
				view.getLocationOnScreen(location);
				int viewX = location[0];
				int viewY = location[1];

				//point is inside view bounds
				if(( x > viewX && x < (viewX + view.getWidth())) &&
						( y > viewY && y < (viewY + view.getHeight())))
				{
					return true;
				}
				else 
				{
					return false;
				}
			}

			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					rowView.setBackgroundColor(context.getResources().getColor(R.color.white));
					return true;
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					if(isPointInsideView(event.getRawX(), event.getRawY(), v))
					{
						comm.goToStepLocation(locations[p]);
						SlidingUpPanelLayout test  = (com.sothree.slidinguppanel.SlidingUpPanelLayout)((MainActivity) context).findViewById(R.id.sliding_layout);
						test.setPanelState(PanelState.COLLAPSED);
					}	

					rowView.setBackgroundColor(Color.TRANSPARENT); 
					return true;

				}
				else if(event.getAction() == MotionEvent.ACTION_CANCEL)
				{
					rowView.setBackgroundColor(Color.TRANSPARENT); 
					return true;
				}

				return false;
			}
		});


		if(position+1 < transitArrivals.length)
		{
			if(transitArrivals[position+1] != null)
			{
				TextView transitDetails = (TextView) rowView.findViewById(R.id.transitTextView);
				transitDetails.setVisibility(View.VISIBLE);
				transitDetails.setText(vehicleTypes[position+1] + " arrives here at " + transitArrivals[position+1]);

				if(vehicleTypes[position+1].equals("Light rail"))
				{
					rowView.findViewById(R.id.nextarrivalsid).setVisibility(View.VISIBLE);
					RealTimeStopsTask task = new RealTimeStopsTask(rowView, main);
					try 
					{
						task.init(locations[position+1]);
					} 
					catch (JSONException e) 
					{}

					Timer myTimer = new Timer();
					myTimer.schedule(task, 10000, 45000);
					timers.add(myTimer);
				}
			}
		}

		directionsDetails.setText(Html.fromHtml(distances[position]));

		String[] parts = durations[position].split("\\ ");
		directionsRowTimeText.setText(Html.fromHtml("<b>" + parts[0] +"</b>" + "<small><font color=#212121>" + parts[1] + "</font></small>"));

		directionsText.setText(values[position]);

		// Change icon based on name
		String imageType = modes[position];

		if (imageType.equals("TRANSIT")) 
		{
			if(vehicleTypes[position].equals("Light rail"))
			{
				imageView.setImageResource(R.drawable.smalllightrail);


			}
			else if(vehicleTypes[position].equals("Bus"))
			{
				imageView.setImageResource(R.drawable.bus);
				RealTimeStopsTask task = new RealTimeStopsTask(rowView, main);
				try 
				{
					task.init(locations[position]);
				} catch (JSONException e) 
				{}

				Timer myTimer = new Timer();
				myTimer.schedule(task, 5000, 30000);
			}
		} 
		else if (imageType.equals("WALKING")) 
		{
			imageView.setImageResource(R.drawable.walking_man);
		}

		return rowView;
	}
}