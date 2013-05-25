package com.cycon.macaufood.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFRequestHelper;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class Home extends SherlockFragmentActivity {

	private static final String TAG = Home.class.getName();

	private TabHost tabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.home);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mTabsAdapter.addTab(bar.newTab().setText(R.string.recommend_tab),
				Recommend.class, null);
		mTabsAdapter.addTab(bar.newTab().setText(R.string.coupon_tab),
				Coupon.class, null);
//		mTabsAdapter.addTab(bar.newTab().setText(R.string.foodNews),
//				FoodNews.class, null);

		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
		if (PreferenceHelper.getPreferenceValueBoolean(this, "disclaimerDialog", true)) {

			TextView text = new TextView(this);
			text.setTextSize(15);
			text.setPadding(20, 5, 20, 0);
			text.setText(R.string.disclaimerText);
			text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.disclaimer))
			.setView(text)
			.setPositiveButton("�?��?以上�?款", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					PreferenceHelper.savePreferencesBoolean(Home.this, "disclaimerDialog", false);
//					if (MFConfig.isOnline(this) && MFConfig.getInstance().getRecommendCafeList().size() == 0) {
//						pDialog = ProgressDialog.show(this, null,
//								"載入資料中...", false, true);
//					}
				}
			}).show();
			
		}
		
		MFRequestHelper.sendFavoriteLog(getApplicationContext());

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
						}@Override
						protected void finalize() throws Throwable {
							// TODO Auto-generated method stub
							super.finalize();
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
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.main_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
    	switch (item.getItemId()) {
		case R.id.menu_search:
			i = new Intent(this, Search.class);
			startActivity(i);
			return true;
		case R.id.menu_map:
			i = new Intent(this, Map.class);
			startActivity(i);
			return true;
		case R.id.menu_favorite_list:
			i = new Intent(this, Favorite.class);
			startActivity(i);
			return true;
		case R.id.menu_refresh:
			refresh();
			return true;
		case R.id.menu_about:
			i = new Intent(this, About.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
    	}
    }
    
    public void refresh() {
    	//TODO

		MFRequestHelper.checkUpdate(getApplicationContext());
    };

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
	}

	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = activity.getSupportActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}
	}

}