package com.cycon.macaufood.xmlhandler;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.utilities.MFConfig;


	public class ServerCafeXMLHandler extends DefaultHandler{

		private ParsedCafeHolder cafe = null;
		private StringBuffer tempValue = new StringBuffer();
		private List tempParsedList;
		
		public ServerCafeXMLHandler(List list) {
			tempParsedList = list;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
			
			if (localName.equals("cafe")) {
				cafe = new ParsedCafeHolder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("list")) return;
			
			if (localName.equals("cafe")) {
				tempParsedList.add(cafe);
//				if (couponType == 0) {
//					Config.getInstance().getNormalCouponCafeList().add(cafe);
//				} else if (couponType == 1) {
//					Config.getInstance().getCreditCouponCafeList().add(cafe);
//				} else if (couponType == 2) {
//					Config.getInstance().getVipCouponCafeList().add(cafe);
//				} else {
//					Config.getInstance().getRecommendCafeList().add(cafe);
//				}
			} else {
//					MFLog.e(localName, tempValue.toString());
				if (localName.equals("id")) {
					cafe.setId(tempValue.toString());
				} else if (localName.equals("cafeid")) {
					cafe.setCafeid(tempValue.toString());
				} else if (localName.equals("title")) {
					cafe.setTitle(tempValue.toString());
				} else if (localName.equals("content")) {
					cafe.setContent(tempValue.toString());
				} else if (localName.equals("forward")) {
					cafe.setForward(tempValue.toString());
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
//				MFLog.e("length", new String(ch) + " " + start + " " + length);
//				currentElement = false;
//			}
//		} else {
//			currentValue = "";
//		}

		}

		
		
	}