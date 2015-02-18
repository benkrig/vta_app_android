package johankrig.hotmail.com;

import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


 class PlaceMobileArrayAdapter extends ArrayAdapter<String> 
 {
	private final Context context;
	private final String[] reviewDetails;
	int[] reviewDates;
 
	public PlaceMobileArrayAdapter(Context context, String[] details, int[] dates) 
	{
		super(context, R.layout.placerow, details);
		this.context = context;
		this.reviewDetails = details;
		this.reviewDates = dates;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.placerow, parent, false);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);

		TextView placeReviewDetails = (TextView) rowView.findViewById(R.id.placeReviewText);
		TextView placeReviewDates = (TextView) rowView.findViewById(R.id.placeReviewDate);
		
		Date date = new Date ();
		date.setTime((long)reviewDates[position]*1000);
		
		placeReviewDates.setText(date.toString());
		
		placeReviewDetails.setText(reviewDetails[position]);
 
		 
		return rowView;
	}
}
