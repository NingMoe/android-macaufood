package com.cycon.macaufood.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.MFUtil;

public class MacauWifi extends BaseActivity {

	private static final String[] macauSpots = {"通訊博物館", "電信管理局一樓", "海事博物館露天茶座", 
		"旅遊活動中心地庫大賽車及葡萄酒博物館","澳門博物館咖啡店","澳門中央圖書館","何東圖書館及該館的前後花園","青洲圖書館",
		"望廈圖書館（暫停服務）","民政總署大樓圖書館","澳門藝術博物館","澳門藝術博物館附屬圖書館","澳門回歸賀禮陳列館",
		"澳門回歸賀禮陳列館A2藝術工作室","何賢公園圖書館","紀念孫中山公園黃營均圖書館","白鴿巢公園黃營均圖書館",
		"黑沙環公園黃營均圖書館","澳門茶文化館","松山市政公園","二龍喉公園","黑沙環三角花園","友誼廣場﹝新麗華廣場﹞",
		"議事亭前地", "塔石廣場","澳門文化中心大堂","政府資訊中心地下","政府綜合服務大樓","公職福利處",
		"台山活動中心","營地活動中心","祐漢活動中心","下環活動中心","得勝體育中心","外港客運碼頭入境層旅遊局諮詢處",
		"關閘大樓入境層旅遊局諮詢處","澳門商務旅遊中心旅遊局諮詢處﹝議事亭前地﹞","澳門漁人碼頭旅遊局諮詢處",
		"旅遊文化活動中心旅遊局諮詢處 ﹝大三巴﹞","旅遊活動中心地下展覽廳","下環圖書館","塔石體育館","巴坡沙體育中心",
		"板樟堂前地","澳門綜藝館（暫停服務）","南灣湖水上活動中心","藝園","康公廟前地","柯邦廸前地﹝司打口﹞",
		"宋玉生公園","螺絲山公園","綜合服務中心﹝中華廣場二樓﹞","華士古達嘉馬花園","鮑思高體育中心","蓮峰體育中心",
		"竹室正街休憩區","主教山眺望台","水塘休憩區﹝近回力﹞","祐漢街市公園","塔石衛生中心","海傍區衛生中心",
		"風順堂衛生中心","黑沙環衛生中心"
	};
	
	private static final String[] taipaSpots = {"氹仔黃營均圖書館", "路氹歷史館", "龍環葡韻住宅式博物館", 
		"嘉模會堂", "花城公園", "離島區市民服務中心", "氹仔臨時客運碼頭入境層旅遊局諮詢處", "澳門國際機場入境層旅遊局諮詢處", 
		"保齡球中心", "網球學校", "嘉模墟 ﹝官也街", "氹仔衛生中心", "奧林匹克體育中心-游泳館", "奧林匹克體育中心-戶外天地"
	};
	
	private static final String[] coloaneSpots = { "路環圖書館", "黑沙公園", "竹灣燒烤公園", 
		"路環小型賽車場", "石排灣郊野公園", "路環衛生站"};
	
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

            int leftPadding = MFUtil.getPixelsFromDip(10f, getResources());
            int topPadding = MFUtil.getPixelsFromDip(7f, getResources());
			if (objects[position].equals(getString(R.string.macauRegion)) || 
					objects[position].equals(getString(R.string.taipaRegion)) || 
					objects[position].equals(getString(R.string.coloaneRegion))) {
                text.setTextColor(Color.WHITE);
                text.setTextSize(18f);
                text.setPadding(leftPadding, topPadding, leftPadding, topPadding);
                text.setBackgroundResource(R.drawable.subheader_bg);
                text.setText((CharSequence) objects[position]);
                return text;
			} else {
                text.setTextColor(Color.BLACK);
                text.setTextSize(16f);
                text.setPadding(leftPadding, topPadding, leftPadding, topPadding);
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
