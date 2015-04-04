


//add custom infowindow on marker click, add more detail to text directions,

//smooth transitions between all screens

package com.metafora.droid;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.os.AsyncTask;


class SendErrorAsync extends AsyncTask<String, Void, List<Address>>
{
	private String error;
	
	public SendErrorAsync(String error)
	{
		this.error = error;
	}

    @Override
    protected void onCancelled() 
    {
    	
    }
	
	@Override 
	protected void onPreExecute()
	{
		super.onPreExecute();
		
		
		if(error == null)
			this.cancel(true);
	}
	
    @Override
    protected List<Address> doInBackground(String... params)
    {
	    	// 1. create HttpClient
	        
	        JSONObject jsonObject = new JSONObject();
	        try 
	        {
				jsonObject.put("errorlog", error);
				jsonObject.put("unixtimestamp", System.currentTimeMillis());
			} 
	        catch (JSONException e1) 
	        {
			}
	        
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpGet httpPost = new HttpGet("http://metafora.herokuapp.com/error");
	        
	        
	        String body = "";
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
			} 
	        catch (IOException e) 
			{
			}
	        httpPost.abort();
	        
	        return null;

    }

    @Override
    protected void onPostExecute(List<Address> addresses) 
    {
    	
    }
}
