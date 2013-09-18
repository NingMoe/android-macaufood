package com.cycon.macaufood.xmlhandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cycon.macaufood.utilities.MFLog;

import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.MFConfig;


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
				MFConfig.getInstance().getCafeLists().add(cafe);
			} else {
				try {
					cafe.setAnyField(localName, tempValue.toString());
				} catch (Exception e) {
					MFLog.e("CafeXMLHandler", "setFieldException " + e.getMessage());
					e.printStackTrace();
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			
				tempValue.append(ch, start, length); // append to buffer

		}
	}