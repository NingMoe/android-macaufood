package com.cycon.macaufood.widget;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AdController;
import com.cycon.macaufood.utilities.AdController.AdInfo;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFUtil;

public class AdvView extends RelativeLayout {
	public interface Callback {
		void onAdLoadResultSuccess();
		void onAdLoadResultError();
	}

	private static final String TAG = "AdvViewPager";

	private static final long REFRESH_PERIOD = 7000;

	private boolean mSmallAdv;
	private ImageAdapter mImageAdapter;
	private Context mContext;
	private Timer mTimer;
	private Handler mHandler;
	private List<AdInfo> mAdInfoList;
	private AdController mAdController;
	private ImageLoader mImageLoader;
	private ViewPager mViewPager;
	private GalleryNavigator mNavi;

	private Callback mAdCallback = new Callback() {

		@Override
		public void onAdLoadResultSuccess() {
			getAdInfoAndCreateAdapter();
		}

		@Override
		public void onAdLoadResultError() {
			// TODO Auto-generated method stub

		}
	};

	public AdvView(Context context) {
		super(context);
		init(null);
	}

	public AdvView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		mContext = this.getContext();
		mAdController = AdController.getInstance(mContext
				.getApplicationContext());

		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.AdvView, 0, 0);
		mSmallAdv = a.getBoolean(R.styleable.AdvView_small, false);
		a.recycle();

		mHandler = new Handler();

		mViewPager = new ViewPager(mContext);
		mNavi = new GalleryNavigator(mContext);
//		mNavi.setVisibility(View.GONE);
//		mNavi.setVisibility(View.VISIBLE);
		addView(mViewPager);
		RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.bottomMargin = MFUtil.getPixelsFromDip(3f, getResources());
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		addView(mNavi, params);
		
		getAdInfoAndCreateAdapter();
	}
	
	private void getAdInfoAndCreateAdapter() {
		if (mSmallAdv) {
			mAdInfoList = mAdController.getSmallAdInfoList();
		} else {
			mAdInfoList = mAdController.getBigAdInfoList();
		}
		if (mAdInfoList.size() <= 1) {
			mNavi.setVisibility(View.GONE);
		} else {
			mNavi.setSize(mAdInfoList.size());
			mNavi.setVisibility(View.VISIBLE);
		}
		if (mAdInfoList.size() == 0) {
			setVisibility(View.GONE);
		} else {
			setVisibility(View.VISIBLE);
			mImageAdapter = new ImageAdapter();
			mViewPager.setAdapter(mImageAdapter);
			mViewPager.setOnPageChangeListener(mImageAdapter);

			mImageLoader = new ImageLoader(mContext, 0, ImageType.ADV);
			mImageLoader.setTaskMaxNumber(1);
			mImageLoader.setImagesToLoadFromAdInfoList(mAdInfoList);
		}
	}

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			if (mAdInfoList.size() == 0) {
			}
			int cur = mViewPager.getCurrentItem();
			mViewPager.setCurrentItem(cur == mAdInfoList.size() - 1 ? 0 : cur + 1, true);
		}
	};

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

		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				startTimer();
			} else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
				stopTimer();
			}
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
		super.onAttachedToWindow();
		if (isShown()) {
//			startTimer();
		}
		AdController.getInstance(mContext.getApplicationContext())
				.registerCallback(mAdCallback);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopTimer();
		AdController.getInstance(mContext.getApplicationContext())
				.unregisterCallback(mAdCallback);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
			startTimer();
		} else {
			stopTimer();
		}
	}

}
