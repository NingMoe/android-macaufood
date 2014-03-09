package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.cycon.macaufood.R;
import com.cycon.macaufood.activities.Map.MyLocationListenner;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFUtil;

public class PSCafeLocation extends SherlockFragmentActivity{

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private boolean mShowDist;
	private TextView cafeSearchText;
	private ListView cafeLocationList;
	private ArrayList<Cafe> searchCafes = new ArrayList<Cafe>();
	private ArrayList<Cafe> nearbyCafes = new ArrayList<Cafe>();
	private SearchAdapter searchAdapter;
	// 定位相关
	LocationClient mLocClient;
	LocationData mCurrentLocation = null;
	public MyLocationListenner myListener = new MyLocationListenner();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// use same logic as in BaseActivity
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			finish();
			Process.killProcess(Process.myPid());
			return;
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.ps_cafe_location);

		 //定位初始化
        mLocClient = new LocationClient( this );
        mCurrentLocation = new LocationData();
        mLocClient.registerLocationListener( myListener );
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
        option.setScanSpan(10000);
        mLocClient.setLocOption(option);
        mLocClient.start();

		cafeLocationList = (ListView) findViewById(R.id.cafeLocationList);
		cafeSearchText = (TextView) findViewById(R.id.cafeSearchText);
		searchAdapter = new SearchAdapter();
		cafeLocationList.setAdapter(searchAdapter);
		cafeLocationList.setOnItemClickListener(searchAdapter);

		cafeSearchText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2,
					int arg3) {
				if (s.toString().trim().length() == 0) {
					searchCafes.clear();
					searchAdapter.setCafes(nearbyCafes);
					searchAdapter.notifyDataSetChanged();
				} else {
					searchAdapter.setCafes(searchCafes);
					doDirectSearch(s.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	private void doDirectSearch(String query) {

		query = query.toLowerCase().trim();
		searchCafes.clear();

		ArrayList<Cafe> priorityList = new ArrayList<Cafe>();
		ArrayList<Cafe> nonPrefixList = new ArrayList<Cafe>();

		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0"))
				continue;
			String name = cafe.getName().toLowerCase();
			if (name.contains(query)) {
				if (name.startsWith(query)) {
					
					if (mShowDist) {
						searchCafes.add(cafe);
					} else if (cafe.getPriority().equals("0")) {
						searchCafes.add(cafe);
					} else {
						int priority = Integer.parseInt(cafe.getPriority());
						if (priorityList.size() == 0) {
							priorityList.add(cafe);
						} else {
							boolean added = false;
							for (int i = 0; i < priorityList.size(); i++) {
								if (Integer.parseInt(priorityList.get(i)
										.getPriority()) < priority) {
									priorityList.add(i, cafe);
									added = true;
									break;
								}
							}
							if (!added) {
								priorityList.add(cafe);
							}
						}
					}

				} else
					nonPrefixList.add(cafe);
			}
		}
		

		if (mShowDist) {
			Collections.sort(searchCafes);
			Collections.sort(nonPrefixList);
		} else {
			searchCafes.addAll(0, priorityList);
		}
		searchCafes.addAll(nonPrefixList);
		searchAdapter.notifyDataSetChanged();
	}

	private void loadNearbyCafe() {
		if (mCurrentLocation == null || mCurrentLocation.latitude > Map.LAT_MAX || 
				mCurrentLocation.latitude < Map.LAT_MIN ||
				mCurrentLocation.longitude > Map.LONG_MAX || 
				mCurrentLocation.longitude < Map.LONG_MIN) {
			return;
		}
		
		
		mShowDist = true;
		
		int displayNumber = 20;
		PriorityQueue<Cafe> queue = new PriorityQueue<Cafe>(displayNumber);
		
		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0")) continue;
			double dist = MFUtil.distFrom(mCurrentLocation.latitude, mCurrentLocation.longitude, Double.parseDouble(cafe.getCoordx()), Double.parseDouble(cafe.getCoordy()));
				cafe.setDistance(dist);
				if (queue.size() < displayNumber) {
					queue.add(cafe);
				} else if (dist < queue.peek().getDistance()) {
					queue.poll();
					queue.add(cafe);
				}
		}
		
//		ArrayList<Cafe> searchList = new ArrayList<Cafe>();
		
		nearbyCafes.clear();
		while (!queue.isEmpty()) {
			Cafe cafe = queue.poll();
			nearbyCafes.add(cafe);
		}
		Collections.reverse(nearbyCafes);
//		nearbyCafes.addAll(searchList);
		searchAdapter.setCafes(nearbyCafes);
		searchAdapter.notifyDataSetChanged();
		
	}
	
	private void addCafe() {
		View view = getLayoutInflater().inflate(R.layout.add_cafe_location, null);
		if (Build.VERSION.SDK_INT < 11) {
			view.setBackgroundColor(Color.WHITE);
		}
		final TextView name = (TextView)view.findViewById(R.id.nameText);
		final TextView addr = (TextView)view.findViewById(R.id.addrText);
		final TextView phone = (TextView)view.findViewById(R.id.phoneText);
		
		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setCancelable(false)
				.setView(view)
				.setPositiveButton(getString(R.string.confirmed), null)
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
		
		
		Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	if (name.getText().toString().trim().length() == 0) {
            		Toast.makeText(PSCafeLocation.this, R.string.pleaseFillCafeName, Toast.LENGTH_SHORT).show();
					return;
				}
            	
            	dialog.dismiss();
    			Cafe cafe = new Cafe();
    			cafe.setId("-1");
    			cafe.setName(name.getText().toString());
    			cafe.setAddress(addr.getText().toString());
    			cafe.setPhone(phone.getText().toString());
    			
    			Intent i = new Intent();
    			i.putExtra("cafe", cafe);
    			setResult(RESULT_OK, i);
    			finish();
            }
        });
	}

	private class SearchAdapter extends BaseAdapter implements
			AdapterView.OnItemClickListener {

		private ArrayList<Cafe> cafes = new ArrayList<Cafe>();
		private LayoutInflater inflater;

		public SearchAdapter() {
			inflater = LayoutInflater.from(PSCafeLocation.this);
		}
		
		public void setCafes(ArrayList<Cafe> cafes) {
			this.cafes = cafes;
		}

		public int getCount() {
			return cafes.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater
						.inflate(R.layout.cafe_location_row, null);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.subText = (TextView) convertView
						.findViewById(R.id.subText);
				holder.distText = (TextView) convertView
						.findViewById(R.id.distText);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			Cafe cafe = cafes.get(position);
			if (cafe != null) {
				holder.name.setText(cafe.getName());
				holder.subText.setText(cafe.getAddress());
				if (mShowDist) {
					holder.distText.setVisibility(View.VISIBLE);
					double dist = cafe.getDistance();
					if (dist > 1000) {
						holder.distText.setText(Math.round(dist / 100) / 10.0 + getString(R.string.km));
					} else {
						holder.distText.setText(Math.round(dist) + getString(R.string.meter));
					}
				} else {
					holder.distText.setVisibility(View.GONE);
				}
				
				

			}
			return convertView;
		}

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Cafe cafe = cafes.get(position);
			Intent i = new Intent();
			i.putExtra("cafe", cafe);
			setResult(RESULT_OK, i);
			finish();
		}
	}

	static class ViewHolder {
		TextView name;
		TextView subText;
		TextView distText;
	}
	
	
	/**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
    	
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;

            mCurrentLocation.latitude = location.getLatitude();
            mCurrentLocation.longitude = location.getLongitude();
            
//            mCurrentLocation.latitude = 22.19971287 + 0.0030;
//            mCurrentLocation.longitude = 113.54500506 + 0.0117;
            loadNearbyCafe();
            
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }


	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocClient.stop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, R.string.addCafe).setIcon(R.drawable.ic_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			addCafe();
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


}
