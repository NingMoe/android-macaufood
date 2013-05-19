package com.cycon.macaufood.xmlhandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.sqlite.LocalDbManager;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;


	public class UpdateXMLHandler extends DefaultHandler{

		public static Cafe cafe = null;
		private StringBuffer tempValue = new StringBuffer();
		private String type;
		private String field;
		private String id;
		private String details;
		private Context mContext;

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("list")) {
				MFConfig.updateSuccessfully = true;
				return;
			}
			
			if (localName.equals("r")) {
				doUpdate();
				type = null;
				field = null;
				id = null;
				details = null;
			} else if (localName.equals("t")) {
				type = tempValue.toString();
			} else if (localName.equals("f")) {
				field = tempValue.toString();
			} else if (localName.equals("id")) {
				id = tempValue.toString();
			} else if (localName.equals("d")) {
				details = tempValue.toString();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
				tempValue.append(ch, start, length); // append to buffer

		}
		
		public void setContext(Context context) {
			mContext = context;
		}
		
		private void doUpdate() {
			if (type.equals("u")) {
				if (field.equals("option_confirm") || field.equals("option_photo") || 
						field.equals("timeadded")) {
					return;
				} else if (field.equals("option_menu")) {//set menu id to 0 if option_menu is 0
					if (details.equals("0")) {
						field = "menuid";
					} else {
						return;
					}
				} else if (field.equals("option_recommend")) {
					if (details.equals("0")) {
						field = "recommendid";
					} else {
						return;
					}
				} else if (field.equals("option_intro")) {
					if (details.equals("0")) {
						field = "introid";
					} else {
						return;
					}
				} 
				
				int index = Integer.parseInt(id) - 1;
				try {
					MFConfig.getInstance().getCafeLists().get(index).setAnyField(field, details);
					LocalDbManager.getInstance(mContext.getApplicationContext()).updateCafeField(field, details, id);
				} catch (Exception e) {
//					ETLog.e("Update", "Exception");
					e.printStackTrace();
				}
			} else if (type.equals("a")) {
				Cafe cafe = new Cafe();
				String[] items = details.split("\\|\\|");
				for (String item : items) {
					String[] temp = item.split("=>");
					String fieldStr = temp[0];
					String valueStr = temp.length > 1? temp[1] : "";
					if (fieldStr.equals("coord")) {
						if (!valueStr.equals("")) {
							String[] xy = valueStr.split(",");
							String x = xy[0];
							String y = xy[1];
							cafe.setCoordx(x);
							cafe.setCoordy(y);
						}
					} else {
						try {
							cafe.setAnyField(fieldStr, valueStr);
						} catch (Exception e) {
	//						ETLog.e("Update", "Exception");
							e.printStackTrace();
						}
					}
				}
				if (Integer.parseInt(cafe.getId()) == MFConfig.getInstance().getCafeLists().size() + 1) { 
					MFConfig.getInstance().getCafeLists().add(cafe);
					LocalDbManager.getInstance(mContext.getApplicationContext()).insertCafe(cafe);
				}

				
			} else if (type.equals("d")) {
				int index = Integer.parseInt(id) - 1;
				try {
					MFConfig.getInstance().getCafeLists().get(index).setStatus("0");
					LocalDbManager.getInstance(mContext.getApplicationContext()).updateCafeField("status", "0", id);
				} catch (Exception e) {
//					ETLog.e("Update", "Exception");
					e.printStackTrace();
				}
			} else if (type.equals("i")) {
				int index = Integer.parseInt(id) - 1;
				FileCache fileCache = new FileCache(mContext, ImageType.REGULAR);
				fileCache.clearFile(id);
				
				fileCache = new FileCache(mContext, ImageType.INFO);
				try {
					String infoid = MFConfig.getInstance().getCafeLists().get(index).getRecommendid();
					File f=fileCache.getFile(infoid + "-page");
					FileInputStream fis = new FileInputStream(f);
					BufferedReader rd = new BufferedReader(new InputStreamReader(fis
							));
					String pageStr = rd.readLine().trim();
					int pages = Integer.parseInt(pageStr);
					
					for (int i = 0; i < pages; i++) {
						fileCache.clearFile(infoid + "-" + i + "-image");
					}
				} catch (Exception e) {
					
				}
				fileCache = new FileCache(mContext, ImageType.INTRO);
				try {
					String introid = MFConfig.getInstance().getCafeLists().get(index).getIntroid();
					File f=fileCache.getFile(introid + "-page");
					FileInputStream fis = new FileInputStream(f);
					BufferedReader rd = new BufferedReader(new InputStreamReader(fis
							));
					String pageStr = rd.readLine().trim();
					int pages = Integer.parseInt(pageStr);
					
					for (int i = 0; i < pages; i++) {
						fileCache.clearFile(introid + "-" + i + "-image");
					}
				} catch (Exception e) {
					
				}
				
			}
			
			
		}
		
	}