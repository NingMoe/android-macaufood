package com.cycon.macaufood.activities;

import java.io.File;
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
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.sqlite.LocalDbManager;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class SplashScreen extends Activity {
	
	private static final String TAG = "SplashScreen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String id = PreferenceHelper.getPreferenceValueStr(SplashScreen.this, "deviceId", "");
		if (id.equals("")) {
			MFConfig.DEVICE_ID = UUID.randomUUID().toString().replaceAll("-", "");
			PreferenceHelper.savePreferencesStr(SplashScreen.this, "deviceId", MFConfig.DEVICE_ID);
		} else {
			MFConfig.DEVICE_ID = id;
		}
		
		String originalVersion = PreferenceHelper.getPreferenceValueStr(SplashScreen.this, "versionNo", "");
		if (!originalVersion.equals(getString(R.string.versionNo))) {
			PreferenceHelper.savePreferencesBoolean(SplashScreen.this, "firstLaunch", true);
			PreferenceHelper.savePreferencesStr(SplashScreen.this, "versionNo", getString(R.string.versionNo));
			PreferenceHelper.savePreferencesStr(SplashScreen.this, "cafe_version_update", MFConfig.cafe_version_update);
			PreferenceHelper.savePreferencesLong(SplashScreen.this, MFConstants.TIME_STAMP_PREF_KEY, 0); //refresh main page after update?
			
			//clear cache for first launch regardless of version (uncomment in version after 3.0)
//			if (originalVersion.length() > 0) {
//				int versionHeadNumber = originalVersion.charAt(0) - '0';
//		    	//clear in ext file / cache file for version below 3.0
//				if (versionHeadNumber < 3) {
					clearFileInSd();
//				}
//			}
             //uncomment in future version to clear profile images and figure a way to update
					
             //clearProfileImages();		
		}
		
		MFConfig.cafe_version_update = PreferenceHelper.getPreferenceValueStr(SplashScreen.this, "cafe_version_update" ,MFConfig.cafe_version_update);
		
		setContentView(R.layout.splash_screen);

		AsyncTaskHelper.execute(new ParseXmlTask());
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		MFConfig.deviceWidth = dm.widthPixels;
		MFConfig.deviceHeight = dm.heightPixels;
	}
	
	private void clearProfileImages() {
		FileCache cacheFile = new FileCache(this, ImageType.PSLOCALAVATAR);
		cacheFile.clear();
	}
	
	private void clearFileInSd() {
		File rootDir = null;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	rootDir=new File(android.os.Environment.getExternalStorageDirectory(),"MacauFood");
        	deleteRecursive(rootDir);
        } else {
        	rootDir = getFilesDir();
        	deleteRecursive(rootDir);
        }
        if (rootDir != null) {
        	rootDir.delete();
        }
	}
	
	private void deleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	        	deleteRecursive(child);

	    fileOrDirectory.delete();
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

			if (PreferenceHelper.getPreferenceValueBoolean(SplashScreen.this, "firstLaunch", true)) {
				MFConfig.initDataFromXml(getResources(), SplashScreen.this);
				LocalDbManager.getInstance(getApplicationContext()).clearTable();
				LocalDbManager.getInstance(getApplicationContext()).insertCafeLists();
				PreferenceHelper.savePreferencesBoolean(SplashScreen.this, "firstLaunch", false);
			} else {
				LocalDbManager.getInstance(getApplicationContext()).getCafeListFromDB();
			}
			
			parseFavoriteList();
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			finish();
			
			Intent i = new Intent(SplashScreen.this, Home.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(i);
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {	   
	    	new AlertDialog.Builder(this)
			.setMessage(getString(R.string.exitProgramPrompt))
			.setPositiveButton(getString(R.string.confirmed),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
					    	Process.killProcess(Process.myPid());   
						}
					})
			.setNegativeButton(getString(R.string.cancel),
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
}
