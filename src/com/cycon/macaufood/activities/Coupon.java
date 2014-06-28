package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFFetchListHelper;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.AdvView;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class Coupon extends SherlockFragment {

	private static final String TAG = Coupon.class.getName();

	private View retryLayout;
	private Button retryButton;
	private AdvView advViewPager;
	private ListView normalCouponList;
	private ListView creditVipCouponList;
	private View mainCouponScrollView;
	private CafeListAdapter normalCouponAdapter;
	private CafeListAdapter creditVipCouponAdapter;
	private SwingBottomInAnimationAdapter animNormalCouponAdapter;
	private SwingBottomInAnimationAdapter animCreditVipCouponAdapter;
	private FileCache fileCache;
	private FileCache mainCouponFileCache;
	private TextView normalCoupon;
	private TextView creditVipCoupon;
	private TextView mainCoupon;
	private ImageView mainCouponImage;
	public int couponType = 2; // 0 = normal, 1 = credit, 2 = main coupon

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
		advViewPager = (AdvView) mView.findViewById(R.id.viewPager);
		mainCouponScrollView = mView.findViewById(R.id.mainCouponScrollView);
		mainCouponImage = (ImageView) mView.findViewById(R.id.mainCouponImage);

		normalCouponList = (ListView) mView.findViewById(R.id.normalCouponList);
		creditVipCouponList = (ListView) mView.findViewById(R.id.creditVipCouponList);
		normalCouponAdapter = new CafeListAdapter(mContext, MFConfig
				.getInstance().getNormalCouponCafeList(), ImageType.COUPON);
		creditVipCouponAdapter = new CafeListAdapter(mContext, MFConfig
				.getInstance().getCreditVipCouponCafeList(), ImageType.COUPON);

		animNormalCouponAdapter = new SwingBottomInAnimationAdapter(
				normalCouponAdapter);
		animNormalCouponAdapter.setListView(normalCouponList);
        if (MFFetchListHelper.isFetching) {
        	animNormalCouponAdapter.setAnimationEnabled(false);
		}
		normalCouponList.setAdapter(animNormalCouponAdapter);

		animCreditVipCouponAdapter = new SwingBottomInAnimationAdapter(
				creditVipCouponAdapter);
		animCreditVipCouponAdapter.setListView(creditVipCouponList);
        if (MFFetchListHelper.isFetching) {
        	animCreditVipCouponAdapter.setAnimationEnabled(false);
		}
		creditVipCouponList.setAdapter(animCreditVipCouponAdapter);

		normalCouponList.setOnItemClickListener(itemClickListener);
		creditVipCouponList.setOnItemClickListener(itemClickListener);

		normalCoupon = (TextView) mView.findViewById(R.id.normalCoupon);
		normalCoupon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (couponType != 0) {
					setNormalCouponTab(true);
					if (couponType == 1)
						setCreditVipCouponTab(false);
					if (couponType == 2)
						setMainCouponTab(false);
					couponType = 0;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					populateListView(couponType);
				}
			}
		});
		creditVipCoupon = (TextView) mView.findViewById(R.id.creditVipCoupon);
		creditVipCoupon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (couponType != 1) {
					setCreditVipCouponTab(true);
					if (couponType == 0)
						setNormalCouponTab(false);
					if (couponType == 2)
						setMainCouponTab(false);
					couponType = 1;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getCreditVipCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					populateListView(couponType);
				}
			}
		});
		mainCoupon = (TextView) mView.findViewById(R.id.mainCoupon);
		mainCoupon.setSelected(true);
		mainCoupon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (couponType != 2) {
					setMainCouponTab(true);
					if (couponType == 0)
						setNormalCouponTab(false);
					if (couponType == 1)
						setCreditVipCouponTab(false);
					couponType = 2;
					retryLayout.setVisibility(View.GONE);

//					if (MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
//						preLoadFromFileCache();
//					}
//					populateListView(couponType);
				}
			}
		});
		
		populateMainCoupon();

		// if no internet and no data in File, show retry message
		if (couponType == 0
				&& MFConfig.getInstance().getNormalCouponCafeList().size() == 0
				|| couponType == 1
				&& MFConfig.getInstance().getCreditVipCouponCafeList().size() == 0) {
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
		mainCouponFileCache = new FileCache(mContext, ImageType.MAINCOUPON);

		preLoadFromFileCache();
		// onResume will call fetch data

	}

	private void setNormalCouponTab(boolean select) {
		if (select) {
			normalCoupon.setSelected(true);
			normalCouponList.setVisibility(View.VISIBLE);
		} else {
			normalCoupon.setSelected(false);
			normalCouponList.setVisibility(View.GONE);
		}
	}

	private void setCreditVipCouponTab(boolean select) {
		if (select) {
			creditVipCoupon.setSelected(true);
			creditVipCouponList.setVisibility(View.VISIBLE);
		} else {
			creditVipCoupon.setSelected(false);
			creditVipCouponList.setVisibility(View.GONE);
		}
	}

	private void setMainCouponTab(boolean select) {
		if (select) {
			mainCoupon.setSelected(true);
			mainCouponScrollView.setVisibility(View.VISIBLE);
			advViewPager.setVisibility(View.GONE);
		} else {
			mainCoupon.setSelected(false);
			mainCouponScrollView.setVisibility(View.GONE);
			advViewPager.setVisibility(View.VISIBLE);
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
				cafeId = MFConfig.getInstance().getCreditVipCouponCafeList()
						.get(position).getCafeid();
				forward = MFConfig.getInstance().getCreditVipCouponCafeList()
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
			if (MFConfig.getInstance().getCreditVipCouponCafeList().size() == 0) {
				displayRetryLayout();
			} else {
				if (retryLayout != null)
					retryLayout.setVisibility(View.GONE);
			}
			creditVipCouponAdapter.imageLoader.cleanup();
			creditVipCouponAdapter.imageLoader
					.setImagesToLoadFromParsedCafe(MFConfig.getInstance()
							.getCreditVipCouponCafeList());
			if (mIsVisible) {
				animCreditVipCouponAdapter.reset();
			} else {
				animCreditVipCouponAdapter.setAnimationEnabled(true);
			}
			creditVipCouponAdapter.notifyDataSetChanged();

		} 
	}
	
	public void populateMainCoupon() {
		String couponInfoStr = MFUtil.getStringFromCache(mainCouponFileCache, MFConstants.MAIN_COUPON_INFO_STR);
		if (couponInfoStr != null) {
			try {
				String[] tokens = couponInfoStr.split("\\|\\|\\|");
				String couponId = tokens[0];
				Integer.parseInt(couponId);
				boolean isClickable = tokens[1].equals("1");
				final String forward = tokens[2];
				final String cafeId = tokens[3];
				Bitmap bitmap = MFUtil.getBitmapFromCache(mainCouponFileCache, couponId);
				if (bitmap != null) {
					mainCouponImage.setImageBitmap(bitmap);
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mainCouponImage.getLayoutParams();
					int imageHeight = MFConfig.deviceWidth * bitmap.getHeight() / bitmap.getWidth();
					params.height = imageHeight;
					mainCouponImage.setLayoutParams(params);
					if (isClickable) {
						mainCouponImage.setOnClickListener(new OnClickListener() {
							
							public void onClick(View v) {
								if (forward.equals("b")) {
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
								} else if (forward.equals("w")) {
									Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(cafeId));
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
							}
						});

					}
				}

				
			} catch (Exception e) {
				
			}
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
				f = fileCache.getFile(MFConstants.CREDIT_VIP_COUPON_XML_FILE_NAME);
				is = new FileInputStream(f);
				MFFetchListHelper.parseXml(is,
						MFConfig.tempParsedCreditVipCouponCafeList, MFConfig
								.getInstance().getCreditVipCouponCafeList());
			} 

		} catch (FileNotFoundException e) {
			MFLog.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		}

		//refresh when file cache xml is deleted by user
		if (!MFFetchListHelper.isFetching && !((Home)getActivity()).isShowingDisClaimer()) {
	        if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0 && couponType == 0 ||
	        		MFConfig.getInstance().getCreditVipCouponCafeList().size() == 0 && couponType == 1) {
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
			if (animCreditVipCouponAdapter != null) {
				animCreditVipCouponAdapter.reset();
				animCreditVipCouponAdapter.notifyDataSetChanged();
			}
		} 
	}

	public void refresh() {
		if (MFConfig.isOnline(mContext)) {
			((Home) getActivity()).refresh();

			if (retryLayout != null)
				retryLayout.setVisibility(View.GONE);
		}
	}

}
