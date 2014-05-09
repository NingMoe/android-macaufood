package com.cycon.macaufood.widget;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AdController;
import com.cycon.macaufood.utilities.AdController.AdInfo;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFServiceCallBack;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;

public class AdvViewPager extends ViewPager {
	public interface Callback {
		void onAdLoadResultSuccess();
		void onAdLoadResultError();
	}

	private static final String TAG = "AdvViewPager";

	private static final long REFRESH_PERIOD = 6000;

	private boolean mSmallAdv;
	private ImageAdapter mImageAdapter;
	private Context mContext;
	private GalleryNavigator mNavi;
	private Timer mTimer;
	private Handler mHandler;
	private View loadingLayout;
	private List<AdInfo> mAdInfoList;
	private AdController mAdController;
	private ImageLoader mImageLoader;

	private Callback mAdCallback = new Callback() {

		@Override
		public void onAdLoadResultSuccess() {
			if (mSmallAdv) {
				mAdInfoList = mAdController.getSmallAdInfoList();
			} else {
				mAdInfoList = mAdController.getBigAdInfoList();
			}
			mImageAdapter.notifyDataSetChanged();
		}

		@Override
		public void onAdLoadResultError() {
			// TODO Auto-generated method stub

		}
	};

	public AdvViewPager(Context context) {
		super(context);
		init(null);
	}

	public AdvViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		mContext = this.getContext();
		mAdController = AdController.getInstance(mContext
				.getApplicationContext());

		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.AdvViewPager, 0, 0);
		mSmallAdv = a.getBoolean(R.styleable.AdvViewPager_small, false);
		a.recycle();

		mHandler = new Handler();

		if (mSmallAdv) {
			mAdInfoList = mAdController.getSmallAdInfoList();
		} else {
			mAdInfoList = mAdController.getBigAdInfoList();
		}

		// normally would not happen as splash screen already request Ad
		// in case connection is very slow
		if (mAdInfoList.size() == 0) {
			// show connection error/loading
		} else {
			mImageAdapter = new ImageAdapter();
			setAdapter(mImageAdapter);
			setOnPageChangeListener(mImageAdapter);

			mImageLoader = new ImageLoader(mContext, 0, ImageType.ADV);
			mImageLoader.setTaskMaxNumber(1);
			mImageLoader.setImagesToLoadFromAdInfoList(mAdInfoList);
		}
	}

	public void setNavi(GalleryNavigator navi) {
		this.mNavi = navi;
		navi.setSize(mAdInfoList.size());
		navi.setVisibility(View.GONE);
		navi.setVisibility(View.VISIBLE);
	}

	public void setLoadingLayout(View loadingLayout) {
		this.loadingLayout = loadingLayout;
		loadingLayout.setVisibility(View.GONE);
	}

	public void setLoadingText(TextView tv) {

	}

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			if (mAdInfoList.size() == 0) {
			}
			int cur = getCurrentItem();
			setCurrentItem(cur == mAdInfoList.size() - 1 ? 0 : cur + 1, true);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			stopTimer();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
//			startTimer();
		}

		return super.onTouchEvent(event);
	}

	public class ImageAdapter extends PagerAdapter implements
			ViewPager.OnPageChangeListener {

		public int getCount() {
			return mAdInfoList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			MFLog.e(TAG, "view pager instantiate pos = " + position);
			ImageView i = new ImageView(mContext);
			mImageLoader.displayImage(mAdInfoList.get(position).advId, i,
					position);
			i.setScaleType(mSmallAdv ? ScaleType.FIT_XY : ScaleType.FIT_END);
			i.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					if (mAdInfoList.size() != 0) {
						Intent i = new Intent(Intent.ACTION_VIEW, Uri
								.parse(mAdInfoList.get(position).advLink));
						mContext.startActivity(i);
					}
				}
			});
			((ViewPager) container).addView(i, 0);
			return i;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		public void onPageSelected(int position) {
			if (mNavi != null) {
				mNavi.setPosition(position);
				mNavi.invalidate();
			}
		}

	}

	public void startTimer() {
		stopTimer();
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHandler.post(mUpdateResults);
			}
		}, REFRESH_PERIOD, REFRESH_PERIOD);

	}

	public void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		Log.e("ZZZ", "onAttachedtowindow");
		super.onAttachedToWindow();
		if (isShown()) {
//			startTimer();
		}
		AdController.getInstance(mContext.getApplicationContext())
				.registerCallback(mAdCallback);
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.e("ZZZ", "onDetachedtowindow");
		super.onDetachedFromWindow();
		stopTimer();
		AdController.getInstance(mContext.getApplicationContext())
				.unregisterCallback(mAdCallback);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
//			startTimer();
		} else {
			stopTimer();
		}
	}

}
