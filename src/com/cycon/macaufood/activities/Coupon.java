package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class Coupon extends SherlockFragment {

	private static final String TAG = Coupon.class.getName();

	private View retryLayout;
	private Button retryButton;
	private ListView normalCouponList;
	private ListView creditCouponList;
	private ListView vipCouponList;
	private CafeListAdapter normalCouponAdapter;
	private CafeListAdapter creditCouponAdapter;
	private CafeListAdapter vipCouponAdapter;
	private SwingBottomInAnimationAdapter animNormalCouponAdapter;
	private SwingBottomInAnimationAdapter animCreditCouponAdapter;
	private SwingBottomInAnimationAdapter animVipCouponAdapter;
	private FileCache fileCache;
	private TextView normalCoupon;
	private TextView creditCoupon;
	private TextView vipCoupon;
	public int couponType = 0; // 0 = normal, 1 = credit, 2 = vip

	private Context mContext;
	private View mView;
	public boolean mIsVisible;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.coupon, null);
		initView();
		return mView;
	}

	private void initView() {

		retryLayout = mView.findViewById(R.id.retryLayout);

		normalCouponList = (ListView) mView.findViewById(R.id.normalCouponList);
		creditCouponList = (ListView) mView.findViewById(R.id.creditCouponList);
		vipCouponList = (ListView) mView.findViewById(R.id.vipCouponList);
		normalCouponAdapter = new CafeListAdapter(mContext, MFConfig
				.getInstance().getNormalCouponCafeList(), ImageType.COUPON);
		creditCouponAdapter = new CafeListAdapter(mContext, MFConfig
				.getInstance().getCreditCouponCafeList(), ImageType.COUPON);
		vipCouponAdapter = new CafeListAdapter(mContext, MFConfig.getInstance()
				.getVipCouponCafeList(), ImageType.COUPON);

		animNormalCouponAdapter = new SwingBottomInAnimationAdapter(
				normalCouponAdapter);
		animNormalCouponAdapter.setListView(normalCouponList);
        if (MFFetchListHelper.isFetching) {
        	animNormalCouponAdapter.setAnimationEnabled(false);
		}
		normalCouponList.setAdapter(animNormalCouponAdapter);

		animCreditCouponAdapter = new SwingBottomInAnimationAdapter(
				creditCouponAdapter);
		animCreditCouponAdapter.setListView(creditCouponList);
        if (MFFetchListHelper.isFetching) {
        	animCreditCouponAdapter.setAnimationEnabled(false);
		}
		creditCouponList.setAdapter(animCreditCouponAdapter);

		animVipCouponAdapter = new SwingBottomInAnimationAdapter(
				vipCouponAdapter);
		animVipCouponAdapter.setListView(vipCouponList);
        if (MFFetchListHelper.isFetching) {
        	animVipCouponAdapter.setAnimationEnabled(false);
		}
		vipCouponList.setAdapter(animVipCouponAdapter);

		normalCouponList.setOnItemClickListener(itemClickListener);
		creditCouponList.setOnItemClickListener(itemClickListener);
		vipCouponList.setOnItemClickListener(itemClickListener);

		normalCoupon = (TextView) mView.findViewById(R.id.normalCoupon);
		normalCoupon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (couponType != 0) {
					setNormalCouponTab(true);
					if (couponType == 1)
						setCreditCouponTab(false);
					if (couponType == 2)
						setVipCouponTab(false);
					couponType = 0;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					populateListView(couponType);
				}
			}
		});
		creditCoupon = (TextView) mView.findViewById(R.id.creditCoupon);
		creditCoupon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (couponType != 1) {
					setCreditCouponTab(true);
					if (couponType == 0)
						setNormalCouponTab(false);
					if (couponType == 2)
						setVipCouponTab(false);
					couponType = 1;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getCreditCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					populateListView(couponType);
				}
			}
		});
		vipCoupon = (TextView) mView.findViewById(R.id.vipCoupon);
		vipCoupon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (couponType != 2) {
					setVipCouponTab(true);
					if (couponType == 0)
						setNormalCouponTab(false);
					if (couponType == 1)
						setCreditCouponTab(false);
					couponType = 2;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					populateListView(couponType);
				}
			}
		});

		// if no internet and no data in File, show retry message
		if (couponType == 0
				&& MFConfig.getInstance().getNormalCouponCafeList().size() == 0
				|| couponType == 1
				&& MFConfig.getInstance().getCreditCouponCafeList().size() == 0
				|| couponType == 2
				&& MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
			if (!MFConfig.isOnline(mContext)) {
				displayRetryLayout();
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		fileCache = new FileCache(mContext, ImageType.COUPON);

		preLoadFromFileCache();
		// onResume will call fetch data

	}

	private void setNormalCouponTab(boolean select) {
		if (select) {
			normalCoupon.setTextColor(Color.parseColor("#FFFFFF"));
			normalCoupon
					.setBackgroundResource(R.drawable.tab_normal_coupon_selected);
			normalCouponList.setVisibility(View.VISIBLE);
		} else {
			normalCoupon.setTextColor(Color.parseColor("#68A6E6"));
			normalCoupon
					.setBackgroundResource(R.drawable.tab_normal_coupon_unselected);
			normalCouponList.setVisibility(View.GONE);
		}
	}

	private void setCreditCouponTab(boolean select) {
		if (select) {
			creditCoupon.setTextColor(Color.parseColor("#FFFFFF"));
			creditCoupon
					.setBackgroundResource(R.drawable.tab_credit_coupon_selected);
			creditCouponList.setVisibility(View.VISIBLE);
		} else {
			creditCoupon.setTextColor(Color.parseColor("#40C28a"));
			creditCoupon
					.setBackgroundResource(R.drawable.tab_credit_coupon_unselected);
			creditCouponList.setVisibility(View.GONE);
		}
	}

	private void setVipCouponTab(boolean select) {
		if (select) {
			vipCoupon.setTextColor(Color.parseColor("#FFFFFF"));
			vipCoupon.setBackgroundResource(R.drawable.tab_vip_coupon_selected);
			vipCouponList.setVisibility(View.VISIBLE);
		} else {
			vipCoupon.setTextColor(Color.parseColor("#EF6666"));
			vipCoupon
					.setBackgroundResource(R.drawable.tab_vip_coupon_unselected);
			vipCouponList.setVisibility(View.GONE);
		}
	}

	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			String cafeId = null;
			String forward = null;

			if (couponType == 0) {
				cafeId = MFConfig.getInstance().getNormalCouponCafeList()
						.get(position).getCafeid();
				forward = MFConfig.getInstance().getNormalCouponCafeList()
						.get(position).getForward();
			}
			if (couponType == 1) {
				cafeId = MFConfig.getInstance().getCreditCouponCafeList()
						.get(position).getCafeid();
				forward = MFConfig.getInstance().getCreditCouponCafeList()
						.get(position).getForward();
			}
			if (couponType == 2) {
				cafeId = MFConfig.getInstance().getVipCouponCafeList()
						.get(position).getCafeid();
				forward = MFConfig.getInstance().getVipCouponCafeList()
						.get(position).getForward();
			}

			if (forward.equals("b")) {
				// String branch =
				// Config.getInstance().getCafeLists().get(Integer.parseInt(cafeId)
				// - 1).getBranch();
				String branch = cafeId;
				if (!branch.equals("0")) {
					Intent i = new Intent(mContext, Branch.class);
					i.putExtra("branch", branch);
					startActivity(i);
				} else {
					// in case that cafe is not added in cafelist yet, return
					if (MFConfig.getInstance().getCafeLists().size() < Integer
							.parseInt(cafeId))
						return;
					Intent i = new Intent(mContext, Details.class);
					i.putExtra("id", cafeId);
					startActivity(i);
				}
			} else {
				// in case that cafe is not added in cafelist yet, return
				if (MFConfig.getInstance().getCafeLists().size() < Integer
						.parseInt(cafeId))
					return;
				Intent i = new Intent(mContext, Details.class);
				i.putExtra("id", cafeId);
				startActivity(i);
			}
		};
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
			normalCouponAdapter.imageLoader.cleanup();
			normalCouponAdapter.imageLoader
					.setImagesToLoadFromParsedCafe(MFConfig.getInstance()
							.getNormalCouponCafeList());
			normalCouponAdapter.notifyDataSetChanged();
		} else {
			mIsVisible = false;
		}

	}

	public void populateListView(int type) {

		if (couponType == 0 && type == 0) {

			// if no internet and no data in File, show retry message
			if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0) {
				displayRetryLayout();
			} else {
				if (retryLayout != null)
					retryLayout.setVisibility(View.GONE);
			}
			
			if(mIsVisible) {
				normalCouponAdapter.imageLoader.cleanup();
				normalCouponAdapter.imageLoader
						.setImagesToLoadFromParsedCafe(MFConfig.getInstance()
								.getNormalCouponCafeList());
				animNormalCouponAdapter.reset();
			} else {
				animNormalCouponAdapter.setAnimationEnabled(true);
			}
			normalCouponAdapter.notifyDataSetChanged();

		} else if (couponType == 1 && type == 1) {

			// if no internet and no data in File, show retry message
			if (MFConfig.getInstance().getCreditCouponCafeList().size() == 0) {
				displayRetryLayout();
			} else {
				if (retryLayout != null)
					retryLayout.setVisibility(View.GONE);
			}
			creditCouponAdapter.imageLoader.cleanup();
			creditCouponAdapter.imageLoader
					.setImagesToLoadFromParsedCafe(MFConfig.getInstance()
							.getCreditCouponCafeList());
			if (mIsVisible) {
				animCreditCouponAdapter.reset();
			} else {
				animCreditCouponAdapter.setAnimationEnabled(true);
			}
			creditCouponAdapter.notifyDataSetChanged();

		} else if (couponType == 2 && type == 2) {

			// if no internet and no data in File, show retry message
			if (MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
				displayRetryLayout();
			} else {
				if (retryLayout != null)
					retryLayout.setVisibility(View.GONE);
			}
			vipCouponAdapter.imageLoader.cleanup();
			vipCouponAdapter.imageLoader.setImagesToLoadFromParsedCafe(MFConfig
					.getInstance().getVipCouponCafeList());
			if (mIsVisible) {
				animVipCouponAdapter.reset();
			} else {
				animVipCouponAdapter.setAnimationEnabled(true);
			}
			vipCouponAdapter.notifyDataSetChanged();
			
		}
	}

	private void preLoadFromFileCache() {
		File f = null;
		FileInputStream is = null;

		try {
			if (couponType == 0) {
				f = fileCache.getFile(MFConstants.NORMAL_COUPON_XML_FILE_NAME);
				is = new FileInputStream(f);
				MFFetchListHelper.parseXml(is,
						MFConfig.tempParsedNormalCouponCafeList, MFConfig
								.getInstance().getNormalCouponCafeList());
			} else if (couponType == 1) {
				f = fileCache.getFile(MFConstants.CREDIT_COUPON_XML_FILE_NAME);
				is = new FileInputStream(f);
				MFFetchListHelper.parseXml(is,
						MFConfig.tempParsedCreditCouponCafeList, MFConfig
								.getInstance().getCreditCouponCafeList());
			} else if (couponType == 2) {
				f = fileCache.getFile(MFConstants.VIP_COUPON_XML_FILE_NAME);
				is = new FileInputStream(f);
				MFFetchListHelper.parseXml(is,
						MFConfig.tempParsedVipCouponCafeList, MFConfig
								.getInstance().getVipCouponCafeList());
			}

		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		}

		//refresh when file cache xml is deleted by user
		if (!MFFetchListHelper.isFetching && !((Home)getActivity()).isShowingDisClaimer()) {
	        if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0 && couponType == 0 ||
	        		MFConfig.getInstance().getCreditCouponCafeList().size() == 0 && couponType == 1 ||
	        		MFConfig.getInstance().getVipCouponCafeList().size() == 0 && couponType == 2) {
	        	refresh();
			}
		}

	}

	
	public void resetListViewAnimation() {
		if (couponType == 0) {
			if (animNormalCouponAdapter != null) {
				animNormalCouponAdapter.reset();
				animNormalCouponAdapter.notifyDataSetChanged();
			}
		} else if (couponType == 1) {
			if (animCreditCouponAdapter != null) {
				animCreditCouponAdapter.reset();
				animCreditCouponAdapter.notifyDataSetChanged();
			}
		} else if (couponType == 2) {
			if (animVipCouponAdapter != null) {
				animVipCouponAdapter.reset();
				animVipCouponAdapter.notifyDataSetChanged();
			}
		}
	}

	@SuppressLint("NewApi")
	public void refresh() {
		if (MFConfig.isOnline(mContext)) {
			((Home) getActivity()).refresh();

			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
	}

}
