package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Intent;
import android.os.Bundle;

import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.PSFriendsActivityAdapter;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.LoginHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.facebook.UiLifecycleHelper;

public class PSDetails extends BaseActivity {

	private StickyListHeadersListView mFriendsActivityListView;
	private PSFriendsActivityAdapter mFriendsActivityAdapter;
	private List<String> mFriendsActivityInfo = new ArrayList<String>();

	private LoginHelper mLoginHelper;

	// FB
	private UiLifecycleHelper uiHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ps_detail);

		mLoginHelper = new LoginHelper(this);
		// FB
		uiHelper = new UiLifecycleHelper(this,
				mLoginHelper.getFBSessionCallback());
		uiHelper.onCreate(savedInstanceState);

		int pos = getIntent().getIntExtra("ps_hot_position", 0);
		String psHotId = MFConfig.getInstance().getPsHotList().get(pos);
		ParsedPSHolder psHotInfo = MFConfig.getInstance().getPsInfoMap()
				.get(psHotId);
		String title = psHotInfo.getCaption();
		if (title.trim().length() == 0 || title.equals("(null)"))
			title = getString(R.string.psHot);
		setTitle(title);

		mFriendsActivityInfo.add(psHotId);
		mFriendsActivityListView = (StickyListHeadersListView) findViewById(R.id.friendsActivityListView);
		mFriendsActivityAdapter = new PSFriendsActivityAdapter(this,
				mFriendsActivityInfo, mLoginHelper);
		mFriendsActivityListView.setAdapter(mFriendsActivityAdapter);
	}

	// FB logic

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
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
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

}
