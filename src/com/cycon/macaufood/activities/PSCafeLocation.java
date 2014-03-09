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
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PSCafeLocation extends SherlockFragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private boolean mShowDist;
	private TextView cafeSearchText;
	private ListView cafeLocationList;
	private ArrayList<Cafe> searchCafes = new ArrayList<Cafe>();
	private ArrayList<Cafe> nearbyCafes = new ArrayList<Cafe>();
	private SearchAdapter searchAdapter;

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

		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();

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
		if (mCurrentLocation == null || mCurrentLocation.getLatitude() > Map.LAT_MAX || 
				mCurrentLocation.getLatitude() < Map.LAT_MIN ||
				mCurrentLocation.getLongitude() > Map.LONG_MAX || 
				mCurrentLocation.getLongitude() < Map.LONG_MIN) {
			return;
		}
		
		
		mShowDist = true;
		
		int displayNumber = 20;
		PriorityQueue<Cafe> queue = new PriorityQueue<Cafe>(displayNumber);
		
		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0")) continue;
			double dist = MFUtil.distFrom(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), Double.parseDouble(cafe.getCoordx()), Double.parseDouble(cafe.getCoordy()));
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

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends SherlockDialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {

			// Get the error code
			int errorCode = connectionResult.getErrorCode();
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		mCurrentLocation = mLocationClient.getLastLocation();
//		mCurrentLocation.setLatitude(22.12);
//		mCurrentLocation.setLongitude(113.58);
		loadNearbyCafe();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				mLocationClient.connect();
				break;
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
		mLocationClient.disconnect();
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
