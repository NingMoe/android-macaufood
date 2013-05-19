package com.cycon.macaufood;


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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.cycon.dbaccess.LocalDaoManager;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;
import com.cycon.macaufood.utilities.UpdateXMLHandler;

public abstract class BaseActivity extends Activity {
	
	protected boolean isTabChild;
	protected boolean addRefreshMenu;
	protected boolean needMenu = true;
	private static final String TAG = "BaseActivity";
	private static long updateCafeListTimeStamp;
	private static final long UPDATE_TIME_PERIOD = 3600 * 1000 * 12; // 12 hours
	private static boolean isUpdating = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Config.getInstance().getCafeLists().size() == 0) {
			Intent i = new Intent(this, SplashScreen.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			Process.killProcess(Process.myPid());
		} 
	}
	
	@Override
	protected void onResume() {
		super.onResume();

    	if (Config.isOnline(this) && !isUpdating && System.currentTimeMillis() - updateCafeListTimeStamp > UPDATE_TIME_PERIOD) {
    		new FetchUpdateTask().execute();
    	}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
	    if ((keyCode == KeyEvent.KEYCODE_BACK && isTabChild)) {	   
	    	new AlertDialog.Builder(this)
			.setMessage("你確定要退出程式嗎?      ")
			.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
					    	Process.killProcess(Process.myPid());   
						}@Override
						protected void finalize() throws Throwable {
							// TODO Auto-generated method stub
							super.finalize();
						}
					})
			.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
							dialog.dismiss();
						}
					})
			.show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if (!needMenu) return false;
    	if (addRefreshMenu) {
    		menu.add(Menu.NONE, 1, Menu.NONE, "重新整理").setIcon(R.drawable.ic_menu_refresh);
    	}
    	menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.foodNews)).setIcon(R.drawable.ic_menu_dishes);
    	menu.add(Menu.NONE, 3, Menu.NONE, "最新情報").setIcon(R.drawable.rss);
    	menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.aboutUs)).setIcon(R.drawable.ic_menu_info_details);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case 1:
			refresh();
			break;
		case 2:
			Intent i = new Intent(this, FoodNews.class);
			startActivity(i);
			break;
		case 3:
			Intent i2 = new Intent(this, Latest.class);
			startActivity(i2);
			break;
		case 4:
			Intent i3 = new Intent(this, About.class);
			startActivity(i3);
			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    public void refresh() {};
    
    
    
    public class FetchUpdateTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		if (isUpdating) return null; 
    		isUpdating = true;
    		String urlStr = "http://www.cycon.com.mo/xml_updatelogandroid.php?key=cafecafe&lastupdatetime=" + Config.cafe_version_update;
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
            	LocalDaoManager.getInstance(getApplicationContext()).beginWritableDb();
            	parseUpdateXml(is);
            	LocalDaoManager.getInstance(getApplicationContext()).endWritableDb();
            	if (Config.updateSuccessfully) {
            		ETLog.e("BaseActivity", "update success");
            		
            		urlStr = "http://www.cycon.com.mo/cafe_version_update.txt";
				    try {
				    	request = new HttpGet(urlStr);
		            	response = client.execute(request);
		            	is= response.getEntity().getContent();
				        
				    	BufferedReader rd = new BufferedReader(new InputStreamReader(is
								));
				    	String str = rd.readLine().trim();
				    	Integer.parseInt(str);
				    	Config.cafe_version_update = str;
				    	Config.savePreferencesStr(BaseActivity.this, "cafe_version_update", str);
				    	ETLog.e("cafe_version_update", Config.cafe_version_update);
				        
					} catch (MalformedURLException e) {
						ETLog.e(TAG, "malformed url exception");
						e.printStackTrace();
					} catch (IOException e) {
						ETLog.e(TAG, "io exception");
						e.printStackTrace();
					} catch (Exception e) {
						ETLog.e(TAG, "EXCEPTION" + e.getMessage());
					}
					updateCafeListTimeStamp = System.currentTimeMillis();
    	            Config.updateSuccessfully = false;
            	}
				
				
			} catch (MalformedURLException e) {
				ETLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				ETLog.e(TAG, "io exception");
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
    
    private void parseUpdateXml(InputStream is) {    	
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			UpdateXMLHandler myXMLHandler = new UpdateXMLHandler();
			myXMLHandler.setContext(BaseActivity.this);
			xr.setContentHandler(myXMLHandler);
			xr.parse(new InputSource(is));	
		} catch (FactoryConfigurationError e) {
			ETLog.e(TAG, "FactoryConfigurationError");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			ETLog.e(TAG, "ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			ETLog.e(TAG, "SAXException");
			// when it shows "1" in xml
			updateCafeListTimeStamp = System.currentTimeMillis();
			e.printStackTrace();
		} catch (IOException e) {
			ETLog.e(TAG, "IOException");
			e.printStackTrace();
		}
    }
	
}
