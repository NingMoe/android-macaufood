package com.cycon.macaufood.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.widget.AdvView;

public class NearbyList extends BaseActivity {

	private static final String TAG = NearbyList.class.getName();
	private ListView list;
	private AdvView banner;
	private TextView searchResultsNumber;
	private CafeSearchListAdapter cafeAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.nearbylist);

        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(itemClickListener);
        banner = (AdvView) findViewById(R.id.banner);
        searchResultsNumber = (TextView) findViewById(R.id.searchResultsNumber);
        searchResultsNumber.setText("共 " + MFConfig.getInstance().getNearbyLists().size() + " 項�?果(從近到�?�排列)");
        
        cafeAdapter = new CafeSearchListAdapter(NearbyList.this, MFConfig.getInstance().getNearbyLists());
        list.setAdapter(cafeAdapter);
	}
	
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(NearbyList.this, Details.class);
			i.putExtra("id", MFConfig.getInstance().getNearbyLists().get(position).getId());
			startActivity(i);
    	};
    };
    
    protected void onResume() {
    	super.onResume();
		banner.startTask();
    };
    
    @Override
    protected void onPause() {
    	super.onPause();
		banner.stopTask();
    }
    
    @Override
    protected void onDestroy() {
    	banner.unbind();
    	list.setAdapter(null);
    	super.onDestroy();
    }
    
}
