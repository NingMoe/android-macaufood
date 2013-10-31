package com.cycon.macaufood.adapters;

import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedPSHotHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.TouchImageView;

public class PSHotAdapter extends BaseAdapter {

    public ImageLoader imageLoader; 
    private List<ParsedPSHotHolder> mHolderList;
    private Context mContext;
    private LayoutInflater mInflater;
    private final int imageWidth;
    public final static int SPACING_IN_DP = 4;

    public PSHotAdapter(Context context, List<ParsedPSHotHolder> holderList) {
            this.mHolderList = holderList;
            mContext = context;
        	imageLoader=new ImageLoader(context, holderList.size(), ImageType.PHOTOSHARE_HOT);
        	imageLoader.setTaskMaxNumber(holderList.size());
        	imageLoader.setImagesToLoadFromParsedPSHot(holderList);
        	
        	imageWidth = (MFConfig.deviceWidth - MFUtil.getPixelsFromDip(SPACING_IN_DP, mContext.getResources()) * 5) / 4;
        	mInflater =  (LayoutInflater)context.getSystemService
        		      (Context.LAYOUT_INFLATER_SERVICE);
    }

	public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.ps_image, null);
            	holder = new ViewHolder();
            	holder.image = (ImageView) convertView.findViewById(R.id.imageView);
            	convertView.setTag(holder);
            	convertView.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
            
            ParsedPSHotHolder psHolder = mHolderList.get(position);
            if (holder != null) {
				imageLoader.displayImage(psHolder.getFilename(), holder.image, position);
			}
            return convertView;
    }
	
    static class ViewHolder {
        ImageView image;
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
