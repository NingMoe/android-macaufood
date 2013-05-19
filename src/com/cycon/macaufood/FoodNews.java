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
import com.cycon.macaufood.adapters.FoodNewsListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.FoodNewsXMLHandler;
import com.cycon.macaufood.utilities.ServerCafeXMLHandler;
import com.cycon.macaufood.widget.AdvView;

public class FoodNews extends BaseActivity {

	private static final String TAG = FoodNews.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView list;
	private AdvView banner;
	private FoodNewsListAdapter foodListAdapter;
	private FileCache fileCache;
	private ProgressDialog pDialog;
	private static final String CACHE_FILE_STR = "foodnews_parsed_xml";
	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 48; // 48 hours
	private long dataTimeStamp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		addRefreshMenu = true;
    	ETLog.e(TAG, "onCreate");
        setContentView(R.layout.foodnews);
        dataTimeStamp = Config.getPreferenceValueLong(getApplicationContext(),"foodNewsTimeStamp", 0);

        list = (ListView) findViewById(R.id.list);
        foodListAdapter = new FoodNewsListAdapter(FoodNews.this, Config.getInstance().getFoodNewsList(), ImageType.FOODNEWS);
        list.setAdapter(foodListAdapter);
        list.setOnItemClickListener(itemClickListener);
        banner = (AdvView) findViewById(R.id.banner);
        fileCache=new FileCache(this, ImageType.FOODNEWS);
        File f=fileCache.getFile(CACHE_FILE_STR);
		try {
			FileInputStream is = new FileInputStream(f);
			parseXml(is);

			if (Config.getInstance().getFoodNewsList().size() == 0) {
				displayRetryLayout();
			}
		} catch (FileNotFoundException e) {
	    	ETLog.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
		
		//if no internet and no data in File, show retry message
		if (Config.getInstance().getFoodNewsList().size() == 0) {

	        if (!Config.isOnline(this)) {
	        	displayRetryLayout();
			} 
		}
        
    }
    
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			String foodnews_id = Config.getInstance().getFoodNewsList().get(position).getId();
			Intent i = new Intent(FoodNews.this, FoodNewsImage.class);
			i.putExtra("foodnews_id", foodnews_id);
			startActivity(i);
			
		}
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
    		pDialog = ProgressDialog.show(FoodNews.this, null,
						"載入資料中...", false, true);
    	}
    	@Override
    	protected Void doInBackground(Void... params) {
    		String urlStr = "http://www.cycon.com.mo/xml_article.php?key=cafecafe";
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
            	if (Config.tempParsedFoodNewsList.size() != 0) {
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
			if (Config.getInstance().getFoodNewsList().size() == 0) {
				displayRetryLayout();
			} else {
	            dataTimeStamp = System.currentTimeMillis();
	            Config.savePreferencesLong(getApplicationContext(), "foodNewsTimeStamp", dataTimeStamp);
			}
			foodListAdapter.imageLoader.cleanup();
			foodListAdapter.imageLoader.setImagesToLoadFromParsedFoodNews(Config.getInstance().getFoodNewsList());
			ETLog.e(TAG, "populate");
    		foodListAdapter.notifyDataSetChanged();
    	}
    }
    
    private void parseXml(InputStream is) {    	
    	Config.tempParsedFoodNewsList.clear();
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			FoodNewsXMLHandler myXMLHandler = new FoodNewsXMLHandler();
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
		
    	if (Config.tempParsedFoodNewsList.size() != 0) {
    		Config.getInstance().getFoodNewsList().clear();
    		Config.getInstance().getFoodNewsList().addAll(Config.tempParsedFoodNewsList);
    	}
    }


}
