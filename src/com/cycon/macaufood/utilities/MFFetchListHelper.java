package com.cycon.macaufood.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.cycon.macaufood.activities.Coupon;
import com.cycon.macaufood.activities.FoodNews;
import com.cycon.macaufood.activities.Home;
import com.cycon.macaufood.activities.Recommend;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.xmlhandler.FoodNewsXMLHandler;
import com.cycon.macaufood.xmlhandler.ServerCafeXMLHandler;

public class MFFetchListHelper {

	private static final String TAG = MFFetchListHelper.class.getName();
	public static boolean isFetching = false;

	public static void fetchAllList(Home homeActivity) {
		if (!isFetching) {
			isFetching = true;

			homeActivity.showProgressDialog();
			FetchListInfo recommendInfo = new FetchListInfo(MFURL.RECOMMEND_LIST, MFConstants.RECOMMEND_XML_FILE_NAME, ImageType.RECOMMEND, MFConfig.tempParsedRecommendCafeList, MFConfig.getInstance().getRecommendCafeList());
			FetchListInfo normalCouponInfo = new FetchListInfo(MFURL.NORMAL_COUPON_LIST, MFConstants.NORMAL_COUPON_XML_FILE_NAME, ImageType.COUPON, MFConfig.tempParsedNormalCouponCafeList, MFConfig.getInstance().getNormalCouponCafeList());
			FetchListInfo creditVipCouponInfo = new FetchListInfo(MFURL.CREDIT_VIP_COUPON_LIST, MFConstants.CREDIT_VIP_COUPON_XML_FILE_NAME, ImageType.COUPON, MFConfig.tempParsedCreditVipCouponCafeList, MFConfig.getInstance().getCreditVipCouponCafeList());
			FetchListInfo foodNewsInfo = new FetchListInfo(MFURL.FOOD_NEWS_LIST, MFConstants.FOODNEWS_XML_FILE_NAME, ImageType.FOODNEWS, MFConfig.tempParsedFoodNewsList, MFConfig.getInstance().getFoodNewsList());
			AsyncTaskHelper.execute(new FetchXmlTask(recommendInfo,
					homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(
					normalCouponInfo, homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(
					creditVipCouponInfo, homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(foodNewsInfo,
					homeActivity));
			AsyncTaskHelper.executeWithResultBitmap(new FetchMainCouponTask(homeActivity));
		}
	}

	private static class FetchXmlTask extends AsyncTask<Void, Void, Void> {

		private FetchListInfo info;
		private Home homeActivity;
		private ByteArrayOutputStream baos;

		private FetchXmlTask(FetchListInfo i, Home h) {
			info = i;
			homeActivity = h;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			String urlStr = info.url;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpParams httpParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
				HttpGet request = new HttpGet(urlStr);

				HttpResponse response = client.execute(request);
				InputStream is = response.getEntity().getContent();

				baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				baos.flush();


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
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			parseXml(new ByteArrayInputStream(baos.toByteArray()), info.tempParsedList, info.contentList);

			try {
				if (info.tempParsedList.size() != 0) {
					FileCache fileCache=new FileCache(homeActivity, info.imageType);
					File f = fileCache.getFile(info.cacheFileName);
					OutputStream os = new FileOutputStream(f);
					os.write(baos.toByteArray());
					os.close();
				}
				info.tempParsedList.clear();
			} catch (Exception e) {
			}
			
			Fragment[] fragment = homeActivity.getFragments();
			Recommend recommendFragment = (Recommend)fragment[0];
			Coupon couponFragment = (Coupon)fragment[1];
			FoodNews foodNewsFragment = (FoodNews)fragment[2];
			if (info.imageType == ImageType.RECOMMEND && recommendFragment != null) {
				if (recommendFragment.mIsVisible) {
					homeActivity.hideProgressDialog();
				}
				recommendFragment.populateListView();

				homeActivity.setDataTimeStamp(System.currentTimeMillis());
				PreferenceHelper.savePreferencesLong(
						homeActivity.getApplicationContext(), MFConstants.TIME_STAMP_PREF_KEY,
						homeActivity.getDataTimeStamp());
				
			} else if (info.imageType == ImageType.COUPON && couponFragment != null) {
				int type = 0;
				if (info.cacheFileName.equals(MFConstants.CREDIT_VIP_COUPON_XML_FILE_NAME)) {
					type = 1;
				} 
				if (couponFragment.mIsVisible && couponFragment.couponType == type) {
					homeActivity.hideProgressDialog();
				}
				couponFragment.populateListView(type);
			} else if (info.imageType == ImageType.FOODNEWS && foodNewsFragment != null) {
				if (foodNewsFragment.mIsVisible) {
					homeActivity.hideProgressDialog();
				}
				foodNewsFragment.populateListView();
			} 
			
			isFetching = false;
			
		}
	}
	
	
    public static class FetchMainCouponTask extends AsyncTask<Void, Void, Bitmap> {
    	
    	private Home homeActivity;
    	
    	private FetchMainCouponTask(Home h) {
    		homeActivity = h;
    	}
    	
    	@Override
    	protected Bitmap doInBackground(Void... params) {

            try {
        		FileCache fileCache = new FileCache(homeActivity, ImageType.MAINCOUPON); 
        		File f = fileCache.getFile(MFConstants.MAIN_COUPON_INFO_STR);
        		
    			String infoStr = MFService.getString(MFURL.MAIN_COUPON, f);
        		if (infoStr != null) {
        			String[] tokens = infoStr.split("\\|\\|\\|");
        			String couponId = tokens[0];
    				Integer.parseInt(couponId);
    				return MFService.getBitmap(MFURL.getImageUrl(ImageType.MAINCOUPON, couponId), fileCache.getFile(couponId));
        		}
				
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			} catch (Exception e) {
				
			}
    		
    		return null;
    	}
    	
    	
    	@Override
    	protected void onPostExecute(Bitmap result) {
    		super.onPostExecute(result);
    		
			Fragment[] fragment = homeActivity.getFragments();
			Coupon couponFragment = (Coupon)fragment[1];
			if (couponFragment != null) {
				if (couponFragment.mIsVisible && couponFragment.couponType == 2) {
					homeActivity.hideProgressDialog();
				}
				if (result != null) {
					couponFragment.populateMainCoupon();
				}
			} 
			
			isFetching = false;
    	}
    	
    }

	public static void parseXml(InputStream is, List tempParsedList, List contentList) {
		tempParsedList.clear();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			DefaultHandler xmlHandler;
			if (tempParsedList == MFConfig.tempParsedFoodNewsList) {
				xmlHandler = new FoodNewsXMLHandler(tempParsedList);
			} else {
				xmlHandler = new ServerCafeXMLHandler(tempParsedList);
			}
			xr.setContentHandler(xmlHandler);
			xr.parse(new InputSource(is));
		} catch (FactoryConfigurationError e) {
			Log.e(TAG, "FactoryConfigurationError");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(TAG, "SAXException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
		}

		if (tempParsedList.size() != 0) {
			contentList.clear();
			contentList.addAll(tempParsedList);
		}
	}

	private static class FetchListInfo {
		private String url;
		private String cacheFileName;
		private ImageType imageType;
		private List tempParsedList;
		private ArrayList contentList;

		private FetchListInfo(String url, String cacheFileName,
				ImageType imageType, List tempParsedList, ArrayList contentList) {
			super();
			this.url = url;
			this.cacheFileName = cacheFileName;
			this.imageType = imageType;
			this.tempParsedList = tempParsedList;
			this.contentList = contentList;
		}

	}

}
