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
import java.util.concurrent.Executor;

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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.xmlhandler.ServerCafeXMLHandler;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

public class Recommend extends SherlockFragment {

	private static final String TAG = Recommend.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView list;
	private CafeListAdapter cafeAdapter;
	private FileCache fileCache;
	private ProgressDialog pDialog;
	private static final String CACHE_FILE_STR = "recommend_parsed_xml";
	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 24; // 24 hours
//	private static final long REFRESH_TIME_PERIOD = 3600; // 24 hours
	private long dataTimeStamp;
	private Context mContext;
	private View mView;
	private SwingLeftInAnimationAdapter swingBottomInAnimationAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.recommend, null);
		initView();
		return mView;
	}
	
	private void initView() {
        list = (ListView) mView.findViewById(R.id.list);
        cafeAdapter = new CafeListAdapter(mContext, MFConfig.getInstance().getRecommendCafeList(), ImageType.RECOMMEND);
        
        swingBottomInAnimationAdapter = new SwingLeftInAnimationAdapter(cafeAdapter);
        swingBottomInAnimationAdapter.setListView(list);

		list.setAdapter(swingBottomInAnimationAdapter);
        list.setOnItemClickListener(itemClickListener);
        
        if (MFConfig.getInstance().getRecommendCafeList().size() == 0) {
			displayRetryLayout();
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = getActivity();

        dataTimeStamp = PreferenceHelper.getPreferenceValueLong(mContext.getApplicationContext(),"recommendTimeStamp", 0);
        
        fileCache=new FileCache(mContext, ImageType.RECOMMEND);
        File f=fileCache.getFile(CACHE_FILE_STR);
		try {
			FileInputStream is = new FileInputStream(f);
			parseXml(is);
//
//			if (MFConfig.getInstance().getRecommendCafeList().size() == 0) {
//				displayRetryLayout();
//			}
		} catch (FileNotFoundException e) {
	    	Log.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
		
		//if no internet and no data in File, show retry message
//		if (MFConfig.getInstance().getRecommendCafeList().size() == 0) {
//
//	        if (!MFConfig.isOnline(mContext)) {
//	        	displayRetryLayout();
//			} 
//		}
		
		
        
    }
    

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			String cafeId = MFConfig.getInstance().getRecommendCafeList().get(position).getCafeid();
			String forward = MFConfig.getInstance().getRecommendCafeList().get(position).getForward();
			
			if (forward.equals("b")) {
//				String branch = Config.getInstance().getCafeLists().get(Integer.parseInt(cafeId) - 1).getBranch();
				String branch = cafeId;
				Log.e("branch", branch);
				if (!branch.equals("0")) {
					Intent i = new Intent(mContext, Branch.class);
					i.putExtra("branch", branch);
					startActivity(i);
				} else {
					//in case that cafe is not added in cafelist yet, return
					if (MFConfig.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
					Intent i = new Intent(mContext, Details.class);
					i.putExtra("id", cafeId);
					startActivity(i);
				}
			} else {
//				Toast.makeText(Recommend.this, Config.updateSuccessfully + "", Toast.LENGTH_SHORT).show();
//	            Config.updateSuccessfully = false;
				
				//in case that cafe is not added in cafelist yet, return
				if (MFConfig.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
				Intent i = new Intent(mContext, Details.class);
				i.putExtra("id", cafeId);
				startActivity(i);
			}
    	};
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
    

	public void resetListViewAnimation() {
		if (swingBottomInAnimationAdapter != null) {
			swingBottomInAnimationAdapter.reset();
			swingBottomInAnimationAdapter.notifyDataSetChanged();
		}
	}
    
	@SuppressLint("NewApi")
	public void refresh() {
		if (MFConfig.isOnline(mContext)) {
			
			AsyncTaskHelper.execute(new FetchXmlTask());

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
    		if (!PreferenceHelper.getPreferenceValueBoolean(mContext, "disclaimerDialog", true)) {
	    		pDialog = ProgressDialog.show(mContext, null,
						"載入資料中...", false, true);
    		}
    		Log.e("ZZZ", "on pre execute");
    	}
    	@Override
    	protected Void doInBackground(Void... params) {

        	Log.e("ZZZ", "init execute");
    		String urlStr = "http://www.cycon.com.mo/xml_caferecommend_new.php?key=cafecafe";
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	Log.e("ZZZ", "pre http get");
            	HttpGet request = new HttpGet(urlStr);

            	Log.e("ZZZ", "pre execute");
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
            	
            	Log.e("ZZZ", "finish getting reponse" + response.toString());
            	Log.e("ZZZ", "finish getting reponse" + response.getEntity().toString());
            	
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0 ) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                

            	Log.e("ZZZ", "pre prase xml");
            	
            	parseXml(new ByteArrayInputStream(baos.toByteArray()));
            	
            	Log.e("ZZZ", "post prase xml");
            	if (MFConfig.tempParsedCafeList.size() != 0) {
    				File f=fileCache.getFile(CACHE_FILE_STR);
    	            OutputStream os = new FileOutputStream(f);
    	            os.write(baos.toByteArray());
    	            os.close();
            	}

            	Log.e("ZZZ", "finish write to file");
				
				
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
			if (MFConfig.getInstance().getRecommendCafeList().size() == 0) {
				displayRetryLayout();
			} else {
	            dataTimeStamp = System.currentTimeMillis();
	            PreferenceHelper.savePreferencesLong(mContext.getApplicationContext(), "recommendTimeStamp", dataTimeStamp);
			}
			cafeAdapter.imageLoader.cleanup();
			cafeAdapter.imageLoader.setImagesToLoadFromParsedCafe(MFConfig.getInstance().getRecommendCafeList());
			Log.e(TAG, "populate");
    		cafeAdapter.notifyDataSetChanged();
    	}
    }
    
    private void parseXml(InputStream is) {    	
    	MFConfig.tempParsedCafeList.clear();
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			ServerCafeXMLHandler myXMLHandler = new ServerCafeXMLHandler();
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
		
    	if (MFConfig.tempParsedCafeList.size() != 0) {
    		MFConfig.getInstance().getRecommendCafeList().clear();
    		MFConfig.getInstance().getRecommendCafeList().addAll(MFConfig.tempParsedCafeList);
    	}
    }
    


}
