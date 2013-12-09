package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.os.Bundle;

import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.PSFriendsActivityAdapter;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.MFConfig;

public class PSDetails extends BaseActivity {
	
	private StickyListHeadersListView mFriendsActivityListView;
	private PSFriendsActivityAdapter mFriendsActivityAdapter;
	private List<ParsedPSHolder> mFriendsActivityInfo = new ArrayList<ParsedPSHolder>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ps_detail);
		
		int pos = getIntent().getIntExtra("ps_hot_position", 0);
		ParsedPSHolder psHotInfo = MFConfig.getInstance().getPsHotList().get(pos);
		
		String title = psHotInfo.getCaption();
		if (title.trim().length() == 0) title = getString(R.string.psHot);
		setTitle(title);
		
		mFriendsActivityInfo.add(psHotInfo);
		mFriendsActivityListView = (StickyListHeadersListView) findViewById(R.id.friendsActivityListView);
		mFriendsActivityAdapter = new PSFriendsActivityAdapter(this, mFriendsActivityInfo);
		mFriendsActivityListView.setAdapter(mFriendsActivityAdapter);
	}
	
}
