package com.dwim.wrapper;

import com.dwim.form.analysis.Element;
import com.dwim.form.analysis.IElement;
import com.dwim.wrapper.ISemanticMapping;

public class UnaryMaping implements ISemanticMapping {

	
	
	public IElement map(Object identity) {
		//identity  -> id of the pattern 
		//element   -> infinite element
		int pid = (Integer) identity;
		int eid = -1;
		//manual mapping
		switch(pid) {
			default: eid = 0; break;
		}
		
		return (IElement) Element.getByID(eid);
	}

	
	
}
