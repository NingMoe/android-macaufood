package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AdvancedSearchHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.AdvView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A list view that demonstrates the use of setEmptyView. This example alos uses
 * a custom layout file that adds some extra buttons to the screen.
 */
public class Map extends SherlockFragmentActivity {

	private static final int SHOW_LIST_MENU_ID = 1;
	private static final double LAT_MIN = 22.104;
	private static final double LAT_MAX = 22.24;
	private static final double LONG_MIN = 113.51;
	private static final double LONG_MAX = 113.60;
	private static final double LAT_DEFAULT = 22.19971287;
	private static final double LONG_DEFAULT = 113.54500506;
	private static LatLngBounds mMapBounds = new LatLngBounds(new LatLng(LAT_MIN, LONG_MIN), new LatLng(LAT_MAX, LONG_MAX));
	private String name;
	private String selectedCafeId;
	private Button searchNearby;
	private AdvView smallBanner;
	private View listLayout;
	private View mapLayout;

	private ListView list;
	private CafeSearchListAdapter cafeAdapter;
	private Spinner regionSpinner;
	private Spinner dishesSpinner;
	private Spinner categorySpinner;
	private TextView headerView;
	private TextView listMessage;
	private ArrayAdapter<String> regionAdapter;
	private List<String> regionStrings;
	private boolean disableItemSelect;
	private View mapFilterPanel;
	private TextView displaySearchQuery;
	private boolean needPopulateMarkers;
	private GoogleMap mMap;
	private HashMap<Marker, String> mMarkersHashMap = new HashMap<Marker, String>();
	private BitmapDescriptor greenBitmap;
	private BitmapDescriptor blueBitmap;;
	private BitmapDescriptor favoriteBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// use same logic as in BaseActivity
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			Process.killProcess(Process.myPid());
			finish();
			return;
		}

		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.map);

		smallBanner = (AdvView) findViewById(R.id.smallBanner);
		list = (ListView) findViewById(R.id.list);
		listMessage = (TextView) findViewById(R.id.listMessage);
		mapFilterPanel = findViewById(R.id.mapFilterPanel);
		displaySearchQuery = (TextView) findViewById(R.id.displaySearchQuery);
		
		setUpMapIfNeeded();

		headerView = new TextView(this);
		headerView.setText(getString(R.string.totalResultsFound, MFConfig
				.getInstance().getSearchResultList().size()));
		headerView.setTextSize(12f);
		headerView.setGravity(Gravity.CENTER_HORIZONTAL);
		headerView.setTextColor(getResources().getColor(R.color.tab_gray_text));
		headerView.setPadding(0, MFUtil.getPixelsFromDip(1f, getResources()),
				0, MFUtil.getPixelsFromDip(1f, getResources()));
		headerView.setBackgroundResource(R.drawable.headerview_bg);
		list.addHeaderView(headerView, null, false);

		listLayout = findViewById(R.id.listLayout);
		mapLayout = findViewById(R.id.mapLayout);
		cafeAdapter = new CafeSearchListAdapter(this, MFConfig.getInstance()
				.getSearchResultList());
		list.setAdapter(cafeAdapter);
		list.setOnItemClickListener(itemClickListener);

		regionSpinner = (Spinner) findViewById(R.id.regionSpinner);
		regionStrings = new ArrayList<String>(
				Arrays.asList(MFConstants.regionNames));
		regionAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_textview, regionStrings);
		regionAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_1);
		regionSpinner.setAdapter(regionAdapter);
		int regionIndex = getIntent().getIntExtra("regionIndex", 0);
		regionSpinner.setSelection(regionIndex);
		dishesSpinner = (Spinner) findViewById(R.id.dishesSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spinner_textview, MFConstants.dishesType);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		dishesSpinner.setAdapter(adapter);
		int dishesIndex = getIntent().getIntExtra("dishesIndex", 0);
		dishesSpinner.setSelection(dishesIndex);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		adapter = new ArrayAdapter<String>(this, R.layout.spinner_textview,
				MFConstants.serviceType);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		categorySpinner.setAdapter(adapter);
		int servicesIndex = getIntent().getIntExtra("servicesIndex", 0);
		categorySpinner.setSelection(servicesIndex);

		// to avoid calling onitemselected first time
		regionSpinner.post(new Runnable() {

			public void run() {
				regionSpinner.setOnItemSelectedListener(itemSelectListener);
			}
		});
		dishesSpinner.post(new Runnable() {

			public void run() {
				dishesSpinner.setOnItemSelectedListener(itemSelectListener);
			}
		});
		categorySpinner.post(new Runnable() {

			public void run() {
				categorySpinner.setOnItemSelectedListener(itemSelectListener);
			}
		});


		// display list
		if (MFConfig.getInstance().getSearchResultList().size() > 0) {
			needPopulateMarkers = true;
			listLayout.setVisibility(View.VISIBLE);
			smallBanner.startTask();
			mapLayout.setVisibility(View.GONE);
			setTitle(R.string.searchResults);
		}

		String queryText = getIntent().getStringExtra("querySearch");
		if (queryText != null) {
			mapFilterPanel.setVisibility(View.GONE);
			displaySearchQuery.setVisibility(View.VISIBLE);

			String displayString = getString(R.string.displaySearchQuery, "\""
					+ queryText + "\"");
			SpannableString spannable = new SpannableString(displayString);
			int index = displayString.indexOf("\"") + 1;
			spannable.setSpan(
					new ForegroundColorSpan(getResources().getColor(
							R.color.green_text)), index,
					index + queryText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			displaySearchQuery.setText(spannable);
		} else if (servicesIndex == 0 && dishesIndex == 0 && regionIndex == 0) {
			listMessage.setVisibility(View.VISIBLE);
			list.setVisibility(View.INVISIBLE);
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		greenBitmap = BitmapDescriptorFactory.fromResource(R.drawable.green_pin);
		blueBitmap = BitmapDescriptorFactory.fromResource(R.drawable.blue_pin);
		favoriteBitmap = BitmapDescriptorFactory.fromResource(R.drawable.favorite_heart_pin);
		mMap.setMyLocationEnabled(true);
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			public View getInfoWindow(Marker marker) {
				View view = Map.this.getLayoutInflater().inflate(R.layout.balloon_overlay, null);
				TextView title = (TextView) view.findViewById(R.id.balloon_item_title);
				TextView snippet = (TextView) view.findViewById(R.id.balloon_item_snippet);
				title.setText(marker.getTitle());
				snippet.setText(marker.getSnippet());
				return view;
			}
			
			public View getInfoContents(Marker marker) {
				// TODO Auto-generated method stub
				return null;
			}
		});
//		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
//			
//			public boolean onMarkerClick(Marker marker) {
//				marker.setIcon(lightBlueBitmap);
//				if (mPreviousMarker != null) {
//					mPreviousMarker.setIcon(greenBitmap);
//				}
//				mPreviousMarker = marker;
//				return false;
//			}
//		});
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			public void onInfoWindowClick(Marker marker) {
				String cafeId = mMarkersHashMap.get(marker);
				Intent i = new Intent(Map.this, Details.class);
				i.putExtra("id", cafeId);
				startActivity(i);
			}
		});
		//use location client to get last location
//		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), 16));
		mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
			
			public void onMyLocationChange(Location location) {
				//maybe use location client in the future
//				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
			}
		});
//		mMap.setOnMapClickListener(new OnMapClickListener() {
//			
//			public void onMapClick(LatLng point) {
//				if (mPreviousMarker != null) {
//					mPreviousMarker.setIcon(greenBitmap);
//				}
//			}
//		});
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			public void onCameraChange(CameraPosition position) {
				if (mMapBounds.contains(position.target)) {
					searchNearby.setText(R.string.searchNearby);
				} else {
					searchNearby.setText(R.string.backToMacau);
				}
			}
		});
		
		name = getIntent().getStringExtra("name");
		selectedCafeId = getIntent().getStringExtra("id");
		String coordx = getIntent().getStringExtra("coordx");
		String coordy = getIntent().getStringExtra("coordy");
		if (coordx != null && coordy != null) {
			LatLng selectedLatLng = new LatLng(Double.parseDouble(coordx), Double.parseDouble(coordy));
			mMap.addMarker(new MarkerOptions()
            .position(selectedLatLng)
            .title(name)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin)));
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16));
		}

		searchNearby = (Button) findViewById(R.id.searchNearby);
		searchNearby.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (searchNearby.getText().toString()
						.equals(getString(R.string.backToMacau))) {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAT_DEFAULT, LONG_DEFAULT), 14));
					searchNearby.setText(R.string.searchNearby);
				} else {
					MFConfig.getInstance().getSearchResultList().clear();
					searchNearby();
				}
			}
		});
		
		if (getIntent().getBooleanExtra("fromFavorite", false)) {
			searchNearby.setVisibility(View.GONE);
			mapFilterPanel.setVisibility(View.GONE);
			
			Builder boundsBuilder = new LatLngBounds.Builder();
			
			for (String str : MFConfig.getInstance().getFavoriteLists()) {
				Cafe cafe = MFConfig.getInstance().getCafeLists().get(Integer.parseInt(str) - 1);
				boundsBuilder.include(getLatLngFromCafe(cafe));
				Marker marker = mMap.addMarker(new MarkerOptions()
	            .position(getLatLngFromCafe(cafe))
	            .title(cafe.getName())
	            .snippet(MFUtil.getDishesStringFromCafe(cafe))
	            .icon(favoriteBitmap));
				
				mMarkersHashMap.put(marker, cafe.getId());
				
				if (MFConfig.getInstance().getFavoriteLists().size() == 1) {
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLngFromCafe(cafe), 16));
				}
			}
			if (MFConfig.getInstance().getFavoriteLists().size() > 1) {
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFConfig.deviceWidth, MFConfig.deviceHeight - MFUtil.getPixelsFromDip(60f, getResources()), MFUtil.getPixelsFromDip(50f, getResources())));
			}
		} else if (getIntent().getBooleanExtra("fromBranch", false)) {
			searchNearby.setVisibility(View.GONE);
			mapFilterPanel.setVisibility(View.GONE);
			
			Builder boundsBuilder = new LatLngBounds.Builder();
			ArrayList<Cafe> branchList = (ArrayList<Cafe>) getIntent().getSerializableExtra("branchList");
			for (Cafe cafe : branchList) {
				boundsBuilder.include(getLatLngFromCafe(cafe));
				Marker marker = mMap.addMarker(new MarkerOptions()
	            .position(getLatLngFromCafe(cafe))
	            .title(cafe.getName())
	            .snippet(MFUtil.getDishesStringFromCafe(cafe))
	            .icon(greenBitmap));
				
				mMarkersHashMap.put(marker, cafe.getId());
			}
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFConfig.deviceWidth, MFConfig.deviceHeight - MFUtil.getPixelsFromDip(60f, getResources()), MFUtil.getPixelsFromDip(50f, getResources())));
//			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFUtil.getPixelsFromDip(50f, getResources())));
		}
		
	}

	private void doAdvancedSearch() {

		int regionIndex = regionSpinner.getSelectedItemPosition();
		int dishesIndex = dishesSpinner.getSelectedItemPosition();
		int dishesId = MFConstants.dishesId[dishesIndex];
		int servicesIndex = categorySpinner.getSelectedItemPosition();

		if (regionStrings.get(0).equals(getString(R.string.mapCenterArea))) {
			regionStrings.remove(0);
			regionAdapter.notifyDataSetChanged();
			if (regionIndex != 0) {
				regionSpinner.setSelection(regionIndex - 1);
				return;
			}
		}

		listMessage.setVisibility(View.GONE);
		list.setVisibility(View.VISIBLE);

		if (regionIndex == 0 && dishesIndex == 0 && servicesIndex == 0) {
			listMessage.setVisibility(View.VISIBLE);
			listMessage.setText(R.string.selectOneItemPrompt);
			list.setVisibility(View.INVISIBLE);
			MFConfig.getInstance().getSearchResultList().clear();
			if (mapLayout.isShown()) {
				Toast.makeText(this, R.string.selectOneItemPrompt,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			AdvancedSearchHelper.search(regionIndex, dishesId, servicesIndex);
			if (MFConfig.getInstance().getSearchResultList().size() == 0) {
				listMessage.setVisibility(View.VISIBLE);
				listMessage.setText(R.string.noSearchResults);
				list.setVisibility(View.INVISIBLE);
				if (mapLayout.isShown()) {
					Toast.makeText(this, R.string.noSearchResults,
							Toast.LENGTH_SHORT).show();
				}
			}
			if (listLayout.isShown()) {
				cafeAdapter.notifyDataSetChanged();
				cafeAdapter.imageLoader.cleanup();
				list.setSelection(0);
				headerView.setText(getString(R.string.totalResultsFound,
						MFConfig.getInstance().getSearchResultList().size()));
			}
		}

		if (mapLayout.isShown())
			populateOverlayFromSearchList();
		else
			needPopulateMarkers = true;

	}

	private void searchNearby() {

		PriorityQueue<Cafe> queue = new PriorityQueue<Cafe>();
		
		mMap.clear();
		mMarkersHashMap.clear();
//		mPreviousMarker = null;
		
		LatLng mapCenter = mMap.getCameraPosition().target;
		LatLngBounds currentBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			LatLng cafeLatLng = getLatLngFromCafe(cafe);
			if (currentBounds.contains(cafeLatLng)) {
				cafe.setDistance(Math.hypot(mapCenter.latitude - cafeLatLng.latitude, mapCenter.longitude - cafeLatLng.longitude));
				queue.add(cafe);
			}
		}
		Builder boundsBuilder = new LatLngBounds.Builder();
		int displayNumber = 50;
		for (int i = 0; i < displayNumber && queue.size() > 0; i++) {
			Cafe cafe = queue.poll();
			if (cafe.getStatus().equals("0")) {
				displayNumber++;
				continue;
			}
			MFConfig.getInstance().getSearchResultList().add(cafe);
			if (cafe.getId().equals(selectedCafeId))
				continue;
			
			LatLng cafeLatLng = getLatLngFromCafe(cafe);
			boundsBuilder.include(cafeLatLng);
			Marker marker = mMap.addMarker(new MarkerOptions()
            .position(cafeLatLng)
            .title(cafe.getName())
            .snippet(MFUtil.getDishesStringFromCafe(cafe))
//            .snippet(cafe.getPhone().trim().length() == 0 ? null
//					: cafe.getPhone())
            .icon(blueBitmap));
			
			mMarkersHashMap.put(marker, cafe.getId());
		}
		if (mMap.getCameraPosition().zoom < 17 && MFConfig.getInstance().getSearchResultList().size() > 0)
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFUtil.getPixelsFromDip(50f, getResources())));

		disableItemSelect = true;

		//if not search map results, result dishes and service spinner to all and add map center text
		if (!regionStrings.get(0).equals(getString(R.string.mapCenterArea))) {
			regionStrings.add(0, getString(R.string.mapCenterArea));
			regionAdapter.notifyDataSetChanged();
			regionSpinner.setSelection(0);
			dishesSpinner.setSelection(0);
			categorySpinner.setSelection(0);
		}

		mapFilterPanel.setVisibility(View.VISIBLE);
		displaySearchQuery.setVisibility(View.GONE);
		
		if (MFConfig.getInstance().getSearchResultList().size() == 0) {
			Toast.makeText(this, R.string.noMapSearchResults,
					Toast.LENGTH_SHORT).show();
			listMessage.setVisibility(View.VISIBLE);
			listMessage.setText(R.string.noMapSearchResults);
			list.setVisibility(View.INVISIBLE);
		} else {
			listMessage.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		}

		new Handler().postDelayed(new Runnable() {

			public void run() {
				disableItemSelect = false;
			}
		}, 400);
	}
	
	private LatLng getLatLngFromCafe(Cafe cafe) {
		return new LatLng(Double.parseDouble(cafe.getCoordx()), Double.parseDouble(cafe.getCoordy()));
	}

	private void populateOverlayFromSearchList() {
		
		mMap.clear();
		mMarkersHashMap.clear();
//		mPreviousMarker = null;
		
//		Builder boundsBuilder = new LatLngBounds.Builder();

		for (Cafe cafe : MFConfig.getInstance().getSearchResultList()) {
//			boundsBuilder.include(getLatLngFromCafe(cafe));
			Marker marker = mMap.addMarker(new MarkerOptions()
            .position(getLatLngFromCafe(cafe))
            .title(cafe.getName())
            .snippet(MFUtil.getDishesStringFromCafe(cafe))
//            .snippet(cafe.getPhone().trim().length() == 0 ? null
//					: cafe.getPhone())
            .icon(greenBitmap));
			
			mMarkersHashMap.put(marker, cafe.getId());
			
			
		}
//		LatLngBounds bounds = boundsBuilder.build();
//		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MFUtil.getPixelsFromDip(50f, getResources())));
//		Log.e("ZZZ", bounds.southwest.latitude + " " + bounds.southwest.longitude + "\n" 
//				+ bounds.northeast.latitude + " " + bounds.northeast.longitude);
	}

	@Override
	protected void onPause() {
		super.onPause();
		smallBanner.stopTask();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (listLayout.isShown()) {
			smallBanner.startTask();
		}
	}

	@Override
	protected void onDestroy() {
		MFConfig.getInstance().getSearchResultList().clear();
		cafeAdapter.imageLoader.cleanup();
		smallBanner.unbind();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		if (getIntent().getBooleanExtra("fromFavorite", false)) return false;
		if (getIntent().getBooleanExtra("fromBranch", false)) return false;
		
		menu.add(0, SHOW_LIST_MENU_ID, 0, R.string.showList)
				.setIcon(R.drawable.ic_action_list)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		if (MFConfig.getInstance().getSearchResultList().size() > 0) {
			menu.getItem(0).setIcon(R.drawable.map)
					.setTitle(R.string.showMap);
		} 
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case SHOW_LIST_MENU_ID:
			if (listLayout.isShown()) {
				if (needPopulateMarkers) {
					needPopulateMarkers = false;
					populateOverlayFromSearchList();
				}
				listLayout.setVisibility(View.GONE);
				smallBanner.stopTask();
				mapLayout.setVisibility(View.VISIBLE);
				item.setIcon(R.drawable.ic_action_list).setTitle(
						R.string.showList);
				setTitle(R.string.map_search);
			} else {
				listLayout.setVisibility(View.VISIBLE);
				smallBanner.startTask();
				mapLayout.setVisibility(View.GONE);
				item.setIcon(R.drawable.map).setTitle(R.string.showMap);
				setTitle(R.string.searchResults);
				// update cafeadapter only when switch to listview
				cafeAdapter.notifyDataSetChanged();
				cafeAdapter.imageLoader.cleanup();
				list.setSelection(0);
				headerView.setText(getString(R.string.totalResultsFound,
						MFConfig.getInstance().getSearchResultList().size()));
			}
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			Intent i = new Intent(Map.this, Details.class);
			i.putExtra(
					"id",
					MFConfig.getInstance().getSearchResultList()
							.get(position - 1).getId());
			startActivity(i);
		};
	};

	AdapterView.OnItemSelectedListener itemSelectListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			if (!disableItemSelect) {
				doAdvancedSearch();
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

}
