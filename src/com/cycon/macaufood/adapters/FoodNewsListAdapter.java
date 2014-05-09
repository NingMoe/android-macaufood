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
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedFoodNewsHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFUtil;

public class FoodNewsListAdapter extends BaseAdapter {

    public ImageLoader imageLoader; 
	private LayoutInflater inflater;
    private ArrayList<ParsedFoodNewsHolder> cafes;

    public FoodNewsListAdapter(Context context, ArrayList<ParsedFoodNewsHolder> cafes, ImageType imageType) {
            this.cafes = cafes;
        	inflater = LayoutInflater.from(context);
        	imageLoader=new ImageLoader(context, 5, imageType);
        	imageLoader.setImagesToLoadFromParsedFoodNews(cafes);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;
            if (convertView == null) {
            	convertView = inflater.inflate(R.layout.foodnews_row, null);
                
                holder = new ViewHolder();
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.image = (ImageView) convertView.findViewById(R.id.imageView);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            
            ParsedFoodNewsHolder cafe = cafes.get(position);
            if (cafe != null) {
				holder.subject.setText(cafe.getSubject());
				holder.content.setText(cafe.getContent());
				holder.time.setText(cafe.getTimeadded());
				
				imageLoader.displayImage(cafe.getId(), holder.image, position);
				
			}
            return convertView;
    }
    
    static class ViewHolder {
        TextView subject;
        TextView content;
        TextView time;
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
