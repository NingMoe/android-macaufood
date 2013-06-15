package com.cycon.macaufood.activities;

import java.util.ArrayList;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.widget.AdvFlingGallery;
import com.cycon.macaufood.widget.AdvView;
import com.cycon.macaufood.widget.GalleryNavigator;

public class Search extends BaseActivity {
	
	private static final String TAG = "Search";

	private TabsAdapter mTabsAdapter;
	private View directSearchLayout;
	private View advancedSearchLayout;
	private View searchResultsLayout;
	private View advLoadBg;
	private TextView searchResultsNumber;
//	private AdvView banner;
	private GalleryNavigator navi;
	private AdvFlingGallery gallery;
	private AdvView smallBanner;
	private EditText searchTextBox;
	private Button clearBtn;
	private Button directSearchBtn;
	private Button searchBtn;
	private ListView searchList;
	private ListView searchResultsList;
	private TextView regionTitle;
	private WheelView region;
	private WheelView dishesType;
	private WheelView servicesType;
	private ArrayWheelAdapter<String> regionAdapter;
	private ArrayWheelAdapter<String> dishesTypeAdapter;
	private ArrayWheelAdapter<String> servicesTypeAdapter;
	
	private ArrayList<Cafe> searchCafes = new ArrayList<Cafe>();
	private SearchAdapter searchAdapter;
	private ArrayList<Cafe> searchResultCafes = new ArrayList<Cafe>();
	private CafeSearchListAdapter searchResultsAdapter;
	
	
	private int fromWhichSearch; //1 = direct search, 2 = advanced search 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.e(getClass().getSimpleName(), "---onCreate");

        setContentView(R.layout.search);

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab tab1 = mActionBar.newTab().setText(R.string.directSearch);
        Tab tab2 = mActionBar.newTab().setText(R.string.advancedSearch);
        Tab tab3 = mActionBar.newTab().setText(R.string.searchResults);
        tab1.setTabListener(new TabsAdapter());
        tab2.setTabListener(new TabsAdapter());
        tab3.setTabListener(new TabsAdapter());

		if (savedInstanceState != null) {
			mActionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
//        banner = (AdvView) findViewById(R.id.banner);
        smallBanner = (AdvView) findViewById(R.id.smallBanner);
        advLoadBg = findViewById(R.id.advLoadBg);
        directSearchLayout = findViewById(R.id.directSearchLayout);
        advancedSearchLayout = findViewById(R.id.advancedSearchLayout);
        searchResultsLayout = findViewById(R.id.searchResultsLayout);
        searchResultsNumber = (TextView) findViewById(R.id.searchResultsNumber);
        searchList = (ListView) findViewById(R.id.searchList);
        searchAdapter = new SearchAdapter(searchCafes);
        searchList.setAdapter(searchAdapter);
        searchList.setOnItemClickListener(itemClickListener);
        searchResultsList = (ListView) findViewById(R.id.searchResultsList);
        searchResultsAdapter = new CafeSearchListAdapter(this, searchResultCafes);
        searchResultsList.setAdapter(searchResultsAdapter);
        searchResultsList.setOnItemClickListener(resultItemClickListener);
        
		navi = (GalleryNavigator) findViewById(R.id.navi);
		gallery = (AdvFlingGallery) findViewById(R.id.gallery);
		gallery.setNavi(navi);
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1, int id,
					long arg3) {
					
				navi.setPosition(id);
				navi.invalidate();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
        
        advancedSearchLayout.getBackground().setDither(true);
        
        regionTitle = (TextView) findViewById(R.id.regionTitle);
        
        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAdvancedSearch();
				fromWhichSearch = 2;
			}
		});
        
        directSearchBtn = (Button) findViewById(R.id.directSearchBtn);
        directSearchBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				hideKeyboard(v);
				doPostDirectSearch();
				fromWhichSearch = 1;
			}
		});
        
        clearBtn = (Button) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				searchTextBox.setText("");
				clearBtn.setVisibility(View.GONE);
				directSearchBtn.setVisibility(View.GONE);
				searchCafes.clear();
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
//					clearBtn.setVisibility(View.VISIBLE);
			}
		});
        
        searchTextBox.setOnEditorActionListener(new OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboard(v);
					doPostDirectSearch();
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
					directSearchBtn.setVisibility(View.GONE);
					searchList.setVisibility(View.GONE);
					clearBtn.setVisibility(View.GONE);
					searchCafes.clear();
					gallery.setVisibility(View.VISIBLE);
//					banner.startTask();
				} else {
					directSearchBtn.setVisibility(View.VISIBLE);
					clearBtn.setVisibility(View.VISIBLE);
					searchList.setVisibility(View.VISIBLE);
					advLoadBg.setBackgroundColor(Color.BLACK);
					doDirectSearch(s.toString());
					gallery.setVisibility(View.GONE);
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
        mActionBar.addTab(tab3);
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
    
    private void setSearchResultsTab(boolean select) {
    	if (select) {
    		searchResultsLayout.setVisibility(View.VISIBLE);
			smallBanner.startTask();
    	} else {
    		searchResultsLayout.setVisibility(View.GONE);
    		smallBanner.stopTask();
    	}
    }
    
    private void doPostDirectSearch() {
    	
		searchResultCafes.clear();
		searchResultCafes.addAll(searchCafes);
		searchResultsAdapter.imageLoader.setImagesToLoadFromCafe(searchResultCafes);
		searchResultsAdapter.notifyDataSetChanged();
		setDirectSearchTab(false);
		setSearchResultsTab(true);
		searchResultsNumber.setText("共 " + searchResultCafes.size() + " 項�?果");
		searchResultsList.setSelectionAfterHeaderView();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

    	gallery.startTimer();

		if (directSearchLayout.isShown()) {
			if (searchTextBox.getText().toString().trim().length() == 0) {
//				banner.startTask();
			}
    	} else if (searchResultsLayout.isShown()) {
    		smallBanner.startTask();
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	searchResultsAdapter.imageLoader.cleanup();
//		banner.stopTask();
    	gallery.stopTimer();
		smallBanner.stopTask();
    }
    
    
    public void hideKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    public void showKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.showSoftInput(view, 0);
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
		searchResultCafes.clear();
		
		ArrayList<Cafe> priorityList = new ArrayList<Cafe>(); 
		
		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0")) continue;
			boolean matchDistrict;
			boolean matchDishes;
			boolean matchServices;
			int regionIndex = region.getCurrentItem();
			int dishesIndex = dishesType.getCurrentItem();
			int dishesId = MFConstants.dishesId[dishesIndex];
			int servicesIndex = servicesType.getCurrentItem();
			
			
			if (regionIndex == 0 || regionIndex == Integer.parseInt(cafe.getDistrict())) {
				matchDistrict = true;
			} else {
				matchDistrict = false;
			}
			
			if (dishesId == 0 || dishesId == Integer.parseInt(cafe.getType0())
					|| dishesId == Integer.parseInt(cafe.getType1()) 
					|| dishesId == Integer.parseInt(cafe.getType2())) {
				matchDishes = true;
			} else {
				matchDishes = false;
			}
			
			switch(servicesIndex) {
				case 0:
					matchServices = true;
					break;
				case 1:
					if (cafe.getOption_phoneorder().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 2:
					if (cafe.getOption_booking().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 3:
					if (cafe.getOption_night().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 4:
					if (cafe.getOption_call().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 5:
					if (cafe.getOption_buffet().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 6:
					if (cafe.getOption_banquet().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 7:
					if (cafe.getOption_wifi().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 8:
					if (cafe.getOption_parking().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				default:
					matchServices = false;
			}
			
			if (matchDishes && matchDistrict && matchServices) {
				if (cafe.getPriority().equals("0")) {
					searchResultCafes.add(cafe);
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
			
			
			
		}
		searchResultCafes.addAll(0, priorityList);
		searchResultsAdapter.imageLoader.setImagesToLoadFromCafe(searchResultCafes);
		searchResultsAdapter.notifyDataSetChanged();
		setAdvancedSearchTab(false);
		setSearchResultsTab(true);
		searchResultsNumber.setText("共 " + searchResultCafes.size() + " 項�?果");
		searchResultsList.setSelectionAfterHeaderView();
	}
	
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(Search.this, Details.class);
			i.putExtra("id", searchCafes.get(position).getId());
			startActivity(i);
    	};
    };
	
    AdapterView.OnItemClickListener resultItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(Search.this, Details.class);
			i.putExtra("id", searchResultCafes.get(position).getId());
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
                text.setTextColor(Color.BLACK);
                text.setTextSize(20f);
                text.setPadding(10, 3, 7, 3);
                text.setShadowLayer(1, 0, 1, Color.parseColor("#999999"));
            } else {
                text = (TextView)convertView;
            }
            
            text.setText(cafe.getName());

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
			else if (tab.getPosition() == 2)
				setSearchResultsTab(true);
			
		}

		public void onTabUnselected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {

			if (tab.getPosition() == 0)
				setDirectSearchTab(false);
			else if (tab.getPosition() == 1) 
				setAdvancedSearchTab(false);
			else if (tab.getPosition() == 2)
				setSearchResultsTab(false);

	    	searchResultsAdapter.imageLoader.cleanup();
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
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {	 
	    	if (searchResultsLayout.isShown() && fromWhichSearch != 0) {
				if (fromWhichSearch == 1) {
					setDirectSearchTab(true);
					setSearchResultsTab(false);
			    	searchResultsAdapter.imageLoader.cleanup();
				} else {
					setAdvancedSearchTab(true);
					setSearchResultsTab(false);
			    	searchResultsAdapter.imageLoader.cleanup();
				}
				return true;
			} else {
				finish();
			}
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
//	@Override
//	public void onAttachedToWindow() {
//	    super.onAttachedToWindow();
//	    Window window = getWindow();
//	    window.setFormat(PixelFormat.RGBA_8888);
//	}
    
    
}
