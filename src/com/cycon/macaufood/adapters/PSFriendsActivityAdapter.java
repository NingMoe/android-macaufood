package com.cycon.macaufood.adapters;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.widget.PSDetailsView;
import com.cycon.macaufood.widget.PSHeaderView;

public class PSFriendsActivityAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    public ImageLoader psDetailsImageLoader; 
    public ImageLoader psHeaderImageLoader; 
    private List<String> mHolderList;
    private Context mContext;
    private LayoutInflater mInflater;
//    private final int imageWidth;
    public final static int SPACING_IN_DP = 4;

    public PSFriendsActivityAdapter(Context context, List<String> holderList) {
            this.mHolderList = holderList;
            mContext = context;
            psDetailsImageLoader=new ImageLoader(context, 0, ImageType.PHOTOSHARE);
            psDetailsImageLoader.setTaskMaxNumber(2);
            psDetailsImageLoader.setNoAnim(true);
            //no need to keep loading detail images right?
//            psDetailsImageLoader.setPSDetailsImagesToLoadFromParsedPS(holderList);

            psHeaderImageLoader=new ImageLoader(context, holderList.size(), ImageType.PSLOCALAVATAR);
//            psHeaderImageLoader.setProfileImagesToLoadFromParsedPS(holderList);
            psHeaderImageLoader.setNoAnim(true);
            psHeaderImageLoader.setAllowedDuplicate(true);
        	mInflater =  (LayoutInflater)context.getSystemService
        		      (Context.LAYOUT_INFLATER_SERVICE);
    }

	

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		PSHeaderView.ViewHolder holder;
		PSHeaderView psHeaderView;
		ParsedPSHolder psHolder = MFConfig.getInstance().getPsInfoMap().get(mHolderList.get(position));
        if (convertView == null) {
        	psHeaderView = (PSHeaderView) mInflater.inflate(R.layout.ps_header_view, null);
        	holder = psHeaderView.initView();
        	convertView = psHeaderView;
        	convertView.setTag(holder);
//        	MFService.loadImage(mContext.getApplicationContext(), ImageType.PSLOCALAVATAR, psHolder.getMemberid(), holder.profilePic, false, false);
        } else {
        	psHeaderView = (PSHeaderView) convertView;
        	holder = (PSHeaderView.ViewHolder) convertView.getTag();
        	Log.e("ZZZ", "header view recycle");
        }
        psHeaderView.loadInfo(psHolder, holder, psHeaderImageLoader, position);
        
        return convertView;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		PSDetailsView.ViewHolder holder;
		PSDetailsView psDetailsView;
        if (convertView == null) {
        	psDetailsView = (PSDetailsView) mInflater.inflate(R.layout.ps_detail_view, null);
        	holder = psDetailsView.initView();
        	convertView = psDetailsView;
        	convertView.setTag(holder);
        	Log.e("ZZZ", "detail view null");
        } else {
        	psDetailsView = (PSDetailsView) convertView;
        	holder = (PSDetailsView.ViewHolder) convertView.getTag();
        	Log.e("ZZZ", "detail view recycle");
        }
        ParsedPSHolder psHolder = MFConfig.getInstance().getPsInfoMap().get(mHolderList.get(position));
        psDetailsView.loadInfo(psHolder, holder, psDetailsImageLoader, position);
        return convertView;
	}


	public int getCount() {
		return mHolderList.size();
	}

	public Object getItem(int position) {
		return mHolderList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
//	static class Item {
//
//		public static final int PS_DETAILS = 0;
//		public static final int PS_HEADER = 1;
//
//		public final int type;
//		public final ParsedPSHolder psInfo;
//
//		public Item(int type, ParsedPSHolder info) {
//		    this.type = type;
//		    this.psInfo = info;
//		}
//
//	}
		
}
