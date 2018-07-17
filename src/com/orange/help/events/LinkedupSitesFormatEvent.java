package com.orange.help.events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import com.orange.util.CommonUtils;

public class LinkedupSitesFormatEvent implements ActionListener{

	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			
			URL url = LinkedupSitesFormatEvent.class.getResource(FormatsLoc.linkedupSitesFormatFile);
			CommonUtils.showOnTable(url.getPath());
		
		
		
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,e+"\nPlease unzip the jar file and retry.");
		}
		
	}

	
	
}
