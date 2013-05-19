package com.cycon.macaufood.activities;


import java.util.List;
import java.util.PriorityQueue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Process;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.widget.MyItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


/**
 * A list view that demonstrates the use of setEmptyView. This example alos uses
 * a custom layout file that adds some extra buttons to the screen.
 */
public class Map extends MapActivity{

	private static final double LAT_MIN = 22.104;
	private static final double LAT_MAX = 22.21;
	private static final double LONG_MIN = 113.52;
	private static final double LONG_MAX = 113.60;
	private static final double LAT_DEFAULT = 22.194791;
	private static final double LONG_DEFAULT = 113.545122;
	private static final long DISMISS_LOC_TIME= 60000;
	private static final int MIN_DIST_RELOC = 20;
	private ImageButton location;
	private ImageButton showList;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //use same logic as in BaseActivity
		if (MFConfig.getInstance().getCafeLists().size() == 0) {
			Process.killProcess(Process.myPid());
		} 
        setContentView(R.layout.map);
        checkLandscape();
        name = getIntent().getStringExtra("name");
        selectedCafeId = getIntent().getStringExtra("id");
        String coordx = getIntent().getStringExtra("coordx");
        String coordy = getIntent().getStringExtra("coordy");
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

    	// Acquire a reference to the system Location Manager
    	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	
    	// Define a listener that responds to location updates
    	locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	    	
    	      // Called when a new location is found by the network location provider.
    	      makeUseOfNewLocation(location);
    	      curLocationTimeStamp = System.currentTimeMillis();
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

    	    public void onProviderEnabled(String provider) {}

    	    public void onProviderDisabled(String provider) {}
    	  };
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        GeoPoint point = null;
        
        if (coordx != null && coordy != null) {
            Drawable redMarker = this.getResources().getDrawable(R.drawable.red_marker);
            redMarker.setBounds(-redMarker.getIntrinsicWidth() / 2 + 14, -redMarker.getIntrinsicHeight(), redMarker.getIntrinsicWidth() / 2 + 14, 0);
        	selectRestOverlay = new MyItemizedOverlay(null, redMarker, mapView, false, true);
	        point = new GeoPoint((int)(Double.parseDouble(coordx)*1E6),(int)(Double.parseDouble(coordy)*1E6));
	        OverlayItem overlayitem = new OverlayItem(point, name, null);
	        selectRestOverlay.addOverlay(overlayitem, null);
	        mapOverlays.add(selectRestOverlay);
	        selectRestOverlay.callPopulate();
        } else {
	        point = new GeoPoint((int)(LAT_DEFAULT*1E6),(int)(LONG_DEFAULT*1E6));
        }

        MapController mapController = mapView.getController();
        if (name != null) {
        	mapController.setZoom(18);
            mapController.animateTo(point);
        } else {
        	mapController.setZoom(16);
        	mapController.setCenter(point);
        }

        
        location = (ImageButton) findViewById(R.id.location);
        location.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (loc != null) makeUseOfNewLocation(loc);
				// Register the listener with the Location Manager to receive location updates
		    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DIST_RELOC, locationListener);
		    	
			}
		});
        
        showList = (ImageButton) findViewById(R.id.showList);
        showList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Map.this, NearbyList.class);
				startActivity(i);
			}
		});
        
        searchNearby = (Button) findViewById(R.id.searchNearby);
        searchNearby.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (searchNearby.getText().toString().equals(getString(R.string.backToMacau))) {
					GeoPoint point = new GeoPoint((int)(LAT_DEFAULT*1E6),(int)(LONG_DEFAULT*1E6));
			        MapController mapController = mapView.getController();
			        mapController.setCenter(point);
		        	mapController.setZoom(16);
		        	searchNearby.setText(R.string.searchNearby);
				} else {
					MFConfig.getInstance().getNearbyLists().clear();
					searchNearby();
					if (MFConfig.getInstance().getNearbyLists().size() != 0 )
						showList.setVisibility(View.VISIBLE);
					else
						showList.setVisibility(View.GONE);
				}
			}
		});
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
    	
    	
    	PriorityQueue<Cafe> queue = new  PriorityQueue<Cafe>(); 
    	List<Overlay> mapOverlays = mapView.getOverlays();
    	if (itemizedoverlay == null) {
	        Drawable blueMarker = this.getResources().getDrawable(R.drawable.blue_marker);
	        blueMarker.setBounds(-blueMarker.getIntrinsicWidth() / 2 + 14, -blueMarker.getIntrinsicHeight(), blueMarker.getIntrinsicWidth() / 2 + 14, 0);
        	
    	itemizedoverlay = new MyItemizedOverlay(this, blueMarker, mapView, true, true);
		mapOverlays.add(itemizedoverlay);
    	}
    	else 
        	itemizedoverlay.clearOverlayData();
    		
    	for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
    		int coordx = 0;
    		int coordy = 0;
    		try {
				coordx = (int) (Double.parseDouble(cafe.getCoordx())*1E6);
				coordy = (int) (Double.parseDouble(cafe.getCoordy())*1E6);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    		if (coordx < nwLatitude && coordx > seLatitude 
    				&& coordy < seLongitutde && coordy > nwLongitutde) {
    			cafe.setDistance(Math.hypot(centerLatitude - coordx, centerLongitutde - coordy));
    			queue.add(cafe);
    		}
    	}
    	for (int i = 0; i < 50 && queue.size() > 0; i++) {
    		Cafe cafe = queue.poll();
			if (cafe.getStatus().equals("0")) continue;
    		MFConfig.getInstance().getNearbyLists().add(cafe);
    		if (cafe.getId().equals(selectedCafeId)) continue;
			GeoPoint point = new GeoPoint((int) (Double.parseDouble(cafe.getCoordx())*1E6), (int) (Double.parseDouble(cafe.getCoordy())*1E6));
	        OverlayItem overlayitem = new OverlayItem(point, cafe.getName(), 
	        		cafe.getPhone().trim().length() == 0 ? null : cafe.getPhone());
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
    	if (location == null) return;
    	List<Overlay> mapOverlays = mapView.getOverlays();
    	
    	if (curLocationOverlay == null) {
	        Drawable drawable = this.getResources().getDrawable(R.drawable.cur_location);
	        drawable.setBounds(-drawable.getIntrinsicWidth() / 2 + 20, -drawable.getIntrinsicHeight(), drawable.getIntrinsicWidth() / 2 + 20, 0);
	        curLocationOverlay = new MyItemizedOverlay(null, drawable, mapView, false, true);
	        mapOverlays.add(curLocationOverlay);
    	} else {
    		curLocationOverlay.clearOverlayData();
    	}
        GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
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
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	checkLandscape();
    }
    
	  private void checkLandscape() {
		if (MFConfig.getInstance().isLandscape(this)) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
	  
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
		zoomlevel = mapView.getZoomLevel();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		//if longer than a period time, dismiss the cur Location marker
		if (curLocationTimeStamp != 0 && System.currentTimeMillis() - curLocationTimeStamp > DISMISS_LOC_TIME) {
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
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && name == null) {	   
	    	new AlertDialog.Builder(this)
			.setMessage("你確定�?退出程�?嗎?      ")
			.setPositiveButton("確定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
					    	Process.killProcess(Process.myPid());   
						}
					})
			.setNegativeButton("�?�消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
							dialog.dismiss();
						}
					})
			.show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {    	
    	menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.foodNews)).setIcon(R.drawable.ic_menu_dishes);
    	menu.add(Menu.NONE, 3, Menu.NONE, "最新情報").setIcon(R.drawable.rss);
    	menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.aboutUs)).setIcon(R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case 2:
			Intent i = new Intent(this, FoodNews.class);
			startActivity(i);
			break;
		case 3:
			Intent i2 = new Intent(this, Latest.class);
			startActivity(i2);
			break;
		case 4:
			Intent i3 = new Intent(this, About.class);
			startActivity(i3);
			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
}
