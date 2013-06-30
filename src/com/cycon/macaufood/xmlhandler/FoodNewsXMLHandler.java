package com.cycon.macaufood.xmlhandler;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cycon.macaufood.bean.ParsedFoodNewsHolder;
import com.cycon.macaufood.utilities.MFConfig;


	public class FoodNewsXMLHandler extends DefaultHandler{

		public static ParsedFoodNewsHolder cafe = null;
		private StringBuffer tempValue = new StringBuffer();
		private List tempParsedList;
		
		public FoodNewsXMLHandler(List list) {
			tempParsedList = list;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
			tempValue.setLength(0);  //clear buffer
			
			if (localName.equals("articles")) {
				cafe = new ParsedFoodNewsHolder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			
			if (localName.equals("list")) return;
			
			if (localName.equals("articles")) {
				MFConfig.tempParsedFoodNewsList.add(cafe);
			} else {
				if (localName.equals("id")) {
					cafe.setId(tempValue.toString());
				} else if (localName.equals("subject")) {
					cafe.setSubject(tempValue.toString());
				} else if (localName.equals("content")) {
					cafe.setContent(tempValue.toString());
				} else if (localName.equals("timeadded")) {
					cafe.setTimeadded(tempValue.toString());
				} else if (localName.equals("cafeid")) {
					cafe.setCafeId(tempValue.toString());
				} 
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			
			tempValue.append(ch, start, length); // append to buffer

		}

		
		
	}