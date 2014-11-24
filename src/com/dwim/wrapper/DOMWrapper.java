package com.dwim.wrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import com.dwim.index.SearchResultRecord;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTMLDocument;

public class DOMWrapper implements IWrapper{
	private Tidy tidy;
	private XPath xpath;
	private ArrayList<XPathExpression> experssions;
	private ISemanticMapping mapping;
	private Pattern urlPattern;
	private DocumentBuilder bulider;
	
	
	
	public DOMWrapper(String urlRegex,Object[] xpathes) throws XPathExpressionException, ParserConfigurationException, DWIMException {
		tidy = new Tidy();
		tidy.setInputEncoding(ConfigMan.HTTP_CONTENT_CODESET);
		tidy.setTidyMark(false);
		tidy.setXmlOut(true);
		
		tidy.setDocType("strict");
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		tidy.setQuiet(true);
		
		XPathFactory xfactory = XPathFactory.newInstance();
		xpath = xfactory.newXPath();
		if(urlRegex != null) 
			urlPattern = Pattern.compile(urlRegex,Pattern.MULTILINE);
		
		experssions = new ArrayList<XPathExpression>(8);
		
		for(int i = 0 ; i < xpathes.length ; i++) {
			if(xpathes[i] == null || !(xpathes[i] instanceof String))
				throw new DWIMException("the ConfigMan.WRAPPER_SRR_SCRIPT format is not acceptable, regex is expected.");
			experssions.add(xpath.compile((String)xpathes[i]));
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		bulider = factory.newDocumentBuilder();
		
		//experssions.add(xpath.compile("/html/body/div[@class='contsec']/center/table/tbody/tr[3]"));
		
	}
	
	

	public SearchResultRecord extract(String recordPage) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
		//store the input HTML document
		StringBuffer buffer = format(recordPage);
		//System.out.println(buffer.toString());
		StringReader reader = new StringReader(buffer.toString());
		File xml = new File(ConfigMan.SAVE_HTML_PATH, "~temp" + new Random().nextInt(100) + ".xml");

		FileWriter writer = new FileWriter(xml);
		
		tidy.parseDOM(reader, writer);		//the parsed XML document is temporarily stored in StringWriter-writer
		reader.close();
		writer.flush();
		writer.close();
		
		
		
		Document doc = bulider.parse(xml);
		Element root = doc.getDocumentElement();
		//System.out.println(root.getTextContent());  

		//locate desire node using XPath
		
		 SearchResultRecord result = new SearchResultRecord();
		 for(int i = 0 ; i < experssions.size() ; i++) {
			 Node tn =  (Node) experssions.get(i).evaluate(root,XPathConstants.NODE);
			 result.loadValue(mapping.map(i), tn.getTextContent());
		 }
		xml.delete();
		return result;
	}
	
	
	public void setSemanticMapping(ISemanticMapping mapping) {
		this.mapping = mapping;
	}
	
	/**
	 * Format the HTML tags to XML
	 * @param buffer
	 * @return
	 */
	protected StringBuffer format(String in) {
		
		in = in.replaceAll("<!DOCTYPE[^>]*>", " ");
		StringBuffer buffer = new StringBuffer(in);
		buffer.insert(0, "<?xml version=\"1.0\" encoding=\"" + ConfigMan.SAVE_HTML_CODESET + "\"?>\r\n");
		for(int i = 0 ; i < buffer.length() ; i++) {
			if(buffer.charAt(i) == '&') {
				if(i <= buffer.length()-6 && buffer.substring(i,i+6).equals("&nbsp;")) {
					buffer.replace(i, i+6, " ");
				}
				if(i <= buffer.length()-5 && !buffer.substring(i,i+5).equals("&amp;")) {
					buffer.replace(i, i+1, "&amp;");
				}
			}
		}
		
		return buffer;
		
	}

	public ArrayList<String> getRecordEntries(String searchResultPage) {
		ArrayList<String> result = new ArrayList<String>(32);
		Matcher macther = urlPattern.matcher(searchResultPage);
		while(macther.find()) {
			result.add(macther.group(1));
		}
		return result;
	}
	
	public static void main(String args[]) throws Exception {
		ConfigMan.SAVE_HTML_CODESET = "utf-8";
		ConfigMan.SAVE_HTML_PATH = "D:\\records\\wikicfp";
			
		DOMWrapper wrapper = new DOMWrapper(null,new String[]{"/html/body/div[@class='contsec']/center/table/tr[2]"});
	
		wrapper.extract(HTMLDocument.read("E:\\","temp","ascii"));
		//wrapper.loadXML("E:\\","temp1.xml");
	}
}
