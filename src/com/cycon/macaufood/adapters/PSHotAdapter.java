package com.cycon.macaufood.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedPSHotHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFUtil;

public class PSHotAdapter extends BaseAdapter {

    public ImageLoader imageLoader; 
    private List<ParsedPSHotHolder> mHolderList;
    private Context mContext;
    private final int imageWidth;
    private final int SPACING_IN_DP = 8;

    public PSHotAdapter(Context context, List<ParsedPSHotHolder> holderList) {
            this.mHolderList = holderList;
            mContext = context;
        	imageLoader=new ImageLoader(context, holderList.size(), ImageType.PHOTOSHARE_HOT);
        	imageLoader.setTaskMaxNumber(holderList.size());
        	imageLoader.setImagesToLoadFromParsedPSHot(holderList);
        	
        	imageWidth = (MFConfig.deviceWidth - MFUtil.getPixelsFromDip(SPACING_IN_DP, mContext.getResources()) * 5) / 4;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView i;

            if (convertView == null) {
            	i = new ImageView(mContext);
                i.setScaleType(ImageView.ScaleType.CENTER_CROP);
                i.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
            } else {
            	i = (ImageView) convertView;
            }
            
            ParsedPSHotHolder holder = mHolderList.get(position);
            if (holder != null) {
				imageLoader.displayImage(holder.getFilename(), i, position);
			}
            return i;
    }

	public int getCount() {
		return mHolderList.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}
		
}
