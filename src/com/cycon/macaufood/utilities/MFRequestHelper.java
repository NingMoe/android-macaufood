package com.cycon.macaufood.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class MFRequestHelper {
	
	private static final String TAG = MFRequestHelper.class.getName();

	public static boolean updateSuccessfully;
	private static long updateCafeListTimeStamp;
	private static final long UPDATE_TIME_PERIOD = 3600 * 1000 * 12; // 12 hours
	private static boolean isUpdating = false;
	private static Context appContext;
	private static final int TIMEOUT_PERIOD = 10000;

	public static void checkUpdate(Context c) {
		appContext = c;
		if (MFConfig.isOnline(appContext) && !isUpdating && System.currentTimeMillis() - updateCafeListTimeStamp > UPDATE_TIME_PERIOD) {
			AsyncTaskHelper.execute(new FetchUpdateTask());
		}
	}
	
	public static void sendFavoriteLog(Context c) {
		appContext = c;
		if (MFConfig.isOnline(appContext)) {
			AsyncTaskHelper.execute(new SendFavoriteLogTask());
		}
			
	}
	
	public static InputStream executeRequest(String url) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_PERIOD);
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		InputStream is = response.getEntity().getContent();
		return is;
	}
	
	public static Bitmap getBitmap(String url, File cacheFile) throws ClientProtocolException, IOException {
		InputStream is = executeRequest(url);
		
		if (cacheFile == null) {
			return BitmapFactory.decodeStream(MFUtil.flushedInputStream(is));
		}

		OutputStream os = new FileOutputStream(cacheFile);
		MFUtil.CopyStream(is, os);
		os.close();

		FileInputStream fis = new FileInputStream(cacheFile);
		return BitmapFactory.decodeStream(MFUtil.flushedInputStream(fis));
	}
	
	public static String getString(String url, File cacheFile) throws ClientProtocolException, IOException {
		InputStream is = executeRequest(url);
		
		StringBuilder sb = new StringBuilder();
		
		if (cacheFile == null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					is));
			String line = null;
			while ((line = rd.readLine()) != null) {
				sb.append(line + "\n");
			}
			rd.close();
			return sb.toString().trim();
		}

		OutputStream os = new FileOutputStream(cacheFile);
		MFUtil.CopyStream(is, os);
		os.close();

		FileInputStream fis = new FileInputStream(cacheFile);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				fis));
		String line = null;
		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n");
		}
		rd.close();
		return sb.toString().trim();
	}
	
	
    public static class FetchUpdateTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		if (isUpdating) return null; 
    		isUpdating = true;
    		String urlStr = "http://www.cycon.com.mo/xml_updatelogandroid.php?key=cafecafe&lastupdatetime=" + MFConfig.cafe_version_update;
            try {
            	InputStream is = executeRequest(urlStr);
            	LocalDbManager.getInstance(appContext).beginWritableDb();
            	parseUpdateXml(is);
            	LocalDbManager.getInstance(appContext).endWritableDb();
            	if (updateSuccessfully) {
            		Log.e("BaseActivity", "update success");
            		
            		urlStr = "http://www.cycon.com.mo/cafe_version_update.txt";
				    try {
				    	is = executeRequest(urlStr);
				        
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
    	            updateSuccessfully = false;
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
