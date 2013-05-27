package com.cycon.macaufood.activities;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.FoodNewsListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.AdvView;
import com.cycon.macaufood.xmlhandler.FoodNewsXMLHandler;

public class FoodNews extends SherlockFragment {

	private static final String TAG = FoodNews.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView list;
	private FoodNewsListAdapter foodListAdapter;
	private FileCache fileCache;
	private ProgressDialog pDialog;
	private static final String CACHE_FILE_STR = "foodnews_parsed_xml";
	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 48; // 48 hours
	private long dataTimeStamp;
	private Context mContext;
	private View mView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.foodnews, null);
		initView();
		return mView;
	}
	
	private void initView() {
		list = (ListView) mView.findViewById(R.id.list);
        foodListAdapter = new FoodNewsListAdapter(mContext, MFConfig.getInstance().getFoodNewsList(), ImageType.FOODNEWS);
        list.setAdapter(foodListAdapter);
        list.setOnItemClickListener(itemClickListener);
        
		if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
			displayRetryLayout();
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Log.e("ZZZ", "oncreate foodnews ");
        mContext = getActivity();

        dataTimeStamp = PreferenceHelper.getPreferenceValueLong(mContext.getApplicationContext(),"foodNewsTimeStamp", 0);

        
        fileCache=new FileCache(mContext, ImageType.FOODNEWS);
        File f=fileCache.getFile(CACHE_FILE_STR);
		try {
			FileInputStream is = new FileInputStream(f);
			parseXml(is);


		} catch (FileNotFoundException e) {
	    	Log.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
		
		//if no internet and no data in File, show retry message
//		if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
//
//	        if (!MFConfig.isOnline(this)) {
//	        	displayRetryLayout();
//			} 
//		}
        
    }
    
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			String foodnews_id = MFConfig.getInstance().getFoodNewsList().get(position).getId();
			Intent i = new Intent(mContext, FoodNewsImage.class);
			i.putExtra("foodnews_id", foodnews_id);
			startActivity(i);
			
		}
    };
    
    private void displayRetryLayout() {
        retryLayout = mView.findViewById(R.id.retryLayout);
		retryLayout.setVisibility(View.VISIBLE);
		retryButton = (Button) mView.findViewById(R.id.retryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				refresh();
			}
		});
    }
    
    public void refresh() {
    	if (MFConfig.isOnline(mContext)) {
    		new FetchXmlTask().execute();
        	if (retryLayout != null)
        		retryLayout.setVisibility(View.GONE);
    	}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	if (System.currentTimeMillis() - dataTimeStamp > REFRESH_TIME_PERIOD)
    		refresh();
    }

    @Override
    public void onDestroy()
    {
    	Log.e(TAG, "onDestroy");
    	list.setAdapter(null);
        super.onDestroy();
    }
    
    public class FetchXmlTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		pDialog = ProgressDialog.show(mContext, null,
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
            	if (MFConfig.tempParsedFoodNewsList.size() != 0) {
    				File f=fileCache.getFile(CACHE_FILE_STR);
    	            OutputStream os = new FileOutputStream(f);
    	            os.write(baos.toByteArray());
    	            os.close();
            	}
				
				
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			} 
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);

			Log.e(TAG, "onPostExecute");
    		if (pDialog != null) {
    			pDialog.dismiss();
    		}
    		
			//if no internet and no data in File, show retry message
			if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
				displayRetryLayout();
			} else {
	            dataTimeStamp = System.currentTimeMillis();
	            PreferenceHelper.savePreferencesLong(mContext.getApplicationContext(), "foodNewsTimeStamp", dataTimeStamp);
			}
			foodListAdapter.imageLoader.cleanup();
			foodListAdapter.imageLoader.setImagesToLoadFromParsedFoodNews(MFConfig.getInstance().getFoodNewsList());
			Log.e(TAG, "populate");
    		foodListAdapter.notifyDataSetChanged();
    	}
    }
    
    private void parseXml(InputStream is) {    	
    	MFConfig.tempParsedFoodNewsList.clear();
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			FoodNewsXMLHandler myXMLHandler = new FoodNewsXMLHandler();
			xr.setContentHandler(myXMLHandler);
			xr.parse(new InputSource(is));	
		} catch (FactoryConfigurationError e) {
			Log.e(TAG, "FactoryConfigurationError");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(TAG, "SAXException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
		}
		
    	if (MFConfig.tempParsedFoodNewsList.size() != 0) {
    		MFConfig.getInstance().getFoodNewsList().clear();
    		MFConfig.getInstance().getFoodNewsList().addAll(MFConfig.tempParsedFoodNewsList);
    	}
    }


}
