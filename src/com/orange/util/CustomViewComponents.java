package com.orange.util;

import java.awt.Dimension;

import javax.swing.JTextField;

import com.orange.ui.component.custom.CSMJTextArea;

public class CustomViewComponents {

	private JTextField fileToValidate;
	private CSMJTextArea queryResult;
	private ConnectionPanel connectionPanel;
	
	public CustomViewComponents(){
		queryResult = new CSMJTextArea();
		fileToValidate = new JTextField();
		
	}
	
	public JTextField getFileToValidate(){
		return fileToValidate;
	}
	public CSMJTextArea getQueryResult(){
		return queryResult;
	}
	public ConnectionPanel loadConnectionPanel(Dimension screenSize,boolean isGold,boolean isCSI, boolean isArchive){
		connectionPanel =  new ConnectionPanel(screenSize,isGold,isCSI,isArchive);
		return connectionPanel;
	}
	public ConnectionPanel getConnetionPane(){
		return connectionPanel;
	}
	
}
