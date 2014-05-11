package com.cycon.macaufood.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.sqlite.LocalDbManager;
import com.cycon.macaufood.xmlhandler.UpdateXMLHandler;

public class MFService {
	
	private static final String TAG = MFService.class.getName();

	public static boolean updateSuccessfully;
	private static long updateCafeListTimeStamp;
	private static final long UPDATE_TIME_PERIOD = 3600 * 1000 * 12; // 12 hours
	private static boolean isUpdating = false;
	private static Context appContext;
	private static final int TIMEOUT_PERIOD = 10000;
	//dont decode bitmap larger than 1MB to avoid memory leak
	public static final long MAX_BITMAP_SIZE = 1000000; 
	
	public static void loadImage(Context c, ImageType imageType, String id,
			ImageView imageView, final boolean useCache, boolean fadeInAnimation) {
		appContext = c;
		FileCache fileCache = null;
		Bitmap bitmap = null;
		if (useCache) {
			fileCache = new FileCache(c, imageType);
			bitmap = MFUtil.getBitmapFromCache(fileCache, id);
		}
		
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else if (MFConfig.isOnline(appContext)) {
			AsyncTaskHelper.executeWithResultBitmap(new FetchImageTask(
					imageView, MFURL.getImageUrl(imageType, id),
					useCache ? fileCache.getFile(id) : null, fadeInAnimation));
		} else {
			// no connection
		}

	}

	//check update everytime fresh launch as we did not save updatetimestamp in preferences
	public static void checkUpdate(Context c) {
		appContext = c;
		if (System.currentTimeMillis() - updateCafeListTimeStamp > UPDATE_TIME_PERIOD && MFConfig.isOnline(appContext) && !isUpdating) {
			AsyncTaskHelper.execute(new FetchUpdateTask());
		}
	}
	
	public static void sendFavoriteLog(Context c) {
		StringBuilder sb = new StringBuilder();
		for (String id : MFConfig.getInstance().getFavoriteLists()) {
			int idValue = Integer.parseInt(id) - 1;
			sb.append(idValue + ",");
		}
		
		sendRequest(MFURL.FAVORITE_LOG + sb.toString(), c);
	}
	
	public static void sendRequest(String url, Context c) {
		appContext = c;
		if (MFConfig.isOnline(appContext)) {
			AsyncTaskHelper.execute(new SendRequestTask(url, null));
		}
	}
	
	public static void sendRequestWithParams(String url, Context c, List<NameValuePair> pairs) {
		appContext = c;
		if (MFConfig.isOnline(appContext)) {
			AsyncTaskHelper.execute(new SendRequestTask(url, pairs));
		}
	}
	
	public static void fetchFrontPage(Context c) {
		appContext = c;
		if (MFConfig.isOnline(appContext)) {
			AsyncTaskHelper.execute(new FetchFrontPageTask());
		}
	}
	
	public static void getString(String url, File cacheFile, MFServiceCallBack callback) {
		AsyncTaskHelper.executeWithResultString(new FetchStringTask(url, cacheFile, callback));
	}
	
	private static InputStream executeRequest(String url) throws ClientProtocolException, IOException {
//		URL myUrl = new URL(url);
//		URLConnection urlConnection = myUrl.openConnection(); 
//		InputStream is = new BufferedInputStream(urlConnection.getInputStream());
		
		
		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_PERIOD);
		HttpGet request = new HttpGet(url);	
		HttpResponse response = client.execute(request);
		InputStream is = response.getEntity().getContent();
		return is;
	}
	
	public static InputStream executeRequestWithHttpParams(String url, List<NameValuePair> pairs) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_PERIOD);
		HttpPost request = new HttpPost(url);
		request.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
		HttpResponse response = client.execute(request);
		InputStream is = response.getEntity().getContent();
		return is;
	}
	
	public static Bitmap getBitmap(String url, File cacheFile) throws ClientProtocolException, IOException {
		InputStream is = executeRequest(url);
		
		if (cacheFile == null) {
			try {
				return BitmapFactory.decodeStream(MFUtil.flushedInputStream(is));
			} finally {
				is.close();
			}
		}
		OutputStream os = new FileOutputStream(cacheFile);
		MFUtil.CopyStream(is, os);
		is.close();
		os.close();

		if (cacheFile.length() > MAX_BITMAP_SIZE) {
			return null;
		}
		FileInputStream fis = new FileInputStream(cacheFile);
		try {
			return BitmapFactory.decodeStream(MFUtil.flushedInputStream(fis));
		} finally {
			fis.close();
		}
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
			is.close();
			rd.close();

			return sb.toString().trim();
		}

		OutputStream os = new FileOutputStream(cacheFile);
		MFUtil.CopyStream(is, os);
		is.close();
		os.close();

		FileInputStream fis = new FileInputStream(cacheFile);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				fis));
		try {
			String line = null;
			while ((line = rd.readLine()) != null) {
				sb.append(line + "\n");
			}
			return sb.toString().trim();
		} finally {
			rd.close();
			fis.close();
		}
	}
	
    public static class FetchImageTask extends AsyncTask<Void, Void, Bitmap> {
    	
    	private ImageView imageView;
    	private String url;
    	private File file;
    	private boolean fadeInAnimation;
    	
    	private FetchImageTask(ImageView i, String u, File f, boolean anim) {
    		imageView = i;
    		url = u;
    		file = f;
    		fadeInAnimation = anim;
    	}
    	
    	@Override
    	protected Bitmap doInBackground(Void... params) {

    		Bitmap bitmap = null;
    		
    		try {
    			bitmap = MFService.getBitmap(url, file);
				
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
				e.printStackTrace();
			} catch (NumberFormatException e) {
				MFLog.e(TAG, "NumberFormatException");
				e.printStackTrace();
			}
    		
    		return bitmap;
    	}
    	
    	@Override
    	protected void onPostExecute(Bitmap result) {
    		super.onPostExecute(result);
    		
    		if (result != null) {
    			imageView.setImageBitmap(result);
    			if (fadeInAnimation) {
    				imageView.setAnimation(AnimationUtils.loadAnimation(appContext, android.R.anim.fade_in));
				}
    		} else {
    			//callback onFailure
    		}
    	}
    }

	
    public static class FetchFrontPageTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected Void doInBackground(Void... params) {

    		try {
				String timeStampStr = MFService.getString(MFURL.FRONT_PAGE_TIME, null);

				long timeStamp = Long.parseLong(timeStampStr);
				long currentFrontPageTime = PreferenceHelper.getPreferenceValueLong(appContext, MFConstants.FRONT_PAGE_STAMP_PREF_KEY, 0);
				
				Bitmap bitmap = null;
				if (timeStamp > currentFrontPageTime) {
					String frontPageLink = MFService.getString(MFURL.FRONT_PAGE_LINK_URL, null);
					PreferenceHelper.savePreferencesStr(appContext, MFConstants.FRONT_PAGE_LINK_PREF_KEY, frontPageLink);
					FileCache fileCache = new FileCache(appContext, ImageType.FRONTPAGE);
					File f = fileCache.getFile("1");
					bitmap = MFService.getBitmap(MFURL.getImageUrl(ImageType.FRONTPAGE, "1"), f);
				}
				
				if (bitmap != null) {
					PreferenceHelper.savePreferencesLong(appContext, MFConstants.FRONT_PAGE_STAMP_PREF_KEY, timeStamp);
				}
				
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
				e.printStackTrace();
			} catch (NumberFormatException e) {
				MFLog.e(TAG, "NumberFormatException");
				e.printStackTrace();
			}
    		
    		return null;
    	}
    }
	
	
    public static class FetchUpdateTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		if (isUpdating) return null; 
    		isUpdating = true;
            try {
            	InputStream is = executeRequest(MFURL.UPDATE_CAFE_LOG + MFConfig.cafe_version_update);
            	LocalDbManager.getInstance(appContext).beginWritableDb();
            	parseUpdateXml(is);
            	LocalDbManager.getInstance(appContext).endWritableDb();
            	if (updateSuccessfully) {
            		MFLog.e("BaseActivity", "update success");
            		
				    try {
				    	is = executeRequest(MFURL.CAFE_VERSION_UPDATE);
				        
				    	BufferedReader rd = new BufferedReader(new InputStreamReader(is
								));
				    	String str = rd.readLine().trim();
				    	Integer.parseInt(str);
				    	MFConfig.cafe_version_update = str;
				    	PreferenceHelper.savePreferencesStr(appContext, "cafe_version_update", str);
				    	MFLog.e("cafe_version_update", MFConfig.cafe_version_update);
				        
					} catch (MalformedURLException e) {
						MFLog.e(TAG, "malformed url exception");
						e.printStackTrace();
					} catch (IOException e) {
						MFLog.e(TAG, "io exception");
						e.printStackTrace();
					} catch (Exception e) {
						MFLog.e(TAG, "EXCEPTION" + e.getMessage());
					} finally {
						is.close();
					}
					updateCafeListTimeStamp = System.currentTimeMillis();
    	            updateSuccessfully = false;
            	}
				
				
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
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
			MFLog.e(TAG, "FactoryConfigurationError");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			MFLog.e(TAG, "ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			MFLog.e(TAG, "SAXException");
			// when it shows "1" in xml
			updateCafeListTimeStamp = System.currentTimeMillis();
			e.printStackTrace();
		} catch (IOException e) {
			MFLog.e(TAG, "IOException");
			e.printStackTrace();
		}
    }
	
	private static class SendRequestTask extends AsyncTask<Void, Void, Void> {
		
		private String url;
		private List<NameValuePair> pairs;
		
		private SendRequestTask(String url, List<NameValuePair> pairs) {
			this.url = url;
			this.pairs = pairs;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
            try {
            	if (pairs == null) {
            		executeRequest(url).close();
				} else {
					executeRequestWithHttpParams(url, pairs).close();
				}
            	
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				MFLog.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
			
			return null;
		}
	}
	
	
    public static class FetchStringTask extends AsyncTask<Void, Void, String> {
    	
    	private MFServiceCallBack callback;
    	private String url;
    	private File file;
    	
    	public FetchStringTask(String url, File file, MFServiceCallBack callback) {
    		this.callback = callback;
    		this.url = url;
    		this.file = file;
    	}
    	
    	@Override
    	protected String doInBackground(Void... params) {

    		try {
				return MFService.getString(url, file);
				
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
				e.printStackTrace();
			} catch (NumberFormatException e) {
				MFLog.e(TAG, "NumberFormatException");
				e.printStackTrace();
			}
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
    		
    		if (result == null) {
				callback.onLoadResultError();
			} else {
				callback.onLoadResultSuccess(result);
			}
    	}
    }
	
}
