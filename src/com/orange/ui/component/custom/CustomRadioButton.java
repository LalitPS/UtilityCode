package com.orange.ui.component.custom;

import java.util.Properties;

import javax.swing.JRadioButton;

import com.orange.util.CommonUtils;

public class CustomRadioButton extends JRadioButton{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomRadioButton(){
		super();
		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.setBackground(CustomColorTheme.jradioBoxColor);
			}
		
	}
	public CustomRadioButton(String label){
		super(label);
		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.setBackground(CustomColorTheme.jradioBoxColor);
			}
	}
	
	public CustomRadioButton(String label,boolean isDefault){
		super(label,isDefault);
		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.setBackground(CustomColorTheme.jradioBoxColor);
			}
	}
}
