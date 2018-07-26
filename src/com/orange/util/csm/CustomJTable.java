package com.orange.util.csm;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.orange.ui.component.custom.Directories;
import com.orange.util.CommonUtils;

public class CustomJTable  {
	private int arr[];
	private ArrayList<String[]> csvData;
	private ArrayList<Integer> csvIndexes;
	private JTable jtable;
	private ArrayList<Integer> tableIndexes;
	private DedupFileValidator validator;
	
	public CustomJTable()  {
		jtable = new JTable();
		
		UIManager.put("Table.alternateRowColor", Color.decode("#F4F4F4"));
		jtable.setFillsViewportHeight(true);
		
		jtable.setAutoCreateRowSorter(true);
		jtable.setGridColor(Color.ORANGE);
		try
		{
			Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
			if(properties.getProperty("TableScroll").equalsIgnoreCase("YES"))
			{
				jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	
			}
			else
			{
			jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			}
		}catch(Exception e){}
		
		jtable.addMouseListener(new MouseAdapter() {
		      public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		        	CommonUtils.setTablePopup(getCustomJTable()).show((JComponent) e.getSource(), e.getX(), e.getY());
		        }
		      }
		    });
		
	
		}

	public CustomJTable(DedupFileValidator validator,ArrayList<Integer> csvIndexes, ArrayList<Integer> tableIndexes)

	{
		jtable = new JTable();
		jtable.setGridColor(Color.ORANGE);
		
		this.validator = validator;
		this.csvIndexes = csvIndexes;
		this.tableIndexes = tableIndexes;
		this.csvData = this.validator.getCsvFileData();
		
		jtable.setAutoCreateRowSorter(true);
		jtable.setGridColor(Color.ORANGE);
		Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
		if(properties.getProperty("TableScroll").equalsIgnoreCase("YES"))
		{
			jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	
		}
		else
		{
		jtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}
		jtable.addMouseListener(new MouseAdapter() {
		      public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		        	CommonUtils.setTablePopup(getCustomJTable()).show((JComponent) e.getSource(), e.getX(), e.getY());
		        }
		      }
		    });
		
	}
	
	public void decorateRows() {
		int rows = getTable().getRowCount();
		int columns = getTable().getColumnCount();
		if (columns > 1 && rows > 1) {
			if (null != validator) {

				for (int row = 0; row < rows; row++) {
					String colsStatusValue ="";
					String matchWith = "";
					for (Integer i : tableIndexes) {
						matchWith += (String) getTable().getModel().getValueAt(row, i)
								+ "@";
						colsStatusValue = (String)getTable().getModel().getValueAt(row, 1);
					}
					boolean isMatch = false;
					for (String[] csvRow : csvData) {
						String matchFrom = "";
						for (Integer i : csvIndexes) {
							String coldata = csvRow[i].trim();
							if (coldata.trim().equalsIgnoreCase("True")) {
								coldata = "0";
							}
							if (coldata.trim().equalsIgnoreCase("False")) {
								coldata = "3";
							}
							matchFrom += coldata + "@";
						}
						if (matchWith.trim().equalsIgnoreCase(matchFrom.trim())) {
							isMatch = true;
						}
					}
					if (isMatch) {
						getTable().getModel().setValueAt("Pass("+colsStatusValue+")", row, 1);
					} else {
						getTable().getModel().setValueAt("Failed("+colsStatusValue+")", row, 1);
					}
					
					colsStatusValue = "";
					isMatch = false;
				}// end of for
			}// end of if validator
		}
	}
	public CustomJTable getCustomJTable(){
		return this;
	}
	
	public int[] getHideColumnsIndex(){
		return arr;
	}
	public JTable getTable(){
		return jtable;
	}
	public void hideColumns(int arr1[]){
		arr = arr1;
		if(null == arr || arr.length==0){
			return;
		}
		for(int  x= 0 ; x< arr.length;x++)
		{
			jtable.getColumnModel().getColumn(arr[x]).setMinWidth(0);
			jtable.getColumnModel().getColumn(arr[x]).setMaxWidth(0);
			jtable.getColumnModel().getColumn(arr[x]).setWidth(0);
			jtable.doLayout();
		}
		
	}
	
	public void setModel(DefaultTableModel model){
		getTable().setModel(model);
	}
	
	

	public void showColumns(int arr1[]){
		arr = arr1;
		if(null == arr || arr.length==0){
			return;
		}
		for(int  x= 0 ; x< arr.length;x++){
			jtable.getColumnModel().getColumn(arr[x]).setMinWidth(70);
			jtable.getColumnModel().getColumn(arr[x]).setMaxWidth(170);
			jtable.getColumnModel().getColumn(arr[x]).setWidth(70);
			jtable.doLayout();
		}
		
	}
}