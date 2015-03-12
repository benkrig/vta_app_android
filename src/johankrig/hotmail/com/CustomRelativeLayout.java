package johankrig.hotmail.com;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class CustomRelativeLayout extends RelativeLayout 
{
	public CustomRelativeLayout(Context context) 
    {
		super(context);
	}


    public CustomRelativeLayout(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) 
    {
    	for(int i = 0; i < this.getChildCount(); i++){
	      this.getChildAt(i).dispatchTouchEvent(event);
	   } 
    	return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
    	
        // Never allow swiping to switch between pages
        return false;
    }

}
