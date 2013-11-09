package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.PSHotAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFUtil;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

@SuppressLint("NewApi")
public class PhotoShare extends SherlockFragment implements OnMenuItemClickListener{

	private static final String TAG = PhotoShare.class.getName();
	
	private static final int MENU_FIND_FRIENDS = 1;
	private static final int MENU_LOGOUT = 2;
	private static final int MENU_TAKE_PHOTO = 3;
	private static final int MENU_USE_ALBUM = 4;
	
	private View retryLayout;
	private Button retryButton;
	private GridView mHotGV;
	private PSHotAdapter mPsHotAdapter;
	private FileCache fileCache;
	private Context mContext;
	private View mView;
	
	private TextView mPsFriends;
	private TextView mPsHot;
	private View mPsCamera;
	private View mPsSettings;
	private View mFriendsLayoutSV;
	private int mCurrentTab = 1; //friends = 0, hot = 1;
	
	private AlertDialog mLoginDialog;
	private PopupMenu mSettingsMenu;
	private PopupMenu mCameraMenu;
	
	//FB
	private LoginButton mLoginButton;
	private GraphUser mUser;
	private UiLifecycleHelper uiHelper;
	
	public boolean mIsVisible;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.photo_share, null);
		initView();
		return mView;
	}
	
	private void initView() {
		mHotGV = (GridView) mView.findViewById(R.id.hotLayoutGV);
		int paddingInPx = MFUtil.getPixelsFromDip(PSHotAdapter.SPACING_IN_DP, getResources());
		int paddingForImageBg = MFUtil.getPixelsFromDip(2, getResources());
		mHotGV.setPadding(paddingInPx + paddingForImageBg, paddingInPx + paddingForImageBg * 2, paddingInPx + paddingForImageBg, paddingInPx);
        retryLayout = mView.findViewById(R.id.retryLayout);
		mPsHotAdapter = new PSHotAdapter(mContext, MFConfig.getInstance().getPsHotList());
		mHotGV.setAdapter(mPsHotAdapter);
		mHotGV.setOnItemClickListener(itemClickListener);
        
		if (MFConfig.getInstance().getPsHotList().size() == 0) {
			if (!MFConfig.isOnline(mContext)) {
        		displayRetryLayout();
        	}
		}
		mFriendsLayoutSV = mView.findViewById(R.id.friendsLayoutSV);
		
		mPsFriends = (TextView) mView.findViewById(R.id.psFriends);
		mPsFriends.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mCurrentTab == 0) return;
				mPsHot.setSelected(false);
				mPsFriends.setSelected(true);
				mCurrentTab = 0;
				mHotGV.setVisibility(View.GONE);
				mFriendsLayoutSV.setVisibility(View.VISIBLE);
			}
		});
		mPsHot = (TextView) mView.findViewById(R.id.psHot);
		mPsHot.setSelected(true);
		mPsHot.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mCurrentTab == 1) return;
				mPsHot.setSelected(true);
				mPsFriends.setSelected(false);
				mCurrentTab = 1;
				mHotGV.setVisibility(View.VISIBLE);
				mFriendsLayoutSV.setVisibility(View.GONE);
			}
		});
		mPsCamera = mView.findViewById(R.id.psCamera);
		mPsCamera.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				checkLogin(PendingAction.CAMERA);
			}
		});
		mPsSettings = mView.findViewById(R.id.psSettings);
		mPsSettings.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				checkLogin(PendingAction.SETTINGS);
			}
		});

	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);

        mContext = getActivity();
        
        fileCache=new FileCache(mContext, ImageType.PHOTOSHARE_HOT);
        File f=fileCache.getFile(MFConstants.PS_HOT_XML_FILE_NAME);
		try {
			FileInputStream is = new FileInputStream(f);
			MFFetchListHelper.parseXml(is, MFConfig.tempParsedPSHotList, MFConfig.getInstance().getPsHotList());
		} catch (FileNotFoundException e) {
	    	MFLog.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 

		//refresh when file cache xml is deleted by user
        if (MFConfig.getInstance().getPsHotList().size() == 0 && !MFFetchListHelper.isFetching && !((Home)getActivity()).isShowingDisClaimer()) {
        	refresh();
		}
        
    }
    
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			Intent i = new Intent(mContext, PSDetails.class);
			i.putExtra("ps_hot_position", position);
			startActivity(i);
		}
    };
    
    public void displayRetryLayout() {
		retryLayout.setVisibility(View.VISIBLE);
		retryButton = (Button) mView.findViewById(R.id.retryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				refresh();
			}
		});
    }
    
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			mIsVisible = true;
		} else {
			mIsVisible = false;
		}

	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		if (mIsVisible) {
//			((Home)activity).hideBanner();
//		} else {
//			((Home)activity).showBanner();
//		}
		
	}
	
    public void populateGridView() {
		//if no internet and no data in File, show retry message
		if (MFConfig.getInstance().getPsHotList().size() == 0) {
			displayRetryLayout();
		} else {
			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
		mPsHotAdapter.imageLoader.cleanup();
		mPsHotAdapter.imageLoader.setTaskMaxNumber(MFConfig.getInstance().getPsHotList().size());
		mPsHotAdapter.imageLoader.setImagesToLoadFromParsedPSHot(MFConfig.getInstance().getPsHotList());
		mPsHotAdapter.notifyDataSetChanged();
    }
    
	public void refresh() {
		if (MFConfig.isOnline(mContext)) {
			((Home)getActivity()).refresh();
        	
			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
	}
	
	private void checkLogin(PendingAction pa) {
		Session session = Session.getActiveSession();
		boolean login = session != null && session.isOpened();
		if (login) {
			handlePendingAction(pa);
		} else {
			showLoginDialog(pa);
		}
	}
	
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FIND_FRIENDS:
			break;
		case MENU_LOGOUT:
			callFacebookLogout();
			break;
		case MENU_TAKE_PHOTO:
			break;
		case MENU_USE_ALBUM:
			break;
		default:
			break;
		}
		// TODO Auto-generated method stub
		return false;
	}
	
	private void handlePendingAction(PendingAction pa) {
		switch (pa) {
		case SETTINGS:
			if (mSettingsMenu == null) {
				mSettingsMenu = new PopupMenu(mContext, mPsSettings);
				mSettingsMenu.getMenu().add(Menu.NONE, MENU_FIND_FRIENDS, Menu.NONE, R.string.findFriends);
				mSettingsMenu.getMenu().add(Menu.NONE, MENU_LOGOUT, Menu.NONE, R.string.logout);
				mSettingsMenu.setOnMenuItemClickListener(this);
			}
			mSettingsMenu.show();
			break;
		case CAMERA:
			// TODO
			break;
		default:
		}
	}
	
	//FB logic
	
    private enum PendingAction {
        NONE,
        FRIENDS,
        CAMERA,
        SETTINGS,
    }
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
    	@Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
    	@Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.e("HelloFacebook", String.format("Error: %s", error.toString()));
        }
    	
    	@Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.e("HelloFacebook", "Success!");
        }
    };
	
	private void showLoginDialog(final PendingAction pa) {
		
		Session session = Session.getActiveSession();
		if (session == null) {
			Log.e("ZZZ", "session null");
		}
		boolean enableButtons = (session != null && session.isOpened());
		Log.e("ZZZ", "open " + enableButtons);
		
		View view = getActivity().getLayoutInflater().inflate(R.layout.login_dialog, null);
		TextView fbTv = (TextView) view.findViewById(R.id.fbLogin);
		TextView weiboTv = (TextView) view.findViewById(R.id.weiboLogin);
		
		if (mLoginButton == null) {
			Log.e("ZZZ", "login button == null");
			mLoginButton = (LoginButton) view.findViewById(R.id.login_button);
			mLoginButton.setFragment(this);
			mLoginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
				
				@Override
	            public void onUserInfoFetched(GraphUser user) {
	            	mUser = user;
	            	if (user != null) {
	            		Toast.makeText(mContext, getString(R.string.loginMessage, user.getFirstName()), Toast.LENGTH_LONG).show();
	            	}
	                handlePendingAction(pa);
	            }
	        });
		}
//		fbTv.setOnClickListener(new OnClickListener() {
//			
//			public void onClick(View arg0) {
//				startFacebookLogin();
//			}
//		});
		weiboTv.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
//				 Session.openActiveSession(getActivity(), true, new Session.StatusCallback() {
//
//				      // callback when session changes state
//				      public void call(Session session, SessionState state, Exception exception) {
//				        if (session.isOpened()) {
//				        	Log.e("ZZZ", "isOpened!!!!!!!!!!!");
//				        }
//				      }
//				    });
			}
		});
		
		mLoginDialog = new AlertDialog.Builder(mContext)
		.setTitle(R.string.pleaseLogin)
		.setView(view)
		.setPositiveButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
	}
	
	private void callFacebookLogout() {
	    Session session = Session.getActiveSession();
	    if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
	    }
	}

	
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (session.isOpened()) {
			if (mLoginDialog != null) {
				mLoginDialog.dismiss();
			}
		}
    	if (exception != null) {
    		Log.e("ZZZ", "exception= " + exception.getMessage());
    	}
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy()
    {
    	MFLog.e(TAG, "onDestroy");
    	if (mHotGV != null) {
    		mHotGV.setAdapter(null);
    	}
        super.onDestroy();
        uiHelper.onDestroy();
    }



}
