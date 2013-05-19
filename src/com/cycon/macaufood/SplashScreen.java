package com.cycon.macaufood;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Window;

import com.cycon.dbaccess.LocalDaoManager;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.UpdateXMLHandler;

public class SplashScreen extends Activity {
	
	private static final String TAG = "SplashScreen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String id = Config.getPreferenceValueStr(SplashScreen.this, "deviceId", "");
		if (id.equals("")) {
			Config.DEVICE_ID = UUID.randomUUID().toString().replaceAll("-", "");
			Config.savePreferencesStr(SplashScreen.this, "deviceId", Config.DEVICE_ID);
		} else {
			Config.DEVICE_ID = id;
		}
		
		String originalVersion = Config.getPreferenceValueStr(SplashScreen.this, "versionNo", "");
		if (!originalVersion.equals(getString(R.string.versionNo))) {
			Config.savePreferencesBoolean(SplashScreen.this, "disclaimerDialog", true);
			Config.savePreferencesBoolean(SplashScreen.this, "firstLaunch", true);
			Config.savePreferencesStr(SplashScreen.this, "versionNo", getString(R.string.versionNo));
			Config.savePreferencesStr(SplashScreen.this, "cafe_version_update", Config.cafe_version_update);
		}
		
		Config.cafe_version_update = Config.getPreferenceValueStr(SplashScreen.this, "cafe_version_update" ,Config.cafe_version_update);
		
		setContentView(R.layout.splash_screen);
		new ParseXmlTask().execute();
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Config.deviceWidth = dm.widthPixels;
	}
	
	private void parseFavoriteList() {
		Config.getInstance().getFavoriteLists().clear();
		SharedPreferences prefs = getSharedPreferences(
				"macaufood.preferences", 0);
		String str = prefs.getString("favorites", "");
		if (str.equals("")) return;
		String[] list = str.split(",");
		
		for (int i = 0; i < list.length; i++) {
			Config.getInstance().getFavoriteLists().add(list[i]);
		}
		
	}
	
	private class ParseXmlTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			if (Config.getPreferenceValueBoolean(SplashScreen.this, "firstLaunch", true)) {
				Config.initDataFromXml(getResources(), SplashScreen.this);
				LocalDaoManager.getInstance(getApplicationContext()).clearTable();
				LocalDaoManager.getInstance(getApplicationContext()).insertCafeLists();
				Config.savePreferencesBoolean(SplashScreen.this, "firstLaunch", false);
			} else {
				LocalDaoManager.getInstance(getApplicationContext()).getCafeListFromDB();
			}
			

//			updateCafeData();
//			setBranchtoMap();
			parseFavoriteList();
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			finish();
			
			Intent i = new Intent(SplashScreen.this, Home.class);
			startActivity(i);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {	   
	    	new AlertDialog.Builder(this)
			.setMessage("你確定要退出程式嗎?      ")
			.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
					    	Process.killProcess(Process.myPid());   
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
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	
//	private void updateCafeData() {
//		FileCache fileCache=new FileCache(this, ImageType.REGULAR);
//		File f=fileCache.getFile(BaseActivity.UPDATE_FILE_STR);
//		try {
//			FileInputStream is = new FileInputStream(f);
//			parseUpdateXml(is);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
	
//    private void parseUpdateXml(InputStream is) {    	
//    	try {
//			SAXParserFactory spf = SAXParserFactory.newInstance();
//			SAXParser sp = spf.newSAXParser();
//			XMLReader xr = sp.getXMLReader();
//			UpdateXMLHandler myXMLHandler = new UpdateXMLHandler();
//			xr.setContentHandler(myXMLHandler);
//			xr.parse(new InputSource(is));	
//		} catch (FactoryConfigurationError e) {
//			ETLog.e(TAG, "FactoryConfigurationError");
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			ETLog.e(TAG, "ParserConfigurationException");
//			e.printStackTrace();
//		} catch (SAXException e) {
//			ETLog.e(TAG, "SAXException");
//			e.printStackTrace();
//		} catch (IOException e) {
//			ETLog.e(TAG, "IOException");
//			e.printStackTrace();
//		}
//    }
}
