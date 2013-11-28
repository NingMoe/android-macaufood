package com.cycon.macaufood.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
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
import com.cycon.macaufood.bean.ParsedPSHotHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFServiceCallBack;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.xmlhandler.FriendListXMLHandler;

public class FindFriendsDialogView extends LinearLayout {
	private ImageLoader mImageLoader; 
	private View mLoadingProgressLayout;
	private Button mFollowAll;
	private ListView mListView;
	private LayoutInflater mInflater;
	private List<ParsedFriendsHolder> mHolderList = new ArrayList<ParsedFriendsHolder>(); 
	private FriendListAdapter mFriendListAdapter;
	private String memberId;
	private int mRequestCurrentCount;
	private int mRequestTotalCount;
	private boolean mFollowAllError;
	private Context mContext;
	
	public FindFriendsDialogView(Context context) {
		super(context);
		init(context);
	}

	public FindFriendsDialogView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init(context);
	}

	public void init(final Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.find_friends_dialog, this);
		mLoadingProgressLayout = findViewById(R.id.loadingProgressLayout);
		mListView = (ListView) findViewById(R.id.friendlistView);
		mFollowAll = (Button) findViewById(R.id.followAll);
		mFriendListAdapter = new FriendListAdapter();
		mListView.setAdapter(mFriendListAdapter);
		memberId = PreferenceHelper.getPreferenceValueStr(context, MFConstants.PS_MEMBERID_PREF_KEY, "0");
		String url = MFURL.PHOTOSHARE_FIND_FRIENDS + memberId;
		DefaultHandler handler = new FriendListXMLHandler(mHolderList);
		MFFetchListHelper.fetchList(url, handler, new MFServiceCallBack() {
			
			@Override
			public void onLoadResultSuccess(Object result) {
				mLoadingProgressLayout.setVisibility(View.GONE);
				if (mHolderList.size() > 0) {
					mListView.setVisibility(View.VISIBLE);
					mImageLoader=new ImageLoader(context, 6, null);
					mImageLoader.setImagesToLoadFromParsedFriendsList(mHolderList);
					mFriendListAdapter.notifyDataSetChanged();
					
					for (ParsedFriendsHolder pHolder : mHolderList) {
						if (!pHolder.isFollowed()) {
							//if there is one friend not followed, show follow all button
							mFollowAll.setVisibility(View.VISIBLE);
							mFollowAll.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									mFollowAll.setVisibility(View.GONE);
									sendFollowAllRequest();
								}
							});
							break;
						}
					}
					

				} else {
					TextView tv = (TextView) findViewById(R.id.friendsListError);
					tv.setVisibility(View.VISIBLE);
					tv.setText(R.string.noFriendsFound);
				}
			}
			
			@Override
			public void onLoadResultError() {
				mLoadingProgressLayout.setVisibility(View.GONE);
				findViewById(R.id.friendsListError).setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void sendFollowAllRequest() {
		if (!MFConfig.isOnline(mContext)) {
			Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
			return;
		}
		
		
		for (int i = 0; i < mHolderList.size(); i++) {
			final ParsedFriendsHolder pHolder = mHolderList.get(i);
			if (pHolder.isFollowed()) {
				continue;
			}
			mRequestTotalCount++;
			pHolder.setLoading(true);
			
			String url = String.format(Locale.US, MFURL.PHOTOSHARE_FOLLOW_FRIENDS, pHolder.getId(), memberId, i);
			MFService.getString(url, null, new MFServiceCallBack() {
				
				@Override
				public void onLoadResultSuccess(Object result) {
					pHolder.setLoading(false);
					
					try {
						String tokens[] = ((String) result).split("\\|\\|\\|");
						String followedStr = tokens[2];
						if (followedStr.equals("1")) {
							pHolder.setFollowed(true);
						} else {
							pHolder.setFollowed(false);
						}
						mRequestCurrentCount++;
					} catch (Exception e) {
						onLoadResultError();
					}
					
					if (mRequestCurrentCount >= mRequestTotalCount) {
						mRequestCurrentCount = 0;
						mRequestTotalCount = 0;
						mFriendListAdapter.notifyDataSetChanged();
						
						if (mFollowAllError) {
							mFollowAllError = false;
							mFollowAll.setVisibility(View.VISIBLE);
							Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
						}
					}
				}
				
				@Override
				public void onLoadResultError() {
					mRequestCurrentCount++;
					mFollowAllError = true;
					pHolder.setLoading(false);
					
					if (mRequestCurrentCount >= mRequestTotalCount) {
						mRequestCurrentCount = 0;
						mRequestTotalCount = 0;
						mFriendListAdapter.notifyDataSetChanged();
						
						if (mFollowAllError) {
							mFollowAllError = false;
							mFollowAll.setVisibility(View.VISIBLE);
							Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
		}
		//to display button loading state
		mFriendListAdapter.notifyDataSetChanged();

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
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.friend_list_row, null);
            	holder = new ViewHolder();
            	holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            	holder.name = (TextView) convertView.findViewById(R.id.name);
            	holder.followButton = (Button) convertView.findViewById(R.id.followTv);
            	holder.pBar = (ProgressBar) convertView.findViewById(R.id.pBar);
            	convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
            
            final ParsedFriendsHolder pHolder = mHolderList.get(position);
            holder.followButton.setTag(position);
            if (pHolder.isLoading()) {
				holder.followButton.setEnabled(false);
				holder.pBar.setVisibility(View.VISIBLE);
				holder.followButton.setText("");
            } else {
				holder.followButton.setEnabled(true);
				holder.pBar.setVisibility(View.GONE);
	            if (pHolder.isFollowed()) {
					holder.followButton.setText(R.string.unfollow);
				} else {
					holder.followButton.setText(R.string.follow);
				}
            }
            holder.name.setText(pHolder.getName());

            holder.followButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					pHolder.setLoading(true);
					holder.followButton.setEnabled(false);
					holder.pBar.setVisibility(View.VISIBLE);
					holder.followButton.setText("");
					String url = String.format(Locale.US, MFURL.PHOTOSHARE_FOLLOW_FRIENDS, pHolder.getId(), memberId, position);
					MFService.getString(url, null, new MFServiceCallBack() {
						
						@Override
						public void onLoadResultSuccess(Object result) {
							// TODO Auto-generated method stub

							pHolder.setLoading(false);
							holder.pBar.setVisibility(View.GONE);
							holder.followButton.setEnabled(true);
							
							try {
								String tokens[] = ((String) result).split("\\|\\|\\|");
								int tag = Integer.parseInt(tokens[1]);
								String followedStr = tokens[2];
								if (followedStr.equals("1")) {
									pHolder.setFollowed(true);
									if ((Integer)holder.followButton.getTag() == tag)
									holder.followButton.setText(R.string.unfollow);
								} else {
									pHolder.setFollowed(false);
									if ((Integer)holder.followButton.getTag() == tag)
									holder.followButton.setText(R.string.follow);
								}
							} catch (Exception e) {
								onLoadResultError();
							}
							
						}
						
						@Override
						public void onLoadResultError() {
							pHolder.setLoading(false);
							holder.pBar.setVisibility(View.GONE);
							holder.followButton.setEnabled(true);
				            if (pHolder.isFollowed()) {
								holder.followButton.setText(R.string.unfollow);
							} else {
								holder.followButton.setText(R.string.follow);
							}
							Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
            if (holder != null) {
				mImageLoader.displayImage(pHolder.getId(), holder.imageView, position);
			}
            return convertView;
		}
	}
	
    static class ViewHolder {
        TextView name;
        ImageView imageView;
        Button followButton;
        ProgressBar pBar;
    }
	
}
