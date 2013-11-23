package com.cycon.macaufood.widget;

import java.io.IOException;
import java.net.MalformedURLException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;

public class BannerView extends FrameLayout {
	
	private static final String TAG = BannerView.class.getName();
	private static final long REFRESH_TIME = 2000;
	private Context mContext;
	private static String linkId;
	private FetchAdvTask advTask;
	private static Bitmap cacheAdv;
	private View loadingAdv;
	private ImageView bannerImageView;
	
	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;	
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.banner_layout, this, true);
		bannerImageView = (ImageView) findViewById(R.id.bannerImageView);
		loadingAdv = findViewById(R.id.loadingAdv);
		
		bannerImageView.setScaleType(ScaleType.FIT_XY);
		
		setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (linkId == null || linkId == "") return;
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(MFURL.CLICK_ADV, linkId, MFConfig.DEVICE_ID)));
				mContext.startActivity(myIntent);
			}
		});
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (isShown()) {
			startTask();
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopTask();
		unbind();
	}
	
	public void unbind() {
		bannerImageView.setImageBitmap(null);
	}
	
	public void stopTask() {
		if (advTask != null) {
			advTask.cancel(true);
		}
	}
	
	public void startTask() {
		if (cacheAdv != null && loadingAdv != null) loadingAdv.setVisibility(View.GONE); 
		bannerImageView.setImageBitmap(cacheAdv);
		
		stopTask();
		advTask = new FetchAdvTask();
		AsyncTaskHelper.execute(advTask, cacheAdv != null);
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
			startTask();
		} else {
			stopTask();
		}
	}
	
	
    private class FetchAdvTask extends AsyncTask<Boolean, Void, Bitmap> {
    	
    	private String tempLinkId;
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    	}
    	@Override
    	protected Bitmap doInBackground(Boolean... delayed) {
    		
    		if (delayed[0])
				try {
					Thread.sleep(REFRESH_TIME);
				} catch (InterruptedException e1) {
				}
    		if (isCancelled() && cacheAdv != null) return null;

    		if (!MFConfig.isOnline(mContext)) return null;

            try {
            	Log.e("ZZZ", "send adv service");
            	tempLinkId = MFService.getString(MFURL.SMALL_ADV, null);
            	// no need fetch image if same id
            	if (tempLinkId.equals(linkId)) return null;
				if (tempLinkId == null || tempLinkId.equals("")) return null;
				
				try {
					Integer.parseInt(tempLinkId);
				} catch (Exception e) {
					return null;
				}
            	
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				MFLog.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
            
            if (isCancelled()) return null;
    		
            try {
            	Log.e("ZZZ", "fetch bitmap");
            	Bitmap bitmap = MFService.getBitmap(MFURL.getImageUrl(ImageType.ADV, tempLinkId), null);
            	
	    		linkId = tempLinkId;
				cacheAdv = bitmap;
				
				return bitmap;
				
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				MFLog.e(TAG, "io exception");
				e.printStackTrace();
			} 
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Bitmap result) {
    		super.onPostExecute(result);
    		setVisibility(View.VISIBLE);
    		if (result == null) {
    			if (cacheAdv == null) {
    				if (loadingAdv != null)
    					loadingAdv.setVisibility(View.GONE);
    				bannerImageView.setImageResource(R.drawable.adv2);
    			}
    		}
    		else {
    			if (loadingAdv != null)
    				loadingAdv.setVisibility(View.GONE);
    			bannerImageView.setImageBitmap(result);
    		}
    		
    		advTask = new FetchAdvTask();
    		AsyncTaskHelper.execute(advTask, true);
    	}
    }
    

}
