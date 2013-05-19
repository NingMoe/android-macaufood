package com.cycon.macaufood;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cycon.macaufood.adapters.CafeListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.ServerCafeXMLHandler;
import com.cycon.macaufood.widget.AdvView;

public class Recommend extends BaseActivity {

	private static final String TAG = Recommend.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView list;
	private AdvView banner;
	private CafeListAdapter cafeAdapter;
	private FileCache fileCache;
	private ProgressDialog pDialog;
	private static final String CACHE_FILE_STR = "recommend_parsed_xml";
	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 24; // 24 hours
	private long dataTimeStamp;
	private View loadingAdv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		isTabChild = true;
		addRefreshMenu = true;
    	ETLog.e(TAG, "onCreate");
        setContentView(R.layout.recommend);
        loadingAdv = findViewById(R.id.loadingAdv);
        dataTimeStamp = Config.getPreferenceValueLong(getApplicationContext(),"recommendTimeStamp", 0);

        list = (ListView) findViewById(R.id.list);
        cafeAdapter = new CafeListAdapter(Recommend.this, Config.getInstance().getRecommendCafeList(), ImageType.RECOMMEND);
        list.setAdapter(cafeAdapter);
        list.setOnItemClickListener(itemClickListener);
        banner = (AdvView) findViewById(R.id.banner);
        banner.setLoadingAdv(loadingAdv);
        fileCache=new FileCache(this, ImageType.RECOMMEND);
        File f=fileCache.getFile(CACHE_FILE_STR);
		try {
			FileInputStream is = new FileInputStream(f);
			parseXml(is);

			if (Config.getInstance().getRecommendCafeList().size() == 0) {
				displayRetryLayout();
			}
		} catch (FileNotFoundException e) {
	    	ETLog.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
		
		//if no internet and no data in File, show retry message
		if (Config.getInstance().getRecommendCafeList().size() == 0) {

	        if (!Config.isOnline(this)) {
	        	displayRetryLayout();
			} 
		}
		
		if (Config.getPreferenceValueBoolean(this, "disclaimerDialog", true)) {

			TextView text = new TextView(Recommend.this);
			text.setTextColor(Color.WHITE);
			text.setTextSize(15);
			text.setPadding(20, 5, 20, 0);
			text.setText(R.string.disclaimerText);
			text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			new AlertDialog.Builder(Recommend.this)
			.setTitle(getString(R.string.disclaimer))
			.setView(text)
			.setPositiveButton("同意以上條款", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Config.savePreferencesBoolean(Recommend.this, "disclaimerDialog", false);
					if (Config.isOnline(Recommend.this) && Config.getInstance().getRecommendCafeList().size() == 0) {
						pDialog = ProgressDialog.show(Recommend.this, null,
								"載入資料中...", false, true);
					}
				}
			}).show();
			
		}
		
		if (Config.isOnline(this)) {
			new SendFavoriteLogTask().execute();
		}
        
    }
    

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			String cafeId = Config.getInstance().getRecommendCafeList().get(position).getCafeid();
			String forward = Config.getInstance().getRecommendCafeList().get(position).getForward();
			
			if (forward.equals("b")) {
//				String branch = Config.getInstance().getCafeLists().get(Integer.parseInt(cafeId) - 1).getBranch();
				String branch = cafeId;
				ETLog.e("branch", branch);
				if (!branch.equals("0")) {
					Intent i = new Intent(Recommend.this, Branch.class);
					i.putExtra("branch", branch);
					startActivity(i);
				} else {
					//in case that cafe is not added in cafelist yet, return
					if (Config.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
					Intent i = new Intent(Recommend.this, Details.class);
					i.putExtra("id", cafeId);
					startActivity(i);
				}
			} else {
//				Toast.makeText(Recommend.this, Config.updateSuccessfully + "", Toast.LENGTH_SHORT).show();
//	            Config.updateSuccessfully = false;
				
				//in case that cafe is not added in cafelist yet, return
				if (Config.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
				Intent i = new Intent(Recommend.this, Details.class);
				i.putExtra("id", cafeId);
				startActivity(i);
			}
    	};
    };
    
    private void displayRetryLayout() {
        retryLayout = findViewById(R.id.retryLayout);
		retryLayout.setVisibility(View.VISIBLE);
		retryButton = (Button) findViewById(R.id.retryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				refresh();
			}
		});
    }
    
    public void refresh() {
    	if (Config.isOnline(this)) {
    		new FetchXmlTask().execute();
    		new FetchUpdateTask().execute();
        	if (retryLayout != null)
        		retryLayout.setVisibility(View.GONE);
    	}
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	if (banner != null)
    		banner.startTask();
    	if (System.currentTimeMillis() - dataTimeStamp > REFRESH_TIME_PERIOD)
    		refresh();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	//let recommend and coupon screen show all images every levae the screen
//    	if (cafeAdapter != null)
//    		cafeAdapter.imageLoader.cleanup();
    	if (banner != null)
    		banner.stopTask();
    }

    @Override
    public void onDestroy()
    {
    	ETLog.e(TAG, "onDestroy");
    	list.setAdapter(null);
        super.onDestroy();
    }
    
    public class FetchXmlTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		if (!Config.getPreferenceValueBoolean(Recommend.this, "disclaimerDialog", true)) {
	    		pDialog = ProgressDialog.show(Recommend.this, null,
						"載入資料中...", false, true);
    		}
    	}
    	@Override
    	protected Void doInBackground(Void... params) {
    		String urlStr = "http://www.cycon.com.mo/xml_caferecommend_new.php?key=cafecafe";
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
            	
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0 ) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
            	
            	parseXml(new ByteArrayInputStream(baos.toByteArray()));
            	if (Config.tempParsedCafeList.size() != 0) {
    				File f=fileCache.getFile(CACHE_FILE_STR);
    	            OutputStream os = new FileOutputStream(f);
    	            os.write(baos.toByteArray());
    	            os.close();
            	}
				
				
			} catch (MalformedURLException e) {
				ETLog.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				ETLog.e(TAG, "io exception");
				e.printStackTrace();
			} 
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);

			ETLog.e(TAG, "onPostExecute");
    		if (pDialog != null) {
    			pDialog.dismiss();
    		}
    		
			//if no internet and no data in File, show retry message
			if (Config.getInstance().getRecommendCafeList().size() == 0) {
				displayRetryLayout();
			} else {
	            dataTimeStamp = System.currentTimeMillis();
	            Config.savePreferencesLong(getApplicationContext(), "recommendTimeStamp", dataTimeStamp);
			}
			cafeAdapter.imageLoader.cleanup();
			cafeAdapter.imageLoader.setImagesToLoadFromParsedCafe(Config.getInstance().getRecommendCafeList());
			ETLog.e(TAG, "populate");
    		cafeAdapter.notifyDataSetChanged();
    	}
    }
    
    private void parseXml(InputStream is) {    	
    	Config.tempParsedCafeList.clear();
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			ServerCafeXMLHandler myXMLHandler = new ServerCafeXMLHandler();
			xr.setContentHandler(myXMLHandler);
			xr.parse(new InputSource(is));	
		} catch (FactoryConfigurationError e) {
			ETLog.e(TAG, "FactoryConfigurationError");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			ETLog.e(TAG, "ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			ETLog.e(TAG, "SAXException");
			e.printStackTrace();
		} catch (IOException e) {
			ETLog.e(TAG, "IOException");
			e.printStackTrace();
		}
		
    	if (Config.tempParsedCafeList.size() != 0) {
    		Config.getInstance().getRecommendCafeList().clear();
    		Config.getInstance().getRecommendCafeList().addAll(Config.tempParsedCafeList);
    	}
    }
    
	private class SendFavoriteLogTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			
//			SharedPreferences prefs = getSharedPreferences(
//					"macaufood.preferences", 0);
//			String str = prefs.getString("favorites", "");
//			if (str.equals("")) return null;
			
			StringBuilder sb = new StringBuilder();
			for (String id : Config.getInstance().getFavoriteLists()) {
				int idValue = Integer.parseInt(id) - 1;
				sb.append(idValue + ",");
			}
			
			String urlStr = "http://www.cycon.com.mo/xml_favouritelog.php?key=cafecafe&udid=android-" + 
					Config.DEVICE_ID + "&cafeid=" + sb.toString();
			
            try {
            	HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	client.execute(request);
            	
			} catch (MalformedURLException e) {
				ETLog.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				ETLog.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				ETLog.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
			
			return null;
		}
	}


}
