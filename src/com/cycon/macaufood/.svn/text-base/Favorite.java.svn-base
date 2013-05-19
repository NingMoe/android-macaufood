package com.cycon.macaufood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.widget.DDListView;

public class Favorite extends BaseActivity {
        
	private FavoriteAdapter cafeAdapter;
	private DDListView list;
	private Button editBtn;
	private boolean isEditMode;
	private TextView noFavoriteList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		isTabChild = true;
        setContentView(R.layout.favorite);
        noFavoriteList = (TextView) findViewById(R.id.noFavoriteList);
        editBtn = (Button) findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (isEditMode) {
					isEditMode = false;
					editBtn.setText(getResources().getString(R.string.editList));
					cafeAdapter.notifyDataSetChanged();
			        list.setDropListener(null);
				} else {
					isEditMode = true;
					editBtn.setText(getResources().getString(R.string.done));
					cafeAdapter.notifyDataSetChanged();
			        list.setDropListener(onDrop);
				}
			}
		});
        list = (DDListView) findViewById(R.id.list);
        cafeAdapter = new FavoriteAdapter();
        list.setAdapter(cafeAdapter);
        list.setOnItemClickListener(favoriteItemClickListener);
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
				for (String id : Config.getInstance().getFavoriteLists()) {
					str += id + ",";
				}
				prefsPrivateEditor.putString("favorites", str);
				prefsPrivateEditor.commit();
		}
	};
    
    protected void onResume() {
    	super.onResume();
        if (Config.getInstance().getFavoriteLists().size() == 0) {
        	noFavoriteList.setVisibility(View.VISIBLE);
        } else {
        	noFavoriteList.setVisibility(View.GONE);
        }
		isEditMode = false;
		editBtn.setText(getResources().getString(R.string.editList));
        list.setDropListener(null);
    	cafeAdapter.notifyDataSetChanged();
    };

    AdapterView.OnItemClickListener favoriteItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			Intent i = new Intent(Favorite.this, Details.class);
			i.putExtra("id", Config.getInstance().getFavoriteLists().get(position));
			startActivity(i);
    	};
    };
    
	class FavoriteAdapter extends ArrayAdapter<String> implements AdapterView.OnItemClickListener {
        
        public FavoriteAdapter() {
			super(Favorite.this, R.layout.favorite_row, Config.getInstance().getFavoriteLists());
		}

        public View getView(final int position, View convertView, ViewGroup parent) {
        	Cafe cafe = Config.getInstance().getCafeLists().get(Integer.parseInt(Config.getInstance().getFavoriteLists().get(position)) - 1);
            
//            if (convertView == null) {
				LayoutInflater inflater=getLayoutInflater();
				convertView =inflater.inflate(R.layout.favorite_row, parent, false);
//            } 
            
            TextView text = (TextView) convertView.findViewById(R.id.label);
            text.setText(cafe.getName());
            
            ImageView deleteBtn = (ImageView) convertView.findViewById(R.id.delete_btn);
            deleteBtn.setOnClickListener(new OnClickListener() {
				
				public void onClick(View arg0) {
					Config.getInstance().getFavoriteLists().remove(position);
					notifyDataSetChanged();
					
					SharedPreferences prefs = getSharedPreferences(
							"macaufood.preferences", 0);
					Editor prefsPrivateEditor = prefs.edit();
					
					String str = ""; 
					for (String id : Config.getInstance().getFavoriteLists()) {
						str += id + ",";
					}
					prefsPrivateEditor.putString("favorites", str);
					prefsPrivateEditor.commit();
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
			i.putExtra("id", Config.getInstance().getFavoriteLists().get(position));
			startActivity(i);
        }
    }
    
}
    
