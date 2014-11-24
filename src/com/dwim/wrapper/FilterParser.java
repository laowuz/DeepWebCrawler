package com.dwim.wrapper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;

import com.dwim.index.SearchResultRecord;
import com.dwim.util.DWIMException;

public class FilterParser implements IWrapper{
	private ISemanticMapping mapping;
	private ArrayList<NodeFilter> filters;
	private Pattern urlPattern;
	
	public FilterParser(String urlRegex,Object[] filters) throws DWIMException, PatternSyntaxException {
		this.filters = new ArrayList<NodeFilter>(filters.length);
		//if(elements == null)	
		//	throw new DWIMException("The elements of the form are unknown.");
		//mapping
		//if( regexs.length != elements.size())
		//	throw new DWIMException("The elements number and the regexs number do not match.");
		if(urlRegex != null) 
			urlPattern = Pattern.compile(urlRegex,Pattern.MULTILINE);
		for(int i = 0 ; i < filters.length ; i++) {
			if(filters[i] == null || !(filters[i] instanceof NodeFilter))
				throw new DWIMException("the ConfigMan.WRAPPER_SRR_SCRIPT format is not acceptable, NodeFilter is expected.");
			this.filters.add(i, (NodeFilter)filters[i]);
		}
	}
	public SearchResultRecord extract(String recordPage) throws Exception {
		if(mapping == null)
			throw new DWIMException ("please first assign a sematic mappping to this wrapper (call setSemanticMapping()).");
		
		recordPage = recordPage.replaceAll("<\\s*script[^>]*>[\\s\\r\\n]*.*[\\s\\r\\n]*<\\s*/\\s*script>", " ");
		recordPage = recordPage.replaceAll("<\\s*style[^>]*>[\\s\\r\\n]*.*[\\s\\r\\n]*<\\s*/\\s*style>", " ");
		Page page = new Page(recordPage);
		Lexer lexer = new Lexer(page);
		Parser parser = new Parser(lexer);
		
		SearchResultRecord srr = new SearchResultRecord();
		
		
		for(int i = 0 ; i < filters.size() ; i++) {
			NodeList list = parser.extractAllNodesThatMatch(filters.get(i));
			StringBuffer result = new StringBuffer();
			for (int j = 0; j < list.size(); j++) {
				Node n = list.elementAt(j);
				if(n instanceof TextNode && n.getText().trim().length() > 0)
					result.append(n.getText());
			}
			srr.loadValue(mapping.map(i), result.toString());
		}
		return srr;
	}

	public ArrayList<String> getRecordEntries(String searchResultPage) throws Exception {
		ArrayList<String> result = new ArrayList<String>(32);
		Matcher macther = urlPattern.matcher(searchResultPage);
		while(macther.find()) {
			result.add(macther.group(1));
		}
		return result;
	}
	

}
