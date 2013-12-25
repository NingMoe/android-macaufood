package com.cycon.macaufood.utilities;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.cycon.macaufood.activities.Map;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class LatLngBoundHelper {

	public static GeoPoint[] regionBounds = {
		null,
		new GeoPoint((int)((22.210447 + 22.213278 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.542343 + 113.54523 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.204312 + 22.210466 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.538876 + 113.544817 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.210061 + 22.214801 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.544323 + 113.551166 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.205778 + 22.214608 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.549146 + 113.559096 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.201066 + 22.205494 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.544749 + 113.55013 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.197245 + 22.204754 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.539082 + 113.546917 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.203021 + 22.21422 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.547542 + 113.554031 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.193269 + 22.202991 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.541555 + 113.55377 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.191918 + 22.19874 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.535936 + 113.544302 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.186327 + 22.197771 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.541341 + 113.558032 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.185627 + 22.192392 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.546093 + 113.554666 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.195324 + 22.205132 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.536113 + 113.544093 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.187147 + 22.195988 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.531165 + 113.536636 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.179815 + 22.192782 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.53073 + 113.546702 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.143488 + 22.163729 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.541888 + 113.574032 + Map.LONG_DIFF * 2) * 1E6 / 2)),
		new GeoPoint((int)((22.113117 + 22.132271 + Map.LAT_DIFF * 2) * 1E6 / 2), (int)((113.550747 + 113.577527 + Map.LONG_DIFF * 2) * 1E6 / 2))
	};
	
}
