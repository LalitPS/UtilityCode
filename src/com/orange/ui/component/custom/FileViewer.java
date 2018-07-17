package com.orange.ui.component.custom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CommonUtils;
import com.orange.util.JTabbedPaneCloseButton;
import com.orange.util.csm.CustomJTable;

public class FileViewer {

	JTabbedPaneCloseButton tabbedPane;

	public FileViewer() {
		tabbedPane = new JTabbedPaneCloseButton();
	}

	public JTabbedPaneCloseButton addTab(String fileName, String filePath) throws IOException {
		
		int LI = fileName.lastIndexOf(".");
		String ext = fileName.substring(LI+1);
		//JOptionPane.showMessageDialog(null, "ext is "+ext +" >> "+filePath+"  >> "+fileName);
		ImageIcon imageIcon = new ImageIcon("");
		if(ext.equalsIgnoreCase("csv") || ext.contains("prop")|| ext.equalsIgnoreCase("txt"))
				{		
						CustomJTable table = new CustomJTable();
						table.getTable().setAutoCreateRowSorter(true);
						DefaultTableModel model = new DefaultTableModel(0, 0);
						table.setModel(model);
						JScrollPane tableContainer = new JScrollPane(table.getTable());
						
						readData(model, filePath);
				
						tabbedPane.addTab(fileName+" ("+table.getTable().getRowCount()+")", imageIcon, tableContainer, filePath);
				}
		else if(ext.contains("htm") )
		  {
			 
			  	BufferedReader br = new BufferedReader(new FileReader(filePath));
	            int count = 0;
	            String sCurrentLine="";
	            StringBuilder SB = new StringBuilder();
	            while ((sCurrentLine = br.readLine()) != null) 
				{
	            	SB.append(sCurrentLine);
	            	count++;
				}
	            br.close();
	        
	            JScrollPane scroll = CommonUtils.getEditorPane(SB.toString());
	            tabbedPane.addTab(fileName+" ("+count+")", imageIcon, scroll, filePath);
	            SB = null;
	            sCurrentLine = null;
	            count = 0;
		  }
		else{
			JOptionPane.showMessageDialog(new JFrame(), ext + " format is not supported." );
		}
	
		return tabbedPane;
	}

	public JTabbedPaneCloseButton getTabbedPane1() {
		return tabbedPane;
	}

	private void readData(DefaultTableModel model, String filePath) throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String[] columnHeader = csvReader.readNext();
		model.setColumnIdentifiers(columnHeader);
		while ((columnHeader = csvReader.readNext()) != null) {
			model.addRow(columnHeader);
		}
		csvReader.close();
	}

	public void setTabbedPane(JTabbedPaneCloseButton tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

}
