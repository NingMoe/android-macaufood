package com.cycon.macaufood.adapters;

import java.util.ArrayList;

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

public class CafeSearchListAdapter extends BaseAdapter {

    public ImageLoader imageLoader; 
	private LayoutInflater inflater;
    private ArrayList<Cafe> cafes;

    public CafeSearchListAdapter(Context context, ArrayList<Cafe> cafes) {
            this.cafes = cafes;
        	inflater = LayoutInflater.from(context);
        	imageLoader=new ImageLoader(context, 5, ImageType.REGULAR);
//        	imageLoader.setImagesToLoadFromCafe(cafes);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

    		ViewHolder holder;
            if (convertView == null) {
            	convertView = inflater.inflate(R.layout.cafe_search_row, null);
                
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.image = (ImageView) convertView.findViewById(R.id.imageView);
                holder.delivery = (ImageView) convertView.findViewById(R.id.delivery);
                holder.booking = (ImageView) convertView.findViewById(R.id.booking);
                holder.midnight = (ImageView) convertView.findViewById(R.id.midnight);
                holder.party = (ImageView) convertView.findViewById(R.id.party);
                holder.buffet = (ImageView) convertView.findViewById(R.id.buffet);
                holder.banquet = (ImageView) convertView.findViewById(R.id.banquet);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            
            Cafe cafe = cafes.get(position);
            if (cafe != null) {
				holder.name.setText(cafe.getName());
				if (!cafe.getOption_phoneorder().equals("1")) holder.delivery.getDrawable().mutate().setAlpha(50); 
				else holder.delivery.getDrawable().mutate().setAlpha(255); 
				if (!cafe.getOption_booking().equals("1")) holder.booking.getDrawable().mutate().setAlpha(50); 
				else holder.booking.getDrawable().mutate().setAlpha(255); 
				if (!cafe.getOption_night().equals("1")) holder.midnight.getDrawable().mutate().setAlpha(50); 
				else holder.midnight.getDrawable().mutate().setAlpha(255); 
				if (!cafe.getOption_call().equals("1")) holder.party.getDrawable().mutate().setAlpha(50); 
				else holder.party.getDrawable().mutate().setAlpha(255); 
				if (!cafe.getOption_buffet().equals("1")) holder.buffet.getDrawable().mutate().setAlpha(50); 
				else holder.buffet.getDrawable().mutate().setAlpha(255); 
				if (!cafe.getOption_banquet().equals("1")) holder.banquet.getDrawable().mutate().setAlpha(50); 
				else holder.banquet.getDrawable().mutate().setAlpha(255); 
				
				imageLoader.displayImage(cafe.getId(), holder.image, position);
				
			}
            return convertView;
    }
    
    static class ViewHolder {
        TextView name;
        ImageView image;
        ImageView delivery;
        ImageView booking;
        ImageView midnight;
        ImageView party;
        ImageView buffet;
        ImageView banquet;
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
