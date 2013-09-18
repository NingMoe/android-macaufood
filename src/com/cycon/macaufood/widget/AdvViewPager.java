package com.cycon.macaufood.widget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import com.cycon.macaufood.utilities.MFLog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;

public class AdvViewPager extends ViewPager {
	
	private static final String TAG = "AdvFlingGallery";
	private static final String ADV_ID_LIST = "adv_id_list";

	private static final long REFRESH_PERIOD = 6000;
	
	private ImageAdapter imageAdapter = new ImageAdapter();
	private Context mContext;
	private ArrayList<String> idList = new ArrayList<String>();

	public List<Bitmap> imageList = Collections.synchronizedList(new ArrayList<Bitmap>());
	public List<String> linkIdList = Collections.synchronizedList(new ArrayList<String>());
	
	private GalleryNavigator navi;
	private Timer timer;
	private Handler mHandler;
	private Drawable noadv;
	private boolean isFetchingId;
	private FileCache fileCache;
	private boolean isUsingCache;
	private View loadingLayout;

	public AdvViewPager(Context context) {
		super(context);
		init();
	}
	
	public AdvViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		
		mHandler = new Handler();
		noadv = getContext().getResources().getDrawable(R.drawable.searchadv);
		mContext = this.getContext();
		setOnPageChangeListener(imageAdapter);
		
		fileCache = new FileCache(mContext, ImageType.ADV);
		
		
		String advListStr = MFUtil.getStringFromCache(fileCache, ADV_ID_LIST);
		
		if (advListStr != null) {
			String[] advIdList = advListStr.split(",");
        	for (String id : advIdList) {

        		if (!id.equals("")) {
        			Integer.parseInt(id);
        			//to make sure the adv is in random order
        			Random rand = new Random(); 
        			boolean randomValue = rand.nextBoolean();
        			if (randomValue) {
        				linkIdList.add(id);
        			} else {
        				linkIdList.add(0, id);
        			}
        		}
        	}
        	
        	for (int i = 0; i < linkIdList.size(); i++) {
        		Bitmap bm = MFUtil.getBitmapFromCache(fileCache, linkIdList.get(i));
        		if (bm != null) {
        			imageList.add(bm);
        		}
			}
		}
        	
		if (imageList.size() > 0) {
			isUsingCache = true;
    		
    		setAdapter(imageAdapter);
    		
    		startTimer();
		}
		
		
		if (MFConfig.isOnline(mContext)) {
			AsyncTaskHelper.execute(new FetchAdvIdTask());
		} else if (!isUsingCache){
			imageList.add(((BitmapDrawable) noadv).getBitmap());
			setAdapter(imageAdapter);
		}
	}
	
	public void setNavi(GalleryNavigator navi) {
		this.navi = navi;
		if (isUsingCache) {
			navi.setSize(linkIdList.size());
			navi.setVisibility(View.GONE);
			navi.setVisibility(View.VISIBLE);
		}
	}
	
	public void setLoadingLayout(View loadingLayout) {
		this.loadingLayout = loadingLayout;
		if (isUsingCache) {
			loadingLayout.setVisibility(View.GONE);
		}
	}

   private class FetchAdvIdTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		isFetchingId = true;
    	}
    	@Override
    	protected Void doInBackground(Void... params) {
    		if (isCancelled()) return null;

    		if (!MFConfig.isOnline(mContext)) return null;

            try {
            	File f = fileCache.getFile(ADV_ID_LIST);
            	String advListStr = MFService.getString(MFURL.NEW_BIG_ADV, f);
            	
            	String[] advIdList = advListStr.split(",");
            	for (String id : advIdList) {

    				MFLog.e(TAG, id);
            		if (!id.equals("")) {
            			Integer.parseInt(id);
            			idList.add(id);
            		}
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
			
			return null;
    	}
            
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		
    		isFetchingId = false;
    		
    		if (idList.size() == 0 && !isUsingCache) {
				imageList.add(((BitmapDrawable) noadv).getBitmap());
				setAdapter(imageAdapter);
    		} else {
	    		for (String id : idList) {
	    			AsyncTaskHelper.executeWithResultBitmap(new FetchAdvTask(id));
	    		}
    		}
    	}
   }
   
   private class FetchAdvTask extends AsyncTask<Void, Void, Bitmap> {
	   
	private String id;
	
	public FetchAdvTask(String id) {
		this.id = id;
	}
   	
   	@Override
   	protected Bitmap doInBackground(Void... params) {
   		if (isCancelled()) return null;

   		if (!MFConfig.isOnline(mContext)) return null;

            try {
            	File f = fileCache.getFile(id);
            	return MFService.getBitmap(MFURL.getImageUrl(ImageType.ADV, id), f);
				
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
    		if (result != null && !isUsingCache) {
    			Random rand = new Random();
    			boolean randomValue = rand.nextBoolean();
    			if (randomValue) {
	    			imageList.add(result);
	    			linkIdList.add(id);
    			} else {
	    			imageList.add(0, result);
	    			linkIdList.add(0, id);
    			}
    		}
    		
    		if (!isUsingCache) {
	    		//populate after all images are load
	    		if (imageList.size() == idList.size()) {

	    			loadingLayout.setVisibility(View.GONE);
		    		navi.setSize(idList.size());
		    		navi.setVisibility(View.GONE);
		    		navi.setVisibility(View.VISIBLE);
		    		
		    		setAdapter(imageAdapter);
		    		
		    		startTimer();
	    		}
    		}
            
    	}
    }
   
	final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	
        	if (linkIdList.size() == 0) {
        		if (MFConfig.isOnline(mContext)) {
        			imageList.clear();
        			if (!isFetchingId)
        				AsyncTaskHelper.execute(new FetchAdvIdTask());
        		} else {
//            		setVisibility(View.VISIBLE);
        			imageList.add(((BitmapDrawable) noadv).getBitmap());
        			setAdapter(imageAdapter);
        		}
        	} else {
				int cur = getCurrentItem(); 
				setCurrentItem(cur == linkIdList.size() - 1 ? 0 : cur + 1, true);
        	}

        }
    };
   
   @Override
	public boolean onTouchEvent(MotionEvent event) {
	   if (event.getAction() == MotionEvent.ACTION_DOWN) {
		   stopTimer();
	   } else if (event.getAction() == MotionEvent.ACTION_UP) {
		   startTimer();
	   }
		return super.onTouchEvent(event);
   }
   
   public class ImageAdapter extends PagerAdapter implements
	ViewPager.OnPageChangeListener {
	   

       public int getCount() {
       	return imageList.size();
       }
       
       @Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}
       
       @Override
		public Object instantiateItem(ViewGroup container, int position) {
    	   if (imageList.size() == 0) return null;
	        Bitmap bmp = imageList.get(position);
	        ImageView i = new ImageView(mContext);
	        i.setImageBitmap(bmp);
	        i.setScaleType(ScaleType.FIT_END);
	        final int pos = position;
	        i.setOnClickListener(new OnClickListener() {
				
				public void onClick(View arg0) {
					if (linkIdList.size() != 0) {
						Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MFURL.getAdvClickUrl(linkIdList.get(pos))));
						mContext.startActivity(myIntent);
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
		navi.setPosition(position);
		navi.invalidate();
	}
	
   }
   
   public void startTimer() {
	   stopTimer();
	   timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				mHandler.post(mUpdateResults);
			}
		}, REFRESH_PERIOD, REFRESH_PERIOD);

   }
   
   public void stopTimer() {
	   if (timer != null) {
		   timer.cancel();
	   }
   }
	
	
}
