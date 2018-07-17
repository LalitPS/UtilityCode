package com.orange.ui.component.custom;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Properties;

import javax.swing.JFrame;

import com.orange.util.CommonUtils;

public class CustomJFrame  extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CustomJFrame(){
		super();
		 	Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
			{
	        	this.getContentPane().setBackground( CustomColorTheme.frameColor );
			}
		
	}
	public CustomJFrame(String S){
		super(S);
		Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
		{
        	this.getContentPane().setBackground( CustomColorTheme.frameColor );
		}
	}
	public CustomJFrame(String S,String iconPath){
		super(S);
		this.setIconImage(CommonUtils.setSizeImage(iconPath, 35, 35));
		
		Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
		{
        	this.getContentPane().setBackground( CustomColorTheme.frameColor );
		}
		
	}
	
	
}
