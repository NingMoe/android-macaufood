/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.cycon.macaufood.widget;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.cycon.macaufood.activities.Details;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class MyItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();
	private ArrayList<String> idList = new ArrayList<String>();
	private Activity activity;
	private boolean showArrow;

	public MyItemizedOverlay(Activity activity, Drawable defaultMarker, MapView mapView, boolean showArrow, boolean tappable) {
		super((defaultMarker), mapView, showArrow, tappable);
		setBalloonBottomOffset(defaultMarker.getIntrinsicHeight());
		setBalloonSideOffset(32);
		this.activity = activity;
		this.showArrow = showArrow;
	}

	public void addOverlay(OverlayItem overlay, String id) {
	    m_overlays.add(overlay);
	    idList.add(id);
	    setLastFocusedIndex(-1);
//	    populate();
	}
	
	public void callPopulate() {
		populate();
	}
	
	public void clearOverlayData() {
	    m_overlays.clear();
	    idList.clear();
	    setLastFocusedIndex(-1);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		if (!showArrow) return false;
		Intent i = new Intent(activity, Details.class);
		i.putExtra("id", idList.get(index));
		i.putExtra("fromMap", true);
		activity.startActivity(i);
		return true;
	}
	
	

}