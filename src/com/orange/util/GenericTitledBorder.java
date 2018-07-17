package com.orange.util;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class GenericTitledBorder extends TitledBorder{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GenericTitledBorder(String titleOption){
		super(titleOption);
		setTitleJustification(TitledBorder.LEFT);
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		
		Border border = BorderFactory.createCompoundBorder(raisedbevel,loweredbevel);
		setBorder(border);
	}
}
