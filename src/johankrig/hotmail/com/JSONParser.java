package johankrig.hotmail.com;

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
    public String getJSONFromUrl(String url, int x)
    {
    	if(x == 0){
    	try{
    
    	URL test = new URL(url);
		URLConnection yc = test.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
        json = json + inputLine;
        }
        in.close();
        }
        catch(Exception e)
        {
        	
        }
    	return json;
    	}
    	else{
    	
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
            e.printStackTrace();
        } 
        catch (ClientProtocolException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
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
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
    	
        return json;

    	}
    }
}