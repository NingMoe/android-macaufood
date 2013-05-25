package com.cycon.macaufood.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.cycon.macaufood.activities.BaseActivity;
import com.cycon.macaufood.sqlite.LocalDbManager;
import com.cycon.macaufood.xmlhandler.UpdateXMLHandler;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MFRequestHelper {
	
	private static final String TAG = MFRequestHelper.class.getName();

	private static long updateCafeListTimeStamp;
	private static final long UPDATE_TIME_PERIOD = 3600 * 1000 * 12; // 12 hours
	private static boolean isUpdating = false;
	private static Context appContext;

	public static void checkUpdate(Context c) {
		appContext = c;
		if (MFConfig.isOnline(appContext) && !isUpdating && System.currentTimeMillis() - updateCafeListTimeStamp > UPDATE_TIME_PERIOD) {
			new FetchUpdateTask().execute();
		}
	}
	
	public static void sendFavoriteLog(Context c) {
		appContext = c;
		if (MFConfig.isOnline(appContext)) {
			new SendFavoriteLogTask().execute();
		}
			
	}
	
	
    public static class FetchUpdateTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		if (isUpdating) return null; 
    		isUpdating = true;
    		String urlStr = "http://www.cycon.com.mo/xml_updatelogandroid.php?key=cafecafe&lastupdatetime=" + MFConfig.cafe_version_update;
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
            	LocalDbManager.getInstance(appContext).beginWritableDb();
            	parseUpdateXml(is);
            	LocalDbManager.getInstance(appContext).endWritableDb();
            	if (MFConfig.updateSuccessfully) {
            		Log.e("BaseActivity", "update success");
            		
            		urlStr = "http://www.cycon.com.mo/cafe_version_update.txt";
				    try {
				    	request = new HttpGet(urlStr);
		            	response = client.execute(request);
		            	is= response.getEntity().getContent();
				        
				    	BufferedReader rd = new BufferedReader(new InputStreamReader(is
								));
				    	String str = rd.readLine().trim();
				    	Integer.parseInt(str);
				    	MFConfig.cafe_version_update = str;
				    	PreferenceHelper.savePreferencesStr(appContext, "cafe_version_update", str);
				    	Log.e("cafe_version_update", MFConfig.cafe_version_update);
				        
					} catch (MalformedURLException e) {
						Log.e(TAG, "malformed url exception");
						e.printStackTrace();
					} catch (IOException e) {
						Log.e(TAG, "io exception");
						e.printStackTrace();
					} catch (Exception e) {
						Log.e(TAG, "EXCEPTION" + e.getMessage());
					}
					updateCafeListTimeStamp = System.currentTimeMillis();
    	            MFConfig.updateSuccessfully = false;
            	}
				
				
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			} 
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);

    		isUpdating = false;
    	}
    }
    
    private static void parseUpdateXml(InputStream is) {    	
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			UpdateXMLHandler myXMLHandler = new UpdateXMLHandler();
			myXMLHandler.setContext(appContext);
			xr.setContentHandler(myXMLHandler);
			xr.parse(new InputSource(is));	
		} catch (FactoryConfigurationError e) {
			Log.e(TAG, "FactoryConfigurationError");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(TAG, "SAXException");
			// when it shows "1" in xml
			updateCafeListTimeStamp = System.currentTimeMillis();
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
		}
    }
	
	private static class SendFavoriteLogTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			
			StringBuilder sb = new StringBuilder();
			for (String id : MFConfig.getInstance().getFavoriteLists()) {
				int idValue = Integer.parseInt(id) - 1;
				sb.append(idValue + ",");
			}
			
			String urlStr = "http://www.cycon.com.mo/xml_favouritelog.php?key=cafecafe&udid=android-" + 
					MFConfig.DEVICE_ID + "&cafeid=" + sb.toString();
			
            try {
            	HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	client.execute(request);
            	
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				Log.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
			
			return null;
		}
	}
}
