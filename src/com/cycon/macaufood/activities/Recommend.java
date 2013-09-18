package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.cycon.macaufood.utilities.MFLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

public class Recommend extends SherlockFragment {

	private static final String TAG = Recommend.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView list;
	private CafeListAdapter cafeAdapter;
	private FileCache fileCache;
	private Context mContext;
	private View mView;
	private SwingLeftInAnimationAdapter swingLeftInAnimationAdapter;
	public boolean mIsVisible;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.recommend, null);
		initView();
		return mView;
	}
	
	private void initView() {
        list = (ListView) mView.findViewById(R.id.list);
        retryLayout = mView.findViewById(R.id.retryLayout);
        cafeAdapter = new CafeListAdapter(mContext, MFConfig.getInstance().getRecommendCafeList(), ImageType.RECOMMEND);
        
        swingLeftInAnimationAdapter = new SwingLeftInAnimationAdapter(cafeAdapter);
        swingLeftInAnimationAdapter.setListView(list);
        if (MFFetchListHelper.isFetching || ((Home)getActivity()).isShowingDisClaimer()) {
			swingLeftInAnimationAdapter.setAnimationEnabled(false);
		}

		list.setAdapter(swingLeftInAnimationAdapter);
        list.setOnItemClickListener(itemClickListener);
        
        if (MFConfig.getInstance().getRecommendCafeList().size() == 0) {
        	if (!MFConfig.isOnline(mContext)) {
        		displayRetryLayout();
        	}
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = getActivity();

        fileCache=new FileCache(mContext, ImageType.RECOMMEND);
        File f=fileCache.getFile(MFConstants.RECOMMEND_XML_FILE_NAME);
		try {
			FileInputStream is = new FileInputStream(f);
			MFFetchListHelper.parseXml(is, MFConfig.tempParsedRecommendCafeList, MFConfig.getInstance().getRecommendCafeList());
		} catch (FileNotFoundException e) {
	    	MFLog.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
		//refresh when file cache xml is deleted by user
        if (MFConfig.getInstance().getRecommendCafeList().size() == 0 && !MFFetchListHelper.isFetching && !((Home)getActivity()).isShowingDisClaimer() ) {
        	refresh();
		}
        
    }
    

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			String cafeId = MFConfig.getInstance().getRecommendCafeList().get(position).getCafeid();
			String forward = MFConfig.getInstance().getRecommendCafeList().get(position).getForward();
			
			if (forward.equals("b")) {
				String branch = cafeId;
				MFLog.e("branch", branch);
				if (!branch.equals("0")) {
					Intent i = new Intent(mContext, Branch.class);
					i.putExtra("branch", branch);
					startActivity(i);
				} else {
					//in case that cafe is not added in cafelist yet, return
					if (MFConfig.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
					Intent i = new Intent(mContext, Details.class);
					i.putExtra("id", cafeId);
					startActivity(i);
				}
			} else {
				//in case that cafe is not added in cafelist yet, return
				if (MFConfig.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
				Intent i = new Intent(mContext, Details.class);
				i.putExtra("id", cafeId);
				startActivity(i);
			}
    	};
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
    
    public void populateListView() {
		
		//if no internet and no data in File, show retry message
		if (MFConfig.getInstance().getRecommendCafeList().size() == 0) {
			displayRetryLayout();
		} else {
			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
		cafeAdapter.imageLoader.cleanup();
		cafeAdapter.imageLoader.setImagesToLoadFromParsedCafe(MFConfig.getInstance().getRecommendCafeList());
		if (mIsVisible) {
			swingLeftInAnimationAdapter.reset();
		} else {
			swingLeftInAnimationAdapter.setAnimationEnabled(true);
		}
		cafeAdapter.notifyDataSetChanged();
    }
    

	public void resetListViewAnimation() {
		if (swingLeftInAnimationAdapter != null) {
			swingLeftInAnimationAdapter.reset();
			swingLeftInAnimationAdapter.notifyDataSetChanged();
		}
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
    	if (list != null) {
    		list.setAdapter(null);
    	}
        super.onDestroy();
    }
    


}
