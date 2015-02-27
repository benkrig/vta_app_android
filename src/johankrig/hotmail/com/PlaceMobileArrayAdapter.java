package johankrig.hotmail.com;

import java.util.Date;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;


 class PlaceMobileArrayAdapter extends ArrayAdapter<String> 
 {
	private final Context context;
	private final String[] reviewDetails;
	private String[] reviewNames;
	private int[] reviewDates;
	private float[] reviewRatings;
 
	public PlaceMobileArrayAdapter(Context context, String[] details, String[] reviewerNames, int[] dates, float[] ratings) 
	{
		super(context, R.layout.placereviewrow, details);
		this.context = context;
		this.reviewDetails = details;
		this.reviewNames = reviewerNames;
		this.reviewDates = dates;
		this.reviewRatings = ratings;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		
		View rowView = inflater.inflate(R.layout.placereviewrow, parent, false);
		
		TextView userName = (TextView) rowView.findViewById(R.id.reviewUserName);
		TextView placeReviewDetails = (TextView) rowView.findViewById(R.id.userReviewDetails);
		TextView placeReviewDates = (TextView) rowView.findViewById(R.id.dateOfReview);
		RatingBar placeReviewRatings = (RatingBar) rowView.findViewById(R.id.userReviewRating);
		
		userName.setTypeface(null, Typeface.BOLD);
		userName.setText(reviewNames[position]);
		
		Date date = new Date ();
		date.setTime((long)reviewDates[position]*1000);
		placeReviewDates.setText(date.toString());
		
		placeReviewDetails.setText(reviewDetails[position]);
		placeReviewRatings.setStepSize((float) 0.25);
		placeReviewRatings.setRating(reviewRatings[position]);
		 
		return rowView;
	}
}
