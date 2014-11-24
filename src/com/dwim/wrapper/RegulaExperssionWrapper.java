package com.dwim.wrapper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.htmlparser.util.ParserException;

import com.dwim.index.SearchResultRecord;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTML2TXT;

public class RegulaExperssionWrapper implements IWrapper{
	private ISemanticMapping mapping;
	private ArrayList<Pattern> patterns;
	private Pattern urlPattern;
	private boolean textonly;
	
	public RegulaExperssionWrapper() throws PatternSyntaxException, DWIMException {
		this(ConfigMan.WRAPPER_ENTRY_REXS, ConfigMan.WRAPPER_SRR_SCRIPT);
	}
	
	
	
	/**
	 * The input patterns of the elements in corresponding order
	 * @param regexs
	 * @throws DWIMException
	 * @throws PatternSyntaxException
	 */
	public RegulaExperssionWrapper(String urlRegex, Object[] regexs) throws DWIMException, PatternSyntaxException {
		patterns = new ArrayList<Pattern>(regexs.length);
		//if(elements == null)	
		//	throw new DWIMException("The elements of the form are unknown.");
		//mapping
		//if( regexs.length != elements.size())
		//	throw new DWIMException("The elements number and the regexs number do not match.");
		if(urlRegex != null) 
			urlPattern = Pattern.compile(urlRegex,Pattern.MULTILINE);
		for(int i = 0 ; i < regexs.length ; i++) {
			if(regexs[i] == null || !(regexs[i] instanceof String))
				throw new DWIMException("the ConfigMan.WRAPPER_SRR_SCRIPT format is not acceptable, regex is expected.");
			patterns.add(i, Pattern.compile((String)regexs[i]));
		}
		textonly = true;
		/*
		for(int i = 0 ; i < elements.size() ; i++) {
			if (elements.get(i) instanceof IElement) {
				patterns.add(Pattern.compile(regexs[i],Pattern.MULTILINE));
			}
		}
		*/
	}
	
	public void setTextOnly(boolean boo) {
		textonly = boo;
	}
	
	public void setSemanticMapping(ISemanticMapping mapping) {
		this.mapping = mapping;
	}
	
	/**
	 * You can use capture group here when you implemented your own wrapper.
	 * By default the first capture group is the result
	 */
	public ArrayList<String> getRecordEntries(String searchResultPage) {
		ArrayList<String> result = new ArrayList<String>(32);
		Matcher macther = urlPattern.matcher(searchResultPage);
		while(macther.find()) {
			result.add(macther.group(1));
		}
		return result;
	}

	public SearchResultRecord extract(String recordPage) throws DWIMException, ParserException {
		if(mapping == null)
			throw new DWIMException ("please first assign a sematic mappping to this wrapper (call setSemanticMapping()).");
		SearchResultRecord srr = new SearchResultRecord();
		for(int i = 0 ; i < patterns.size() ; i++) {
			Matcher m = patterns.get(i).matcher(recordPage);
			if(m.find()) {
				String value = m.group(1);
				if(textonly)	value = HTML2TXT.extract(value);
				srr.loadValue(mapping.map(i), value);
			}

		}
		return srr;
	}
}
