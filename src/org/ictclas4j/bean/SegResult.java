package org.ictclas4j.bean;

import java.util.ArrayList;

public class SegResult {

	private long startTime;

	private String rawContent;// ԭʼ�ִ�����

	private ArrayList<MidResult> mrList;// �м���

	private String finalResult;// ���շִʽ��

	public SegResult(String rawContent) {
		this.rawContent = rawContent;
		startTime = System.currentTimeMillis();
	}

	public String getFinalResult() {
		return finalResult;
	}

	public void setFinalResult(String finalResult) {
		this.finalResult = finalResult;
	}

	public ArrayList<MidResult> getMrList() {
		return mrList;
	}

	public void setMrList(ArrayList<MidResult> mrList) {
		this.mrList = mrList;
	}

	public String getRawContent() {
		return rawContent;
	}

	public void setRawContent(String rawContent) {
		this.rawContent = rawContent;
	}

	public long getSpendTime() {
		return System.currentTimeMillis()-startTime;
	}

	public void addMidResult(MidResult mr) {
		if (mrList == null)
			mrList = new ArrayList<MidResult>();
		if (mr != null)
			mrList.add(mr);
	}
	
	public ArrayList<POSWord> toPOSWord() {
		if(finalResult == null)	return null;
		String[] spliter = this.finalResult.split(" ");
		ArrayList<POSWord> result = new ArrayList<POSWord>(32);
		for(int i = 0 ; i < spliter.length ; i++) {
			int index = spliter[i].lastIndexOf("/");
			if(index < 0 || index >= spliter[i].length()-1)	continue;
			POSWord word = new POSWord();
			word.setContent(spliter[i].substring(0,index));
			word.setPos(spliter[i].substring(index+1,spliter[i].length()));
			result.add(word);
		}
		return result;
		
	}

	public String toHTML() {
		StringBuffer html = new StringBuffer();

		if (rawContent != null) {
			html.append("ԭ�����ݣ�");
			html.append("<table border=\"1\" width=\"100%\"><tr><td width=\"100%\">");
			html.append(rawContent);
			html.append("</td></tr></table>");

			if (mrList != null) {
				for (MidResult mr : mrList) {
					html.append(mr.toHTML());
				}
			}

			if (finalResult != null) {
				html.append("<p>���շִʽ����");
				html.append("<table border=\"1\" width=\"100%\"><tr><td width=\"100%\">");
				html.append("<font color=\"blue\" size=6><b>" + finalResult + "</b></font>");
				html.append("</td></tr></table>");
			}
		}

		return html.toString();

	}
}
