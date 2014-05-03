import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cycon.macaufood.bean.Cafe;


public class PListReformat {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public void parse() throws Exception {
		// TODO Auto-generated method stub
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("cafe.plist");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("cafe_output.xml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(fos, "UTF-8");
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = null;
	    try {
	        builder = builderFactory.newDocumentBuilder();
	    } catch (ParserConfigurationException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
	    }

	    Document document = null;
	    try {
	        document = builder.parse(fis);
	        NodeList nodeList = document.getElementsByTagName("dict");
	        out.write("<cafelist>\n");
	        
	        for (int index = 0; index < nodeList.getLength(); index++) {
	        	out.write("<cafe>\n");
	        	Node node = nodeList.item(index);
	        	
	        	Element e = (Element) node;
	        	NodeList nodeKey = e.getElementsByTagName("key");
	        	NodeList nodeValue = e.getElementsByTagName("string");
	        	NodeList nodeValueInt = e.getElementsByTagName("integer");
	        	
	        	int j = 0, k = 0;
	        	for (int i = 0; i < nodeKey.getLength(); i++) {
	        		final Element eleKey = (Element)nodeKey.item(i);
        		    NodeList nodeKeyValue = eleKey.getChildNodes();
        		    String keyValue = ((Node)nodeKeyValue.item(0)).getNodeValue();
        		    
        		    NodeList nodeStringValue = null;
        		    if (!keyValue.startsWith("option_")) {
        		    	final Element eleString = (Element)nodeValue.item(j++);
        		    	nodeStringValue = eleString.getChildNodes();
        		    } else {
        		    	final Element eleInteger = (Element)nodeValueInt.item(k++);
        		    	nodeStringValue = eleInteger.getChildNodes();
        		    }

        		    if (keyValue.equals("option_confirm")) continue;
        		    if (keyValue.equals("option_photo")) continue;
        		    if (keyValue.equals("timeadded")) continue;
        		    if (keyValue.equals("imageupdatetime")) continue;
        		    if (keyValue.equals("option_recommend")) continue;
        		    if (keyValue.equals("option_menu")) continue;
        		    if (keyValue.equals("option_intro")) continue;
        		    
        		    
        		    
        		    Node strNode = (Node)nodeStringValue.item(0);
        		    String strValue = strNode == null ? "" : strNode.getNodeValue();
        		    
        		    Cafe cafe = new Cafe();
        		    String cafeKey = cafe.getAnyField(keyValue);
        		    if (cafeKey != null && cafeKey.equals(strValue)) continue;
        		    
        		    strValue = strValue.replaceAll("&", "&amp;");
        		    strValue = strValue.replaceAll("<", "&lt;");
        		    strValue = strValue.replaceAll(">", "&gt;");
        		    out.write("<" + keyValue + ">" + strValue + "</" + keyValue + ">" + "\n");
	        	}
	        	
	        	out.write("</cafe>\n");
	        }
	        
	        out.write("</cafelist>");

	        
	    } catch (SAXException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
		
	    
	    
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//
//	 // fetch value from Text Node only
//	 private   String getElementValue(Node elem) {
//	  Node kid;
//	  if (elem != null) {
//	   if (elem.hasChildNodes()) {
//	    for (kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
//	     if (kid.getNodeType() == Node.TEXT_NODE) {
//	      return kid.getNodeValue();
//	     }
//	    }
//	   }
//	  }
//	  return "";
//	 }
//
//	 /// Fetch value from XML Node
//	 private   String getValue(Element item, String str) {
//	  NodeList n = item.getChildNodes();
//	  return getElementValue(n.item(0));
//	 }
	 

	public static void main(String[] args) throws Exception {
		PListReformat reformat = new PListReformat();
		reformat.parse();
	}

}
