package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;

public class Branch extends BaseActivity {
	
	private static final int DISPLAY_MAP_MENU_ID = 1;

	private static final String TAG = Branch.class.getName();
	private ListView list;
	private CafeSearchListAdapter cafeAdapter;
	private ArrayList<Cafe> branchList = new ArrayList<Cafe>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.branch);
        
        String branchId = getIntent().getStringExtra("branch");
        
        for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
        	if (cafe.getBranch().equals(branchId)) {
        		branchList.add(cafe);
        	}
        }
        
		Collections.sort(branchList, new Comparator<Cafe>() {
			public int compare(Cafe cafe1, Cafe cafe2) {
				
				return Integer.parseInt(cafe2.getPriority()) - Integer.parseInt(cafe1.getPriority());
			};
			
		});
        
        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(itemClickListener);
        
        cafeAdapter = new CafeSearchListAdapter(Branch.this, branchList);
        list.setAdapter(cafeAdapter);
	}
	
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(Branch.this, Details.class);
			i.putExtra("id", branchList.get(position).getId());
			i.putExtra("fromBranch", true);
			startActivity(i);
    	};
    };
    
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	if (branchList.size() > 1) {
    		menu.add(0, DISPLAY_MAP_MENU_ID, 0, R.string.showMap).setIcon(R.drawable.map).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
    	return super.onCreateOptionsMenu(menu);
    };
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case DISPLAY_MAP_MENU_ID:
			Intent i = new Intent(Branch.this, Map.class);
			i.putExtra("fromBranch", true);
			i.putExtra("branchList", branchList);
			startActivity(i);
			return true;
    	default:
			return super.onOptionsItemSelected(item);
    	}
    	
    }
}
