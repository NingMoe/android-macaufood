package com.cycon.macaufood.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.bean.ParsedFoodNewsHolder;

public class ImageLoader {
	
	private static final String TAG = ImageLoader.class.getName();
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private Drawable nophoto;
    private Drawable nointernet;
    
    private LinkedList<String> imagesToLoad;
    private ConcurrentLinkedQueue<FetchImageTask> imagesLoading;
    private HashSet<String> currentDisplayImages = new HashSet<String>(); 
    
    private int lastVisibleRowIndex;
    
    private String imageTypeUrl;
    
    private static final int MAX_TASKS_NUMBER = 7;
    
    public ImageLoader(Context context, int lastRowIndex, ImageType imageType){
    	ETLog.e(TAG, "initImageLoader");
    	if (imageType == ImageType.RECOMMEND) {
    		imageTypeUrl = "recommend_new";
    	} else if (imageType == ImageType.COUPON) {
    		imageTypeUrl = "coupon_new";
    	} else if (imageType == ImageType.FOODNEWS){
    		imageTypeUrl = "article_thumbnail";
    	} else {
    		imageTypeUrl = "cafephoto";
    	} 
        fileCache=new FileCache(context, imageType);
        lastVisibleRowIndex = lastRowIndex;
        nophoto = context.getResources().getDrawable(R.drawable.nophoto);
        nointernet = context.getResources().getDrawable(R.drawable.nointernet);
        
        imagesLoading = new ConcurrentLinkedQueue<FetchImageTask>();
        imagesToLoad = new LinkedList<String>();
    }
    
    public void setImagesToLoadFromCafe(ArrayList<Cafe> cafes) {
    	imagesToLoad.clear();
        for (Cafe cafe : cafes) {
        	imagesToLoad.add(cafe.getId());
        }
    }
    
    public void setImagesToLoadFromParsedCafe(ArrayList<ParsedCafeHolder> cafes) {
    	imagesToLoad.clear();
        for (ParsedCafeHolder cafe : cafes) {
        	imagesToLoad.add(cafe.getId());
        }
    }
    
    public void setImagesToLoadFromParsedFoodNews(ArrayList<ParsedFoodNewsHolder> cafes) {
    	imagesToLoad.clear();
        for (ParsedFoodNewsHolder cafe : cafes) {
        	imagesToLoad.add(cafe.getId());
        }
    }
    
    public void displayImage(String id, ImageView imageView, int position)
    {
    	imagesToLoad.remove(id);
//    	Log.e(TAG, "remove " + id);
//    	if (imagesToLoad.remove(id) == false) {
//    		Log.e(TAG, "error....id in imagestoload does not exist");
//    	}
		imageViews.put(imageView, id);
        Bitmap bitmap=memoryCache.get(id);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else {
            File f=fileCache.getFile(id);
            
//            from SD cache
        	bitmap = decodeFile(f);
            
            if(bitmap!=null) {
                imageView.setImageBitmap(bitmap);
                memoryCache.put(id, bitmap);
            } else {
            	imageView.setImageDrawable(nophoto);
            	
            	boolean needLoad = true;
            	for (FetchImageTask task : imagesLoading) {
            		if (task == null || task.p.id == null) {
            			needLoad = true; 
            			break;
            		}
            		if (task.p.id.equals(id)) {
            			needLoad = false; 
            			break;
            		}
            	}
            	
            	if (needLoad) {
            		loadImages(id, imageView);
            	}
            }
        }
        
        if (position == lastVisibleRowIndex && imagesLoading.isEmpty()) {
			while (!imagesToLoad.isEmpty() && imagesLoading.size() <= MAX_TASKS_NUMBER) {
				String pollId = imagesToLoad.poll();	
				//check if poll id is in memoryCache or filecache;
		        Bitmap b = memoryCache.get(pollId);
		        if(b !=null) {
		        	continue;
		        } else {
		            File f=fileCache.getFile(pollId);
		            
//		            from SD cache
		        	bitmap = decodeFile(f);
		            
		            if(bitmap!=null) {
		                memoryCache.put(pollId, bitmap);
		                continue;
		            }
		        }
				ETLog.e(TAG, "poll id " + pollId);
				loadImages(pollId, null);
			}
        }
    }
        
    public void loadImages(String id, ImageView imageView)
    {
    	
    	if (imagesLoading.size() > MAX_TASKS_NUMBER) {
    		//scroll to specific position
    		if (imageView != null) {
	    		imagesToLoad.addFirst(id);
	    		return;
    		} 
    	}
    	
        ImageToLoad p=new ImageToLoad(id, imageView);
                
        FetchImageTask task = new FetchImageTask(p);

    	imagesLoading.add(task);

    	if (imageView != null)
    		currentDisplayImages.add(id);
    	
        try {
			task.execute();
		} catch (RejectedExecutionException e) {
			ETLog.e(TAG, "catchRejectedExecution");
			e.printStackTrace();
		}
    }
    
    private Bitmap decodeFile(File f){
        try {
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
//        	ETLog.e(TAG, "filenotfounde");
        }
        return null;
    }
    
    //Task for the queue
    private class ImageToLoad
    {
        public String id;
        public ImageView imageView;
        public ImageToLoad(String id, ImageView i){
            this.id=id; 
            imageView=i;
        }
    }
    
    
    private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
    	
    	public ImageToLoad p;
    	private boolean noConnection;
    	
    	public FetchImageTask(ImageToLoad p) {
    		this.p = p;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
            Bitmap bmp=getBitmap(p.id);
            if (bmp != null) {
//            	ETLog.e(TAG, "load successful " + p.id + " max tasks no = " + imagesLoading.size());
            	memoryCache.put(p.id, bmp);
            }
            else {
            	if (!noConnection)
            		memoryCache.put(p.id, ((BitmapDrawable) nophoto).getBitmap());
            }
            
        	for (FetchImageTask task : imagesLoading) {
        		if (task.p.id.equals(p.id)) {
        			imagesLoading.remove(task);
        		}
        	}
        	
        	if (p.imageView != null)
				currentDisplayImages.remove(p.id);
        	
            if (noConnection) {ETLog.e(TAG, "no connection");
            	imagesToLoad.addFirst(p.id);
            	return null;
            }

            return bmp;
		}
    	
		@Override
		protected void onPostExecute(Bitmap result) {
			
			if (noConnection) return;
            
            if (currentDisplayImages.isEmpty()) {
				while (!imagesToLoad.isEmpty() && imagesLoading.size() <= MAX_TASKS_NUMBER) {
					String id = imagesToLoad.poll();
					
					//check if poll id is in memoryCache or filecache;
			        Bitmap bitmap=memoryCache.get(id);
			        if(bitmap!=null) {
			        	continue;
			        } else {
			            File f=fileCache.getFile(id);
			            
//			            from SD cache
			        	bitmap = decodeFile(f);
			            
			            if(bitmap!=null) {
			                memoryCache.put(id, bitmap);
			                continue;
			            }
			        }
//					ETLog.e(TAG, "poll id " + id);
					loadImages(id, null);
				}
            }

			//if image is loading before displayImage, populates image
			if (p.imageView == null) {
				if (imageViews.containsValue(p.id)) {
					for (ImageView view : imageViews.keySet()) {
						String tag=imageViews.get(view);
						if(tag!=null && tag.equals(p.id)){ 
//							ETLog.e(TAG, "set photo after load " + p.id);
							if (result == null) {
//								ETLog.e(TAG, "set nophoto id = " + p.id);
								view.setImageDrawable(nophoto);
							} else {
								view.setImageBitmap(result);
							}
						}
					}
				} else {
					return; 
				}
			} else {
				String tag=imageViews.get(p.imageView);
				if(tag!=null && tag.equals(p.id)){
					if (noConnection) {
						p.imageView.setImageDrawable(nointernet);
					} else {
						if (result == null) {
							ETLog.e(TAG, "set nophoto id = " + p.id);
							p.imageView.setImageDrawable(nophoto);
						} else {
							ETLog.e(TAG, "set photo id = " + p.id);
							p.imageView.setImageBitmap(result);
						}
					}
				}
			}
			
		}
		
		   private Bitmap getBitmap(String id) 
		    {
		        //from web
		        try {
		            Bitmap bitmap=null;

					String urlStr = "http://www.cycon.com.mo/appimages/" + imageTypeUrl + "/" + id + ".jpg";
					
					HttpClient client = new DefaultHttpClient();
	            	HttpParams httpParams = client.getParams();
	            	HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
	            	HttpGet request = new HttpGet(urlStr);
	            	HttpResponse response = client.execute(request);
	            	InputStream is= response.getEntity().getContent();
		            File f=fileCache.getFile(id);
		            OutputStream os = new FileOutputStream(f);
		            Utilities.CopyStream(is, os);
		            os.close();
		            bitmap = decodeFile(f);

		            if (bitmap == null) ETLog.e(TAG, "decode returns null");
		            return bitmap;
		        } catch (FileNotFoundException ex){
		        	ETLog.e(TAG, "no photo");
		           ex.printStackTrace();
		        	   return null;
		        } catch (Exception e) {
		        	noConnection = true;
		        	//socket error here
		        	ETLog.e(TAG, "error = " + e.getMessage());
		        	return null;
		        }
		    }
    }
    
    
    public void cleanup()
    { 
    	for (FetchImageTask task : imagesLoading) {
    		task.cancel(true);
    	}
//    	imagesToLoad.clear();
    	imagesLoading.clear();
    	currentDisplayImages.clear();
    	imageViews.clear();
    }
    
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
    

}
