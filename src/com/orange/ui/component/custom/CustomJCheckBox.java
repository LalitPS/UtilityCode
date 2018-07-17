package com.orange.ui.component.custom;

import java.util.Properties;

import javax.swing.JCheckBox;

import com.orange.util.CommonUtils;

public class CustomJCheckBox extends JCheckBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CustomJCheckBox()
	{
		super();
		   Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.setBackground(CustomColorTheme.jcheckBoxColor);
			}
	}
	public CustomJCheckBox(String S){
		super(S);
		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.setBackground(CustomColorTheme.jcheckBoxColor);
			}
	        setToolTipText(S);
	}
	public CustomJCheckBox(String S,boolean b){
		super(S,b);
		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.setBackground(CustomColorTheme.jcheckBoxColor);
			}
	        setToolTipText(S);
	}
}
