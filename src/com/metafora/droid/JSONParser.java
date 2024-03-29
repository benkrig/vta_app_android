//
//Help and ideas from: http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android

package com.metafora.droid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.util.Log;

public class JSONParser 
{
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    // constructor
    public JSONParser() 
    {
    }
    //callType 0 for GeoLocationAPI
    //callType 1 for DirectionsAPI
    public String getJSONFromUrl(String url, int callType)
    {
    	if(callType == 0)
    	{
    		try
    		{
    
    			URL test = new URL(url);
    			URLConnection yc = test.openConnection();
    			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
    			String inputLine;

    			while ((inputLine = in.readLine()) != null) 
    			{
    				json = json + inputLine;
    			}
        	in.close();
    		}
    		catch(Exception e)
    		{
    			SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
    		}
    		return json;
    	}
    	else if(callType == 1)
    	{
    	
    		try 
    		{
    			// defaultHttpClient
    			DefaultHttpClient httpClient = new DefaultHttpClient();
    			HttpPost httpPost = new HttpPost(url);

    			HttpResponse httpResponse = httpClient.execute(httpPost);
    			//added
    			httpResponse.addHeader("Accept-Language", "en-US");
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		} 
    		catch (UnsupportedEncodingException e) 
    		{
    			SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	        	
    			e.printStackTrace();
    		} 
    		catch (ClientProtocolException e) 
    		{
    			SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	        	
    			e.printStackTrace();
    		} 
    		catch (IOException e) 
    		{
    			SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	        	
    			e.printStackTrace();
    		}
    		try 
    		{
    			//was iso-8859-1, changed to UTF-8
    			BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
    			StringBuilder sb = new StringBuilder();
    			String line = null;
           	 
    			while ((line = reader.readLine()) != null) 
    			{
    				sb.append(line + "\n");
    			}

    			json = sb.toString();
    			is.close();
    		} 
    		catch (Exception e) 
    		{
    			SendErrorAsync log = new SendErrorAsync(e.toString());
	        	log.execute();
	        	
    			Log.e("Buffer Error", "Error converting result " + e.toString());
    		}
    		return json;
    	}
    	else
    	{
    		return null;
    	}
    }
    public String getVehicleJson()
    {
    	String urlString = "https://transloc-api-1-2.p.mashape.com/vehicles.json?agencies=255";
    	String requestHeader = "X-Mashape-Key";
    	String value = "y28dPGGYKPmshBzOH0xTKSodfOLYp1bJUc2jsnG4TPIT9jW4TZ";
    	String json = "";
    	try
    	{
    		URL url = new URL(urlString);
		
    		URLConnection urlConnection = url.openConnection();
    		urlConnection.setRequestProperty(requestHeader, value);
    		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    		String inputLine;

    		while ((inputLine = in.readLine()) != null) 
    		{
    			json = json + inputLine;
    		}
    		in.close();
    	}
    	catch(Exception e)
    	{
    		SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
    	}
    	return json;
    }
    public String getDirectionApiJsonResponse(String url)
    {
		try 
		{
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			//added
			httpResponse.addHeader("Accept-Language", "en-US");
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		} 
		catch (UnsupportedEncodingException e) 
		{
			SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
			e.printStackTrace();
		} 
		catch (ClientProtocolException e) 
		{
			SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
			e.printStackTrace();
		}
		try 
		{
			//was iso-8859-1, changed to UTF-8
			BufferedReader reader = new BufferedReader(new InputStreamReader(
                is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
       	 
			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}

			json = sb.toString();
			is.close();
		} 
		catch (Exception e) 
		{
			SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
		return json;	
	}
    
    public String getSearchBarAddress(String locationName)
    {
    	return json;
    }
    
    public String getGeoLocationApiResponse(String url)
    {

		try
		{
			URL test = new URL(url);
			URLConnection yc = test.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
			{
				json = json + inputLine;
			}
    	in.close();
		}
		catch(Exception e)
		{
			SendErrorAsync log = new SendErrorAsync(e.toString());
        	log.execute();
		}
		return json;

    }
}
