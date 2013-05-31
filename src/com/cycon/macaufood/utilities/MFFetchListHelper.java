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
	private static boolean isFetching = false;

	public static void fetchAllList(Home homeActivity) {
//		if (!isFetching) {
//			isFetching = true;

			homeActivity.showProgressDialog();
			FetchListInfo recommendInfo = new FetchListInfo(MFConstants.RECOMMEND_URL, MFConstants.RECOMMEND_XML_FILE_NAME, ImageType.RECOMMEND, MFConfig.tempParsedRecommendCafeList, MFConfig.getInstance().getRecommendCafeList());
			FetchListInfo normalCouponInfo = new FetchListInfo(MFConstants.NORMAL_COUPON_URL, MFConstants.NORMAL_COUPON_XML_FILE_NAME, ImageType.COUPON, MFConfig.tempParsedNormalCouponCafeList, MFConfig.getInstance().getNormalCouponCafeList());
			FetchListInfo creditCouponInfo = new FetchListInfo(MFConstants.CREDIT_COUPON_URL, MFConstants.CREDIT_COUPON_XML_FILE_NAME, ImageType.COUPON, MFConfig.tempParsedCreditCouponCafeList, MFConfig.getInstance().getCreditCouponCafeList());
			FetchListInfo vipCouponInfo = new FetchListInfo(MFConstants.VIP_COUPON_URL, MFConstants.VIP_COUPON_XML_FILE_NAME, ImageType.COUPON, MFConfig.tempParsedVipCouponCafeList, MFConfig.getInstance().getVipCouponCafeList());
			FetchListInfo foodNewsInfo = new FetchListInfo(MFConstants.FOOD_NEWS_URL, MFConstants.FOODNEWS_XML_FILE_NAME, ImageType.FOODNEWS, MFConfig.tempParsedFoodNewsList, MFConfig.getInstance().getFoodNewsList());
			AsyncTaskHelper.execute(new FetchXmlTask(recommendInfo,
					homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(
					normalCouponInfo, homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(
					creditCouponInfo, homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(vipCouponInfo,
					homeActivity));
			AsyncTaskHelper.execute(new FetchXmlTask(foodNewsInfo,
					homeActivity));
//		}
	}

	private static class FetchXmlTask extends AsyncTask<Void, Void, Void> {

		private FetchListInfo info;
		private Home homeActivity;

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
				HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
				HttpGet request = new HttpGet(urlStr);

				HttpResponse response = client.execute(request);
				InputStream is = response.getEntity().getContent();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				baos.flush();

				parseXml(new ByteArrayInputStream(baos.toByteArray()), info);

				if (info.tempParsedList.size() != 0) {
					FileCache fileCache=new FileCache(homeActivity, info.imageType);
					File f = fileCache.getFile(info.cacheFileName);
					OutputStream os = new FileOutputStream(f);
					os.write(baos.toByteArray());
					os.close();
				}
				info.tempParsedList.clear();

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

			Log.e(TAG, "onPostExecute");
			
			homeActivity.hideProgressDialog();

			// if no internet and no data in File, show retry message
//			if (info.contentList.size() == 0) {
			
			Fragment[] fragment = homeActivity.getFragments();
			Recommend recommendFragment = (Recommend)fragment[0];
			Coupon couponFragment = (Coupon)fragment[1];
			FoodNews foodNewsFragment = (FoodNews)fragment[2];
			if (info.imageType == ImageType.RECOMMEND && recommendFragment != null) {
				recommendFragment.populateListView();
			} else if (info.imageType == ImageType.COUPON && couponFragment != null) {
				int type = 0;
				if (info.cacheFileName.equals(MFConstants.CREDIT_COUPON_XML_FILE_NAME)) {
					type = 1;
				} else if (info.cacheFileName.equals(MFConstants.VIP_COUPON_XML_FILE_NAME)) {
					type = 2;
				}
				couponFragment.populateListView(type);
			} else if (info.imageType == ImageType.FOODNEWS && foodNewsFragment != null) {
				foodNewsFragment.populateListView();
			} 
			
//				if (info.fragment instanceof Recommend) {
//					Log.e("ZZZ", "recommend on post execute");
//					((Recommend)info.fragment).populateListView();
//				} else if (info.fragment instanceof Coupon) {
//					Log.e("ZZZ", "coupon on post execute");
//					((Coupon)info.fragment).populateListView();
//				} else if (info.fragment instanceof FoodNews) {
//					Log.e("ZZZ", "foodnews on post execute");
//					((FoodNews)info.fragment).populateListView();
//				}
//			} else {
//				dataTimeStamp = System.currentTimeMillis();
//				PreferenceHelper.savePreferencesLong(
//						mContext.getApplicationContext(), "recommendTimeStamp",
//						dataTimeStamp);
//			}
		}
	}

	private static void parseXml(InputStream is, FetchListInfo info) {
		info.tempParsedList.clear();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			DefaultHandler xmlHandler;
			if (info.imageType == ImageType.FOODNEWS) {
				xmlHandler = new FoodNewsXMLHandler(info.tempParsedList);
			} else {
				xmlHandler = new ServerCafeXMLHandler(info.tempParsedList);
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

		if (info.tempParsedList.size() != 0) {
			info.contentList.clear();
			info.contentList.addAll(info.tempParsedList);
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
