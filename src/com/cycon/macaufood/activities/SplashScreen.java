package com.cycon.macaufood.activities;

import java.util.UUID;

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

import com.cycon.macaufood.R;
import com.cycon.macaufood.sqlite.LocalDbManager;
import com.cycon.macaufood.utilities.MFConfig;

public class SplashScreen extends Activity {
	
	private static final String TAG = "SplashScreen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String id = MFConfig.getPreferenceValueStr(SplashScreen.this, "deviceId", "");
		if (id.equals("")) {
			MFConfig.DEVICE_ID = UUID.randomUUID().toString().replaceAll("-", "");
			MFConfig.savePreferencesStr(SplashScreen.this, "deviceId", MFConfig.DEVICE_ID);
		} else {
			MFConfig.DEVICE_ID = id;
		}
		
		String originalVersion = MFConfig.getPreferenceValueStr(SplashScreen.this, "versionNo", "");
		if (!originalVersion.equals(getString(R.string.versionNo))) {
			MFConfig.savePreferencesBoolean(SplashScreen.this, "disclaimerDialog", true);
			MFConfig.savePreferencesBoolean(SplashScreen.this, "firstLaunch", true);
			MFConfig.savePreferencesStr(SplashScreen.this, "versionNo", getString(R.string.versionNo));
			MFConfig.savePreferencesStr(SplashScreen.this, "cafe_version_update", MFConfig.cafe_version_update);
		}
		
		MFConfig.cafe_version_update = MFConfig.getPreferenceValueStr(SplashScreen.this, "cafe_version_update" ,MFConfig.cafe_version_update);
		
		setContentView(R.layout.splash_screen);
		new ParseXmlTask().execute();
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		MFConfig.deviceWidth = dm.widthPixels;
	}
	
	private void parseFavoriteList() {
		MFConfig.getInstance().getFavoriteLists().clear();
		SharedPreferences prefs = getSharedPreferences(
				"macaufood.preferences", 0);
		String str = prefs.getString("favorites", "");
		if (str.equals("")) return;
		String[] list = str.split(",");
		
		for (int i = 0; i < list.length; i++) {
			MFConfig.getInstance().getFavoriteLists().add(list[i]);
		}
		
	}
	
	private class ParseXmlTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			if (MFConfig.getPreferenceValueBoolean(SplashScreen.this, "firstLaunch", true)) {
				MFConfig.initDataFromXml(getResources(), SplashScreen.this);
				LocalDbManager.getInstance(getApplicationContext()).clearTable();
				LocalDbManager.getInstance(getApplicationContext()).insertCafeLists();
				MFConfig.savePreferencesBoolean(SplashScreen.this, "firstLaunch", false);
			} else {
				LocalDbManager.getInstance(getApplicationContext()).getCafeListFromDB();
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
			.setMessage("你確定�?退出程�?嗎?      ")
			.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
					    	Process.killProcess(Process.myPid());   
						}
					})
			.setNegativeButton("�?�消",
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
