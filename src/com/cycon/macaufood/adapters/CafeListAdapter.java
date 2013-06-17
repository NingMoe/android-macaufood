package com.cycon.macaufood.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFUtil;

public class CafeListAdapter extends BaseAdapter {

    public ImageLoader imageLoader; 
	private LayoutInflater inflater;
    private ArrayList<ParsedCafeHolder> cafes;

    public CafeListAdapter(Context context, ArrayList<ParsedCafeHolder> cafes, ImageType imageType) {
            this.cafes = cafes;
        	inflater = LayoutInflater.from(context);
        	imageLoader=new ImageLoader(context, 4, imageType);
        	imageLoader.setImagesToLoadFromParsedCafe(cafes);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;
            if (convertView == null) {
            	convertView = inflater.inflate(R.layout.cafe_row, null);
                
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.subText = (TextView) convertView.findViewById(R.id.subText);
                holder.image = (ImageView) convertView.findViewById(R.id.imageView);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            
            ParsedCafeHolder cafe = cafes.get(position);
            if (cafe != null) {
				holder.name.setText(cafe.getTitle());
				holder.subText.setText(cafe.getContent());
				imageLoader.displayImage(cafe.getId(), holder.image, position);
				
			}
            return convertView;
    }
    
    static class ViewHolder {
        TextView name;
        TextView subText;
        ImageView image;
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
