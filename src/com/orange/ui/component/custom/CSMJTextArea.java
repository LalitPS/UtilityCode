package com.orange.ui.component.custom;

import java.awt.Font;
import java.awt.TextArea;
import java.util.ArrayList;

public class CSMJTextArea extends TextArea {

	/**
	 * 
	 */
  private static final long serialVersionUID = 1L;
	
   public CSMJTextArea() {
		setFont(new Font("Verdana", Font.PLAIN, 12));
		
	}

	public void appendListStringBuilder(ArrayList<StringBuilder> list) {
		ArrayList<StringBuilder> queryList = list;
		
		for (StringBuilder queryBuilder : queryList) 
			{
			append(queryBuilder.toString() + "\n");
	    	}
			append("\n");
		
		
	}
	
	
}
