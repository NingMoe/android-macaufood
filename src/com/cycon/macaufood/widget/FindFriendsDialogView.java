package com.cycon.macaufood.widget;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ParsedFriendsHolder;
import com.cycon.macaufood.bean.ParsedPSHotHolder;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFServiceCallBack;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.xmlhandler.FriendListXMLHandler;

public class FindFriendsDialogView extends LinearLayout {
	private View mLoadingProgressLayout;
	private ListView mListView;
	private LayoutInflater mInflater;
	private List<ParsedFriendsHolder> mHolderList = new ArrayList<ParsedFriendsHolder>(); 
	private FriendListAdapter mFriendListAdapter;
	
	public FindFriendsDialogView(Context context) {
		super(context);
		init(context);
	}

	public FindFriendsDialogView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init(context);
	}

	public void init(Context context) {
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.find_friends_dialog, this);
		mLoadingProgressLayout = findViewById(R.id.loadingProgressLayout);
		mListView = (ListView) findViewById(R.id.friendlistView);
		mFriendListAdapter = new FriendListAdapter();
		mListView.setAdapter(mFriendListAdapter);
		String url = MFURL.PHOTOSHARE_FIND_FRIENDS + PreferenceHelper.getPreferenceValueStr(context, MFConstants.PS_MEMBERID_PREF_KEY, "0");
		DefaultHandler handler = new FriendListXMLHandler(mHolderList);
		MFFetchListHelper.fetchList(url, handler, new MFServiceCallBack() {
			
			@Override
			public void onLoadResultSuccess() {
				mLoadingProgressLayout.setVisibility(View.GONE);
				mFriendListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onLoadResultError() {
				mLoadingProgressLayout.setVisibility(View.GONE);
			}
		});
	}
	
	private class FriendListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mHolderList.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.friend_list_row, null);
            	holder = new ViewHolder();
            	holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            	holder.name = (TextView) convertView.findViewById(R.id.name);
            	convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
            
            ParsedFriendsHolder pHolder = mHolderList.get(position);
            holder.name.setText(pHolder.getDetail());
//            if (holder != null) {
//				imageLoader.displayImage(psHolder.getFilename(), holder.image, position);
//			}
            return convertView;
		}
	}
	
    static class ViewHolder {
        TextView name;
        ImageView imageView;
    }
	
}
