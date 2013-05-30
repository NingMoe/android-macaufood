package com.cycon.macaufood.widget;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.MFConfig;

public class AdvView extends ImageView {
	
	private static final String TAG = AdvView.class.getName();
	private static final long REFRESH_TIME = 7000;
	private Context mContext;
	private boolean isSmallAdv;
	private static String linkId;
	private FetchAdvTask advTask;
	private static Bitmap cacheAdv;
	private View loadingAdv;
	
	public AdvView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;	
//		setScaleType(ScaleType.FIT_XY);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AdvView, 0, 0);
		isSmallAdv = a.getBoolean(R.styleable.AdvView_small, true);
		
//		if (isSmallAdv && cacheAdv == null) setVisibility(View.GONE);
		
		if (isSmallAdv)
			setScaleType(ScaleType.FIT_XY);
		else {
			setScaleType(ScaleType.FIT_CENTER);
		}
		
		setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (linkId == null || linkId == "") return;
				String url = "http://www.cycon.com.mo/xml_advclick.php?id= " + linkId + "&code=" + MFConfig.DEVICE_ID;
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				mContext.startActivity(myIntent);
			}
		});
	}
	
	public void setLoadingAdv(View loadingAdv) {
		this.loadingAdv = loadingAdv;
	}
	
	public void unbind() {
		setImageBitmap(null);
	}
	
	public void stopTask() {
		if (advTask != null)
			advTask.cancel(true);
	}
	
	public void startTask() {
		if (cacheAdv != null && loadingAdv != null) loadingAdv.setVisibility(View.GONE); 
		if (isSmallAdv)
			setImageBitmap(cacheAdv);
		stopTask();
		advTask = new FetchAdvTask();
		AsyncTaskHelper.execute(advTask, isSmallAdv && cacheAdv != null);
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
    		if (isCancelled()) return null;

    		if (!MFConfig.isOnline(mContext)) return null;
    		
    		String linkIdUrl = "http://www.cycon.com.mo/xml_adv.php?code=android-" + MFConfig.DEVICE_ID + 
    				"&type=" + (isSmallAdv ? "s" : "b");

            try {
            	HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(linkIdUrl);
            	HttpResponse response = client.execute(request);
            	BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
            	tempLinkId = rd.readLine().trim();
				if (tempLinkId == null || tempLinkId.equals("")) return null;
				
				try {
					Integer.parseInt(tempLinkId);
				} catch (Exception e) {
					return null;
				}
            	
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				Log.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
            
            
    		
    		String urlStr = "http://www.cycon.com.mo/appimages/adv_rotate_banner/" + tempLinkId + ".jpg";
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
				return BitmapFactory.decodeStream(new FlushedInputStream(is));
				
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			} 
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Bitmap result) {
    		super.onPostExecute(result);
//    		ETLog.e(TAG, "getAdv");
    		setVisibility(View.VISIBLE);
    		linkId = tempLinkId;
    		if (result == null) {
    			if (isSmallAdv && cacheAdv == null || !isSmallAdv  && getDrawable() == null) {
    				if (loadingAdv != null)
    					loadingAdv.setVisibility(View.GONE);
    				setImageResource(isSmallAdv ? R.drawable.adv2 : R.drawable.searchadv);
    			}
    		}
    		else {
    			if (loadingAdv != null)
    				loadingAdv.setVisibility(View.GONE);
    			setImageBitmap(result);
    			if (isSmallAdv)
    				cacheAdv = result;
    		}
    		
    		advTask = new FetchAdvTask();
    		AsyncTaskHelper.execute(advTask, true);
    	}
    }
    
    
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                      int bytes = read();
                      if (bytes < 0) {
                          break;  // we reached EOF
                      } else {
                          bytesSkipped = 1; // we read one byte
                      }
               }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

}
