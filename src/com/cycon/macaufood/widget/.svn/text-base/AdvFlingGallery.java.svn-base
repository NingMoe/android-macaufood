package com.cycon.macaufood.widget;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;

public class AdvFlingGallery extends OneFlingGallery {
	
	private static final String TAG = "AdvFlingGallery";
	private ImageAdapter imageAdapter;
	private Context mContext;
	private ArrayList<String> idList = new ArrayList<String>();

	public List<Bitmap> imageList = Collections.synchronizedList(new ArrayList<Bitmap>());
	public List<String> linkIdList = Collections.synchronizedList(new ArrayList<String>());
	
	private GalleryNavigator navi;
	private Timer timer;
	private Handler mHandler;
	private Drawable noadv;
	private boolean isFetchingId;
	private static final long REFRESH_PERIOD = 8000;

	public AdvFlingGallery(Context context) {
		super(context);
		init();
	}
	
	public AdvFlingGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		
		mHandler = new Handler();
		noadv = getContext().getResources().getDrawable(R.drawable.searchadv);
		mContext = this.getContext();
		imageAdapter = new ImageAdapter();
		setAdapter(imageAdapter);
		setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (linkIdList.size() != 0) {
					String url = "http://www.cycon.com.mo/xml_advclick.php?id= " + linkIdList.get(position) + "&code=" + Config.DEVICE_ID;
					Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					mContext.startActivity(myIntent);
				}
				
			}
		});
		
		if (Config.isOnline(mContext)) {
			new FetchAdvIdTask().execute();
		} else {
    		setVisibility(View.VISIBLE);
			imageList.add(((BitmapDrawable) noadv).getBitmap());
			imageAdapter.notifyDataSetChanged();
		}
	}
	
	public void setNavi(GalleryNavigator navi) {
		this.navi = navi;
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

    		if (!Config.isOnline(mContext)) return null;
    		
    		String linkIdUrl = "http://www.cycon.com.mo/xml_adv2.php?code=android-" + Config.DEVICE_ID + 
    				"&type=b";

            try {
            	HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(linkIdUrl);
            	HttpResponse response = client.execute(request);
            	BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
            	String advListStr = rd.readLine().trim();
				ETLog.e(TAG, advListStr);
            	String[] advIdList = advListStr.split(",");
            	for (String id : advIdList) {

    				ETLog.e(TAG, id);
            		if (!id.equals("")) {
            			Integer.parseInt(id);
            			idList.add(id);
            		}
            	}
            	
			} catch (MalformedURLException e) {
				ETLog.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				ETLog.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				ETLog.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
			
			return null;
    	}
            
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		
    		isFetchingId = false;
    		
    		if (idList.size() == 0) {
        		setVisibility(View.VISIBLE);
				imageList.add(((BitmapDrawable) noadv).getBitmap());
	    		imageAdapter.notifyDataSetChanged();
    		} else {
	    		for (String id : idList) {
	    			new FetchAdvTask(id).execute();
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

   		if (!Config.isOnline(mContext)) return null;

    		String urlStr = "http://www.cycon.com.mo/appimages/adv_rotate_banner/" + id + ".jpg";
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
				return BitmapFactory.decodeStream(new FlushedInputStream(is));
				
			} catch (MalformedURLException e) {
				ETLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				ETLog.e(TAG, "io exception");
				e.printStackTrace();
			} 
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Bitmap result) {
    		super.onPostExecute(result);
//    		setVisibility(View.VISIBLE);
    		if (result == null) {
//    			imageList.add(object)
//    				setImageResource(isSmallAdv ? R.drawable.adv2 : R.drawable.searchadv);
    		}
    		else {
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
    		//populate after all images are load
    		if (imageList.size() == idList.size()) {
    			setVisibility(View.VISIBLE);
	    		navi.setSize(idList.size());
	    		navi.setVisibility(View.GONE);
	    		navi.setVisibility(View.VISIBLE);
	    		imageAdapter.notifyDataSetChanged();
	    		startTimer();
    		}
            
    	}
    }
   
	final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	
        	if (linkIdList.size() == 0) {
        		if (Config.isOnline(mContext)) {
        			imageList.clear();
        			if (!isFetchingId)
        				new FetchAdvIdTask().execute();
        		} else {
            		setVisibility(View.VISIBLE);
        			imageList.add(((BitmapDrawable) noadv).getBitmap());
            		imageAdapter.notifyDataSetChanged();
        		}
        	} else {
				int cur = getSelectedItemPosition();
				setSelection(cur == linkIdList.size() - 1 ? 0 : cur + 1, true);
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
   
   public class ImageAdapter extends BaseAdapter {

       public int getCount() {
       	return imageList.size();
       }

       public Object getItem(int position) {
       	return imageList.get(position);
       }

       public long getItemId(int position) {
       	return position;
       }

       public View getView(int position, View convertView, ViewGroup parent) {

   			if (imageList.size() == 0) return null;
	        Bitmap bmp = imageList.get(position);
       	ImageView i = new ImageView(mContext);
//       	if (bmp == null) {
//   	        i.setLayoutParams(new Gallery.LayoutParams(Config.deviceWidth, 360));
//       		return i;
//       	}
	        i.setImageBitmap(bmp);
	        i.setScaleType(ScaleType.FIT_CENTER);
//	        int height = Config.deviceWidth * bmp.getHeight() / bmp.getWidth();
	        i.setLayoutParams(new Gallery.LayoutParams(Config.deviceWidth, getHeight()));
	        
	    	return i;
	    	
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
