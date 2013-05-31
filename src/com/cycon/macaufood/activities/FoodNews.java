package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
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
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.FoodNewsListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.haarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;

public class FoodNews extends SherlockFragment {

	private static final String TAG = FoodNews.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView list;
	private FoodNewsListAdapter foodListAdapter;
	private FileCache fileCache;
	private Context mContext;
	private View mView;
	private ScaleInAnimationAdapter scaleInAnimationAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.foodnews, null);
		initView();
		return mView;
	}
	
	private void initView() {
		list = (ListView) mView.findViewById(R.id.list);
        foodListAdapter = new FoodNewsListAdapter(mContext, MFConfig.getInstance().getFoodNewsList(), ImageType.FOODNEWS);
        
        scaleInAnimationAdapter = new ScaleInAnimationAdapter(foodListAdapter, 0.5f);
        scaleInAnimationAdapter.setListView(list);
        
        list.setAdapter(scaleInAnimationAdapter);
        list.setOnItemClickListener(itemClickListener);
        
		if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
			if (!MFConfig.isOnline(mContext)) {
        		displayRetryLayout();
        	}
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        
        fileCache=new FileCache(mContext, ImageType.FOODNEWS);
        File f=fileCache.getFile(MFConstants.FOODNEWS_XML_FILE_NAME);
		try {
			FileInputStream is = new FileInputStream(f);
			MFFetchListHelper.parseXml(is, MFConfig.tempParsedFoodNewsList, MFConfig.getInstance().getFoodNewsList());
		} catch (FileNotFoundException e) {
	    	Log.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
        
    }
    
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			String foodnews_id = MFConfig.getInstance().getFoodNewsList().get(position).getId();
			Intent i = new Intent(mContext, FoodNewsImage.class);
			i.putExtra("foodnews_id", foodnews_id);
			startActivity(i);
			
		}
    };
    
    public void displayRetryLayout() {
        retryLayout = mView.findViewById(R.id.retryLayout);
		retryLayout.setVisibility(View.VISIBLE);
		retryButton = (Button) mView.findViewById(R.id.retryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				refresh();
			}
		});
    }
    
    public void populateListView() {
		//if no internet and no data in File, show retry message
		if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
			displayRetryLayout();
		} 
		foodListAdapter.imageLoader.cleanup();
		foodListAdapter.imageLoader.setImagesToLoadFromParsedFoodNews(MFConfig.getInstance().getFoodNewsList());
		foodListAdapter.notifyDataSetChanged();
    }
    
	public void resetListViewAnimation() {
		if (scaleInAnimationAdapter != null) {
			scaleInAnimationAdapter.reset();
			scaleInAnimationAdapter.notifyDataSetChanged();
		}
	}
    
	@SuppressLint("NewApi")
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
    	Log.e(TAG, "onDestroy");
    	list.setAdapter(null);
        super.onDestroy();
    }


}
