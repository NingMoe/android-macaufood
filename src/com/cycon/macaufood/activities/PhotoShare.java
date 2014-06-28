package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityOptionsCompat;
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
import com.cycon.macaufood.adapters.PSFriendsActivityAdapter;
import com.cycon.macaufood.adapters.PSHotAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.LoginHelper;
import com.cycon.macaufood.utilities.LoginHelper.PendingAction;
import com.cycon.macaufood.utilities.LoginHelper.RegisterPSCallBack;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFServiceCallBack;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.FindFriendsDialogView;
import com.cycon.macaufood.widget.PSDetailsView.DetailsViewCallback;
import com.cycon.macaufood.xmlhandler.PSDetailXMLHandler;
import com.facebook.UiLifecycleHelper;

public class PhotoShare extends SherlockFragment {

	private static final String TAG = PhotoShare.class.getName();

	private static final long REFRESH_FRIENDS_ACTIVITY_TIME_PERIOD = 3600 * 1000 * 1; // 1
																						// hour

	private static final int MENU_FIND_FRIENDS = 1;
	private static final int MENU_LOGOUT = 2;
	private static final int MENU_TAKE_PHOTO = 3;
	private static final int MENU_USE_ALBUM = 4;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 7001;
	private static final int CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE = 7002;
	private static final int UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE = 8001;

	private View retryLayout;
	private Button retryButton;
	private GridView mHotGV;
	private PSHotAdapter mPsHotAdapter;
	private FileCache fileCache;
	private Context mContext;
	private View mView;

	private TextView mPsFriendsTab;
	private TextView mPsHotTab;
	private View mPsCameraTab;
	private View mPsSettingsTab;

	private long mFriendsActivityTimeStamp;
	private boolean mFirstShowFriendsActivity;
	private View mFriendsActivityLayout;
	private View mProgressBar;
	private Button mFriendsActivityFindFriendsButton;
	private TextView mFriendsActivityError;
	private StickyListHeadersListView mFriendsActivityListView;
	private PSFriendsActivityAdapter mFriendsActivityAdapter;

	private int mCurrentTab = 1; // friends = 0, hot = 1;

	private PopupMenu mSettingsMenu;
	private PopupMenu mCameraMenu;

	public boolean mIsVisible;
	private LoginHelper mLoginHelper;

	private Uri fileUri;
	private File mPhotoFile;

	// FB
	private UiLifecycleHelper uiHelper;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class MenuClickListener implements OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case MENU_FIND_FRIENDS:
				showFindFriendsDialog();
				break;
			case MENU_LOGOUT:
				callLogout(true);
				break;
			case MENU_TAKE_PHOTO:
				takePhoto();
				break;
			case MENU_USE_ALBUM:
				useAlbum();
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
			showFindFriendsDialog();
			break;
		case MENU_LOGOUT:
			callLogout(true);
			break;
		case MENU_TAKE_PHOTO:
			takePhoto();
			break;
		case MENU_USE_ALBUM:
			useAlbum();
			break;
		default:
			break;
		}
		// TODO Auto-generated method stub
		return false;
	}

	private void takePhoto() {
		// create Intent to take a picture and return control to the calling
		// application
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		mPhotoFile = MFUtil.getOutputMediaFile(MFUtil.MEDIA_TYPE_IMAGE);
		fileUri = Uri.fromFile(mPhotoFile); // create a file to save the image
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
															// name

		// start the image capture Intent
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	private void useAlbum() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");

		// start the image capture Intent
		startActivityForResult(intent, CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE);
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
		int paddingInPx = MFUtil.getPixelsFromDip(PSHotAdapter.SPACING_IN_DP,
				getResources());
		int paddingForImageBg = MFUtil.getPixelsFromDip(2, getResources());
		mHotGV.setPadding(paddingInPx + paddingForImageBg, paddingInPx
				+ paddingForImageBg * 2, paddingInPx + paddingForImageBg,
				paddingInPx);
		retryLayout = mView.findViewById(R.id.retryLayout);
		mPsHotAdapter = new PSHotAdapter(mContext, MFConfig.getInstance()
				.getPsHotList());
		mHotGV.setAdapter(mPsHotAdapter);
		mHotGV.setOnItemClickListener(itemClickListener);

		if (MFConfig.getInstance().getPsHotList().size() == 0) {
			if (!MFConfig.isOnline(mContext)) {
				displayRetryLayout();
			}
		}
		mFriendsActivityLayout = mView.findViewById(R.id.friendsActivityLayout);
		mFriendsActivityFindFriendsButton = (Button) mView
				.findViewById(R.id.findFriendsButton);
		mProgressBar = mView.findViewById(R.id.psProgressBar);
		if (MFConfig.isOnline(mContext) && !MFConfig.hasAlreadyRefreshList) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		mFriendsActivityError = (TextView) mView
				.findViewById(R.id.friendsActivityError);
		mFriendsActivityListView = (StickyListHeadersListView) mView
				.findViewById(R.id.friendsActivityListView);
		mFriendsActivityAdapter = new PSFriendsActivityAdapter(mContext,
				MFConfig.getInstance().getFriendsActivityList(), mLoginHelper,
				new DetailsViewCallback() {

					@Override
					public void onDeletePhoto() {
						mFriendsActivityAdapter.notifyDataSetChanged();
						mPsHotAdapter.notifyDataSetChanged();
					}
				});
		mFriendsActivityListView.setAdapter(mFriendsActivityAdapter);

		mFriendsActivityFindFriendsButton
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						showFindFriendsDialog();
					}
				});

		mPsFriendsTab = (TextView) mView.findViewById(R.id.psFriends);
		mPsFriendsTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				checkLogin(PendingAction.FRIENDS);
			}
		});
		mPsHotTab = (TextView) mView.findViewById(R.id.psHot);
		mPsHotTab.setSelected(true);
		mPsHotTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				switchToHotTab();
			}
		});
		mPsCameraTab = mView.findViewById(R.id.psCamera);
		mPsCameraTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				checkLogin(PendingAction.CAMERA);
			}
		});
		mPsSettingsTab = mView.findViewById(R.id.psSettings);
		mPsSettingsTab.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				checkLogin(PendingAction.SETTINGS);
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			registerForContextMenu(mPsCameraTab);
			registerForContextMenu(mPsSettingsTab);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		// reload in case photo deleted in ps details view.
		mPsHotAdapter.notifyDataSetChanged();
	}

	private void switchToHotTab() {
		if (mCurrentTab == 1)
			return;
		mPsHotTab.setSelected(true);
		mPsFriendsTab.setSelected(false);
		mCurrentTab = 1;
		mHotGV.setVisibility(View.VISIBLE);
		mFriendsActivityLayout.setVisibility(View.GONE);
	}

	private void switchToFriendsTab() {
		if (mCurrentTab == 0)
			return;
		mPsHotTab.setSelected(false);
		mPsFriendsTab.setSelected(true);
		mCurrentTab = 0;
		mHotGV.setVisibility(View.GONE);
		mFriendsActivityLayout.setVisibility(View.VISIBLE);
		mFriendsActivityAdapter.notifyDataSetChanged();
		if (System.currentTimeMillis() - mFriendsActivityTimeStamp > REFRESH_FRIENDS_ACTIVITY_TIME_PERIOD) {
			loadFriendsActivity();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v == mPsCameraTab) {
			menu.add(Menu.NONE, MENU_USE_ALBUM, Menu.NONE, R.string.useAlbum);
			menu.add(Menu.NONE, MENU_TAKE_PHOTO, Menu.NONE, R.string.takePhoto);
		} else if (v == mPsSettingsTab) {
			menu.add(Menu.NONE, MENU_FIND_FRIENDS, Menu.NONE,
					R.string.findFriends);
			menu.add(Menu.NONE, MENU_LOGOUT, Menu.NONE, R.string.logout);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();

		mLoginHelper = new LoginHelper(mContext);
		// FB
		uiHelper = new UiLifecycleHelper(getActivity(),
				mLoginHelper.getFBSessionCallback());
		uiHelper.onCreate(savedInstanceState);

		fileCache = new FileCache(mContext, ImageType.PHOTOSHARE);

		if (MFConfig.isOnline(mContext) && !MFConfig.hasAlreadyRefreshList) { // need
																				// to
																				// fetch
																				// hot
																				// list
																				// again
																				// if
																				// not
																				// already
																				// fetched
																				// all
																				// list
			MFFetchListHelper.fetchPSHotList((Home) mContext);
		} else {
			File f = fileCache.getFile(MFConstants.PS_HOT_XML_FILE_NAME);
			try {
				FileInputStream is = new FileInputStream(f);
				MFFetchListHelper.parseXml(is, MFConfig.tempParsedPSHotList,
						MFConfig.getInstance().getPsHotList());
			} catch (FileNotFoundException e) {
				MFLog.e(TAG, "FileNotFoundException");
				e.printStackTrace();
			}
		}

		if (MFConfig.memberId == null) {
			MFConfig.memberId = PreferenceHelper.getPreferenceValueStr(
					mContext, MFConstants.PS_MEMBERID_PREF_KEY, null);
			if (MFConfig.memberId == null) {
				callLogout(false);
			}
		}

		if (MFConfig.memberName == null) {
			MFConfig.memberName = PreferenceHelper.getPreferenceValueStr(
					mContext, MFConstants.PS_MEMBERNAME_PREF_KEY, null);
		}

	}

	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		@SuppressLint("NewApi")
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			Intent i = new Intent(mContext, PSDetails.class);
			i.putExtra("ps_hot_position", position);
			
			if(Build.VERSION.SDK_INT < 11) {
				startActivity(i);
			} else {
				Bundle scaledBundle = ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
				getActivity().startActivity(i, scaledBundle);
			}
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

	// reload friends activity and show progress bar when there is internet
	public void loadFriendsActivity() {
		File f = fileCache
				.getFile(MFConstants.PS_FRIENDS_ACTIVITY_XML_FILE_NAME);
		DefaultHandler handler = new PSDetailXMLHandler(MFConfig.getInstance()
				.getFriendsActivityList());
		MFConfig.getInstance().getFriendsActivityList().clear();
		mFriendsActivityError.setVisibility(View.GONE);
		mFriendsActivityFindFriendsButton.setVisibility(View.INVISIBLE);

		if (!MFConfig.isOnline(mContext)) {
			try {
				FileInputStream is = new FileInputStream(f);
				MFFetchListHelper.parseXml(is, handler);
			} catch (FileNotFoundException e) {
				MFLog.e(TAG, "FileNotFoundException");
				e.printStackTrace();
			}
			mFriendsActivityAdapter.notifyDataSetChanged();
			return;
		}

		mProgressBar.setVisibility(View.VISIBLE);

		String url = MFURL.PHOTOSHARE_SHOW_PHOTOS + MFConfig.memberId;
		// String url = MFURL.PHOTOSHARE_SHOW_PHOTOS + "491";

		MFFetchListHelper.fetchList(url, handler, f, new MFServiceCallBack() {

			@Override
			public void onLoadResultSuccess(Object result) {
				mProgressBar.setVisibility(View.GONE);
				mFriendsActivityTimeStamp = System.currentTimeMillis();
				ArrayList<String> infoList = MFConfig.getInstance()
						.getFriendsActivityList();
				if (infoList.size() > 0) {
					mFriendsActivityAdapter.psDetailsImageLoader.cleanup();
					mFriendsActivityAdapter.psDetailsImageLoader
							.setPSDetailsImagesToLoadFromParsedPS(infoList);
					mFriendsActivityAdapter.psHeaderImageLoader.cleanup();
					mFriendsActivityAdapter.psHeaderImageLoader
							.setProfileImagesToLoadFromParsedPS(infoList);
					mFriendsActivityAdapter.notifyDataSetChanged();
				} else {
					mFriendsActivityError.setVisibility(View.VISIBLE);
					mFriendsActivityFindFriendsButton
							.setVisibility(View.VISIBLE);
					if (mFirstShowFriendsActivity) {
						String userName = PreferenceHelper
								.getPreferenceValueStr(mContext,
										MFConstants.PS_MEMBERNAME_PREF_KEY, "");
						String msg = mContext.getString(
								R.string.firstShowFriendsActivityMsg, userName);
						mFriendsActivityError.setText(msg);
					} else {
						mFriendsActivityError
								.setText(R.string.noFriendsActivityFound);
					}
				}
				mFirstShowFriendsActivity = false;
			}

			@Override
			public void onLoadResultError() {
				mFriendsActivityError.setVisibility(View.VISIBLE);
				mFriendsActivityError.setText(R.string.errorMsg);
				mProgressBar.setVisibility(View.GONE);
			}
		});
	}

	public void populateGridView() {
		mProgressBar.setVisibility(View.GONE);
		// if no internet and no data in File, show retry message
		if (MFConfig.getInstance().getPsHotList().size() == 0) {
			displayRetryLayout();
		} else {
			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
		mPsHotAdapter.imageLoader.cleanup();
		mPsHotAdapter.imageLoader.setTaskMaxNumber(MFConfig.getInstance()
				.getPsHotList().size());
		mPsHotAdapter.imageLoader.setPSHotImagesToLoadFromParsedPS(MFConfig
				.getInstance().getPsHotList());
		mPsHotAdapter.notifyDataSetChanged();
		mFriendsActivityAdapter.notifyDataSetChanged();
	}

	public void refresh() {
		if (MFConfig.isOnline(mContext)) {
			((Home) getActivity()).refresh();

			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
	}

	private void checkLogin(PendingAction pa) {

		if (mLoginHelper.isLogin()) {
			handlePendingAction(pa);
		} else {
			mFirstShowFriendsActivity = true;
			mFriendsActivityTimeStamp = 0;
			mLoginHelper.showLoginDialog(this, pa, new RegisterPSCallBack() {

				@Override
				public void onErrorRegistered() {
					switchToHotTab();
				}

				@Override
				public void onCompleteRegistered(PendingAction pa) {
					handlePendingAction(pa);
				}
			});
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void handlePendingAction(PendingAction pa) {
		switch (pa) {
		case SETTINGS:
			if (Build.VERSION.SDK_INT < 11) {
				getActivity().openContextMenu(mPsSettingsTab);
			} else {
				if (mSettingsMenu == null) {
					mSettingsMenu = new PopupMenu(mContext, mPsSettingsTab);
					mSettingsMenu.getMenu().add(Menu.NONE, MENU_FIND_FRIENDS,
							Menu.NONE, R.string.findFriends);
					mSettingsMenu.getMenu().add(Menu.NONE, MENU_LOGOUT,
							Menu.NONE, R.string.logout);
					mSettingsMenu
							.setOnMenuItemClickListener(new MenuClickListener());
				}
				mSettingsMenu.show();
			}
			break;
		case CAMERA:
			if (Build.VERSION.SDK_INT < 11) {
				getActivity().openContextMenu(mPsCameraTab);
			} else {
				if (mCameraMenu == null) {
					mCameraMenu = new PopupMenu(mContext, mPsSettingsTab);
					mCameraMenu.getMenu().add(Menu.NONE, MENU_USE_ALBUM,
							Menu.NONE, R.string.useAlbum);
					mCameraMenu.getMenu().add(Menu.NONE, MENU_TAKE_PHOTO,
							Menu.NONE, R.string.takePhoto);
					mCameraMenu
							.setOnMenuItemClickListener(new MenuClickListener());
				}
				mCameraMenu.show();
			}
			break;
		case FRIENDS:
			switchToFriendsTab();
			break;
		default:
		}
	}

	private void showFindFriendsDialog() {

		final FindFriendsDialogView view = new FindFriendsDialogView(mContext);
		if (Build.VERSION.SDK_INT < 11) {
			view.setBackgroundColor(Color.WHITE);
		}

		new AlertDialog.Builder(mContext)
				.setView(view)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.confirmed),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								if (view.isModified()) {
									reloadActivityOrResetTimeStamp();
								}
							}
						}).show();
	}

	public void reloadActivityOrResetTimeStamp() {
		if (mCurrentTab == 0) {
			loadFriendsActivity();
		} else {
			mFriendsActivityTimeStamp = 0;
		}
	}

	private void callLogout(boolean showToast) {
		mLoginHelper.callLogout(showToast);
		switchToHotTab();
	}

	// FB logic

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Intent i = new Intent(mContext, PSUploadPhoto.class);
				i.setData(fileUri);
				i.putExtra("photoFile", mPhotoFile);
				startActivityForResult(i, UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE);
				try {
					mContext.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_MOUNTED,	
							Uri.parse("file://"
									+ Environment
											.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
			} else {
				Toast.makeText(mContext, R.string.imageCaptureFailed,
						Toast.LENGTH_LONG).show();
				// Image capture failed, advise user
			}
		} else if (requestCode == CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Intent i = new Intent(mContext, PSUploadPhoto.class);
				i.setData(data.getData());
				startActivityForResult(i, UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE);
			} else if (resultCode == Activity.RESULT_CANCELED) {
			} else {
				Toast.makeText(mContext, R.string.imageChooseFailed,
						Toast.LENGTH_LONG).show();
				// Image capture failed, advise user
			}
		} else if (requestCode == UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				mFriendsActivityTimeStamp = 0;
				if (mCurrentTab == 0) {
					loadFriendsActivity();
				} else {
					switchToFriendsTab();
				}

				if (MFConfig.isOnline(mContext)) {
					MFFetchListHelper.fetchPSHotList((Home) mContext);
				}
			}
		}
		uiHelper.onActivityResult(requestCode, resultCode, data);
		if (mLoginHelper.getWeiboLoginButton() != null) {
			mLoginHelper.getWeiboLoginButton().onActivityResult(requestCode,
					resultCode, data);
		}
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
	public void onDestroy() {
		if (mHotGV != null) {
			mHotGV.setAdapter(null);
		}
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

}
