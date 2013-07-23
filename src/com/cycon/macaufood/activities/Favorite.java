package com.cycon.macaufood.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.FeedBackDialogHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.DDListView;

public class Favorite extends BaseActivity {
        
	private static final int EDIT_LIST_MENU_ID = 1;
	private static final int DISPLAY_MAP_MENU_ID = 2;
	private FavoriteAdapter cafeAdapter;
	private DDListView list;
	private boolean isEditMode;
	private TextView noFavoriteList;
	private com.actionbarsherlock.view.MenuItem mMapMenuItem;
	private com.actionbarsherlock.view.MenuItem mEditMenuItem;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.favorite);
        noFavoriteList = (TextView) findViewById(R.id.noFavoriteList);
        list = (DDListView) findViewById(R.id.list);
        cafeAdapter = new FavoriteAdapter();
        list.setAdapter(cafeAdapter);
        list.setOnItemClickListener(favoriteItemClickListener);
	}
    
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		mEditMenuItem = menu.add(0, EDIT_LIST_MENU_ID, 0, R.string.editList).setIcon(R.drawable.ic_edit);
		mEditMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mMapMenuItem = menu.add(0, DISPLAY_MAP_MENU_ID, 1, R.string.showMap).setIcon(R.drawable.map);
		mMapMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        updateMapMenuItem();
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_LIST_MENU_ID:
			if (isEditMode) {
				isEditMode = false;
				mMapMenuItem.setVisible(true);
				mEditMenuItem.setIcon(R.drawable.ic_edit);
				cafeAdapter.notifyDataSetChanged();
		        list.setDropListener(null);
		        updateMapMenuItem();
			} else {
				isEditMode = true;
				mMapMenuItem.setVisible(false);
				mEditMenuItem.setIcon(R.drawable.ic_done);
				cafeAdapter.notifyDataSetChanged();
		        list.setDropListener(onDrop);
			}
			return true;
		case DISPLAY_MAP_MENU_ID:
			Intent i = new Intent(Favorite.this, Map.class);
			i.putExtra("fromFavorite", true);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private DDListView.DropListener onDrop=new DDListView.DropListener() {
		public void drop(int from, int to) {

				String item=cafeAdapter.getItem(from);
				
				cafeAdapter.remove(item);
				cafeAdapter.insert(item, to);
				
				SharedPreferences prefs = getSharedPreferences(
						"macaufood.preferences", 0);
				Editor prefsPrivateEditor = prefs.edit();
				
				String str = ""; 
				for (String id : MFConfig.getInstance().getFavoriteLists()) {
					str += id + ",";
				}
				prefsPrivateEditor.putString("favorites", str);
				prefsPrivateEditor.commit();
		}
	};
	
	private void updateMapMenuItem() {
		
        if (MFConfig.getInstance().getFavoriteLists().size() == 0) {
        	noFavoriteList.setVisibility(View.VISIBLE);
        } else {
        	noFavoriteList.setVisibility(View.GONE);
        }
		
		if (mMapMenuItem == null) {
			return;
		}
		
		if (MFConfig.getInstance().getFavoriteLists().size() == 0) {
			mMapMenuItem.setVisible(false);
			mEditMenuItem.setVisible(false);
		} else {
			mMapMenuItem.setVisible(true);
			mEditMenuItem.setVisible(true);
		}
	}
    
    protected void onResume() {
    	super.onResume();

        updateMapMenuItem();
        if (mEditMenuItem != null)
        	mEditMenuItem.setIcon(R.drawable.ic_edit);
		isEditMode = false;
        list.setDropListener(null);
    	cafeAdapter.notifyDataSetChanged();
    };

    AdapterView.OnItemClickListener favoriteItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			if (!isEditMode) {
				Intent i = new Intent(Favorite.this, Details.class);
				i.putExtra("id", MFConfig.getInstance().getFavoriteLists().get(position));
				startActivity(i);
			}
    	};
    };
    
	class FavoriteAdapter extends ArrayAdapter<String> implements AdapterView.OnItemClickListener {
        
        public FavoriteAdapter() {
			super(Favorite.this, R.layout.favorite_row, MFConfig.getInstance().getFavoriteLists());
		}

        public View getView(final int position, View convertView, ViewGroup parent) {
        	Cafe cafe = MFConfig.getInstance().getCafeLists().get(Integer.parseInt(MFConfig.getInstance().getFavoriteLists().get(position)) - 1);
            
//            if (convertView == null) {
				LayoutInflater inflater=getLayoutInflater();
				convertView =inflater.inflate(R.layout.favorite_row, parent, false);
//            } 
            
            TextView text = (TextView) convertView.findViewById(R.id.label);
            text.setText(cafe.getName());
            
            ImageView deleteBtn = (ImageView) convertView.findViewById(R.id.delete_btn);
            deleteBtn.setOnClickListener(new OnClickListener() {
				
				public void onClick(View arg0) {
					MFConfig.getInstance().getFavoriteLists().remove(position);
					notifyDataSetChanged();
					
					StringBuilder sb = new StringBuilder();
					for (String id : MFConfig.getInstance().getFavoriteLists()) {
						sb.append(id);
						sb.append(',');
					}
					PreferenceHelper.savePreferencesStr(Favorite.this, "favorites", sb.toString());
				}
			});

            if (isEditMode) {
            	convertView.findViewById(R.id.icon).setVisibility(View.VISIBLE);
            	deleteBtn.setVisibility(View.VISIBLE);
			} else {
            	convertView.findViewById(R.id.icon).setVisibility(View.GONE);
            	deleteBtn.setVisibility(View.GONE);
			}

            return convertView;
        }


        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent i = new Intent(Favorite.this, Details.class);
			i.putExtra("id", MFConfig.getInstance().getFavoriteLists().get(position));
			startActivity(i);
        }
    }
    
}
    
