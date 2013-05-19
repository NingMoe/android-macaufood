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
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.adapters.CafeListAdapter;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.AdvView;
import com.cycon.macaufood.xmlhandler.ServerCafeXMLHandler;

public class Coupon extends BaseActivity {

	private static final String TAG = Coupon.class.getName();
	
	private View retryLayout;
	private Button retryButton;
	private ListView normalCouponList;
	private ListView creditCouponList;
	private ListView vipCouponList;
	private AdvView banner;
	private CafeListAdapter normalCouponAdapter;
	private CafeListAdapter creditCouponAdapter;
	private CafeListAdapter vipCouponAdapter;
	private FileCache fileCache;
	private ProgressDialog pDialog;
	private static final String NORMAL_COUPON_CACHE_FILE_STR = "normal_coupon_parsed_xml";
	private static final String CREDIT_COUPON_CACHE_FILE_STR = "credit_coupon_parsed_xml";
	private static final String VIP_COUPON_CACHE_FILE_STR = "vip_coupon_parsed_xml";
	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 48; // 48 hours
	private long normalCouponDataTimeStamp;
	private long creditCouponDataTimeStamp;
	private long vipCouponDataTimeStamp;
	
	private TextView normalCoupon;
	private TextView creditCoupon;
	private TextView vipCoupon;
	private int couponType = 0; //0 = normal, 1 = credit, 2 = vip
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		isTabChild = true;
		addRefreshMenu = true;
    	Log.e(TAG, "onCreate");
        setContentView(R.layout.coupon);

        retryLayout = findViewById(R.id.retryLayout);

        normalCouponDataTimeStamp = PreferenceHelper.getPreferenceValueLong(getApplicationContext(), "normalCouponTimeStamp", 0);
        creditCouponDataTimeStamp = PreferenceHelper.getPreferenceValueLong(getApplicationContext(), "creditCouponTimeStamp", 0);
        vipCouponDataTimeStamp = PreferenceHelper.getPreferenceValueLong(getApplicationContext(), "vipCouponTimeStamp", 0);

        normalCouponList = (ListView) findViewById(R.id.normalCouponList);
        creditCouponList = (ListView) findViewById(R.id.creditCouponList);
        vipCouponList = (ListView) findViewById(R.id.vipCouponList);
        normalCouponAdapter = new CafeListAdapter(Coupon.this, MFConfig.getInstance().getNormalCouponCafeList(), ImageType.COUPON);
        creditCouponAdapter = new CafeListAdapter(Coupon.this, MFConfig.getInstance().getCreditCouponCafeList(), ImageType.COUPON);
        vipCouponAdapter = new CafeListAdapter(Coupon.this, MFConfig.getInstance().getVipCouponCafeList(), ImageType.COUPON);
        normalCouponList.setAdapter(normalCouponAdapter);
        creditCouponList.setAdapter(creditCouponAdapter);
        vipCouponList.setAdapter(vipCouponAdapter);
        normalCouponList.setOnItemClickListener(itemClickListener);
        creditCouponList.setOnItemClickListener(itemClickListener);
        vipCouponList.setOnItemClickListener(itemClickListener);
        banner = (AdvView) findViewById(R.id.banner);
        fileCache=new FileCache(this, ImageType.COUPON);
		
		normalCoupon = (TextView) findViewById(R.id.normalCoupon);
		normalCoupon.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (couponType != 0) {
					setNormalCouponTab(true);
					if (couponType == 1) setCreditCouponTab(false);
					if (couponType == 2) setVipCouponTab(false);
					couponType = 0;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					checkIfNeededRefresh();
				}
			}
		});
		creditCoupon = (TextView) findViewById(R.id.creditCoupon);
		creditCoupon.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (couponType != 1) {
					setCreditCouponTab(true);
					if (couponType == 0) setNormalCouponTab(false);
					if (couponType == 2) setVipCouponTab(false);
					couponType = 1;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getCreditCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					checkIfNeededRefresh();
				}
			}
		});
		vipCoupon = (TextView) findViewById(R.id.vipCoupon);
		vipCoupon.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (couponType != 2) {
					setVipCouponTab(true);
					if (couponType == 0) setNormalCouponTab(false);
					if (couponType == 1) setCreditCouponTab(false);
					couponType = 2;
					retryLayout.setVisibility(View.GONE);
					if (MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
						preLoadFromFileCache();
					}
					checkIfNeededRefresh();
				}
			}
		});
		
		preLoadFromFileCache();
		//onResume will call fetch data
        
    }
    
    private void setNormalCouponTab(boolean select) {
    	if (select) {
    		normalCoupon.setTextColor(Color.parseColor("#FFFFFF"));
    		normalCoupon.setBackgroundResource(R.drawable.tab_normal_coupon_selected);
    		normalCouponList.setVisibility(View.VISIBLE);
    	} else {
    		normalCoupon.setTextColor(Color.parseColor("#68A6E6"));
    		normalCoupon.setBackgroundResource(R.drawable.tab_normal_coupon_unselected);
    		normalCouponList.setVisibility(View.GONE);
    	}
    }
    
    private void setCreditCouponTab(boolean select) {
    	if (select) {
    		creditCoupon.setTextColor(Color.parseColor("#FFFFFF"));
    		creditCoupon.setBackgroundResource(R.drawable.tab_credit_coupon_selected);
    		creditCouponList.setVisibility(View.VISIBLE);
    	} else {
    		creditCoupon.setTextColor(Color.parseColor("#40C28a"));
    		creditCoupon.setBackgroundResource(R.drawable.tab_credit_coupon_unselected);
    		creditCouponList.setVisibility(View.GONE);
    	}
    }
    
    private void setVipCouponTab(boolean select) {
    	if (select) {
    		vipCoupon.setTextColor(Color.parseColor("#FFFFFF"));
    		vipCoupon.setBackgroundResource(R.drawable.tab_vip_coupon_selected);
    		vipCouponList.setVisibility(View.VISIBLE);
    	} else {
    		vipCoupon.setTextColor(Color.parseColor("#EF6666"));
    		vipCoupon.setBackgroundResource(R.drawable.tab_vip_coupon_unselected);
    		vipCouponList.setVisibility(View.GONE);
    	}
    }
    

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
    		
			String cafeId = null;
			String forward = null;
			
			if (couponType == 0) {
				cafeId = MFConfig.getInstance().getNormalCouponCafeList().get(position).getCafeid();
				forward = MFConfig.getInstance().getNormalCouponCafeList().get(position).getForward();
			}
			if (couponType == 1) {
				cafeId = MFConfig.getInstance().getCreditCouponCafeList().get(position).getCafeid();
				forward = MFConfig.getInstance().getCreditCouponCafeList().get(position).getForward();
			}
			if (couponType == 2) {
				cafeId = MFConfig.getInstance().getVipCouponCafeList().get(position).getCafeid();
				forward = MFConfig.getInstance().getVipCouponCafeList().get(position).getForward();
			}
			
			
			if (forward.equals("b")) {
//				String branch = Config.getInstance().getCafeLists().get(Integer.parseInt(cafeId) - 1).getBranch();
				String branch = cafeId;
				if (!branch.equals("0")) {
					Intent i = new Intent(Coupon.this, Branch.class);
					i.putExtra("branch", branch);
					startActivity(i);
				} else {
					//in case that cafe is not added in cafelist yet, return
					if (MFConfig.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
					Intent i = new Intent(Coupon.this, Details.class);
					i.putExtra("id", cafeId);
					startActivity(i);
				}
			} else {
				//in case that cafe is not added in cafelist yet, return
				if (MFConfig.getInstance().getCafeLists().size() < Integer.parseInt(cafeId)) return;
				Intent i = new Intent(Coupon.this, Details.class);
				i.putExtra("id", cafeId);
				startActivity(i);
			}
    	};
    };
    
    private void displayRetryLayout() {
		retryLayout.setVisibility(View.VISIBLE);
		retryButton = (Button) findViewById(R.id.retryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
		    	refresh();
			}
		});
    }
    
    private void preLoadFromFileCache() {
		File f = null;

        if (couponType == 0)
			f=fileCache.getFile(NORMAL_COUPON_CACHE_FILE_STR);
        else if (couponType == 1)
			f=fileCache.getFile(CREDIT_COUPON_CACHE_FILE_STR);
        else if (couponType == 2)
			f=fileCache.getFile(VIP_COUPON_CACHE_FILE_STR);
		try {
			FileInputStream is = new FileInputStream(f);
			parseXml(is, couponType);
			//if no internet and no data in File, show retry message
			if (couponType == 0 && MFConfig.getInstance().getNormalCouponCafeList().size() == 0 || 
					couponType == 1 && MFConfig.getInstance().getCreditCouponCafeList().size() == 0 ||
					couponType == 2 && MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
		        	displayRetryLayout();
			} 
		} catch (FileNotFoundException e) {
	    	Log.e(TAG, "FileNotFoundException");
			e.printStackTrace();
		} 
		
		//if no internet and no data in File, show retry message
		if (couponType == 0 && MFConfig.getInstance().getNormalCouponCafeList().size() == 0 || 
				couponType == 1 && MFConfig.getInstance().getCreditCouponCafeList().size() == 0 ||
				couponType == 2 && MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
	        if (!MFConfig.isOnline(this)) {
	        	displayRetryLayout();
			} 
		} 
    }
    
    public void refresh() {
    	if (MFConfig.isOnline(this)) {
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
    	checkIfNeededRefresh();
    }
    
    private void checkIfNeededRefresh() {
    	long dataTimeStamp = 0;
    	if (couponType == 0) dataTimeStamp = normalCouponDataTimeStamp;
    	else if (couponType == 1) dataTimeStamp = creditCouponDataTimeStamp;
    	else if (couponType == 2) dataTimeStamp = vipCouponDataTimeStamp;
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
    
    public class FetchXmlTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		pDialog = ProgressDialog.show(Coupon.this, null,
					"載入資料中...", false, true);
    	}
    	@Override
    	protected Void doInBackground(Void... params) {
			File f = null;
			
			int type = couponType;
			
            if (couponType == 0)
				f=fileCache.getFile(NORMAL_COUPON_CACHE_FILE_STR);
            else if (couponType == 1)
				f=fileCache.getFile(CREDIT_COUPON_CACHE_FILE_STR);
            else if (couponType == 2)
				f=fileCache.getFile(VIP_COUPON_CACHE_FILE_STR);
            
    		
    		String urlStr = "http://www.cycon.com.mo/xml_cafecoupon_new.php?key=cafecafe&type=" + couponType;
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
                
            	parseXml(new ByteArrayInputStream(baos.toByteArray()), type);
            	if (MFConfig.tempParsedCafeList.size() != 0) {
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

    		if (pDialog != null) {
    			pDialog.dismiss();
    		}
    		
    		if (couponType == 0) {
	    		
				//if no internet and no data in File, show retry message
				if (MFConfig.getInstance().getNormalCouponCafeList().size() == 0) {
					displayRetryLayout();
				} else {
		            normalCouponDataTimeStamp = System.currentTimeMillis();
		            PreferenceHelper.savePreferencesLong(getApplicationContext(), "normalCouponTimeStamp", normalCouponDataTimeStamp);
				}
				normalCouponAdapter.imageLoader.cleanup();
				normalCouponAdapter.imageLoader.setImagesToLoadFromParsedCafe(MFConfig.getInstance().getNormalCouponCafeList());
				normalCouponAdapter.notifyDataSetChanged();
    		
    		} else if (couponType == 1) {
	    		
				//if no internet and no data in File, show retry message
				if (MFConfig.getInstance().getCreditCouponCafeList().size() == 0) {
					displayRetryLayout();
				} else {
		            creditCouponDataTimeStamp = System.currentTimeMillis();
		            PreferenceHelper.savePreferencesLong(getApplicationContext(), "creditCouponTimeStamp", normalCouponDataTimeStamp);
					
				}
				creditCouponAdapter.imageLoader.cleanup();
				creditCouponAdapter.imageLoader.setImagesToLoadFromParsedCafe(MFConfig.getInstance().getCreditCouponCafeList());
				creditCouponAdapter.notifyDataSetChanged();
    			
    		} else if (couponType == 2) {
	    		
				//if no internet and no data in File, show retry message
				if (MFConfig.getInstance().getVipCouponCafeList().size() == 0) {
					displayRetryLayout();
				} else {
		            vipCouponDataTimeStamp = System.currentTimeMillis();
		            PreferenceHelper.savePreferencesLong(getApplicationContext(), "vipCouponTimeStamp", normalCouponDataTimeStamp);
					
				}
				vipCouponAdapter.imageLoader.cleanup();
				vipCouponAdapter.imageLoader.setImagesToLoadFromParsedCafe(MFConfig.getInstance().getVipCouponCafeList());
				vipCouponAdapter.notifyDataSetChanged();
    			
    		}
    	}
    }
    
    private void parseXml(InputStream is, int type) {	
    	MFConfig.tempParsedCafeList.clear();
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			ServerCafeXMLHandler myXMLHandler = new ServerCafeXMLHandler();
//			myXMLHandler.setCouponType(couponType);
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
    		if (type == 0) {
	    		MFConfig.getInstance().getNormalCouponCafeList().clear();
	    		MFConfig.getInstance().getNormalCouponCafeList().addAll(MFConfig.tempParsedCafeList);
    		} else if (type == 1) {
	    		MFConfig.getInstance().getCreditCouponCafeList().clear();
	    		MFConfig.getInstance().getCreditCouponCafeList().addAll(MFConfig.tempParsedCafeList);
    		} else if (type == 2) {
	    		MFConfig.getInstance().getVipCouponCafeList().clear();
	    		MFConfig.getInstance().getVipCouponCafeList().addAll(MFConfig.tempParsedCafeList);
    		}
    	}
		
    }
    
}
