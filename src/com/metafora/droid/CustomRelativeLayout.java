package com.metafora.droid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class CustomRelativeLayout extends RelativeLayout 
{
	Context context;
	public CustomRelativeLayout(Context context) 
    {
		super(context);
		this.context=context;
	}


    public CustomRelativeLayout(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
        this.context = context;
    }
    
    public static boolean isPointInsideView(float x, float y, View view)
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
    public boolean onInterceptTouchEvent(MotionEvent event) 
    {
    	for(int i = 0; i < this.getChildCount(); i++)
    	{
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
