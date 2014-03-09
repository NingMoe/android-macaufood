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
import com.cycon.macaufood.xmlhandler.FriendListXMLHandler;

public class FindFriendsDialogView extends LinearLayout {
	private ImageLoader mImageLoader; 
	private View mLoadingProgressLayout;
	private Button mFollowAll;
	private ListView mListView;
	private LayoutInflater mInflater;
	private List<ParsedFriendsHolder> mHolderList = new ArrayList<ParsedFriendsHolder>(); 
	private LinkedList<ParsedFriendsHolder> mFollowAllList = new LinkedList<ParsedFriendsHolder>();
	private FriendListAdapter mFriendListAdapter;
	private int mRequestCompleteCount;
	private int mRequestTotalCount;
	private boolean mFollowAllError;
	private boolean mModified;
	private Context mContext;
	
	public FindFriendsDialogView(Context context) {
		super(context);
		init(context);
	}

	public void init(final Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.find_friends_dialog, this);
		mLoadingProgressLayout = findViewById(R.id.loadingProgressLayout);
		mListView = (ListView) findViewById(R.id.friendlistView);
		mFollowAll = (Button) findViewById(R.id.followAll);
		mFollowAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				sendFollowAllRequest();
			}
		});
		mFriendListAdapter = new FriendListAdapter();
		mListView.setAdapter(mFriendListAdapter);
		String url = MFURL.PHOTOSHARE_FIND_FRIENDS + MFConfig.memberId;
//		String url = MFURL.PHOTOSHARE_FIND_FRIENDS + "29";
		DefaultHandler handler = new FriendListXMLHandler(mHolderList);
		MFFetchListHelper.fetchList(url, handler, null, new MFServiceCallBack() {
			
			@Override
			public void onLoadResultSuccess(Object result) {
				mLoadingProgressLayout.setVisibility(View.GONE);
				if (mHolderList.size() > 0) {
					mListView.setVisibility(View.VISIBLE);
					mImageLoader=new ImageLoader(context, 6, ImageType.PSLOCALAVATAR);
					mImageLoader.setImagesToLoadFromParsedFriendsList(mHolderList);
					mFriendListAdapter.notifyDataSetChanged();
					checkIfShowFollowAll();
				} else {
					TextView tv = (TextView) findViewById(R.id.friendsListError);
					tv.setVisibility(View.VISIBLE);
					tv.setText(R.string.noFriendsFound);
				}
			}
			
			@Override
			public void onLoadResultError() {
				mLoadingProgressLayout.setVisibility(View.GONE);
				TextView tv = (TextView) findViewById(R.id.friendsListError);
				tv.setVisibility(View.VISIBLE);
				tv.setText(R.string.errorMsg);
			}
		});
	}
	
	private void checkIfShowFollowAll() {
		int count = 0;
		for (ParsedFriendsHolder pHolder : mHolderList) {
			if (!pHolder.isFollowed()) {
				count++;
				if (count >= 1) {
					break;
				}
			}
		}
		
		if (count >= 1) {
			mFollowAll.setVisibility(View.VISIBLE);
		} else {
			mFollowAll.setVisibility(View.GONE);
		}
	}
	
	private void sendFollowAllRequest() {
		if (!MFConfig.isOnline(mContext)) {
			Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
			return;
		}
		mFollowAll.setVisibility(View.GONE);
		
		for (int i = 0; i < mHolderList.size(); i++) {
			ParsedFriendsHolder pHolder = mHolderList.get(i);
			if (!pHolder.isFollowed()) {
				mFollowAllList.add(pHolder);
				pHolder.setLoading(true);
				mRequestTotalCount++;
			}
		}
		
		final int maxNumberTasks = 20;
		for (int i = 0; i < maxNumberTasks && !mFollowAllList.isEmpty(); i++) {
			sendOneRequest(mFollowAllList.poll());
		}
		
		//to display button loading state
		mFriendListAdapter.notifyDataSetChanged();

	}
		
    private void sendOneRequest(final ParsedFriendsHolder pHolder) {
//		mRequestTotalCount++;
//		pHolder.setLoading(true);
		
		String url = String.format(Locale.US, MFURL.PHOTOSHARE_FOLLOW_FRIENDS, pHolder.getId(), MFConfig.memberId, 0);
		
		MFService.getString(url, null, new MFServiceCallBack() {
			
			@Override
			public void onLoadResultSuccess(Object result) {
				mModified = true;
				pHolder.setLoading(false);
				
				try {
					String tokens[] = ((String) result).split("\\|\\|\\|");
					String followedStr = tokens[2];
					if (followedStr.equals("1")) {
						pHolder.setFollowed(true);
					} else {
						pHolder.setFollowed(false);
					}
					mRequestCompleteCount++;
				} catch (Exception e) {
					onLoadResultError();
				}
				
				doPostLoadResult();
			}
			
			@Override
			public void onLoadResultError() {
				mRequestCompleteCount++;
				mFollowAllError = true;
				pHolder.setLoading(false);
				
				doPostLoadResult();
			}
		});
	}
    
    private void doPostLoadResult() {
		//send another request after one request is done, keep loading tasks as 20
		if (!mFollowAllList.isEmpty()) {
			sendOneRequest(mFollowAllList.poll());
		}
		
		if (mRequestCompleteCount >= mRequestTotalCount) {
			mRequestCompleteCount = 0;
			mRequestTotalCount = 0;
			mFriendListAdapter.notifyDataSetChanged();
			
			if (mFollowAllError) {
				mFollowAllError = false;
				mFollowAll.setVisibility(View.VISIBLE);
				Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.followAllSucceed, Toast.LENGTH_SHORT).show();
			}
		}
    }
    
    private void setButtonState(boolean followed, Button button) {
    	if (followed) {
			button.setText(R.string.unfollow);
			button.setBackgroundResource(R.drawable.button_gray_selector);
			button.setTextColor(mContext.getResources().getColor(R.color.button_gray_text));
		} else {
			button.setText(R.string.follow);
			button.setBackgroundResource(R.drawable.button_green_selector);
			button.setTextColor(Color.WHITE);
		}
    }
    
    public boolean isModified() {
    	return mModified;
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
	            	setButtonState(true, holder.followButton);
				} else {
	            	setButtonState(false, holder.followButton);
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
					String url = String.format(Locale.US, MFURL.PHOTOSHARE_FOLLOW_FRIENDS, pHolder.getId(), MFConfig.memberId, position);
					MFService.getString(url, null, new MFServiceCallBack() {
						
						@Override
						public void onLoadResultSuccess(Object result) {
							// TODO Auto-generated method stub

							mModified = true;
							pHolder.setLoading(false);
							holder.pBar.setVisibility(View.GONE);
							holder.followButton.setEnabled(true);
							
							try {
								String tokens[] = ((String) result).split("\\|\\|\\|");
								int tag = Integer.parseInt(tokens[1]);
								String followedStr = tokens[2];
								if (followedStr.equals("1")) {
									pHolder.setFollowed(true);
									checkIfShowFollowAll();
									if ((Integer)holder.followButton.getTag() == tag) {
						            	setButtonState(true, holder.followButton);
									}
								} else {
									pHolder.setFollowed(false);
									checkIfShowFollowAll();
									if ((Integer)holder.followButton.getTag() == tag) {
						            	setButtonState(false, holder.followButton);
									}
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
				            	setButtonState(true, holder.followButton);
							} else {
				            	setButtonState(false, holder.followButton);
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
