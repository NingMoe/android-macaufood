package com.cycon.macaufood.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ParsedLatestHolder;

public class Latest extends BaseActivity {
	
	private static final String TAG = "Latest";
	private ListView list;
	private ArrayList<ParsedLatestHolder> contentList = new ArrayList<ParsedLatestHolder>();
	private ProgressDialog pDialog;
//	private LayoutInflater inflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.latest);

		needMenu = false;
//		inflater = LayoutInflater.from(this);
		
		list = (ListView) findViewById(R.id.list);
		
		new FetchXmlTask().execute();
	}
	
	private class LatestAdapter extends BaseAdapter {

		public int getCount() {
			return contentList.size();
		}

		public Object getItem(int arg0) {
			return arg0;
		}

		public long getItemId(int arg0) {
			return arg0;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		public View getView(int position, View convertView, ViewGroup arg2) {
			ParsedLatestHolder holder = contentList.get(position);
        	
            TextView text;
            
            if (convertView == null) {
                text = new TextView(Latest.this);
                text.setTextColor(Color.BLACK);
                text.setTextSize(17f);
                text.setPadding(15, 8, 15, 8);
                text.setShadowLayer(1, 0, 1, Color.parseColor("#999999"));
            } else {
                text = (TextView)convertView;
            }
            
            if (holder.getTimeadded() == null) {
                text.setText(holder.getContent());
            } else {
            	text.setText(holder.getTimeadded() + "\n\n" + holder.getContent());
            }

            return text;
		}
		
	}
	
    public class FetchXmlTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		pDialog = ProgressDialog.show(Latest.this, null,
					"載入資料中...", false, true);
    	}
    	@Override
    	protected Void doInBackground(Void... params) {
    		String urlStr = "http://www.cycon.com.mo/xml_latest.php?key=cafecafe";
            try {
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();

				parseXml(is);
				
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
    		
    		// add no internet msg
			if (contentList.size() == 0) {
				ParsedLatestHolder holder = new ParsedLatestHolder();
				holder.setContent(getString(R.string.noInternetMsg));
				contentList.add(holder);
			}
	    	list.setAdapter(new LatestAdapter());
			
    	}
    }
    
    private void parseXml(InputStream is) {
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			LatestXMLHandler myXMLHandler = new LatestXMLHandler();
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
    }
	
	private class LatestXMLHandler extends DefaultHandler{

		private StringBuffer tempValue = new StringBuffer();
		private ParsedLatestHolder holder;

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
			
			if (localName.equals("news")) {
				holder = new ParsedLatestHolder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("list")) return;
			
			if (localName.equals("news")) {
				contentList.add(holder);
			} else {
				
				if (localName.equals("timeadded")) {
					holder.setTimeadded(tempValue.toString());
				} else if (localName.equals("content")) {
					holder.setContent(tempValue.toString());
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			tempValue.append(ch, start, length); // append to buffer

		}
	}
	
	
	
}
