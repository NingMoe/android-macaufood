package com.cycon.macaufood.activities;

import java.util.List;
import java.util.PriorityQueue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Process;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AdvancedSearchHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.AdvView;
import com.cycon.macaufood.widget.MyItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * A list view that demonstrates the use of setEmptyView. This example alos uses
 * a custom layout file that adds some extra buttons to the screen.
 */
public class Map extends SherlockMapActivity{

	private static final int SHOW_LIST_MENU_ID = 1;
	private static final double LAT_MIN = 22.104;
	private static final double LAT_MAX = 22.21;
	private static final double LONG_MIN = 113.52;
	private static final double LONG_MAX = 113.60;
	private static final double LAT_DEFAULT = 22.194791;
	private static final double LONG_DEFAULT = 113.545122;
	private static final long DISMISS_LOC_TIME = 60000;
	private static final int MIN_DIST_RELOC = 20;
	private ImageButton location;
	private MapView mapView;
	private MyItemizedOverlay itemizedoverlay;
	private MyItemizedOverlay selectRestOverlay;
	private MyItemizedOverlay curLocationOverlay;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private String name;
	private String selectedCafeId;
	private Button searchNearby;
	private boolean shownNearby;
	private long curLocationTimeStamp;
	private int zoomlevel;
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

	@SuppressLint("NewApi")
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

		headerView = new TextView(this);
		headerView.setText(R.string.totalResultsFound);
		headerView.setTextSize(12f);
		headerView.setGravity(Gravity.CENTER_HORIZONTAL);
		headerView.setTextColor(getResources().getColor(R.color.tab_gray_text));
		headerView.setPadding(0, MFUtil.getPixelsFromDip(1f, getResources()), 0, MFUtil.getPixelsFromDip(1f, getResources()));
		headerView.setBackgroundResource(R.drawable.headerview_bg);
		list.addHeaderView(headerView);
		list.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		
		listLayout = findViewById(R.id.listLayout);
		mapLayout = findViewById(R.id.mapLayout);
		cafeAdapter = new CafeSearchListAdapter(this, MFConfig.getInstance()
				.getSearchResultList());
//		cafeAdapter.imageLoader.setImagesToLoadFromCafe(MFConfig.getInstance()
//				.getSearchResultList());
		list.setAdapter(cafeAdapter);
		list.setOnItemClickListener(itemClickListener);
		
		regionSpinner = (Spinner) findViewById(R.id.regionSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_textview, MFConstants.regionNames);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		regionSpinner.setAdapter(adapter);
		regionSpinner.setOnItemSelectedListener(itemSelectListener);
		dishesSpinner = (Spinner) findViewById(R.id.dishesSpinner);
		adapter = new ArrayAdapter<String>(this, R.layout.spinner_textview, MFConstants.dishesType);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		dishesSpinner.setAdapter(adapter);
		dishesSpinner.setOnItemSelectedListener(itemSelectListener);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		adapter = new ArrayAdapter<String>(this, R.layout.spinner_textview, MFConstants.serviceType);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		categorySpinner.setAdapter(adapter);
		categorySpinner.setOnItemSelectedListener(itemSelectListener);

		name = getIntent().getStringExtra("name");
		selectedCafeId = getIntent().getStringExtra("id");
		String coordx = getIntent().getStringExtra("coordx");
		String coordy = getIntent().getStringExtra("coordy");

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {

				// Called when a new location is found by the network location
				// provider.
				makeUseOfNewLocation(location);
				curLocationTimeStamp = System.currentTimeMillis();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		List<Overlay> mapOverlays = mapView.getOverlays();
		GeoPoint point = null;

		if (coordx != null && coordy != null) {
			Drawable redMarker = this.getResources().getDrawable(
					R.drawable.red_marker);
			int offset = MFUtil.getPixelsFromDip(9f, getResources());
			redMarker.setBounds(-redMarker.getIntrinsicWidth() / 2 + offset,
					-redMarker.getIntrinsicHeight(),
					redMarker.getIntrinsicWidth() / 2 + offset, 0);
			selectRestOverlay = new MyItemizedOverlay(null, redMarker, mapView,
					false, true);
			point = new GeoPoint((int) (Double.parseDouble(coordx) * 1E6),
					(int) (Double.parseDouble(coordy) * 1E6));
			OverlayItem overlayitem = new OverlayItem(point, name, null);
			selectRestOverlay.addOverlay(overlayitem, null);
			mapOverlays.add(selectRestOverlay);
			selectRestOverlay.callPopulate();
		} else {
			point = new GeoPoint((int) (LAT_DEFAULT * 1E6),
					(int) (LONG_DEFAULT * 1E6));
		}

		MapController mapController = mapView.getController();
		if (name != null) {
			mapController.setZoom(18);
			mapController.animateTo(point);
		} else {
			mapController.setZoom(17);
			mapController.setCenter(point);
		}

		location = (ImageButton) findViewById(R.id.location);
		location.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Location loc = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (loc != null)
					makeUseOfNewLocation(loc);
				// Register the listener with the Location Manager to receive
				// location updates
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, MIN_DIST_RELOC,
						locationListener);

			}
		});

		searchNearby = (Button) findViewById(R.id.searchNearby);
		searchNearby.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (searchNearby.getText().toString()
						.equals(getString(R.string.backToMacau))) {
					GeoPoint point = new GeoPoint((int) (LAT_DEFAULT * 1E6),
							(int) (LONG_DEFAULT * 1E6));
					MapController mapController = mapView.getController();
					mapController.setCenter(point);
					mapController.setZoom(17);
					searchNearby.setText(R.string.searchNearby);
				} else {
					MFConfig.getInstance().getSearchResultList().clear();
					searchNearby();
					if (MFConfig.getInstance().getSearchResultList().size() != 0)
						mShowListMenuItem.setVisible(true);
					else
						mShowListMenuItem.setVisible(false);
				}
			}
		});
		
		if (MFConfig.getInstance().getSearchResultList().size() > 0) {
			
			listLayout.setVisibility(View.VISIBLE);
			smallBanner.startTask();
			mapLayout.setVisibility(View.GONE);
			setTitle(R.string.searchResults);
		}
	}
	
	private void doAdvancedSearch() {
		int regionIndex = regionSpinner.getSelectedItemPosition();
		int dishesIndex = dishesSpinner.getSelectedItemPosition();
		int dishesId = MFConstants.dishesId[dishesIndex];
		int servicesIndex = categorySpinner.getSelectedItemPosition();
		
		//if all zero...
		//TODO
		
		AdvancedSearchHelper.search(regionIndex, dishesId, servicesIndex);
		
		//if result size 0...
		//TODO
		
		cafeAdapter.notifyDataSetChanged();
	}

	private void searchNearby() {
		int nwLatitude;
		int nwLongitutde;
		int seLatitude;
		int seLongitutde;

		int centerLatitude = mapView.getMapCenter().getLatitudeE6();
		int centerLongitutde = mapView.getMapCenter().getLongitudeE6();

		nwLatitude = centerLatitude + mapView.getLatitudeSpan() / 2;
		seLatitude = centerLatitude - mapView.getLatitudeSpan() / 2;
		nwLongitutde = centerLongitutde - mapView.getLongitudeSpan() / 2;
		seLongitutde = centerLongitutde + mapView.getLongitudeSpan() / 2;

		PriorityQueue<Cafe> queue = new PriorityQueue<Cafe>();
		List<Overlay> mapOverlays = mapView.getOverlays();
		if (itemizedoverlay == null) {

			int offset = MFUtil.getPixelsFromDip(9f, getResources());
			Drawable blueMarker = this.getResources().getDrawable(
					R.drawable.blue_marker);
			blueMarker.setBounds(-blueMarker.getIntrinsicWidth() / 2 + offset,
					-blueMarker.getIntrinsicHeight(),
					blueMarker.getIntrinsicWidth() / 2 + offset, 0);

			itemizedoverlay = new MyItemizedOverlay(this, blueMarker, mapView,
					true, true);
			mapOverlays.add(itemizedoverlay);
		} else
			itemizedoverlay.clearOverlayData();

		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			int coordx = 0;
			int coordy = 0;
			try {
				coordx = (int) (Double.parseDouble(cafe.getCoordx()) * 1E6);
				coordy = (int) (Double.parseDouble(cafe.getCoordy()) * 1E6);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (coordx < nwLatitude && coordx > seLatitude
					&& coordy < seLongitutde && coordy > nwLongitutde) {
				cafe.setDistance(Math.hypot(centerLatitude - coordx,
						centerLongitutde - coordy));
				queue.add(cafe);
			}
		}
		for (int i = 0; i < 50 && queue.size() > 0; i++) {
			Cafe cafe = queue.poll();
			if (cafe.getStatus().equals("0"))
				continue;
			MFConfig.getInstance().getSearchResultList().add(cafe);
			if (cafe.getId().equals(selectedCafeId))
				continue;
			GeoPoint point = new GeoPoint((int) (Double.parseDouble(cafe
					.getCoordx()) * 1E6), (int) (Double.parseDouble(cafe
					.getCoordy()) * 1E6));
			OverlayItem overlayitem = new OverlayItem(point, cafe.getName(),
					cafe.getPhone().trim().length() == 0 ? null
							: cafe.getPhone());
			itemizedoverlay.addOverlay(overlayitem, cafe.getId());
		}

		itemizedoverlay.hideBalloon();
		itemizedoverlay.callPopulate();
		if (!shownNearby) {
			mapOverlays.add(itemizedoverlay);
			shownNearby = true;
		}
		cafeAdapter.notifyDataSetChanged();
		cafeAdapter.imageLoader.cleanup();
//		cafeAdapter.imageLoader.setImagesToLoadFromCafe(MFConfig.getInstance()
//				.getSearchResultList());

		mapView.postInvalidate();
	}
	
	private void populateOverlayFromSearchList() {

		List<Overlay> mapOverlays = mapView.getOverlays();

		if (itemizedoverlay == null) {

			int offset = MFUtil.getPixelsFromDip(9f, getResources());
			Drawable blueMarker = this.getResources().getDrawable(
					R.drawable.blue_marker);
			blueMarker.setBounds(-blueMarker.getIntrinsicWidth() / 2 + offset,
					-blueMarker.getIntrinsicHeight(),
					blueMarker.getIntrinsicWidth() / 2 + offset, 0);

			itemizedoverlay = new MyItemizedOverlay(this, blueMarker, mapView,
					true, true);
			mapOverlays.add(itemizedoverlay);
		} else
			itemizedoverlay.clearOverlayData();
		
		
		for (Cafe  cafe : MFConfig.getInstance().getSearchResultList()) {
			GeoPoint point = new GeoPoint((int) (Double.parseDouble(cafe
					.getCoordx()) * 1E6), (int) (Double.parseDouble(cafe
					.getCoordy()) * 1E6));
			OverlayItem overlayitem = new OverlayItem(point, cafe.getName(),
					cafe.getPhone().trim().length() == 0 ? null
							: cafe.getPhone());
			itemizedoverlay.addOverlay(overlayitem, cafe.getId());
		}
		
		itemizedoverlay.hideBalloon();
		itemizedoverlay.callPopulate();
		if (!shownNearby) {
			mapOverlays.add(itemizedoverlay);
			shownNearby = true;
		}

		mapView.postInvalidate();
	}

	private void makeUseOfNewLocation(Location location) {
		if (location == null)
			return;
		List<Overlay> mapOverlays = mapView.getOverlays();

		if (curLocationOverlay == null) {
			Drawable drawable = this.getResources().getDrawable(
					R.drawable.cur_location);

			int offset = MFUtil.getPixelsFromDip(13f, getResources());
			drawable.setBounds(-drawable.getIntrinsicWidth() / 2 + offset,
					-drawable.getIntrinsicHeight(),
					drawable.getIntrinsicWidth() / 2 + offset, 0);
			curLocationOverlay = new MyItemizedOverlay(null, drawable, mapView,
					false, true);
			mapOverlays.add(curLocationOverlay);
		} else {
			curLocationOverlay.clearOverlayData();
		}
		GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));
		OverlayItem overlayitem = new OverlayItem(point, "你在這裡", null);

		curLocationOverlay.addOverlay(overlayitem, null);
		curLocationOverlay.hideBalloon();
		curLocationOverlay.callPopulate();

		MapController mapController = mapView.getController();
		mapController.animateTo(point);
		mapController.setZoom(18);

		mapView.postInvalidate();

		double x = location.getLatitude();
		double y = location.getLongitude();
		if (x < LAT_MIN || x > LAT_MAX || y < LONG_MIN || y > LONG_MAX) {
			searchNearby.setText(R.string.backToMacau);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
		zoomlevel = mapView.getZoomLevel();
		smallBanner.stopTask();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if longer than a period time, dismiss the cur Location marker
		if (curLocationTimeStamp != 0
				&& System.currentTimeMillis() - curLocationTimeStamp > DISMISS_LOC_TIME) {
			if (curLocationOverlay != null) {
				curLocationOverlay.clearOverlayData();
				curLocationOverlay.hideBalloon();
			}
		}

		if (itemizedoverlay != null) {
			MapController mapController = mapView.getController();
			if (zoomlevel != 0)
				mapController.setZoom(zoomlevel);
			mapView.postInvalidate();
		}
		
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
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		menu.add(0, SHOW_LIST_MENU_ID, 0, R.string.showList)
				.setIcon(R.drawable.ic_action_list)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		// menu.add(0, SHOW_MAP_MENU_ID, 1,
		// R.string.showMap).setIcon(R.drawable.map).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mShowListMenuItem = menu.getItem(0);
		// mShowMapMenuItem = menu.getItem(1);
		if (MFConfig.getInstance().getSearchResultList().size() > 0) {
			mShowListMenuItem.setIcon(R.drawable.map).setTitle(
					R.string.showMap);
		} else {
			mShowListMenuItem.setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case SHOW_LIST_MENU_ID:
			if (listLayout.isShown()) {
				populateOverlayFromSearchList();
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
			i.putExtra("id",
					MFConfig.getInstance().getSearchResultList().get(position)
							.getId());
			startActivity(i);
		};
	};
	
	AdapterView.OnItemSelectedListener itemSelectListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			doAdvancedSearch();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};

}
