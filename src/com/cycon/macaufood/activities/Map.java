package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.cycon.macaufood.widget.MyItemizedOverlay;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBoundsCreator;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

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
//	private static final long DISMISS_LOC_TIME = 60000;
//	private static final int MIN_DIST_RELOC = 20;
	private static LatLngBounds mMapBounds = new LatLngBounds(new LatLng(LAT_MIN, LONG_MIN), new LatLng(LAT_MAX, LONG_MAX));
//	private ImageButton location;
	// private MapView mapView;
//	private MyItemizedOverlay itemizedoverlay;
//	private MyItemizedOverlay selectRestOverlay;
//	private MyItemizedOverlay curLocationOverlay;
//	private LocationManager locationManager;
//	private LocationListener locationListener;
	private String name;
	private String selectedCafeId;
	private Button searchNearby;
//	private boolean shownNearby;
//	private long curLocationTimeStamp;
//	private int zoomlevel;
	private com.actionbarsherlock.view.MenuItem mShowListMenuItem;
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

		setUpMapIfNeeded();

		smallBanner = (AdvView) findViewById(R.id.smallBanner);
		list = (ListView) findViewById(R.id.list);
		listMessage = (TextView) findViewById(R.id.listMessage);
		mapFilterPanel = findViewById(R.id.mapFilterPanel);
		displaySearchQuery = (TextView) findViewById(R.id.displaySearchQuery);

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
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapClickListener(new OnMapClickListener() {
			
			public void onMapClick(LatLng point) {
				Log.e("ZZZ", point.latitude + " " + point.longitude);
			}
		});
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
            .title(name));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
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
		
		LatLng mapCenter = mMap.getCameraPosition().target;
		LatLngBounds currentBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			LatLng cafeLatLng = getLatLngFromCafe(cafe);
			if (currentBounds.contains(cafeLatLng)) {
				cafe.setDistance(Math.hypot(mapCenter.latitude - cafeLatLng.latitude, mapCenter.longitude - cafeLatLng.longitude));
				queue.add(cafe);
			}
		}
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
			mMap.addMarker(new MarkerOptions()
            .position(getLatLngFromCafe(cafe))
            .title(cafe.getName())
            .snippet(cafe.getPhone().trim().length() == 0 ? null
					: cafe.getPhone())
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		}

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

		for (Cafe cafe : MFConfig.getInstance().getSearchResultList()) {
			mMap.addMarker(new MarkerOptions()
            .position(getLatLngFromCafe(cafe))
            .title(cafe.getName())
            .snippet(cafe.getPhone().trim().length() == 0 ? null
					: cafe.getPhone())
            .icon(BitmapDescriptorFactory.defaultMarker(90)));
		}
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
		menu.add(0, SHOW_LIST_MENU_ID, 0, R.string.showList)
				.setIcon(R.drawable.ic_action_list)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mShowListMenuItem = menu.getItem(0);
		if (MFConfig.getInstance().getSearchResultList().size() > 0) {
			mShowListMenuItem.setIcon(R.drawable.map)
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
