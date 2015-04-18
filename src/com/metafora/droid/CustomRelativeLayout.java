package com.metafora.droid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
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
    
    private static boolean isPointInsideView(float x, float y, View view)
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
    	View searchView = null;
    	View myLoc = null;
    	for(int i = 0; i < this.getChildCount(); i++)
    	{
    		if(this.getChildAt(i).getId() == R.id.barid)
    			searchView = this.getChildAt(i);
    		
    		if(this.getChildAt(i).getId() == R.id.locmylocationbutton)
    			myLoc = this.getChildAt(i);
    	} 
    	boolean insearch = false;
    	boolean inloc = false;
    	
    	
    	insearch = isPointInsideView(event.getRawX(), event.getRawY(), searchView);
    	
    	
    	inloc = isPointInsideView(event.getX(0), event.getY(0), myLoc);
    	
    	if(insearch)
    	{
    		searchView.dispatchTouchEvent(event);
    		return false;
    	}
    	else if(inloc)
    	{
	    	myLoc.dispatchTouchEvent(event);
	    	return false;
    	}
    	else
    	{
    		for(int i = 0; i < this.getChildCount(); i++)
        	{	
        		if(this.getChildAt(i).getId() == R.id.barid || this.getChildAt(i).getId() == R.id.locmylocationbutton)
        		{
        		}
        		
        		
        		
        		else
        			this.getChildAt(i).dispatchTouchEvent(event);
        	}
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
