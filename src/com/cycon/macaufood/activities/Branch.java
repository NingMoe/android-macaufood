package com.cycon.macaufood.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.widget.AdvView;

public class Branch extends BaseActivity {

	private static final String TAG = Branch.class.getName();
	private ListView list;
	private AdvView banner;
	private TextView searchResultsNumber;
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
        
        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(itemClickListener);
        banner = (AdvView) findViewById(R.id.banner);
        searchResultsNumber = (TextView) findViewById(R.id.searchResultsNumber);
        searchResultsNumber.setText("共 " + branchList.size() + " 間分店");
        
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
    	super.onDestroy();
    }
}
