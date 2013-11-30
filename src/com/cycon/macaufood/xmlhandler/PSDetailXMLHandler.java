package com.cycon.macaufood.xmlhandler;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cycon.macaufood.bean.ParsedFoodNewsHolder;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.MFConfig;


	public class PSDetailXMLHandler extends DefaultHandler{

		public static ParsedPSHolder holder = null;
		private StringBuffer tempValue = new StringBuffer();
		private List<ParsedPSHolder> tempParsedList;
		
		public PSDetailXMLHandler(List<ParsedPSHolder> list) {
			tempParsedList = list;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
			
			if (localName.equals("photo")) {
				holder = new ParsedPSHolder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("photos")) return;
			
			if (localName.equals("photo")) {
				tempParsedList.add(holder);
			} else {
				if (localName.equals("photoid")) {
					holder.setPhotoid(tempValue.toString());
				} else if (localName.equals("memberid")) {
					holder.setMemberid(tempValue.toString());
				} else if (localName.equals("cafeid")) {
					holder.setCafeid(tempValue.toString());
				} else if (localName.equals("imgwidth")) {
					holder.setImgwidth(tempValue.toString());
				} else if (localName.equals("imgheight")) {
					holder.setImgheight(tempValue.toString());
				} else if (localName.equals("fbid")) {
					holder.setFbid(tempValue.toString());
				} else if (localName.equals("name")) {
					holder.setName(tempValue.toString());
				} else if (localName.equals("logintype")) {
					holder.setLogintype(tempValue.toString());
				} else if (localName.equals("caption")) {
					holder.setCaption(tempValue.toString());
				} else if (localName.equals("place")) {
					holder.setPlace(tempValue.toString());
				} else if (localName.equals("uploaddate")) {
					holder.setUploaddate(tempValue.toString());
				} else if (localName.equals("link")) {
					holder.setLink(tempValue.toString());
				} else if (localName.equals("cafephone")) {
					holder.setCafephone(tempValue.toString());
				} else if (localName.equals("cafeaddress")) {
					holder.setCafeaddress(tempValue.toString());
				} else if (localName.equals("filename")) {
					holder.setFilename(tempValue.toString());
				} else if (localName.equals("numoflike")) {
					holder.setNumoflike(tempValue.toString());
				} else if (localName.equals("likes")) {
					holder.setLikes(tempValue.toString());
				} else if (localName.equals("numofcom")) {
					holder.setNumofcom(tempValue.toString());
				} else if (localName.equals("comments")) {
					holder.setComments(tempValue.toString());
				} 
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			
			tempValue.append(ch, start, length); // append to buffer

		}

		
		
	}