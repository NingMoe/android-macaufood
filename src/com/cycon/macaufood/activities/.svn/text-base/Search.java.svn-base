package com.cycon.macaufood;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.OnEditorActionListener;

import com.cycon.macaufood.Intro.ImageAdapter;
import com.cycon.macaufood.adapters.CafeSearchListAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;
import com.cycon.macaufood.utilities.Utilities;
import com.cycon.macaufood.widget.AdvFlingGallery;
import com.cycon.macaufood.widget.AdvView;
import com.cycon.macaufood.widget.DirectSearchLayout;
import com.cycon.macaufood.widget.GalleryNavigator;
import com.cycon.macaufood.widget.OneFlingGallery;

public class Search extends BaseActivity {
	
	private static final String TAG = "Search";

	private TextView directSearch;
	private TextView advancedSearch;
	private TextView searchResults;
	private DirectSearchLayout directSearchLayout;
	private View advancedSearchLayout;
	private View searchResultsLayout;
	private View tabLayout;
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
	
	private Home parentActivity;
	
	private int fromWhichSearch; //1 = direct search, 2 = advanced search 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ETLog.e(getClass().getSimpleName(), "---onCreate");

		isTabChild = true;
        setContentView(R.layout.search);
        
//        boolean landscape = Config.getInstance().isLandscape(this);
        boolean landscape = false;
        
		parentActivity = (Home) Search.this.getParent();
		
		tabLayout = findViewById(R.id.tabLayout);
//        banner = (AdvView) findViewById(R.id.banner);
        smallBanner = (AdvView) findViewById(R.id.smallBanner);
        advLoadBg = findViewById(R.id.advLoadBg);
        directSearchLayout = (DirectSearchLayout) findViewById(R.id.directSearchLayout);
        directSearchLayout.setActivity(this);
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
        
        directSearch = (TextView) findViewById(R.id.directSearch);
        directSearch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (!directSearchLayout.isShown()) {
					setDirectSearchTab(true);
					setAdvancedSearchTab(false);
					setSearchResultsTab(false);
			    	searchResultsAdapter.imageLoader.cleanup();
				}
			}
		});
        advancedSearch = (TextView) findViewById(R.id.advancedSearch);
        advancedSearch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (!advancedSearchLayout.isShown()) {
					setDirectSearchTab(false);
					setAdvancedSearchTab(true);
					setSearchResultsTab(false);
			    	searchResultsAdapter.imageLoader.cleanup();
				}
			}
		});
        searchResults = (TextView) findViewById(R.id.searchResults);
        searchResults.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (!searchResultsLayout.isShown()) {
					setDirectSearchTab(false);
					setAdvancedSearchTab(false);
					setSearchResultsTab(true);
				}
			}
		});
        
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
				expand();
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
					expand();
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
						expand();
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
        regionAdapter = new ArrayWheelAdapter<String>(this, Utilities.regionNames);
        regionAdapter.setItemResource(landscape ? R.layout.wheel_text_item_landscape : R.layout.wheel_text_item);
        regionAdapter.setItemTextResource(R.id.text);
        region.setViewAdapter(regionAdapter);
        region.addChangingListener(new OnWheelChangedListener() {
			
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				regionTitle.setText(Utilities.regionNames[newValue]);
			}
		});
    
        dishesType = (WheelView) findViewById(R.id.foodType);
        dishesTypeAdapter = new ArrayWheelAdapter<String>(this, Utilities.dishesType);
        dishesTypeAdapter.setItemResource(landscape ? R.layout.wheel_text_item_landscape : R.layout.wheel_text_item);
        dishesTypeAdapter.setItemTextResource(R.id.text);
        dishesType.setViewAdapter(dishesTypeAdapter);
        
        servicesType = (WheelView) findViewById(R.id.restType);
        servicesTypeAdapter = new ArrayWheelAdapter<String>(this, Utilities.serviceType);
        servicesTypeAdapter.setItemResource(landscape ? R.layout.wheel_text_item_landscape : R.layout.wheel_text_item);
        servicesTypeAdapter.setItemTextResource(R.id.text);
        servicesType.setViewAdapter(servicesTypeAdapter);
    }
    
    private void setDirectSearchTab(boolean select) {
    	if (select) {
    		directSearch.setTextColor(Color.parseColor("#FFFFFF"));
    		directSearch.setBackgroundResource(R.drawable.search_tab_selected);
    		directSearchLayout.setVisibility(View.VISIBLE);
//    		if (banner.isShown())
//    			banner.startTask();
    	} else {
    		directSearch.setTextColor(Color.parseColor("#888888"));
    		directSearch.setBackgroundResource(R.drawable.search_tab_unselected);
    		directSearchLayout.setVisibility(View.GONE);
//    		banner.stopTask();
    	}
    }
    
    private void setAdvancedSearchTab(boolean select) {
    	if (select) {
    		advancedSearch.setTextColor(Color.parseColor("#FFFFFF"));
    		advancedSearch.setBackgroundResource(R.drawable.search_tab_selected);
    		advancedSearchLayout.setVisibility(View.VISIBLE);
    	} else {
    		advancedSearch.setTextColor(Color.parseColor("#888888"));
    		advancedSearch.setBackgroundResource(R.drawable.search_tab_unselected);
    		advancedSearchLayout.setVisibility(View.GONE);
    	}
    }
    
    private void setSearchResultsTab(boolean select) {
    	if (select) {
    		searchResults.setTextColor(Color.parseColor("#FFFFFF"));
    		searchResults.setBackgroundResource(R.drawable.search_tab_selected);
    		searchResultsLayout.setVisibility(View.VISIBLE);
			smallBanner.startTask();
    	} else {
    		searchResults.setTextColor(Color.parseColor("#888888"));
    		searchResults.setBackgroundResource(R.drawable.search_tab_unselected);
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
		searchResultsNumber.setText("共 " + searchResultCafes.size() + " 項結果");
		searchResultsList.setSelectionAfterHeaderView();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	expand();

    	gallery.startTimer();

		if (directSearch.getTextColors().getDefaultColor() == Color.parseColor("#FFFFFF")) {
			if (searchTextBox.getText().toString().trim().length() == 0) {
//				banner.startTask();
			}
    	} else if (searchResults.getTextColors().getDefaultColor() == Color.parseColor("#FFFFFF")) {
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
    
    public void shrink() {
    	parentActivity.invisibleTabs();
    	tabLayout.setVisibility(View.GONE);
//    	gap.setVisibility(View.GONE);
    }
    
    public void expand() {
    	parentActivity.visibleTabs();
    	tabLayout.setVisibility(View.VISIBLE);
//    	gap.setVisibility(View.VISIBLE);
    	if (searchTextBox.getText().toString().equals("")) {
    		clearBtn.setVisibility(View.GONE);
    	}
    }
    
	private void doDirectSearch(String query) {
		
		query = query.toLowerCase().trim();
		searchCafes.clear();
		
		ArrayList<Cafe> priorityList = new ArrayList<Cafe>(); 
		ArrayList<Cafe> nonPrefixList = new ArrayList<Cafe>(); 
		
		for (Cafe cafe: Config.getInstance().getCafeLists()) {
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
		
		for (Cafe cafe : Config.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0")) continue;
			boolean matchDistrict;
			boolean matchDishes;
			boolean matchServices;
			int regionIndex = region.getCurrentItem();
			int dishesIndex = dishesType.getCurrentItem();
			int dishesId = Utilities.dishesId[dishesIndex];
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
		searchResultsNumber.setText("共 " + searchResultCafes.size() + " 項結果");
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
    
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//    	super.onConfigurationChanged(newConfig);
//    	boolean landscape = Config.getInstance().isLandscape(this);
//    	regionAdapter.setItemResource(landscape ? R.layout.wheel_text_item_landscape : R.layout.wheel_text_item);
//    	dishesTypeAdapter.setItemResource(landscape ? R.layout.wheel_text_item_landscape : R.layout.wheel_text_item);
//    	servicesTypeAdapter.setItemResource(landscape ? R.layout.wheel_text_item_landscape : R.layout.wheel_text_item);
//
//        region.setViewAdapter(regionAdapter);
//        dishesType.setViewAdapter(dishesTypeAdapter);
//        servicesType.setViewAdapter(servicesTypeAdapter);
//    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	ETLog.e(getClass().getSimpleName(), "---onDestroy");
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
	    	
		    	new AlertDialog.Builder(this)
				.setMessage("你確定要退出程式嗎?      ")
				.setPositiveButton("確定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int whichButton) {
						    	Process.killProcess(Process.myPid());   
							}
						})
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int whichButton) {
								dialog.dismiss();
							}
						})
				.show();
		        return true;
			}
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
    
    
}
