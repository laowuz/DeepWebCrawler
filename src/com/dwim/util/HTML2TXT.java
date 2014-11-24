package com.dwim.util;


import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


public class HTML2TXT {

  
	public static NodeFilter textFilter = new NodeClassFilter(TextNode.class);
	
	//textFilter = new AndFilter(new NotFilter(new TagNameFilter("script")),new NodeClassFilter(TextNode.class));



	public static String extract(String htmlContent) throws ParserException {
		htmlContent = htmlContent.replaceAll("(?s)(?i)<\\s*script[^>]*>.*?<\\s*/\\s*script>", " ");
		htmlContent = htmlContent.replaceAll("(?s)(?i)<\\s*style[^>]*>.*?<\\s*/\\s*style>", " ");
		Page page = new Page(htmlContent);
		Lexer lexer = new Lexer(page);
		Parser parser = new Parser(lexer);
		
		NodeList list = parser.extractAllNodesThatMatch(textFilter);
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			Node n = list.elementAt(i);
			if(n instanceof TextNode && n.getText().trim().length() > 0)
				result.append(n.getText());
		}
		//System.out.println(result.toString());
		return result.toString();
	}
	
	
	
	public static void main(String args[]) throws ParserException {
		HTML2TXT.extract(HTMLDocument.read("d:\\test", "1", "gb2312"));
	}
	
	
}
