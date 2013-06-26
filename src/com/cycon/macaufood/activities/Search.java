package com.cycon.macaufood.activities;

import java.util.ArrayList;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AdvancedSearchHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.widget.AdvViewPager;
import com.cycon.macaufood.widget.DirectSearchLayout;
import com.cycon.macaufood.widget.GalleryNavigator;

public class Search extends BaseActivity {
	
	private static final String TAG = "Search";

	private DirectSearchLayout directSearchLayout;
	private View advancedSearchLayout;
	private GalleryNavigator navi;
	private AdvViewPager advViewPager;
	private EditText searchTextBox;
	private View clearBtn;
	private Button searchBtn;
	private ListView searchList;
	private TextView regionTitle;
	private WheelView region;
	private WheelView dishesType;
	private WheelView servicesType;
	private ArrayWheelAdapter<String> regionAdapter;
	private ArrayWheelAdapter<String> dishesTypeAdapter;
	private ArrayWheelAdapter<String> servicesTypeAdapter;
	
	private ArrayList<Cafe> searchCafes = new ArrayList<Cafe>();
	private SearchAdapter searchAdapter;
	
	
	private int fromWhichSearch; //1 = direct search, 2 = advanced search 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.e(getClass().getSimpleName(), "---onCreate");

        setContentView(R.layout.search);

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab tab1 = mActionBar.newTab().setText(R.string.directSearch);
        Tab tab2 = mActionBar.newTab().setText(R.string.advancedSearch);
        tab1.setTabListener(new TabsAdapter());
        tab2.setTabListener(new TabsAdapter());

		if (savedInstanceState != null) {
			mActionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
        directSearchLayout = (DirectSearchLayout) findViewById(R.id.directSearchLayout);
        directSearchLayout.setActivity(this);
        advancedSearchLayout = findViewById(R.id.advancedSearchLayout);
        searchList = (ListView) findViewById(R.id.searchList);
        searchAdapter = new SearchAdapter(searchCafes);
        searchList.setAdapter(searchAdapter);
        searchList.setOnItemClickListener(itemClickListener);
        searchList.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					hideKeyboard();
				}
				return false;
			}
		});
        
		navi = (GalleryNavigator) findViewById(R.id.navi);
		advViewPager = (AdvViewPager) findViewById(R.id.gallery);
		advViewPager.setNavi(navi);
		advViewPager.setLoadingLayout(findViewById(R.id.loadingProgressLayout));
        
        regionTitle = (TextView) findViewById(R.id.regionTitle);
        
        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAdvancedSearch();
				fromWhichSearch = 2;
			}
		});
        
        clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				searchTextBox.setText("");
				clearBtn.setVisibility(View.GONE);
				searchCafes.clear();
				shrink();
				showKeyboard(searchTextBox);
			}
		});
        
        searchTextBox = (EditText) findViewById(R.id.searchTextBox);
        
//        searchTextBox.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (!hasFocus) {
//					expand();
//					hideKeyboard(v);
//				} 
//			}
//		});
        
        searchTextBox.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
					shrink();
//					clearBtn.setVisibility(View.VISIBLE);
			}
		});
        
        searchTextBox.setOnEditorActionListener(new OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboard(v);
					doPostDirectSearch();
//					expand();
					fromWhichSearch = 1;
					return true;
				}
				return false;
			}
		});
        
        searchTextBox.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						hideKeyboard(v);
						doPostDirectSearch();
//						expand();
						fromWhichSearch = 1;
						return true;
					}
				}
				return false;
			}
		});
        
        searchTextBox.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().trim().length() == 0) {
					searchList.setVisibility(View.GONE);
					clearBtn.setVisibility(View.GONE);
					searchCafes.clear();
					advViewPager.setVisibility(View.VISIBLE);
					navi.setVisibility(View.VISIBLE);
//					banner.startTask();
				} else {
					clearBtn.setVisibility(View.VISIBLE);
					searchList.setVisibility(View.VISIBLE);
					doDirectSearch(s.toString());
					navi.setVisibility(View.GONE);
					advViewPager.setVisibility(View.GONE);
//					banner.stopTask();
				}
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			public void afterTextChanged(Editable s) {
				searchList.setSelectionAfterHeaderView();
			}
		});
        
        
        region = (WheelView) findViewById(R.id.region);
        regionAdapter = new ArrayWheelAdapter<String>(this, MFConstants.regionNames);
        regionAdapter.setItemResource(R.layout.wheel_text_item);
        regionAdapter.setItemTextResource(R.id.text);
        region.setViewAdapter(regionAdapter);
        region.addChangingListener(new OnWheelChangedListener() {
			
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				regionTitle.setText(MFConstants.regionNames[newValue]);
			}
		});
    
        dishesType = (WheelView) findViewById(R.id.foodType);
        dishesTypeAdapter = new ArrayWheelAdapter<String>(this, MFConstants.dishesType);
        dishesTypeAdapter.setItemResource(R.layout.wheel_text_item);
        dishesTypeAdapter.setItemTextResource(R.id.text);
        dishesType.setViewAdapter(dishesTypeAdapter);
        
        servicesType = (WheelView) findViewById(R.id.restType);
        servicesTypeAdapter = new ArrayWheelAdapter<String>(this, MFConstants.serviceType);
        servicesTypeAdapter.setItemResource(R.layout.wheel_text_item);
        servicesTypeAdapter.setItemTextResource(R.id.text);
        servicesType.setViewAdapter(servicesTypeAdapter);
        

        mActionBar.addTab(tab1);
        mActionBar.addTab(tab2);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getSupportActionBar()
				.getSelectedNavigationIndex());
	}
    
    private void setDirectSearchTab(boolean select) {
    	if (select) {
    		directSearchLayout.setVisibility(View.VISIBLE);
//    		if (banner.isShown())
//    			banner.startTask();
    	} else {
    		directSearchLayout.setVisibility(View.GONE);
//    		banner.stopTask();
    	}
    }
    
    private void setAdvancedSearchTab(boolean select) {
    	if (select) {
    		advancedSearchLayout.setVisibility(View.VISIBLE);
    	} else {
    		advancedSearchLayout.setVisibility(View.GONE);
    	}
    }
    
    
    private void doPostDirectSearch() {
    	
    	if (searchCafes.size() > 0) {
	    	MFConfig.getInstance().getSearchResultList().clear();
	    	MFConfig.getInstance().getSearchResultList().addAll(searchCafes);
	    	Intent i = new Intent(this, Map.class);
	    	startActivity(i);
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	expand();

    	advViewPager.startTimer();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	advViewPager.stopTimer();
    }
    
    public void hideKeyboard() {
    	InputMethodManager imm = (InputMethodManager) getSystemService(
    		    INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    
    public void hideKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    public void showKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.showSoftInput(view, 0);
    }
    
    public void shrink() {
    	mActionBar.hide();
    }
    
    public void expand() {
    	mActionBar.show();
    	if (searchTextBox.getText().toString().equals("")) {
    		clearBtn.setVisibility(View.GONE);
    	}
    }
    
	private void doDirectSearch(String query) {
		
		query = query.toLowerCase().trim();
		searchCafes.clear();
		
		ArrayList<Cafe> priorityList = new ArrayList<Cafe>(); 
		ArrayList<Cafe> nonPrefixList = new ArrayList<Cafe>(); 
		
		for (Cafe cafe: MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0")) continue;
			String name = cafe.getName().toLowerCase();
			if (name.contains(query)) {
				if (name.startsWith(query)) {
					
					if (cafe.getPriority().equals("0")) {
						searchCafes.add(cafe);
					} else {
						int priority = Integer.parseInt(cafe.getPriority());
						if (priorityList.size() == 0) {
							priorityList.add(cafe);
						} else {
							boolean added = false;
							for (int i = 0; i < priorityList.size(); i++) {
								if (Integer.parseInt(priorityList.get(i).getPriority())
										< priority) {
									priorityList.add(i, cafe);
									added = true;
									break;
								}
							}
							if (!added) {
								priorityList.add(cafe);
							}
						}
					}
					
				}
				else
					nonPrefixList.add(cafe);
			}
		}
		searchCafes.addAll(0, priorityList);
		searchCafes.addAll(nonPrefixList);
		searchAdapter.notifyDataSetChanged();
	}
	
	private void doAdvancedSearch() {
		
		int regionIndex = region.getCurrentItem();
		int dishesIndex = dishesType.getCurrentItem();
		int dishesId = MFConstants.dishesId[dishesIndex];
		int servicesIndex = servicesType.getCurrentItem();
		
		if (regionIndex == 0 && dishesIndex == 0 && servicesIndex == 0) {
			Toast.makeText(this, R.string.selectOneItemPrompt, Toast.LENGTH_SHORT).show();
			return;
		}
		
		AdvancedSearchHelper.search(regionIndex, dishesId, servicesIndex);
		
    	if (MFConfig.getInstance().getSearchResultList().size() > 0) {
	    	Intent i = new Intent(this, Map.class);
	    	i.putExtra("regionIndex", regionIndex);
	    	i.putExtra("dishesIndex", dishesIndex);
	    	i.putExtra("servicesIndex", servicesIndex);
	    	startActivity(i);
    	} else {
    		new AlertDialog.Builder(this)
    			.setMessage(R.string.noSearchResults)
    								.setPositiveButton(getString(R.string.confirmed),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							}).show();
    	}
	}
	
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(Search.this, Details.class);
			i.putExtra("id", searchCafes.get(position).getId());
			startActivity(i);
    	};
    };
    
	class SearchAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private final ArrayList<Cafe> cafes;

        public SearchAdapter(ArrayList<Cafe> cafes) {
            this.cafes = cafes;
        }

        public int getCount() {
            return cafes.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	Cafe cafe = cafes.get(position);
        	
            TextView text;
            
            if (convertView == null) {
                text = new TextView(Search.this);
                text.setTextColor(getResources().getColor(R.color.dark_gray_text));
                text.setTextSize(19f);
                int leftPadding = MFUtil.getPixelsFromDip(16, getResources());
                int topPadding = MFUtil.getPixelsFromDip(5, getResources());
                text.setPadding(leftPadding, topPadding, leftPadding, topPadding);
            } else {
                text = (TextView)convertView;
            }
            
            String keyString = searchTextBox.getText().toString().toLowerCase().trim();
            String cafeName = cafe.getName().toLowerCase().trim();
            int index = cafeName.indexOf(keyString);
            SpannableString spannable = new SpannableString(cafe.getName());
            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_text)), index, index + keyString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(spannable);

            return text;
        }


        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent i = new Intent(Search.this, Details.class);
			i.putExtra("id", cafes.get(position).getId());
			startActivity(i);
        }
    }
	
	private class TabsAdapter implements ActionBar.TabListener {

		public void onTabSelected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {
			if (tab.getPosition() == 0)
				setDirectSearchTab(true);
			else if (tab.getPosition() == 1) 
				setAdvancedSearchTab(true);
			
		}

		public void onTabUnselected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {

			if (tab.getPosition() == 0)
				setDirectSearchTab(false);
			else if (tab.getPosition() == 1) 
				setAdvancedSearchTab(false);
		}

		public void onTabReselected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}
		
	}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.e(getClass().getSimpleName(), "---onDestroy");
    }
    
    
}
