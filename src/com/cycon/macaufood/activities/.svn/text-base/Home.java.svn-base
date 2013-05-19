package com.cycon.macaufood;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.cycon.macaufood.utilities.ETLog;

public class Home extends TabActivity {
	
	private static final String TAG = Home.class.getName();
	
	private TabHost tabHost;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setWindowAnimations(android.R.style.Animation);
        
//        checkLandscape();
        
        ETLog.e(TAG, "---onCreate");
    	
        tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getResources().getString(R.string.tablabel1), 
                		getResources().getDrawable(R.drawable.tab1))
                .setContent(new Intent(this, Recommend.class)));
        
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getResources().getString(R.string.tablabel2), 
                		getResources().getDrawable(R.drawable.tab2))
                .setContent(new Intent(this, Coupon.class)));
        
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator(getResources().getString(R.string.tablabel3), 
                		getResources().getDrawable(R.drawable.tab3))
                .setContent(new Intent(this, Search.class)));
        
        tabHost.addTab(tabHost.newTabSpec("tab4")
                .setIndicator(getResources().getString(R.string.tablabel4), 
                		getResources().getDrawable(R.drawable.tab4))
                .setContent(new Intent(this, Map.class)));
        
        tabHost.addTab(tabHost.newTabSpec("tab5")
                .setIndicator(getResources().getString(R.string.tablabel5), 
                		getResources().getDrawable(R.drawable.tab5))
                .setContent(new Intent(this, Favorite.class)));
    }
    
    public void visibleTabs(){
        tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildAt(1).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildAt(3).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildAt(4).setVisibility(View.VISIBLE);

    }

    public void invisibleTabs(){
//    	Animation animation = AnimationUtils.loadAnimation(Home.this,
//				R.anim.push_bottom_in);
//
//    	tabHost.getTabWidget().startAnimation(animation);
        tabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildAt(1).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildAt(3).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildAt(4).setVisibility(View.GONE);
    }
    
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//		ETLog.e("ZZZ", "here1");
//    	if (keyCode == KeyEvent.KEYCODE_BACK) {
//    		ETLog.e("ZZZ", "here");
//        	visibleTabs();
//    	}
//    	return super.onKeyDown(keyCode, event);
//    }
    
    @Override
    protected void onStart() {
        ETLog.e(TAG, "---onStart");
    	super.onStart();
    }
    
    @Override
    protected void onResume() {
        ETLog.e(TAG, "---onResume");
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
        ETLog.e(TAG, "---onPause");
    	super.onPause();
    }
    
    @Override
    protected void onStop() {
        ETLog.e(TAG, "---onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        ETLog.e(TAG, "---onDestroy");
    	super.onDestroy();
//		Intent i = new Intent(this, SplashScreen.class);
//		startActivity(i);
//    	Process.killProcess(Process.myPid()); 
    }
    
    
//    private void checkLandscape() {
//    	if (Config.getInstance().isLandscape(this)) {
//    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    	}
//    	else {
//    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    	}
//    }
    
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//    	// TODO Auto-generated method stub
//    	super.onConfigurationChanged(newConfig);
//    	checkLandscape();
//
//    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	// TODO Auto-generated method stub
    	super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	return super.onOptionsItemSelected(item);
    }
    
    
}