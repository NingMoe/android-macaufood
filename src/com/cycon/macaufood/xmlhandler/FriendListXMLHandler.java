package com.cycon.macaufood.xmlhandler;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cycon.macaufood.bean.ParsedCafeHolder;
import com.cycon.macaufood.bean.ParsedFriendsHolder;
import com.cycon.macaufood.utilities.MFConfig;


	public class FriendListXMLHandler extends DefaultHandler{

		private ParsedFriendsHolder holder = null;
		private StringBuffer tempValue = new StringBuffer();
		private List<ParsedFriendsHolder> tempParsedList;
		
		public FriendListXMLHandler(List<ParsedFriendsHolder> list) {
			tempParsedList = list;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
			
			if (localName.equals("detail")) {
				holder = new ParsedFriendsHolder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("friends")) {
//				holder = new ParsedFriendsHolder();
//				holder.setId("31");
//				holder.setPicLink("https://graph.facebook.com/686125482/picture");
//				tempParsedList.add(holder);
//				holder = new ParsedFriendsHolder();
//				holder.setId("312");
//				holder.setPicLink("https://graph.facebook.com/686125482/picture");
//				tempParsedList.add(holder);
//				holder = new ParsedFriendsHolder();
//				holder.setId("314");
//				holder.setPicLink("https://graph.facebook.com/686125482/picture");
//				tempParsedList.add(holder);
//				holder = new ParsedFriendsHolder();
//				holder.setId("315");
//				holder.setPicLink("https://graph.facebook.com/686125482/picture");
//				tempParsedList.add(holder);
//				holder = new ParsedFriendsHolder();
//				holder.setId("316");
//				holder.setPicLink("https://graph.facebook.com/686125482/picture");
//				tempParsedList.add(holder);
				return;
			}
			
			if (localName.equals("friend")) {
				tempParsedList.add(holder);
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
				if (localName.equals("detail")) {
					String detailStr = tempValue.toString();
					holder.setDetail(detailStr);
					String[] tokens = detailStr.split("\\|\\|\\|");
					holder.setId(tokens[0]);
					holder.setName(tokens[1]);
					holder.setPicLink(tokens[2]);
					String followed = tokens[3];
					if (followed.equals("1")) holder.setFollowed(true);
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