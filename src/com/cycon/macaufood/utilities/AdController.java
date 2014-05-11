package com.cycon.macaufood.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.widget.AdvView;

public class AdController {
	
	private static final String TAG = AdController.class.getName();
	private static AdController mInstance;
	private List<AdInfo> mBigAdList = new ArrayList<AdInfo>();
	private List<AdInfo> mSmallAdList = new ArrayList<AdInfo>();
	private Context mContext;
	public static boolean isUpdating;
	private boolean isSuccessfullyUpdatedOnce;
	private List<AdvView.Callback> mCallBacks = new ArrayList<AdvView.Callback>();
	
	
	public static class AdInfo {
		public String type;
		public String advId;
		public String advLink;;
	}

	
	public static AdController getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new AdController(c);
		}
		return mInstance;
	}
	
	private AdController(Context c) {
		mContext = c;
		if (!MFConfig.isOnline(c)) {//only use cache is not online, if online viewpager should wait for callback
			String str = MFUtil.getStringFromCache(new FileCache(mContext, ImageType.ADV), MFConstants.ADV_INFO_LIST);
			extractAdvInfoList(str, true);
		}
	}
	
	public void registerCallback(AdvView.Callback callback) {
		mCallBacks.add(callback);
	}
	
	public void unregisterCallback(AdvView.Callback callback) {
		mCallBacks.remove(callback);
	}
	
	public void requestAd() {
		if (!isUpdating && !isSuccessfullyUpdatedOnce && MFConfig.isOnline(mContext)) {
			isUpdating = true;
			FileCache fileCache = new FileCache(mContext, ImageType.ADV); 
    		File f = fileCache.getFile(MFConstants.ADV_INFO_LIST);
			MFService.getString(MFURL.ALL_ADV, f, new MFServiceCallBack() {
				
				@Override
				public void onLoadResultSuccess(Object result) {
					Log.e("ZZZ", "onLoadResult Success");
					isUpdating = false;
					isSuccessfullyUpdatedOnce = true;
					extractAdvInfoList((String) result, false);
					for (AdvView.Callback callback : mCallBacks) {
						callback.onAdLoadResultSuccess();
					}
				}
				
				@Override
				public void onLoadResultError() {
					Log.e("ZZZ", "onLoadResult Error");
					isUpdating = false;
					for (AdvView.Callback callback : mCallBacks) {
						callback.onAdLoadResultError();
					}
				}
			});
		}
	}
	
	public void extractAdvInfoList(String str, boolean random) {
		if (str == null) return;
		mBigAdList.clear();
		mSmallAdList.clear();
		String[] tokens = str.split("\\|\\|\\|");
		for (String token : tokens) {
			String[] infoStr = token.split("\\|\\|");
			AdInfo info = new AdInfo();
			info.type = infoStr[0];
			if (infoStr.length > 1){
				info.advId = infoStr[1];
				if (info.advId.equals("admob"))continue;
			}
			if (infoStr.length > 2) info.advLink = infoStr[2];
			//to make sure the adv is in random order
			Random rand = new Random(); 
			boolean randomValue = rand.nextBoolean();
			if (randomValue) {
				if (info.type.equals("b")) mBigAdList.add(info);
				if (info.type.equals("s")) mSmallAdList.add(info);
			} else {
				if (info.type.equals("b")) mBigAdList.add(0, info);
				if (info.type.equals("s")) mSmallAdList.add(0, info);
			}
		}
	}
	
	public List<AdInfo> getBigAdInfoList() {
		return mBigAdList;
	}

	public List<AdInfo> getSmallAdInfoList() {
		return mSmallAdList;
	}
}
