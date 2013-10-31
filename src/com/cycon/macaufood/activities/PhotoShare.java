package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.PSHotAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFUtil;

public class PhotoShare extends SherlockFragment {

	private static final String TAG = PhotoShare.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private GridView mHotGV;
	private PSHotAdapter mPsHotAdapter;
	private FileCache fileCache;
	private Context mContext;
	private View mView;
	
	private TextView mPsFriends;
	private TextView mPsHot;
	private View mPsCamera;
	private View mPsSettings;
	private View mFriendsLayoutSV;
	private int mCurrentTab = 1; //friends = 0, hot = 1;
	
	
	
	public boolean mIsVisible;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.photo_share, null);
		initView();
		return mView;
	}
	
	private void initView() {
		mHotGV = (GridView) mView.findViewById(R.id.hotLayoutGV);
		int paddingInPx = MFUtil.getPixelsFromDip(PSHotAdapter.SPACING_IN_DP, getResources());
		int paddingForImageBg = MFUtil.getPixelsFromDip(2, getResources());
		mHotGV.setPadding(paddingInPx + paddingForImageBg, paddingInPx + paddingForImageBg * 2, paddingInPx + paddingForImageBg, paddingInPx);
        retryLayout = mView.findViewById(R.id.retryLayout);
		mPsHotAdapter = new PSHotAdapter(mContext, MFConfig.getInstance().getPsHotList());
		mHotGV.setAdapter(mPsHotAdapter);
		mHotGV.setOnItemClickListener(itemClickListener);
        
		if (MFConfig.getInstance().getPsHotList().size() == 0) {
			if (!MFConfig.isOnline(mContext)) {
        		displayRetryLayout();
        	}
		}
		mFriendsLayoutSV = mView.findViewById(R.id.friendsLayoutSV);
		
		mPsFriends = (TextView) mView.findViewById(R.id.psFriends);
		mPsFriends.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mCurrentTab == 0) return;
				mPsHot.setSelected(false);
				mPsFriends.setSelected(true);
				mCurrentTab = 0;
				mHotGV.setVisibility(View.GONE);
				mFriendsLayoutSV.setVisibility(View.VISIBLE);
			}
		});
		mPsHot = (TextView) mView.findViewById(R.id.psHot);
		mPsHot.setSelected(true);
		mPsHot.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mCurrentTab == 1) return;
				mPsHot.setSelected(true);
				mPsFriends.setSelected(false);
				mCurrentTab = 1;
				mHotGV.setVisibility(View.VISIBLE);
				mFriendsLayoutSV.setVisibility(View.GONE);
			}
		});
		mPsCamera = mView.findViewById(R.id.psCamera);
		mPsCamera.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		mPsSettings = mView.findViewById(R.id.psSettings);
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        
        fileCache=new FileCache(mContext, ImageType.PHOTOSHARE_HOT);
        File f=fileCache.getFile(MFConstants.PS_HOT_XML_FILE_NAME);
		try {
			FileInputStream is = new FileInputStream(f);
			MFFetchListHelper.parseXml(is, MFConfig.tempParsedPSHotList, MFConfig.getInstance().getPsHotList());
		} catch (FileNotFoundException e) {
	    	MFLog.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 

		//refresh when file cache xml is deleted by user
        if (MFConfig.getInstance().getPsHotList().size() == 0 && !MFFetchListHelper.isFetching && !((Home)getActivity()).isShowingDisClaimer()) {
        	refresh();
		}
        
    }
    
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			Intent i = new Intent(mContext, PSDetails.class);
			i.putExtra("ps_hot_position", position);
			startActivity(i);
		}
    };
    
    public void displayRetryLayout() {
		retryLayout.setVisibility(View.VISIBLE);
		retryButton = (Button) mView.findViewById(R.id.retryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				refresh();
			}
		});
    }
    
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			mIsVisible = true;
		} else {
			mIsVisible = false;
		}

	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		if (mIsVisible) {
//			((Home)activity).hideBanner();
//		} else {
//			((Home)activity).showBanner();
//		}
		
	}
	
    public void populateGridView() {
		//if no internet and no data in File, show retry message
		if (MFConfig.getInstance().getPsHotList().size() == 0) {
			displayRetryLayout();
		} else {
			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
		mPsHotAdapter.imageLoader.cleanup();
		mPsHotAdapter.imageLoader.setTaskMaxNumber(MFConfig.getInstance().getPsHotList().size());
		mPsHotAdapter.imageLoader.setImagesToLoadFromParsedPSHot(MFConfig.getInstance().getPsHotList());
		mPsHotAdapter.notifyDataSetChanged();
    }
    
	public void refresh() {
		if (MFConfig.isOnline(mContext)) {
			((Home)getActivity()).refresh();
        	
			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
	}

    @Override
    public void onDestroy()
    {
    	MFLog.e(TAG, "onDestroy");
    	if (mHotGV != null) {
    		mHotGV.setAdapter(null);
    	}
        super.onDestroy();
    }



}
