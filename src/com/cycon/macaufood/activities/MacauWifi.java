package com.cycon.macaufood.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;

public class MacauWifi extends BaseActivity {

	private static final String[] macauSpots = {"通訊�?�物館", "電信管�?�局一樓", "海事�?�物館露天茶座", 
		"旅�?�活動中心地庫大賽車�?�葡�?�酒�?�物館","澳門�?�物館咖啡店","澳門中央圖書館","何�?�圖書館�?�該館的�?後花園","�?�洲圖書館",
		"望廈圖書館（暫�?��?務）","民政總署大樓圖書館","澳門�?術�?�物館","澳門�?術�?�物館附屬圖書館","澳門回歸賀禮陳列館",
		"澳門回歸賀禮陳列館A2�?術工作室","何賢公園圖書館","紀念孫中山公園黃營�?�圖書館","白鴿巢公園黃營�?�圖書館",
		"黑沙環公園黃營�?�圖書館","澳門茶文化館","�?�山市政公園","二�?喉公園","黑沙環三角花園","�?�誼廣場�?新麗�?�廣場﹞",
		"議事亭�?地", "塔石廣場","澳門文化中心大堂","政府資訊中心地下","政府綜�?��?務大樓","公�?��?利處",
		"�?�山活動中心","營地活動中心","�?漢活動中心","下環活動中心","得�?體育中心","外港客�?�碼頭入境層旅�?�局諮詢處",
		"關閘大樓入境層旅�?�局諮詢處","澳門商務旅�?�中心旅�?�局諮詢處�?議事亭�?地﹞","澳門�?人碼頭旅�?�局諮詢處",
		"旅�?�文化活動中心旅�?�局諮詢處 �?大三巴﹞","旅�?�活動中心地下展覽廳","下環圖書館","塔石體育館","巴�?�沙體育中心",
		"�?�樟堂�?地","澳門綜�?館（暫�?��?務）","�?��?�湖水上活動中心","�?園","康公廟�?地","柯邦廸�?地�?�?�打�?�﹞",
		"宋玉生公園","螺絲山公園","綜�?��?務中心�?中�?�廣場二樓﹞","�?�士�?��?�嘉馬花園","鮑�?高體育中心","蓮峰體育中心",
		"竹室正街休憩�?�","主教山眺望�?�","水塘休憩�?��?近回力﹞","�?漢街市公園","塔石衛生中心","海�?�?�衛生中心",
		"風順堂衛生中心","黑沙環衛生中心"
	};
	
	private static final String[] taipaSpots = {"氹仔黃營�?�圖書館", "路氹歷�?�館", "龍環葡韻�?宅�?�?�物館", 
		"嘉模會堂", "花城公園", "離島�?�市民�?務中心", "氹仔臨時客�?�碼頭入境層旅�?�局諮詢處", "澳門國際機場入境層旅�?�局諮詢處", 
		"�?齡�?�中心", "網�?�學校", "嘉模墟 �?官也街", "氹仔衛生中心", "奧林匹克體育中心-游泳館", "奧林匹克體育中心-戶外天地"
	};
	
	private static final String[] coloaneSpots = { "路環圖書館", "黑沙公園", "竹�?�燒烤公園", 
		"路環�?型賽車場", "石排�?�郊野公園", "路環衛生站"};
	
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		needMenu = false;
		setContentView(R.layout.macauwifi);
		
		int index = 0;
		String[] allSpots = new String[macauSpots.length + taipaSpots.length + coloaneSpots.length + 3];
		
		allSpots[index++] = getString(R.string.macauRegion);
		for (int i = 0; i < macauSpots.length; i++) {
			allSpots[index++] = macauSpots[i];
		}
		allSpots[index++] = getString(R.string.taipaRegion);
		for (int i = 0; i < taipaSpots.length; i++) {
			allSpots[index++] = taipaSpots[i];
		}
		allSpots[index++] = getString(R.string.coloaneRegion);
		for (int i = 0; i < coloaneSpots.length; i++) {
			allSpots[index++] = coloaneSpots[i];
		}
		
		list = (ListView) findViewById(R.id.list);
		
		list.setAdapter(new SectionArrayAdapter(allSpots));
	}
	
	private class SectionArrayAdapter extends BaseAdapter {
		
		private String[] objects;

		public SectionArrayAdapter(String[] objects) {
			this.objects = objects;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView text = null;
			if (convertView == null) {
				text = new TextView(MacauWifi.this);
			} else {
				text = (TextView) convertView;
			}
			if (objects[position].equals(getString(R.string.macauRegion)) || 
					objects[position].equals(getString(R.string.taipaRegion)) || 
					objects[position].equals(getString(R.string.coloaneRegion))) {
                text.setTextColor(Color.WHITE);
                text.setTextSize(18f);
                text.setPadding(10, 7, 7, 7);
                text.setBackgroundColor(Color.parseColor("#888888"));
                text.setText((CharSequence) objects[position]);
                return text;
			} else {
                text.setTextColor(Color.BLACK);
                text.setTextSize(16f);
                text.setPadding(10, 7, 7, 7);
                text.setBackgroundColor(Color.WHITE);
                text.setText((CharSequence) objects[position]);
                return text;
			}
		}

		public int getCount() {
			return objects.length;
		}

		public Object getItem(int arg0) {
			return arg0;
		}

		public long getItemId(int arg0) {
			return arg0;
		}
		
	}
}
