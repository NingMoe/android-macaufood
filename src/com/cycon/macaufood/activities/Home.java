package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.AdvView;

public class Home extends SherlockFragmentActivity {

	private static final String TAG = Home.class.getName();

	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 48; // 48 hours

	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private AdvView banner;
	private View loadingAdv;
	private ProgressDialog pDialog;
	private long dataTimeStamp;
	private boolean isShowingDisclaimer;
	private static boolean showFrontPage = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			Intent i = new Intent(this, SplashScreen.class);
			startActivity(i);
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			return;
		} 

		getWindow().setWindowAnimations(android.R.style.Animation);
		setContentView(R.layout.home);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		loadingAdv = findViewById(R.id.loadingAdv);
		banner = (AdvView) findViewById(R.id.banner);
		banner.setLoadingAdv(loadingAdv);

		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mTabsAdapter.addTab(bar.newTab().setText(R.string.recommend_tab),
				Recommend.class, null);
		mTabsAdapter.addTab(bar.newTab().setText(R.string.coupon_tab),
				Coupon.class, null);
		mTabsAdapter.addTab(bar.newTab().setText(R.string.foodNews),
				FoodNews.class, null);
//		mTabsAdapter.addTab(bar.newTab().setText(R.string.photoShare),
//				PhotoShare.class, null);

		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}

		if (PreferenceHelper.getPreferenceValueBoolean(this,
				MFConstants.SHOW_DISCLAIMER_PREF_KEY, true)) {
			
			isShowingDisclaimer = true;

			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.disclaimer)
					.setMessage(R.string.disclaimerText)
					.setCancelable(false)
					.setPositiveButton(getString(R.string.agreeDisclaimer),
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									isShowingDisclaimer = false;
									dialog.dismiss();
									PreferenceHelper
											.savePreferencesBoolean(
													Home.this,
													MFConstants.SHOW_DISCLAIMER_PREF_KEY,
													false);
									//refresh data only when user click agree disclaimer
									if (System.currentTimeMillis() - dataTimeStamp > REFRESH_TIME_PERIOD)
										refresh();
								}
							}).show();
			
			
			TextView textView = (TextView) dialog.findViewById(android.R.id.message);
			textView.setTextSize(15);

		}

		MFService.sendFavoriteLog(getApplicationContext());
		
        dataTimeStamp = PreferenceHelper.getPreferenceValueLong(getApplicationContext(), MFConstants.TIME_STAMP_PREF_KEY, 0);
        
        if (showFrontPage) {
			FileCache fileCache = new FileCache(Home.this, ImageType.FRONTPAGE);
			Bitmap bitmap = MFUtil.getBitmapFromCache(fileCache, "1");
			if (bitmap != null) {
				new Handler().postDelayed(new Runnable() {
					public void run() {
							Intent i = new Intent(Home.this, FrontPage.class);
							startActivity(i);
							overridePendingTransition(R.anim.front_page_fade_in, 0);
							showFrontPage = false;
							//fetch new front page after show front page
							MFService.fetchFrontPage(getApplicationContext());
					}
				}, 1500);
			} else {
				MFService.fetchFrontPage(getApplicationContext());
			}
        }
	}
	
	private void showDisclaimerDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle(R.string.disclaimer)
		.setMessage(R.string.disclaimerText)
		.setCancelable(false)
		.setPositiveButton(getString(R.string.agreeDisclaimer),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();

		TextView textView = (TextView) dialog.findViewById(android.R.id.message);
		textView.setTextSize(15);
	}
	
//	public int getCurrentFragmentIndex() {
//		return mViewPager.getCurrentItem();
//	}
	
	public void setDataTimeStamp(long dataTimeStamp) {
		this.dataTimeStamp = dataTimeStamp;
	}
	
	public long getDataTimeStamp() {
		return dataTimeStamp;
	}
	
	public boolean isShowingDisClaimer() {
		return isShowingDisclaimer;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (System.currentTimeMillis() - dataTimeStamp > REFRESH_TIME_PERIOD && !isShowingDisclaimer)
			refresh();
		if (banner != null)
			banner.startTask();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (banner != null)
			banner.stopTask();
	}
	
	public void showProgressDialog() {
		pDialog = ProgressDialog.show(this, null,
				getString(R.string.loadingContent), false, true);
	}
	
	public void hideProgressDialog() {
		pDialog.dismiss();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			new AlertDialog.Builder(this)
					.setMessage(getString(R.string.exitProgramPrompt))
					.setPositiveButton(getString(R.string.confirmed),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
									Process.killProcess(Process.myPid());
								}

								@Override
								protected void finalize() throws Throwable {
									// TODO Auto-generated method stub
									super.finalize();
								}
							})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							}).show();
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
			if (!MFConfig.isOnline(this)) {
				Toast.makeText(this, getString(R.string.noInternetMsg), Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.menu_wifi:
			i = new Intent(this, MacauWifi.class);
			startActivity(i);
			return true;
		case R.id.menu_disclaimer:
			showDisclaimerDialog();
			return true;
		case R.id.menu_about:
			i = new Intent(this, About.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public Fragment[] getFragments() {
		Fragment[] fragments = new Fragment[3];
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = mTabsAdapter.getActiveFragment(mViewPager, i);
		}
		return fragments;
	}

	public void refresh() {
		if (MFConfig.isOnline(this)) {
			MFFetchListHelper.fetchAllList(this);
			MFService.checkUpdate(getApplicationContext());
		} 
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final FragmentManager mFragmentManager;
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
			mFragmentManager = activity.getSupportFragmentManager();
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
		
		private void resetListViewAnimation(int pos) {

			Fragment fragment = getActiveFragment(mViewPager, pos);
			if (fragment instanceof Recommend) {
				((Recommend) fragment).resetListViewAnimation();
			} else if (fragment instanceof Coupon) {
				((Coupon) fragment).resetListViewAnimation();
			} else if (fragment instanceof FoodNews) {
				((FoodNews) fragment).resetListViewAnimation();
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
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
					resetListViewAnimation(i);
				}
			}

		}

		public Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

	}

}