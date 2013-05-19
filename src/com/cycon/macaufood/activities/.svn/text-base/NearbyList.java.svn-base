package com.cycon.macaufood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.utilities.Config;
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
        searchResultsNumber.setText("共 " + Config.getInstance().getNearbyLists().size() + " 項結果(從近到遠排列)");
        
        cafeAdapter = new CafeSearchListAdapter(NearbyList.this, Config.getInstance().getNearbyLists());
        list.setAdapter(cafeAdapter);
	}
	
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(NearbyList.this, Details.class);
			i.putExtra("id", Config.getInstance().getNearbyLists().get(position).getId());
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
