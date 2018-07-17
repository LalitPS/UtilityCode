package com.orange.util;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.Icons;

public class MultiLinerPanel 
{
    private static CustomJFrame jframe ;
    private static JScrollPane      scrollPane;
    private static CustomJPanel panelList;
    private static ArrayList<CustomJCheckBox> listOfCB;
    
	static{
		 listOfCB = new ArrayList<CustomJCheckBox>();
		 jframe = new CustomJFrame("Auto Analysis Selection",Icons.leafIcon);
		 panelList = new CustomJPanel();
		 panelList.setLayout(new BoxLayout(panelList, BoxLayout.Y_AXIS));
		 scrollPane = new JScrollPane(panelList);
		 jframe.getContentPane().add(scrollPane,BorderLayout.CENTER);
		 jframe.setBounds(150, 150,600,400);
	}
   
	 public static void addElement(final String label )
	 {
		 CustomJCheckBox box = new CustomJCheckBox(label,true);
		 listOfCB.add(box);
		 panelList.add(box);
	 }
	 
	 
public static CustomJFrame getJframe() {
		return jframe;
	}

	
	

public static ArrayList<File>  getListOfFile() {
	ArrayList<File> selectBoxList = new ArrayList<File>();
	for(CustomJCheckBox box : listOfCB)
	{
		if(box.isSelected())
				{
					selectBoxList.add(new File(box.getText()));
				}
	}
	return selectBoxList;
}


public static CustomJCheckBox  getCheckBox(String text) {

	for(CustomJCheckBox box : listOfCB)
	{
		if(box.getText().equals(text))
		{
			return box;
		}
	}
	return null;
	
}
}
	 
