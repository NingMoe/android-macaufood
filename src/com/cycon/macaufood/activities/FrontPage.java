package com.cycon.macaufood.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.PagerAdapter;
import com.cycon.macaufood.widget.VerticalViewPager;

public class FrontPage extends Activity {

	private View background;
	private VerticalViewPager pager;
	private ImageAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.front_page);

		background = findViewById(R.id.background);
		pager = (VerticalViewPager) findViewById(R.id.pager);
		adapter = new ImageAdapter(this);
		pager.setOnPageChangeListener(adapter);
		pager.setPageTransformer(true, adapter);
		pager.setAdapter(adapter);
		pager.setCurrentItem(1, false);

		Animation alphaAnim = AnimationUtils.loadAnimation(this,
				R.anim.front_page_fade_in);
		final Animation transAnim = AnimationUtils.loadAnimation(this,
				R.anim.front_page_slide_up);
		pager.startAnimation(transAnim);
		background.startAnimation(alphaAnim);

		MFService.fetchFrontPage(getApplicationContext());

	}
	
	@Override
	public void onBackPressed() {
		Animation alphaAnim = AnimationUtils.loadAnimation(this,
				R.anim.front_page_fade_out);
		final Animation transAnim = AnimationUtils.loadAnimation(this,
				R.anim.front_page_slide_down);
		transAnim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		pager.startAnimation(transAnim);
		background.startAnimation(alphaAnim);
	}

	public class ImageAdapter extends PagerAdapter implements
			VerticalViewPager.PageTransformer, VerticalViewPager.OnPageChangeListener {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			ImageView imageView = new ImageView(mContext);
			if (position == 1) {
				FileCache fileCache = new FileCache(mContext,
						ImageType.FRONTPAGE);
				Bitmap bitmap = MFUtil.getBitmapFromCache(fileCache, "1");
				imageView.setImageBitmap(bitmap);
				imageView.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {
						String frontPageLink = PreferenceHelper
								.getPreferenceValueStr(FrontPage.this,
										MFConstants.FRONT_PAGE_LINK_PREF_KEY,
										"");
						if (frontPageLink.length() > 10) {
							Intent i = new Intent(Intent.ACTION_VIEW, Uri
									.parse(frontPageLink));
							startActivity(i);
						}
					}
				});
			}
			((VerticalViewPager) container).addView(imageView,
					new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT));
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((VerticalViewPager) container).removeView((ImageView) object);
		}


		@SuppressLint("NewApi")
		@Override
		public void transformPage(View view, float position) {

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 0) { // [-1,0]
				// Use the default slide transition when moving to the left page
				view.setAlpha(1);

			} else if (position <= 1) { // (0,1]
				// Fade the page out.
				background.setAlpha(1 - position);

				if (position == 1) {
					finish();
				}

			} else { // (1,+Infinity]
				view.setAlpha(0);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int position) {
			if (Build.VERSION.SDK_INT < 11 && position == 0) {
				finish();
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			
		}
	}

}
