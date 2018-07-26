package com.orange.ui.component.custom;

import java.util.Properties;

import javax.swing.JPanel;

import com.orange.util.CommonUtils;

public class CustomJPanel  extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomJPanel(){
		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        		this.setBackground(CustomColorTheme.jPanelColor);
			}
	}
}
