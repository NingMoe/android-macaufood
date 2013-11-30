package com.cycon.macaufood.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.cycon.macaufood.utilities.MFLog;
import android.view.Display;
import android.view.WindowManager;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.bean.ParsedFoodNewsHolder;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.xmlhandler.CafeXMLHandler;

public class MFConfig {
	
	public static String DEVICE_ID;
	
	public static String cafe_version_update = "1380093106";
	
	public static String memberId;

	private List<Cafe> cafeLists = Collections.synchronizedList(new ArrayList<Cafe>(2500));
	
	private ArrayList<String> favoriteLists = new ArrayList<String>(20);
	
	private ArrayList<Cafe> searchResultList = new ArrayList<Cafe>(50);
	
	private ArrayList<ParsedCafeHolder> recommendCafeList = new ArrayList<ParsedCafeHolder>(60);
	private ArrayList<ParsedCafeHolder> normalCouponCafeList = new ArrayList<ParsedCafeHolder>(60);
	private ArrayList<ParsedCafeHolder> creditVipCouponCafeList = new ArrayList<ParsedCafeHolder>(30);
	private ArrayList<ParsedFoodNewsHolder> foodNewsList = new ArrayList<ParsedFoodNewsHolder>(20);
	private ArrayList<ParsedPSHolder> psHotList = new ArrayList<ParsedPSHolder>(26);
	
	public static List<ParsedCafeHolder> tempParsedRecommendCafeList = Collections.synchronizedList(new ArrayList<ParsedCafeHolder>(60));
	public static List<ParsedCafeHolder> tempParsedNormalCouponCafeList = Collections.synchronizedList(new ArrayList<ParsedCafeHolder>(60));
	public static List<ParsedCafeHolder> tempParsedCreditVipCouponCafeList = Collections.synchronizedList(new ArrayList<ParsedCafeHolder>(40));
	public static List<ParsedFoodNewsHolder> tempParsedFoodNewsList = Collections.synchronizedList(new ArrayList<ParsedFoodNewsHolder>(20));
	public static List<ParsedPSHolder> tempParsedPSHotList = Collections.synchronizedList(new ArrayList<ParsedPSHolder>(26));
	
	public static int deviceWidth;
	public static int deviceHeight;
	
	private static MFConfig config;
	
	public List<Cafe> getCafeLists() {
		return cafeLists;
	}
	public ArrayList<String> getFavoriteLists() {
		return favoriteLists;
	}
	public ArrayList<Cafe> getSearchResultList() {
		return searchResultList;
	}
	public void setRecommendCafeList(ArrayList<ParsedCafeHolder> list) {
		recommendCafeList = list;
	}
	public ArrayList<ParsedCafeHolder> getRecommendCafeList() {
		return recommendCafeList;
	}
	public ArrayList<ParsedCafeHolder> getNormalCouponCafeList() {
		return normalCouponCafeList;
	}
	public ArrayList<ParsedCafeHolder> getCreditVipCouponCafeList() {
		return creditVipCouponCafeList;
	}
	public ArrayList<ParsedFoodNewsHolder> getFoodNewsList() {
		return foodNewsList;
	}
	public ArrayList<ParsedPSHolder> getPsHotList() {
		return psHotList;
	}
	
	public synchronized static MFConfig getInstance() {
		if (config == null) {
			config = new MFConfig();
		}
		return config;
	}
	

	public static void initDataFromXml(Resources res, Context context) {
		/** Handling XML */

		
		MFConfig.getInstance().getCafeLists().clear();
		
			try {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				CafeXMLHandler myXMLHandler = new CafeXMLHandler();
				xr.setContentHandler(myXMLHandler);
				xr.parse(new InputSource(res.openRawResource(R.raw.cafe_output)));
			} catch (NotFoundException e) {
				MFLog.e("Config", "NotFoundexception");
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				MFLog.e("Config", "factoryconfiexception");
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				MFLog.e("Config", "parseconfexception");
				e.printStackTrace();
			} catch (SAXException e) {
				MFLog.e("Config", "saxexception");
				e.printStackTrace();
			} catch (IOException e) {
				MFLog.e("Config", "ioexception");
				e.printStackTrace();
			}
	}
	
	public static boolean isOnline(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo ni = cm.getActiveNetworkInfo();
	    if (ni!=null && ni.isAvailable() && ni.isConnected()) {
	        return true;
	    } else {
	        return false; 
	    }
	}
	
	
}
