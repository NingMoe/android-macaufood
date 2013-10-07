package com.cycon.macaufood.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFUtil;

public class PSHotAdapter extends BaseAdapter {

    public ImageLoader imageLoader; 
    private List<Cafe> cafes;
    private Context mContext;

    public PSHotAdapter(Context context, List<Cafe> cafes) {
            this.cafes = cafes;
            mContext = context;
        	imageLoader=new ImageLoader(context, 4, ImageType.PHOTOSHARE_HOT);
//        	imageLoader.setImagesToLoadFromCafe(cafes);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView i;

            if (convertView == null) {
            	i = new ImageView(mContext);
            } else {
            	i = (ImageView) convertView;
            }
            
            Cafe cafe = cafes.get(position);
            if (cafe != null) {
				imageLoader.displayImage(cafe.getId(), i, position);
			}
            return i;
    }

	public int getCount() {
		return cafes.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}
		
}
