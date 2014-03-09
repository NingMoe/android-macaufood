package com.cycon.macaufood.widget;

import android.content.Context;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.cycon.macaufood.R;

@Deprecated
public class TouchImageView extends ImageView implements OnTouchListener{
	
	private Runnable mSetColorFilterRunnable;
	private Runnable mClearColorFilterRunnable;

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnTouchListener(this);
		
		new TouchImageViewHelper(context.getApplicationContext());
		
		mSetColorFilterRunnable = new Runnable() {
			
			public void run() {
				if (getDrawable() != null) {
					getDrawable().setColorFilter(TouchImageViewHelper.mImageFilter);
				}
			}
		};
		mClearColorFilterRunnable = new Runnable() {
			
			public void run() {
				if (getDrawable() != null) {
					getDrawable().clearColorFilter();
				}
			}
		};
		
	}


	public void onClick() {
		// TODO Auto-generated method stub
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			postDelayed(mSetColorFilterRunnable, 100);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			post(mSetColorFilterRunnable);
			postDelayed(mClearColorFilterRunnable, 500);
			onClick();
		} else if (
				event.getAction() == MotionEvent.ACTION_OUTSIDE ||
				event.getAction() == MotionEvent.ACTION_CANCEL) {
			removeCallbacks(mSetColorFilterRunnable);
			post(mClearColorFilterRunnable);
		}
		return true;
	}
	
	private static class TouchImageViewHelper {
		private static PorterDuffColorFilter mImageFilter;
		
		TouchImageViewHelper(Context context) {
			if (mImageFilter == null) {
				int selectedImageMaskColor = context.getResources().getColor(R.color.holo_blue_light);
		        mImageFilter = new PorterDuffColorFilter(selectedImageMaskColor,
		        		android.graphics.PorterDuff.Mode.SRC_ATOP);
			}
		}
	}

	
	
}
