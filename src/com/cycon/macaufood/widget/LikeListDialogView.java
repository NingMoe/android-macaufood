package com.cycon.macaufood.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedFriendsHolder;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFServiceCallBack;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.PSDetailsView.PSLike;
import com.cycon.macaufood.xmlhandler.FriendListXMLHandler;

public class LikeListDialogView extends LinearLayout {
	private ImageLoader mImageLoader; 
	private ListView mListView;
	private LayoutInflater mInflater;
	private List<PSLike> mLikeList = new ArrayList<PSLike>(); 
	private FriendListAdapter mFriendListAdapter;
	private Context mContext;
	
	public LikeListDialogView(Context context, List<PSLike> likeList) {
		super(context);
		mLikeList = likeList;
		init(context);
	}

	public void init(final Context context) {
		mContext = context;

		mImageLoader=new ImageLoader(context, 6, ImageType.PSLOCALAVATAR);
		mImageLoader.setImagesToLoadFromLikeList(mLikeList);
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.like_list_dialog, this);
		mListView = (ListView) findViewById(R.id.friendlistView);
		mFriendListAdapter = new FriendListAdapter();
		mListView.setAdapter(mFriendListAdapter);

	}
	
	
	private class FriendListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mLikeList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.like_list_row, null);
            	holder = new ViewHolder();
            	holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            	holder.name = (TextView) convertView.findViewById(R.id.name);
            	convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
            
            final PSLike pHolder = mLikeList.get(position);
            holder.name.setText(pHolder.name);

            if (holder != null) {
				mImageLoader.displayImage(pHolder.id, holder.imageView, position);
			}
            return convertView;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}
	
    static class ViewHolder {
        TextView name;
        ImageView imageView;
    }
	
}
