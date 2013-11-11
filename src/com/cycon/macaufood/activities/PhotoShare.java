package com.cycon.macaufood.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.PSHotAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

public class PhotoShare extends SherlockFragment{

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
	private UiLifecycleHelper uiHelper;
	
	public boolean mIsVisible;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class MenuClickListener implements OnMenuItemClickListener {
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
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
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
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			registerForContextMenu(mPsCamera);
			registerForContextMenu(mPsSettings);
		}

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v == mPsCamera) {
			menu.add(Menu.NONE, MENU_USE_ALBUM, Menu.NONE, R.string.useAlbum);
			menu.add(Menu.NONE, MENU_TAKE_PHOTO, Menu.NONE, R.string.takePhoto);
		} else if (v == mPsSettings) {
			menu.add(Menu.NONE, MENU_FIND_FRIENDS, Menu.NONE, R.string.findFriends);
			menu.add(Menu.NONE, MENU_LOGOUT, Menu.NONE, R.string.logout);
		}
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void handlePendingAction(PendingAction pa) {
		switch (pa) {
		case SETTINGS:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				getActivity().openContextMenu(mPsSettings);
			} else {
				if (mSettingsMenu == null) {
					mSettingsMenu = new PopupMenu(mContext, mPsSettings);
					mSettingsMenu.getMenu().add(Menu.NONE, MENU_FIND_FRIENDS, Menu.NONE, R.string.findFriends);
					mSettingsMenu.getMenu().add(Menu.NONE, MENU_LOGOUT, Menu.NONE, R.string.logout);
					mSettingsMenu.setOnMenuItemClickListener(new MenuClickListener());
				}
				mSettingsMenu.show();
			}
			break;
		case CAMERA:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				getActivity().openContextMenu(mPsCamera);
			} else {
				if (mCameraMenu == null) {
					mCameraMenu = new PopupMenu(mContext, mPsSettings);
					mCameraMenu.getMenu().add(Menu.NONE, MENU_USE_ALBUM, Menu.NONE, R.string.useAlbum);
					mCameraMenu.getMenu().add(Menu.NONE, MENU_TAKE_PHOTO, Menu.NONE, R.string.takePhoto);
					mCameraMenu.setOnMenuItemClickListener(new MenuClickListener());
				}
				mCameraMenu.show();
			}
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
		
		View view = getActivity().getLayoutInflater().inflate(R.layout.login_dialog, null);
		TextView fbTv = (TextView) view.findViewById(R.id.fbLogin);
		TextView weiboTv = (TextView) view.findViewById(R.id.weiboLogin);
		
//		if (mLoginButton == null) {
			mLoginButton = (LoginButton) view.findViewById(R.id.login_button);
			mLoginButton.setReadPermissions(Arrays.asList("email"));
			mLoginButton.setFragment(this);
			mLoginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
				
				@Override
	            public void onUserInfoFetched(GraphUser user) {
					Log.e("ZZZ", "userinfo");
	            	if (user != null) {
	            		Toast.makeText(mContext, getString(R.string.loginMessage, user.getName()), Toast.LENGTH_SHORT).show();
	            		AsyncTaskHelper.executeWithResultString(new RegisterPS(user));
	            		handlePendingAction(pa);
	            	}
	            }
	        });
//		}
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
            Toast.makeText(mContext, R.string.logoutMessage, Toast.LENGTH_SHORT).show();
	    }
	}

	
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	Log.e("ZZZ", "token = " + session.getAccessToken());
		Log.e("ZZZ", "expire tmie = " + session.getExpirationDate());
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

    private class RegisterPS extends AsyncTask<Void, Void, String> {
    	
    	private GraphUser user;
    	
    	public RegisterPS(GraphUser user) {
    		this.user = user;
		}
    	
    	@Override
    	protected String doInBackground(Void... params) {
    		// TODO Auto-generated method stub
    		
    		try {
    			Object email = user.asMap().get("email");
    			Object gender = user.asMap().get("gender");
    			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    			pairs.add(new BasicNameValuePair("udid", MFConfig.DEVICE_ID));
    			pairs.add(new BasicNameValuePair("email", email == null ? "" : email.toString()));
    			pairs.add(new BasicNameValuePair("name", user.getName()));
    			pairs.add(new BasicNameValuePair("fbid", user.getId()));
    			pairs.add(new BasicNameValuePair("gender", gender == null ? "" : gender.toString()));
    			pairs.add(new BasicNameValuePair("fbtoken", Session.getActiveSession().getAccessToken()));
    			pairs.add(new BasicNameValuePair("fbexpire", Session.getActiveSession().getExpirationDate().toString()));
    			pairs.add(new BasicNameValuePair("pic_link", "https://graph.facebook.com/" + user.getId() + "/picture"));
    			pairs.add(new BasicNameValuePair("devicetoken", "0"));
    			InputStream is = MFService.executeRequestWithHttpParams(MFURL.PHOTOSHARE_REGISTER + "f", pairs);
    			StringBuilder sb = new StringBuilder();
    			BufferedReader rd = new BufferedReader(new InputStreamReader(
    					is));
    			String line = null;
    			while ((line = rd.readLine()) != null) {
    				sb.append(line + "\n");
    			}
    			rd.close();

    			return sb.toString().trim();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {

			}
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
    		
    		PreferenceHelper.savePreferencesStr(mContext, MFConstants.PS_MEMBERID_PREF_KEY, result);
    		PreferenceHelper.savePreferencesStr(mContext, MFConstants.PS_MEMBERNAME_PREF_KEY, user.getName());
    		Log.e("ZZZ", "result = " + result);
    		
    	}
    	
    }


}
