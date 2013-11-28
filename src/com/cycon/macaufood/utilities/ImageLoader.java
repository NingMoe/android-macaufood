package com.cycon.macaufood.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import com.cycon.macaufood.utilities.MFLog;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.bean.ParsedFoodNewsHolder;
import com.cycon.macaufood.bean.ParsedFriendsHolder;
import com.cycon.macaufood.bean.ParsedPSHotHolder;

public class ImageLoader {
	
	private static final String TAG = ImageLoader.class.getName();
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private Drawable nophoto;
//    private Drawable loadingBlankPhoto;
    private Drawable nointernet;
    private Context mContext;
    
    private LinkedList<String> imagesToLoad;
    private ConcurrentLinkedQueue<FetchImageTask> imagesLoading;
    private HashSet<String> currentDisplayImages = new HashSet<String>(); 
    
    //for findfriends image, use it when imagetype == null
    private Map<String, String> idUrlMap = Collections.synchronizedMap(new HashMap<String, String>());
    
    private int lastVisibleRowIndex;
    
    private ImageType imageType;
    
    private int maxTasksNumber = 7;
    
    public ImageLoader(Context context, int lastRowIndex, ImageType imageType){
    	mContext = context;
    	this.imageType = imageType;
    	if (imageType != null) { //image type null means not using cache
    		fileCache=new FileCache(context, imageType);
//    		loadingBlankPhoto = context.getResources().getDrawable(imageType == ImageType.REGULAR ? R.drawable.light_green_gradient_bg : R.drawable.cafe_row_bg);
    	}
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
    
    public void setTaskMaxNumber(int number) {
    	maxTasksNumber = number;
    }
    
    public void setImagesToLoadFromParsedPSHot(List<ParsedPSHotHolder> cafes) {
    	imagesToLoad.clear();
        for (ParsedPSHotHolder cafe : cafes) {
        	imagesToLoad.add(cafe.getFilename());
        }
    }
    
    public void setImagesToLoadFromParsedFriendsList(List<ParsedFriendsHolder> holders) {
    	imagesToLoad.clear();
        for (ParsedFriendsHolder holder : holders) {
        	imagesToLoad.add(holder.getId());
        	idUrlMap.put(holder.getId(), holder.getPicLink());
        }
    }
    
    public void displayImage(String id, ImageView imageView, int position)
    {
    	imagesToLoad.remove(id);
//    	MFLog.e(TAG, "remove " + id);
//    	if (imagesToLoad.remove(id) == false) {
//    		MFLog.e(TAG, "error....id in imagestoload does not exist");
//    	}
		imageViews.put(imageView, id);
        Bitmap bitmap=memoryCache.get(id);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else {
//            from SD cache
        	bitmap = MFUtil.getBitmapFromCache(fileCache, id);
            
            if(bitmap!=null) {
                imageView.setImageBitmap(bitmap);
                memoryCache.put(id, bitmap);
            } else {
//            	imageView.setImageDrawable(loadingBlankPhoto);
            	imageView.setImageDrawable(null);
            	
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
			while (!imagesToLoad.isEmpty() && imagesLoading.size() <= maxTasksNumber) {
				String pollId = imagesToLoad.poll();	
				//check if poll id is in memoryCache or filecache;
		        Bitmap b = memoryCache.get(pollId);
		        if(b !=null) {
		        	continue;
		        } else {
		            
//		            from SD cache
		            bitmap = MFUtil.getBitmapFromCache(fileCache, pollId);
		            
		            if(bitmap!=null) {
		                memoryCache.put(pollId, bitmap);
		                continue;
		            }
		        }
				MFLog.e(TAG, "poll id " + pollId);
				loadImages(pollId, null);
			}
        }
    }
        
    public void loadImages(String id, ImageView imageView)
    {
    	
    	if (imagesLoading.size() > maxTasksNumber) {
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
			AsyncTaskHelper.executeWithResultBitmap(task);
		} catch (RejectedExecutionException e) {
			MFLog.e(TAG, "catchRejectedExecution");
			e.printStackTrace();
		}
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
    
    
    private class FetchImageTask extends AsyncTask<Void, Void, Bitmap> {
    	
    	public ImageToLoad p;
    	private boolean noConnection;
    	
    	public FetchImageTask(ImageToLoad p) {
    		this.p = p;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
            Bitmap bmp=getBitmap(p.id);
            if (bmp != null) {
//            	ETMFLog.e(TAG, "load successful " + p.id + " max tasks no = " + imagesLoading.size());
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
        	
            if (noConnection) {MFLog.e(TAG, "no connection");
            	imagesToLoad.addFirst(p.id);
            	return null;
            }

            return bmp;
		}
    	
		@Override
		protected void onPostExecute(Bitmap result) {
			
			if (noConnection) return;
            
            if (currentDisplayImages.isEmpty()) {
				while (!imagesToLoad.isEmpty() && imagesLoading.size() <= maxTasksNumber) {
					String id = imagesToLoad.poll();
					
					//check if poll id is in memoryCache or filecache;
			        Bitmap bitmap=memoryCache.get(id);
			        if(bitmap!=null) {
			        	continue;
			        } else {
//			            from SD cache
			            bitmap = MFUtil.getBitmapFromCache(fileCache, id);
			            
			            if(bitmap!=null) {
			                memoryCache.put(id, bitmap);
			                continue;
			            }
			        }
//					ETMFLog.e(TAG, "poll id " + id);
					loadImages(id, null);
				}
            }

			//if image is loading before displayImage, populates image
			if (p.imageView == null) {
				if (imageViews.containsValue(p.id)) {
					for (ImageView view : imageViews.keySet()) {
						String tag=imageViews.get(view);
						if(tag!=null && tag.equals(p.id)){ 
//							ETMFLog.e(TAG, "set photo after load " + p.id);
							if (result == null) {
//								ETMFLog.e(TAG, "set nophoto id = " + p.id);
								view.setImageDrawable(nophoto);
							} else {
								view.setImageBitmap(result);
								view.setAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
								
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
							MFLog.e(TAG, "set nophoto id = " + p.id);
							p.imageView.setImageDrawable(nophoto);
						} else {
							MFLog.e(TAG, "set photo id = " + p.id);
							p.imageView.setImageBitmap(result);
							p.imageView.setAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
						}
					}
				}
			}
			
		}
		
		   private Bitmap getBitmap(String id) 
		    {
		        //from web
		        try {
		        	File f = null;
		        	if (fileCache != null) {
		        		f = fileCache.getFile(id);
		        	}
		        	String url = null;
		        	if (imageType != null) {
						url = MFURL.getImageUrl(imageType, id);
					} else {
						url = idUrlMap.get(id);
					}
		        	if (url == null) android.util.Log.e("ZZZ", "url NULL!!");
		            return MFService.getBitmap(url, f);
		        	
		        } catch (FileNotFoundException ex){
		        	MFLog.e(TAG, "no photo");
		           ex.printStackTrace();
		        	   return null;
		        } catch (Exception e) {
		        	noConnection = true;
		        	//socket error here
		        	MFLog.e(TAG, "error = " + e.getMessage());
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
    
//    public void clearCache() {
//        memoryCache.clear();
//        fileCache.clear();
//    }
    

}
