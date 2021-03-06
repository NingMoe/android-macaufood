package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
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

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cycon.macaufood.R;
import com.cycon.macaufood.activities.PSCafeLocation.ErrorDialogFragment;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AdvancedSearchHelper;
import com.cycon.macaufood.utilities.LatLngBoundHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.AdvView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
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
public class Map extends SherlockFragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	public static final int SHOW_MAP_REQUEST_CODE = 1;
	private static final int SHOW_LIST_MENU_ID = 1;
	private static final int SHOW_GOOGLE_MAP_MENU_ID = 2;
	public static final double LAT_MIN = 22.104;
	public static final double LAT_MAX = 22.24;
	public static final double LONG_MIN = 113.51;
	public static final double LONG_MAX = 113.60;
	private static final double LAT_DEFAULT = 22.19971287;
	private static final double LONG_DEFAULT = 113.54500506;
	private static final double LAT_DEFAULT_ISLAND = 22.148;
	private static final double LONG_DEFAULT_ISLAND = 113.559;
	private static final double LAT_ISLAND_BOUNDARY = 22.1735;
	private static LatLngBounds mMapBounds = new LatLngBounds(new LatLng(LAT_MIN, LONG_MIN), new LatLng(LAT_MAX, LONG_MAX));
	private String selectedCafeId;
	private Button searchNearby;
	private AdvView smallBanner;
	private View listLayout;
	private View mapLayout;
	
	private LocationClient mLocationClient;
	private Location mCurrentLocation;

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
	private com.actionbarsherlock.view.MenuItem mOptionsItem;
	private com.actionbarsherlock.view.MenuItem mGoogleMapItem;
	private GoogleMap mMap;
	private HashMap<Marker, String> mMarkersHashMap = new HashMap<Marker, String>();
	private MarkerOptions mSelectedMarkerOptions;
	private Marker mSelectedMarker;
	private Button navigateIsland;
	private BitmapDescriptor greenBitmap;
	private BitmapDescriptor blueBitmap;;
	private BitmapDescriptor favoriteBitmap;
	private ArrayList<Cafe> searchResultCafes;
	private boolean isFirstPopulateFromSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// use same logic as in BaseActivity
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			finish();
			Process.killProcess(Process.myPid());
			return;
		}
		setContentView(R.layout.map);
		
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		
		searchResultCafes = new ArrayList<Cafe>(MFConfig.getInstance().getSearchResultList());
		MFConfig.getInstance().getSearchResultList().clear();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		smallBanner = (AdvView) findViewById(R.id.viewPager);
		list = (ListView) findViewById(R.id.list);
		listMessage = (TextView) findViewById(R.id.listMessage);
		mapFilterPanel = findViewById(R.id.mapFilterPanel);
		displaySearchQuery = (TextView) findViewById(R.id.displaySearchQuery);
		navigateIsland = (Button) findViewById(R.id.navigateIsland);
		
		setUpMapIfNeeded();

		headerView = new TextView(this);
		headerView.setText(getString(R.string.totalResultsFound, searchResultCafes.size()));
		headerView.setTextSize(12f);
		headerView.setGravity(Gravity.CENTER_HORIZONTAL);
		headerView.setTextColor(getResources().getColor(R.color.tab_gray_text));
		headerView.setPadding(0, MFUtil.getPixelsFromDip(1f, getResources()),
				0, MFUtil.getPixelsFromDip(1f, getResources()));
		headerView.setBackgroundResource(R.drawable.headerview_bg);
		list.addHeaderView(headerView, null, false);

		listLayout = findViewById(R.id.listLayout);
		mapLayout = findViewById(R.id.mapLayout);
		cafeAdapter = new CafeSearchListAdapter(this, searchResultCafes);
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
		regionSpinner.setSelection(regionIndex, false);
		dishesSpinner = (Spinner) findViewById(R.id.dishesSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spinner_textview, MFConstants.dishesType);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		dishesSpinner.setAdapter(adapter);
		int dishesIndex = getIntent().getIntExtra("dishesIndex", 0);
		dishesSpinner.setSelection(dishesIndex, false);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		adapter = new ArrayAdapter<String>(this, R.layout.spinner_textview,
				MFConstants.serviceType);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		categorySpinner.setAdapter(adapter);
		int servicesIndex = getIntent().getIntExtra("servicesIndex", 0);
		categorySpinner.setSelection(servicesIndex, false);

		regionSpinner.setOnItemSelectedListener(itemSelectListener);
		dishesSpinner.setOnItemSelectedListener(itemSelectListener);
		categorySpinner.setOnItemSelectedListener(itemSelectListener);
				
		// to avoid calling onitemselected initially
		disableItemSelect = true;
		new Handler().postDelayed(new Runnable() {

			public void run() {
				disableItemSelect = false;
			}
		}, 400);

		
//		if (selectedCafeId != null) {
//			return;
//		}

		// display list
		if (searchResultCafes.size() > 0) {
			needPopulateMarkers = true;
			listLayout.setVisibility(View.VISIBLE);
			mapLayout.setVisibility(View.GONE);
			setTitle(R.string.searchResults);
			isFirstPopulateFromSearch = true;
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
				if (marker.equals(mSelectedMarker)) {
					snippet.setVisibility(View.GONE);
					view.findViewById(R.id.arrow).setVisibility(View.GONE);
				} else {
					snippet.setText(marker.getSnippet());
				}
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

				if (!marker.equals(mSelectedMarker)) {
					String cafeId = mMarkersHashMap.get(marker);
					Intent i = new Intent(Map.this, Details.class);
					i.putExtra("id", cafeId);
					i.putExtra("fromHome", true);
					startActivity(i);
				}
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
//				MFLog.e("ZZZ", point.latitude + " " + point.longitude);
//			}
//		});
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			public void onCameraChange(CameraPosition position) {
				if (mMapBounds.contains(position.target)) {
					searchNearby.setText(R.string.searchNearby);
					if (position.zoom > 13) {
						navigateIsland.setVisibility(View.VISIBLE);
					} else {
						navigateIsland.setVisibility(View.GONE);
					}
				} else {
					searchNearby.setText(R.string.backToMacau);
					navigateIsland.setVisibility(View.GONE);
				}
				
				if (position.target.latitude < LAT_ISLAND_BOUNDARY) {
					navigateIsland.setBackgroundResource(R.drawable.map_arrow_up);
					navigateIsland.setText(R.string.macauPeninsula);
				} else {
					navigateIsland.setBackgroundResource(R.drawable.map_arrow_down);
					navigateIsland.setText(R.string.macauIsland);
				}
			}
		});
		
		String name = getIntent().getStringExtra("name");
		selectedCafeId = getIntent().getStringExtra("id");
		String coordx = getIntent().getStringExtra("coordx");
		String coordy = getIntent().getStringExtra("coordy");
		if (coordx != null && coordy != null) {
			LatLng selectedLatLng = new LatLng(Double.parseDouble(coordx), Double.parseDouble(coordy));
			mSelectedMarkerOptions = new MarkerOptions()
            .position(selectedLatLng)
            .title(name)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_pin));
			
			mSelectedMarker = mMap.addMarker(mSelectedMarkerOptions);
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
					searchResultCafes.clear();
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
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFConfig.deviceWidth, MFConfig.deviceHeight - MFUtil.getPixelsFromDip(96f, getResources()), MFUtil.getPixelsFromDip(50f, getResources())));
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
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFConfig.deviceWidth, MFConfig.deviceHeight - MFUtil.getPixelsFromDip(96f, getResources()), MFUtil.getPixelsFromDip(50f, getResources())));
//			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MFUtil.getPixelsFromDip(50f, getResources())));
		}
		
		if (needPopulateMarkers) {
			needPopulateMarkers = false;
			populateOverlayFromSearchList();
		}
		
		navigateIsland.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if (navigateIsland.getText().toString().equals(getString(R.string.macauPeninsula))) {
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAT_DEFAULT, LONG_DEFAULT), 14));
				} else {
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAT_DEFAULT_ISLAND, LONG_DEFAULT_ISLAND), 13.6f));
				}
			}
		});
		
	}

	private void doAdvancedSearch() {

		if (mGoogleMapItem != null) {
			mOptionsItem.setVisible(true);
			mGoogleMapItem.setVisible(false);
		}

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
			searchResultCafes.clear();
			if (mapLayout.isShown()) {
				Toast.makeText(this, R.string.selectOneItemPrompt,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			AdvancedSearchHelper.search(regionIndex, dishesId, servicesIndex, searchResultCafes);
			if (searchResultCafes.size() == 0) {
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
						searchResultCafes.size()));
			}
		}

		if (mapLayout.isShown())
			populateOverlayFromSearchList();
		else
			needPopulateMarkers = true;

	}

	private void searchNearby() {

		
		
		mMap.clear();
		mMarkersHashMap.clear();
		if (mGoogleMapItem != null) {
			mOptionsItem.setVisible(true);
			mGoogleMapItem.setVisible(false);
		}
		if (mSelectedMarkerOptions != null) {
			mSelectedMarker = mMap.addMarker(mSelectedMarkerOptions);
		}
		
		LatLng mapCenter = mMap.getCameraPosition().target;
		LatLngBounds currentBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

		int displayNumber = 50;
		PriorityQueue<Cafe> queue = new PriorityQueue<Cafe>(displayNumber + 1);
		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0") || cafe.getId().equals(selectedCafeId)) continue;
			LatLng cafeLatLng = getLatLngFromCafe(cafe);
			if (currentBounds.contains(cafeLatLng)) {
				double dist = Math.hypot(mapCenter.latitude - cafeLatLng.latitude, mapCenter.longitude - cafeLatLng.longitude);
				cafe.setDistance(dist);
				if (queue.size() < displayNumber) {
					queue.add(cafe);
				} else if (dist < queue.peek().getDistance()) {
					queue.poll();
					queue.add(cafe);
				}
			}
		}
		Builder boundsBuilder = new LatLngBounds.Builder();
		ArrayList<Cafe> priorityList = new ArrayList<Cafe>();
		while (!queue.isEmpty()) {
			Cafe cafe = queue.poll();
			if (cafe.getPriority().equals("0")) {
				searchResultCafes.add(cafe);
			} else {
				priorityList.add(cafe);
			}
			
			LatLng cafeLatLng = getLatLngFromCafe(cafe);
			boundsBuilder.include(cafeLatLng);
			Marker marker = mMap.addMarker(new MarkerOptions()
            .position(cafeLatLng)
            .title(cafe.getName())
            .snippet(MFUtil.getDishesStringFromCafe(cafe))
            .icon(blueBitmap));
			
			mMarkersHashMap.put(marker, cafe.getId());
		}
		
		Collections.sort(priorityList, new Comparator<Cafe>() {
			public int compare(Cafe cafe1, Cafe cafe2) {
				
				return Integer.parseInt(cafe2.getPriority()) - Integer.parseInt(cafe1.getPriority());
			};
			
		});
		
		Collections.reverse(searchResultCafes);
		searchResultCafes.addAll(0, priorityList);
		
		
		if (mMap.getCameraPosition().zoom < 17 && searchResultCafes.size() > 0)
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
		
		if (searchResultCafes.size() == 0) {
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
		
		if (mMap == null) {
			setUpMapIfNeeded(); //fix a crash report
		}
		
		mMap.clear();
		mMarkersHashMap.clear();

		//to readd the red bin after map is clear
		if (mSelectedMarkerOptions != null) {
			mSelectedMarker = mMap.addMarker(mSelectedMarkerOptions);
		}
		
		Builder boundsBuilder = new LatLngBounds.Builder();

		for (Cafe cafe : searchResultCafes) {
			if (cafe.getId().equals(selectedCafeId)) continue;
			
			Marker marker = mMap.addMarker(new MarkerOptions()
            .position(getLatLngFromCafe(cafe))
            .title(cafe.getName())
            .snippet(MFUtil.getDishesStringFromCafe(cafe))
            .icon(greenBitmap));
			
			mMarkersHashMap.put(marker, cafe.getId());
			
//			//TODO temp
//			if (cafe.getId().equals("1943") || cafe.getId().equals("163") || cafe.getId().equals("1977") || cafe.getId().equals("750")|| cafe.getId().equals("172") || cafe.getId().equals("35")) continue;
			boundsBuilder.include(getLatLngFromCafe(cafe));
			
		}
		
		if (regionSpinner.getSelectedItemPosition() == 0) return;
		
		if (searchResultCafes.size() == 0) {
			LatLngBounds bounds = LatLngBoundHelper.regionBounds[regionSpinner.getSelectedItemPosition()];
			if (bounds != null) {
				if (isFirstPopulateFromSearch) {
					isFirstPopulateFromSearch = false;
					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, MFConfig.deviceWidth , list.getHeight() + smallBanner.getHeight(), MFUtil.getPixelsFromDip(50f, getResources())));
				} else {
					mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MFUtil.getPixelsFromDip(50f, getResources())));
				}
			}
			return;
		}
		if (searchResultCafes.size() == 1) {
			if (isFirstPopulateFromSearch) {
				isFirstPopulateFromSearch = false;
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLngFromCafe(searchResultCafes.get(0)), 17));
			} else {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getLatLngFromCafe(searchResultCafes.get(0)), 17));
			}
			return;
		}
		
		if (searchResultCafes.size() == 2) {
			LatLng cafe1LatLng = getLatLngFromCafe(searchResultCafes.get(0));
			LatLng cafe2LatLng = getLatLngFromCafe(searchResultCafes.get(1));
			double dist = Math.hypot(cafe1LatLng.latitude - cafe2LatLng.latitude, cafe1LatLng.longitude - cafe2LatLng.longitude);
			if (dist < 0.002) {
				if (isFirstPopulateFromSearch) {
					isFirstPopulateFromSearch = false;
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((cafe1LatLng.latitude + cafe2LatLng.latitude) / 2, (cafe1LatLng.longitude + cafe2LatLng.longitude) / 2), 17));
				} else {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((cafe1LatLng.latitude + cafe2LatLng.latitude) / 2, (cafe1LatLng.longitude + cafe2LatLng.longitude) / 2), 17));
				}
				return;
			}
		}
		
		LatLngBounds bounds = boundsBuilder.build();
		if (isFirstPopulateFromSearch) {
			isFirstPopulateFromSearch = false;
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, MFConfig.deviceWidth , list.getHeight() + smallBanner.getHeight(), MFUtil.getPixelsFromDip(50f, getResources())));
		} else {
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MFUtil.getPixelsFromDip(50f, getResources())));
		}
	}

	@Override
	protected void onDestroy() {
//		MFConfig.getInstance().getSearchResultList().clear();
		cafeAdapter.imageLoader.cleanup();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		if (getIntent().getBooleanExtra("fromFavorite", false)) return false;
		if (getIntent().getBooleanExtra("fromBranch", false)) return false;
		
		menu.add(0, SHOW_LIST_MENU_ID, 0, R.string.showList)
				.setIcon(R.drawable.ic_action_list)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mOptionsItem = menu.getItem(0);
		if (searchResultCafes.size() > 0) {
			mOptionsItem.setIcon(R.drawable.map)
					.setTitle(R.string.showMap);
		} 
		if (selectedCafeId != null) {
			menu.add(0, SHOW_GOOGLE_MAP_MENU_ID, 1, R.string.googleMap)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			mGoogleMapItem = menu.getItem(1);
			mOptionsItem.setVisible(false);
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case SHOW_LIST_MENU_ID:
			if (listLayout.isShown()) {
				listLayout.setVisibility(View.GONE);
				mapLayout.setVisibility(View.VISIBLE);
				item.setIcon(R.drawable.ic_action_list).setTitle(
						R.string.showList);
				setTitle(R.string.map_search);
				if (needPopulateMarkers) {
					needPopulateMarkers = false;
					populateOverlayFromSearchList();
				}
			} else {
				listLayout.setVisibility(View.VISIBLE);
				mapLayout.setVisibility(View.GONE);
				item.setIcon(R.drawable.map).setTitle(R.string.showMap);
				setTitle(R.string.searchResults);
				// update cafeadapter only when switch to listview
				cafeAdapter.notifyDataSetChanged();
				cafeAdapter.imageLoader.cleanup();
				list.setSelection(0);
				headerView.setText(getString(R.string.totalResultsFound,
						searchResultCafes.size()));
			}
			return true;
		case SHOW_GOOGLE_MAP_MENU_ID:
			String coordx = getIntent().getStringExtra("coordx");
			String coordy = getIntent().getStringExtra("coordy");
			
			 double latitude = Double.parseDouble(coordx);
			 double longitude = Double.parseDouble(coordy);

			 String label = getIntent().getStringExtra("name");
			 label = label.replaceAll("\\(", " ").replaceAll("\\)", " ");
			 String uriBegin = "geo:" + latitude + "," + longitude;
			 String query = latitude + "," + longitude + "(" + label + ")";
			 String encodedQuery = Uri.encode(query);
			 String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
			 Uri uri = Uri.parse(uriString);
			 Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
			 startActivity(intent);
			
			return true;
		case android.R.id.home:
			finish();
//			Intent i = new Intent(this, Home.class);
//			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(i);
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
					searchResultCafes
							.get(position - 1).getId());
			i.putExtra("fromMapList", true);
			startActivityForResult(i, SHOW_MAP_REQUEST_CODE);
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
	
	
	//--------------Location Listener
	
	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		mCurrentLocation = mLocationClient.getLastLocation();
//		mCurrentLocation.setLatitude(22.19971287);
//		mCurrentLocation.setLongitude(113.54500506);
		if (mMap != null && mCurrentLocation != null && getIntent().getBooleanExtra("fromMap", false)) {
			LatLng selectedLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			if (mMapBounds.contains(selectedLatLng)) {
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
			}
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
	
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
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
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
	
	
	
	

}
