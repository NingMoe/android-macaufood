package com.cycon.macaufood.utilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cycon.macaufood.bean.Cafe;


	public class CafeXMLHandler extends DefaultHandler{

		public static Cafe cafe = null;
		private StringBuffer tempValue = new StringBuffer();

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
			
			if (localName.equals("cafe")) {
				cafe = new Cafe();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("cafelist")) return;
			
			if (localName.equals("cafe")) {
				Config.getInstance().getCafeLists().add(cafe);
//				LocalDaoManager.getInstance().addCafeEntry(cafe.getId() == null ? "" : cafe.getId(), 
//						cafe.getName() == null ? "" : cafe.getName(), cafe.getAddress() == null ? "" : cafe.getAddress());
//				ETLog.e("ZZZ", "set " + cafe.getId());
			} else {
				try {
//					Log.e(localName, tempValue.toString());
					cafe.setAnyField(localName, tempValue.toString());
				} catch (Exception e) {
					ETLog.e("CafeXMLHandler", "setFieldException " + e.getMessage());
					e.printStackTrace();
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			
//		if (currentElement) {
//			if (length == 0) {
//				currentValue = "";
//			} else {
				tempValue.append(ch, start, length); // append to buffer
//				currentValue = new String(ch, start, length);
//				Log.e("length", new String(ch) + " " + start + " " + length);
//				currentElement = false;
//			}
//		} else {
//			currentValue = "";
//		}

		}
	}