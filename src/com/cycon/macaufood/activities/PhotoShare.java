package com.cycon.macaufood.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.PSHotAdapter;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;

public class PhotoShare extends SherlockFragment {

	private static final String TAG = PhotoShare.class.getName();
	
//	private View retryLayout;
//	private Button retryButton;
//	private ListView list;
//	private FoodNewsListAdapter foodListAdapter;
//	private FileCache fileCache;
//	private ProgressDialog pDialog;
//	private static final String CACHE_FILE_STR = "foodnews_parsed_xml";
//	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 48; // 48 hours
//	private long dataTimeStamp;
	private Context mContext;
	private View mView;
	
	private TextView mPsFriends;
	private TextView mPsHot;
	private View mPsCamera;
	private View mPsSettings;
	private GridView mHotLayoutGV;
	private View mFriendsLayoutSV;
	private int mCurrentTab = 1; //friends = 0, hot = 1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView != null) {
			 ((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		mView = inflater.inflate(R.layout.photo_share, null);
		initView();
		return mView;
	}
	
	private void initView() {
//		list = (ListView) mView.findViewById(R.id.list);
//        foodListAdapter = new FoodNewsListAdapter(mContext, MFConfig.getInstance().getFoodNewsList(), ImageType.FOODNEWS);
//        list.setAdapter(foodListAdapter);
//        list.setOnItemClickListener(itemClickListener);
//        
//		if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
//			displayRetryLayout();
//		}
		
		
		mHotLayoutGV = (GridView) mView.findViewById(R.id.hotLayoutGV);
		
		List<Cafe> list = new ArrayList<Cafe>();
		for (int i = 0 ; i < 25; i++) {
			list.add(MFConfig.getInstance().getCafeLists().get(i));
		}
		mHotLayoutGV.setAdapter(new PSHotAdapter(getActivity(), list));
		mFriendsLayoutSV = mView.findViewById(R.id.friendsLayoutSV);
		
		mPsFriends = (TextView) mView.findViewById(R.id.psFriends);
		mPsFriends.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mCurrentTab == 0) return;
				mPsHot.setSelected(false);
				mPsFriends.setSelected(true);
				mCurrentTab = 0;
				mHotLayoutGV.setVisibility(View.GONE);
				mFriendsLayoutSV.setVisibility(View.VISIBLE);
			}
		});
		mPsHot = (TextView) mView.findViewById(R.id.psHot);
		mPsHot.setSelected(true);
		mPsHot.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mCurrentTab == 1) return;
				mPsHot.setSelected(true);
				mPsFriends.setSelected(false);
				mCurrentTab = 1;
				mHotLayoutGV.setVisibility(View.VISIBLE);
				mFriendsLayoutSV.setVisibility(View.GONE);
			}
		});
		mPsCamera = mView.findViewById(R.id.psCamera);
		mPsCamera.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		mPsSettings = mView.findViewById(R.id.psSettings);
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        
    }
    
//    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
//		public void onItemClick(AdapterView<?> parent, View view,
//				int position, long id) {
//			
//			String foodnews_id = MFConfig.getInstance().getFoodNewsList().get(position).getId();
//			Intent i = new Intent(mContext, FoodNewsImage.class);
//			i.putExtra("foodnews_id", foodnews_id);
//			startActivity(i);
//			
//		}
//    };
//    
//    private void displayRetryLayout() {
//        retryLayout = mView.findViewById(R.id.retryLayout);
//		retryLayout.setVisibility(View.VISIBLE);
//		retryButton = (Button) mView.findViewById(R.id.retryButton);
//		retryButton.setOnClickListener(new OnClickListener() {
//			
//			public void onClick(View v) {
//				refresh();
//			}
//		});
//    }
//    
//    public void refresh() {
//    	if (MFConfig.isOnline(mContext)) {
//    		new FetchXmlTask().execute();
//        	if (retryLayout != null)
//        		retryLayout.setVisibility(View.GONE);
//    	}
//    }
//
//    @Override
//    public void onResume() {
//    	super.onResume();
//    	if (System.currentTimeMillis() - dataTimeStamp > REFRESH_TIME_PERIOD)
//    		refresh();
//    }
//
//    @Override
//    public void onDestroy()
//    {
//    	MFLog.e(TAG, "onDestroy");
//    	list.setAdapter(null);
//        super.onDestroy();
//    }
//    
//    public class FetchXmlTask extends AsyncTask<Void, Void, Void> {
//    	
//    	@Override
//    	protected void onPreExecute() {
//    		super.onPreExecute();
//    		pDialog = ProgressDialog.show(mContext, null,
//						"載入資料中...", false, true);
//    	}
//    	@Override
//    	protected Void doInBackground(Void... params) {
//    		String urlStr = "http://www.cycon.com.mo/xml_article.php?key=cafecafe";
//            try {
//				HttpClient client = new DefaultHttpClient();
//            	HttpParams httpParams = client.getParams();
//            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
//            	HttpGet request = new HttpGet(urlStr);
//            	HttpResponse response = client.execute(request);
//            	InputStream is= response.getEntity().getContent();
//            	
//            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                byte[] buffer = new byte[1024];
//                int len;
//                while ((len = is.read(buffer)) > 0 ) {
//                    baos.write(buffer, 0, len);
//                }
//                baos.flush();
//            	
//            	parseXml(new ByteArrayInputStream(baos.toByteArray()));
//            	if (MFConfig.tempParsedFoodNewsList.size() != 0) {
//    				File f=fileCache.getFile(CACHE_FILE_STR);
//    	            OutputStream os = new FileOutputStream(f);
//    	            os.write(baos.toByteArray());
//    	            os.close();
//            	}
//				
//				
//			} catch (MalformedURLException e) {
//				MFLog.e(TAG, "malformed url exception");
//				e.printStackTrace();
//			} catch (IOException e) {
//				MFLog.e(TAG, "io exception");
//				e.printStackTrace();
//			} 
//    		
//    		return null;
//    	}
//    	
//    	@Override
//    	protected void onPostExecute(Void result) {
//    		super.onPostExecute(result);
//
//			MFLog.e(TAG, "onPostExecute");
//    		if (pDialog != null) {
//    			pDialog.dismiss();
//    		}
//    		
//			//if no internet and no data in File, show retry message
//			if (MFConfig.getInstance().getFoodNewsList().size() == 0) {
//				displayRetryLayout();
//			} else {
//	            dataTimeStamp = System.currentTimeMillis();
//	            PreferenceHelper.savePreferencesLong(mContext.getApplicationContext(), "foodNewsTimeStamp", dataTimeStamp);
//			}
//			foodListAdapter.imageLoader.cleanup();
//			foodListAdapter.imageLoader.setImagesToLoadFromParsedFoodNews(MFConfig.getInstance().getFoodNewsList());
//			MFLog.e(TAG, "populate");
//    		foodListAdapter.notifyDataSetChanged();
//    	}
//    }
//    
//    private void parseXml(InputStream is) {    	
//    	MFConfig.tempParsedFoodNewsList.clear();
//    	try {
//			SAXParserFactory spf = SAXParserFactory.newInstance();
//			SAXParser sp = spf.newSAXParser();
//			XMLReader xr = sp.getXMLReader();
//			FoodNewsXMLHandler myXMLHandler = new FoodNewsXMLHandler();
//			xr.setContentHandler(myXMLHandler);
//			xr.parse(new InputSource(is));	
//		} catch (FactoryConfigurationError e) {
//			MFLog.e(TAG, "FactoryConfigurationError");
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			MFLog.e(TAG, "ParserConfigurationException");
//			e.printStackTrace();
//		} catch (SAXException e) {
//			MFLog.e(TAG, "SAXException");
//			e.printStackTrace();
//		} catch (IOException e) {
//			MFLog.e(TAG, "IOException");
//			e.printStackTrace();
//		}
//		
//    	if (MFConfig.tempParsedFoodNewsList.size() != 0) {
//    		MFConfig.getInstance().getFoodNewsList().clear();
//    		MFConfig.getInstance().getFoodNewsList().addAll(MFConfig.tempParsedFoodNewsList);
//    	}
//    }


}
