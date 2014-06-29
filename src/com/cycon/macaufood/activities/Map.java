package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapStatus;
import com.baidu.mapapi.map.MKMapStatusChangeListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AdvancedSearchHelper;
import com.cycon.macaufood.utilities.LatLngBoundHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.AdvView;

/**
 * A list view that demonstrates the use of setEmptyView. This example alos uses
 * a custom layout file that adds some extra buttons to the screen.
 */
public class Map extends SherlockFragmentActivity {

	public static final String BAIDU_MAP_API_KEY = "CjWobmOYcj8eD4ilipMllU5P";
	public static final int SHOW_MAP_REQUEST_CODE = 1;
	private static final int SHOW_LIST_MENU_ID = 1;
	public static final double LAT_DIFF  = 0.0030;
	public static final double LONG_DIFF = 0.0117;
	public static final double LAT_MIN = 22.104 + LAT_DIFF;
	public static final double LAT_MAX = 22.24 + LAT_DIFF;
	public static final double LONG_MIN = 113.51 + LONG_DIFF;
	public static final double LONG_MAX = 113.60 + LONG_DIFF;
	private static final double LAT_DEFAULT = 22.19971287 + LAT_DIFF;
	private static final double LONG_DEFAULT = 113.54500506 + LONG_DIFF;
	private static final double LAT_DEFAULT_ISLAND = 22.148 + LAT_DIFF;
	private static final double LONG_DEFAULT_ISLAND = 113.559 + LONG_DIFF;
	private static final double LAT_ISLAND_BOUNDARY = 22.1735 + LAT_DIFF;
	private static final float MARKER_HEIGHT_DP = 36f; //72px in xhdpi folder
	
	private String selectedCafeId;
	private Button searchNearby;
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
	private com.actionbarsherlock.view.MenuItem mOptionsItem;
//	private GoogleMap mMap;
//	private HashMap<Marker, String> mMarkersHashMap = new HashMap<Marker, String>();
//	private MarkerOptions mSelectedMarkerOptions;
//	private Marker mSelectedMarker;
	private Button navigateIsland;
//	private BitmapDescriptor greenBitmap;
//	private BitmapDescriptor blueBitmap;;
//	private BitmapDescriptor favoriteBitmap;
	private ArrayList<Cafe> searchResultCafes;
	private boolean isFirstPopulateFromSearch;
	
	private boolean mMapShown;
	private BMapManager mBMapMan = null;
	private MapView mMapView = null;
	private MapController mMapController = null;
	private ImageButton curLocation;
	// 定位相关
	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	//定位图层
	MyLocationOverlay myLocationOverlay = null;
	boolean isRequest = false;//是否手动触发请求定位
	boolean isRequestFromStart = false;
	
	private MyOverlay mSelectedCafeOverlay = null;
	private MyOverlay mOverlay = null;
	private PopupOverlay   pop  = null;
	private ArrayList<OverlayItem>  mItems = null; 
	private CafeOverlayItem mCurItem;
	private TextView  popupTitle = null;
	private TextView  popupSnippet = null;
	private View popupArrow = null;
	private View popupView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// use same logic as in BaseActivity
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			finish();
			Process.killProcess(Process.myPid());
			return;
		}
		
		mBMapMan=new BMapManager(getApplication());  
		mBMapMan.init(BAIDU_MAP_API_KEY, null); 
		
		searchResultCafes = new ArrayList<Cafe>(MFConfig.getInstance().getSearchResultList());
		MFConfig.getInstance().getSearchResultList().clear();
		


		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.map);

		list = (ListView) findViewById(R.id.list);
		listMessage = (TextView) findViewById(R.id.listMessage);
		mapFilterPanel = findViewById(R.id.mapFilterPanel);
		displaySearchQuery = (TextView) findViewById(R.id.displaySearchQuery);
		navigateIsland = (Button) findViewById(R.id.navigateIsland);

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
//		regionSpinner.post(new Runnable() {
//
//			public void run() {
				regionSpinner.setOnItemSelectedListener(itemSelectListener);
//			}
//		});
//		dishesSpinner.post(new Runnable() {
//
//			public void run() {
				dishesSpinner.setOnItemSelectedListener(itemSelectListener);
//			}
//		});
//		categorySpinner.post(new Runnable() {
//
//			public void run() {
				categorySpinner.setOnItemSelectedListener(itemSelectListener);
//			}
//		});
		
		
		disableItemSelect = true;
		new Handler().postDelayed(new Runnable() {

			public void run() {
				disableItemSelect = false;
			}
		}, 400);

		
//		if (selectedCafeId != null) {
//			return;
//		}



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
		
		// display list
		if (searchResultCafes.size() > 0) {
			needPopulateMarkers = true;
			listLayout.setVisibility(View.VISIBLE);
			mapLayout.setVisibility(View.GONE);
			setTitle(R.string.searchResults);
			isFirstPopulateFromSearch = true;
			mMapShown = false;
		} else {
			mMapShown = true;
		}

		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		if (mMapView == null) {
			setUpMap();
		}
	}
	
	/**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
    	
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;

            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();
            //如果不显示定位精度圈，将accuracy赋值为0即可
            locData.accuracy = location.getRadius();
            // 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
            locData.direction = location.getDerect();
            //更新定位数据
            myLocationOverlay.setData(locData);
            //更新图层数据执行刷新后生效
            mMapView.refresh();
            //是手动触发请求或首次定位时，移动到定位点
            if (isRequest){
                mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
                isRequest = false;
            }
            if (isRequestFromStart && locData.latitude < LAT_MAX && locData.latitude > LAT_MIN && locData.longitude < LONG_MAX && locData.longitude > LONG_MIN){
                mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
                isRequestFromStart = false;
            }
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }
    
    private static class CafeOverlayItem extends OverlayItem {
    	
    	private String cafeId;

		public CafeOverlayItem(GeoPoint arg0, String arg1, String arg2, String cafeId) {
			super(arg0, arg1, arg2);
			this.cafeId = cafeId;
		}
		
		private String getCafeId() {
			return cafeId;
		}
    	
    }
    
    private class MyOverlay extends ItemizedOverlay<CafeOverlayItem>{

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}
		

		@Override
		public boolean onTap(int index){
			CafeOverlayItem item = (CafeOverlayItem)getItem(index);
			mCurItem = item ;
			popupTitle.setText(item.getTitle());
			if (item.getSnippet() != null) {
				popupSnippet.setVisibility(View.VISIBLE);
				popupSnippet.setText(item.getSnippet());
				popupArrow.setVisibility(View.VISIBLE);
			} else {
				popupSnippet.setVisibility(View.GONE);
				popupArrow.setVisibility(View.GONE);
			}

			pop.showPopup(popupView, item.getPoint(), MFUtil.getPixelsFromDip(MARKER_HEIGHT_DP, getResources()));

			return true;
		}
		
		@Override
		public boolean onTap(GeoPoint pt , MapView mMapView){
			if (pop != null){
                pop.hidePop();
			}
			return false;
		}
    	
    }

	private void setUpMap() {
		mMapView=(MapView)findViewById(R.id.map);  
		mMapView.setBuiltInZoomControls(true);  
		//设置启用内置的缩放控件  
		mMapController=mMapView.getController();  
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放  
		GeoPoint point =new GeoPoint((int)(LAT_DEFAULT* 1E6),(int)(LONG_DEFAULT* 1E6));  
		//用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)  
		mMapController.setCenter(point);//设置地图中心点  
		mMapController.setZoom(16.5f);//设置地图zoom级别  
		mMapController.enableClick(true);
		
		 //定位初始化
        mLocClient = new LocationClient( this );
        locData = new LocationData();
        mLocClient.registerLocationListener( myListener );
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
        option.setScanSpan(10000);
        mLocClient.setLocOption(option);
        //click from map instead from search / map is displayed
        if (mMapShown) {
        	if (!mLocClient.isStarted()) mLocClient.start();
        	//move to my position if click from action button
        	if (getIntent().getBooleanExtra("fromHome", false)) isRequestFromStart = true;
		}
        
		myLocationOverlay = new MyLocationOverlay(mMapView);
	    myLocationOverlay.setData(locData);
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mMapView.refresh();
		
		
		curLocation = (ImageButton)findViewById(R.id.curLocation);
		curLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				isRequest = true;
				if (!mLocClient.isStarted()) mLocClient.start();
		        mLocClient.requestLocation();
			}
		});
		
		popupView = getLayoutInflater().inflate(R.layout.balloon_overlay, null);
		popupTitle = (TextView) popupView.findViewById(R.id.balloon_item_title);
		popupSnippet = (TextView) popupView.findViewById(R.id.balloon_item_snippet);
		popupArrow = popupView.findViewById(R.id.arrow);
		pop = new PopupOverlay(mMapView, new PopupClickListener() {
			
			@Override
			public void onClickedPopup(int index) {
				if (mCurItem.getSnippet() == null) {
					return;
				}
				Intent i = new Intent(Map.this, Details.class);
				i.putExtra("id", mCurItem.getCafeId());
				i.putExtra("fromMap", true);
				startActivity(i);
			}
		});
		
		mMapView.regMapStatusChangeListener(new MKMapStatusChangeListener() {
			
			@Override
			public void onMapStatusChange(MKMapStatus mapStatus) {
				GeoPoint targetGeo = mapStatus.targetGeo;
				double targetLat = targetGeo.getLatitudeE6() / 1000000.0;
				double targetLng = targetGeo.getLongitudeE6() / 1000000.0;
				if (targetLat < LAT_MAX && targetLat > LAT_MIN && targetLng < LONG_MAX && targetLng > LONG_MIN) {
					searchNearby.setText(R.string.searchNearby);
					if (mapStatus.zoom > 15) {
						navigateIsland.setVisibility(View.VISIBLE);
					} else {
						navigateIsland.setVisibility(View.GONE);
					}
				} else {
					searchNearby.setText(R.string.backToMacau);
					navigateIsland.setVisibility(View.GONE);
				}
				if (targetLat < LAT_ISLAND_BOUNDARY) {
					navigateIsland
							.setBackgroundResource(R.drawable.map_arrow_up);
					navigateIsland.setText(R.string.macauPeninsula);
				} else {
					navigateIsland
							.setBackgroundResource(R.drawable.map_arrow_down);
					navigateIsland.setText(R.string.macauIsland);
				}
			}
		});
		
		String name = getIntent().getStringExtra("name");
		selectedCafeId = getIntent().getStringExtra("id");
		String coordx = getIntent().getStringExtra("coordx");
		String coordy = getIntent().getStringExtra("coordy");
		if (coordx != null && coordy != null) {
			if (mSelectedCafeOverlay != null) {
				mSelectedCafeOverlay.removeAll();
			}
			mSelectedCafeOverlay = new MyOverlay(getResources().getDrawable(R.drawable.red_pin), mMapView);
			GeoPoint p = getGeoPointFromString(coordx, coordy);
			CafeOverlayItem item = new CafeOverlayItem(p, name ,null, selectedCafeId);
			mSelectedCafeOverlay.addItem(item);
			mMapController.setCenter(p);
			mMapController.setZoom(17.5f);
			mMapView.getOverlays().add(mSelectedCafeOverlay);
			mMapView.refresh();
		}

		searchNearby = (Button) findViewById(R.id.searchNearby);
		searchNearby.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (searchNearby.getText().toString()
						.equals(getString(R.string.backToMacau))) {
					mMapController.setCenter(new GeoPoint((int)(LAT_DEFAULT * 1e6), (int)(LONG_DEFAULT * 1e6)));
					mMapController.setZoom(16.5f);
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
			
			if (mOverlay != null) {
				mOverlay.removeAll();
			}
			mOverlay = new MyOverlay(getResources().getDrawable(R.drawable.favorite_heart_pin), mMapView);
			
			for (String str : MFConfig.getInstance().getFavoriteLists()) {
				Cafe cafe = MFConfig.getInstance().getCafeLists().get(Integer.parseInt(str) - 1);
				GeoPoint p = getGeoPointFromString(cafe.getCoordx(), cafe.getCoordy());
				CafeOverlayItem item = new CafeOverlayItem(p, cafe.getName() ,MFUtil.getDishesStringFromCafe(cafe), cafe.getId());
				mOverlay.addItem(item);
//				mMarkersHashMap.put(marker, cafe.getId());
			}
			mMapView.getOverlays().add(mOverlay);
			mMapView.refresh();
		} else if (getIntent().getBooleanExtra("fromBranch", false)) {
			searchNearby.setVisibility(View.GONE);
			mapFilterPanel.setVisibility(View.GONE);
			
			if (mOverlay != null) {
				mOverlay.removeAll();
			}
			mOverlay = new MyOverlay(getResources().getDrawable(R.drawable.green_pin), mMapView);
			
			ArrayList<Cafe> branchList = (ArrayList<Cafe>) getIntent().getSerializableExtra("branchList");
			for (Cafe cafe : branchList) {
				GeoPoint p = getGeoPointFromString(cafe.getCoordx(), cafe.getCoordy());
				CafeOverlayItem item = new CafeOverlayItem(p, cafe.getName() ,MFUtil.getDishesStringFromCafe(cafe), cafe.getId());
				mOverlay.addItem(item);
			}
			
			mMapView.getOverlays().add(mOverlay);
			mMapView.refresh();
		}
		
//		if (needPopulateMarkers) {
//			needPopulateMarkers = false;
//			populateOverlayFromSearchList();
//		}
		navigateIsland.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if (navigateIsland.getText().toString().equals(getString(R.string.macauPeninsula))) {
					mMapController.setCenter(new GeoPoint((int)(LAT_DEFAULT * 1e6), (int)(LONG_DEFAULT * 1e6)));
					mMapController.setZoom(16.5f);
				} else {
					mMapController.setCenter(new GeoPoint((int)(LAT_DEFAULT_ISLAND * 1e6), (int)(LONG_DEFAULT_ISLAND * 1e6)));
					mMapController.setZoom(16f);
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

		
		if (pop != null){
            pop.hidePop();
		}
		if (mOverlay != null) {
			mOverlay.removeAll();
		}
		mOverlay = new MyOverlay(getResources().getDrawable(R.drawable.blue_pin), mMapView);
		
//		mMarkersHashMap.clear();
		
		GeoPoint geoPoint = mMapView.getMapCenter();
    	int centerLat = geoPoint.getLatitudeE6();
    	int centerLong = geoPoint.getLongitudeE6();
		int longSpan = mMapView.getLongitudeSpan();
		int latSpan = mMapView.getLatitudeSpan();
    	
    	int nwLatitude = centerLat + latSpan / 2;
    	int seLatitude = centerLat - latSpan / 2;
    	int nwLongitutde = centerLong - longSpan / 2;
    	int seLongitutde = centerLong + longSpan / 2;
		

		int displayNumber = 50;
		PriorityQueue<Cafe> queue = new PriorityQueue<Cafe>(displayNumber + 1);
		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0") || cafe.getId().equals(selectedCafeId)) continue;
			GeoPoint p = getGeoPointFromString(cafe.getCoordx(), cafe.getCoordy());
			if (p.getLatitudeE6() < nwLatitude && p.getLatitudeE6() > seLatitude 
				&& p.getLongitudeE6() < seLongitutde && p.getLongitudeE6() > nwLongitutde) {
				double dist = Math.hypot(centerLat - p.getLatitudeE6(), centerLong - p.getLongitudeE6());
				cafe.setDistance(dist);
				if (queue.size() < displayNumber) {
					queue.add(cafe);
				} else if (dist < queue.peek().getDistance()) {
					queue.poll();
					queue.add(cafe);
				}
			}
		}
		ArrayList<Cafe> priorityList = new ArrayList<Cafe>(); 
		while (!queue.isEmpty()) {
			Cafe cafe = queue.poll();
			if (cafe.getPriority().equals("0")) {
				searchResultCafes.add(cafe);
			} else {
				priorityList.add(cafe);
			}
			
			GeoPoint p = getGeoPointFromString(cafe.getCoordx(), cafe.getCoordy());
			CafeOverlayItem item = new CafeOverlayItem(p, cafe.getName() ,MFUtil.getDishesStringFromCafe(cafe), cafe.getId());
			mOverlay.addItem(item);
		}
		mMapView.getOverlays().add(mOverlay);
		mMapView.refresh();
		if (mMapView.getZoomLevel() < 18) {
			MKMapStatus status = mMapView.getMapStatus();
			status.zoom = 18f;
			mMapController.setMapStatusWithAnimation(status);
		}
		
		
		Collections.sort(priorityList, new Comparator<Cafe>() {
			public int compare(Cafe cafe1, Cafe cafe2) {
				
				return Integer.parseInt(cafe2.getPriority()) - Integer.parseInt(cafe1.getPriority());
			};
			
		});
		
		Collections.reverse(searchResultCafes);
		searchResultCafes.addAll(0, priorityList);
		

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
	
	private int getLatFromString(String coord) {
		return (int)((Double.parseDouble(coord) + LAT_DIFF) * 1E6);
	}
	
	private int getLngFromString(String coord) {
		return (int)((Double.parseDouble(coord) + LONG_DIFF) * 1E6);
	}
	
	private GeoPoint getGeoPointFromString(String coordx, String coordy) {
		GeoPoint p = new GeoPoint(getLatFromString(coordx), getLngFromString(coordy));
		return p;
	}

	private void populateOverlayFromSearchList() {
		
		setUpMapIfNeeded(); //fix a crash report
		
		if (pop != null){
            pop.hidePop();
		}
		if (mOverlay != null) {
			mOverlay.removeAll();
		}
		mOverlay = new MyOverlay(getResources().getDrawable(R.drawable.green_pin), mMapView);
		

		for (Cafe cafe : searchResultCafes) {
			if (cafe.getId().equals(selectedCafeId)) continue;
			
			GeoPoint p = getGeoPointFromString(cafe.getCoordx(), cafe.getCoordy());
			CafeOverlayItem item = new CafeOverlayItem(p, cafe.getName() ,MFUtil.getDishesStringFromCafe(cafe), cafe.getId());
			mOverlay.addItem(item);
		}
		
		mMapView.getOverlays().add(mOverlay);
		mMapView.refresh();
		
		if (regionSpinner.getSelectedItemPosition() == 0) return;
		
		GeoPoint p = LatLngBoundHelper.regionBounds[regionSpinner.getSelectedItemPosition()];
		if (isFirstPopulateFromSearch) {
			isFirstPopulateFromSearch = false;
			mMapController.setCenter(p);
			if (LatLngBoundHelper.regionBounds.length - regionSpinner.getSelectedItemPosition() <= 2) {
				mMapController.setZoom(16f);
			} else if (LatLngBoundHelper.regionBounds.length - regionSpinner.getSelectedItemPosition() <= 3) {
				mMapController.setZoom(17f);
			} else {
				mMapController.setZoom(18f);
			}
		} else {
			MKMapStatus status = mMapView.getMapStatus();
			status.targetGeo = p;
			if (LatLngBoundHelper.regionBounds.length - regionSpinner.getSelectedItemPosition() <= 2) {
				status.zoom = 16f;
			} else if (LatLngBoundHelper.regionBounds.length - regionSpinner.getSelectedItemPosition() <= 3) {
				status.zoom = 17f;
			} else {
				status.zoom = 18f;
			}
			mMapController.setMapStatusWithAnimation(status);
		}
		
		

		
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
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case SHOW_LIST_MENU_ID:
			if (listLayout.isShown()) {
				mMapShown = true;
				if (!mLocClient.isStarted()) {
					mLocClient.start();
				}
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
				mMapShown = false;
				mLocClient.stop();
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
	
	
	@Override  
	protected void onDestroy(){  
	        mMapView.destroy();  
	        if(mBMapMan!=null){  
//	                mBMapMan.destroy();  
	                mBMapMan=null;  
	        }  
            mLocClient.stop();
	        cafeAdapter.imageLoader.cleanup();
	        super.onDestroy();  
	}  
	
//	@Override
//	protected void onStart() {
//		if (mBMapMan == null) {
//			mBMapMan=new BMapManager(getApplication());  
//			mBMapMan.init(BAIDU_MAP_API_KEY, null); 	
//		}	
//		super.onStart();
//	}
	
	@Override  
	protected void onPause(){  
	        mMapView.onPause();  
	        if(mBMapMan!=null){  
	               mBMapMan.stop();  
	        }  
	        mLocClient.stop();
	        super.onPause();  
	}  
	@Override  
	protected void onResume(){  
	        mMapView.onResume();  
	        if(mBMapMan!=null){  
	                mBMapMan.start();  
	        }  
	        if (!mLocClient.isStarted() && mMapShown) {
				mLocClient.start();
			}
	       super.onResume();  
	}  

}
