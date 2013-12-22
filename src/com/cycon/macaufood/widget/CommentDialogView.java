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
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.PSDetailsView.PSComment;
import com.cycon.macaufood.widget.PSDetailsView.PSLike;
import com.cycon.macaufood.xmlhandler.FriendListXMLHandler;

public class CommentDialogView extends LinearLayout {
	private ImageLoader mImageLoader; 
	private ListView mListView;
	private LayoutInflater mInflater;
	private List<PSComment> mCommentList = new ArrayList<PSComment>(); 
	private FriendListAdapter mFriendListAdapter;
	private Context mContext;
	private static final int LIST_VIEW_HEIGHT_ROW = 48;
	
	public CommentDialogView(Context context, List<PSComment> commentList) {
		super(context);
		mCommentList = commentList;
		init(context);
	}

	public void init(final Context context) {
		mContext = context;

		mImageLoader=new ImageLoader(context, 6, ImageType.PSLOCALAVATAR);
		mImageLoader.setImagesToLoadFromCommentList(mCommentList);
		mImageLoader.setAllowedDuplicate(true);
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.comment_list_dialog, this);
		mListView = (ListView) findViewById(R.id.friendlistView);
		ViewGroup.LayoutParams params = mListView.getLayoutParams();
		if (mCommentList.size() < 3) {
			params.height = MFUtil.getPixelsFromDip(LIST_VIEW_HEIGHT_ROW * 3, mContext.getResources());
		} else if (mCommentList.size() > 5) {
			params.height = MFUtil.getPixelsFromDip(LIST_VIEW_HEIGHT_ROW * 7, mContext.getResources());
		} else {
			params.height = MFUtil.getPixelsFromDip(LIST_VIEW_HEIGHT_ROW * (mCommentList.size() + 1), mContext.getResources());
		}
		mFriendListAdapter = new FriendListAdapter();
		mListView.setAdapter(mFriendListAdapter);
	}
	
	
	private class FriendListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mCommentList.size();
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
			Log.e("ZZZ", "getView");
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.comment_list_row, null);
            	holder = new ViewHolder();
            	holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            	holder.name = (TextView) convertView.findViewById(R.id.name);
            	holder.comment = (TextView) convertView.findViewById(R.id.comment);
            	holder.time = (TextView) convertView.findViewById(R.id.time);
            	convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
            
            final PSComment pHolder = mCommentList.get(position);
            holder.name.setText(pHolder.name);
            holder.comment.setText(pHolder.comment);
            
            long time = Long.parseLong(pHolder.timeStamp);
    		holder.time.setText(MFUtil.getPastTime(time, mContext) + mContext.getResources().getString(R.string.before));

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
        TextView comment;
        TextView time;
    }
	
}
