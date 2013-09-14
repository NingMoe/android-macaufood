package com.cycon.macaufood.activities;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;

public abstract class BaseActivity extends SherlockActivity {
	
	private static final String TAG = "BaseActivity";
	protected ActionBar mActionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			//this order is critical, dont change
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			return;
		} 
		
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	private TextView getActionbarTitle() {

	    TextView title = (TextView) findViewById(R.id.abs__action_bar_title);// for ActionBarSerlock
	    if (title == null) title = (TextView) findViewById(Resources.getSystem()
	            .getIdentifier("action_bar_title", "id", "android"));// for default action bar

	    return title;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MFService.checkUpdate(getApplicationContext());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, Home.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	
	}
    

	
}
