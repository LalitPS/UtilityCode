package com.orange.util.csm;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.orange.ui.component.DedupViewComponents;
import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.util.CommonUtils;

public class DataTableCreator {

	private DedupViewComponents cSMViewComponents;
	private DedupFileDataValidator dataValidator;
	private JTabbedPane tabbedPane;
	private int tabcount = 0 ;
	private CSMJTextArea textArea;

	private DedupFileValidator validator;

	public DataTableCreator(DedupViewComponents cSMViewComponents,JTabbedPane tabbedPane, DedupFileDataValidator dataValidator,DedupFileValidator validator) {
		this.cSMViewComponents = cSMViewComponents;
		this.tabbedPane = tabbedPane;
		this.dataValidator = dataValidator;
		this.validator = validator;
		this.textArea = cSMViewComponents.getQueryResult();
	}

	private JTable createTable(String tabName,	ArrayList<String[]> tableDataRows) {
		String[] header = tableDataRows.get(0);
		CustomJTable table ;
		if(tabName.contains("Post Run Checks"))
		{
			ArrayList<Integer> csvIndexes = new ArrayList<Integer>();
			ArrayList<Integer> tableIndexes = new ArrayList<Integer>();
			
			csvIndexes.add(1);
			csvIndexes.add(22);
			csvIndexes.add(10);
			csvIndexes.add(3);
			csvIndexes.add(4);
			
			tableIndexes.add(1);
			tableIndexes.add(2);
			tableIndexes.add(3);
			tableIndexes.add(4);
			tableIndexes.add(5);
	
			table = new CustomJTable(validator,csvIndexes,tableIndexes);
		}
		else{
			table = new CustomJTable();
		}
	
		DefaultTableModel model = new DefaultTableModel(0, 0);
		model.setColumnIdentifiers(header);
		
		table.setModel(model);
		
		JScrollPane tableContainer = new JScrollPane(table.getTable());
		for (int row = 1; row < tableDataRows.size(); row++) {
			model.addRow(tableDataRows.get(row));
		}
		
		tabbedPane.addTab(tabName,new ImageIcon(CommonUtils.setSizeImage("/resources/Refresh.png", 20, 20)), tableContainer);
		tabbedPane.setToolTipTextAt(tabcount, "Total Row(s) "+table.getTable().getRowCount());
		table.decorateRows();
		tabcount++;
		return table.getTable();
	}
	
public void databaseSQLAddressIDFilterListExecution(
		ArrayList<StringBuilder> stringBuilderQuery,
		ArrayList<String[]> csvFileData, String tabName) {
	
	if (cSMViewComponents.hasDataValidation()) {
		try {
			ArrayList<String[]> tableData = dataValidator.getQueryListResult(stringBuilderQuery);
			ArrayList<String[]> filterTableData = new ArrayList<String[]>();
			for (String[] rowdata : tableData) {

				String ICO = rowdata[0];
				boolean isDataMatch = false;
				for (String[] csvRowData : csvFileData) 
				{
					String ICOFromFile = csvRowData[2];
					
					if ((ICO).equals((ICOFromFile))) 
					{
						isDataMatch = true;
						break;
					}
				}
				if (!isDataMatch) {
					filterTableData.add(rowdata);
				}
			}
			setQueryResultDecorator(filterTableData, tabName);
		} catch (Exception e) {
			cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
			cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
		}
	}
}
	
	public void databaseSQLExecution(StringBuilder stringBuilderQuery,String tabName) {
		if (cSMViewComponents.hasDataValidation()) {
			try {
				ArrayList<String[]> tableData = dataValidator.getQueryResult(validator, stringBuilderQuery);
				setQueryResultDecorator(tableData, tabName);
			} catch (Exception e) {
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
				cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			}
		}
	}

	public void databaseSQLFilterListExecution(ArrayList<String[]> tableData,String tabName, String[] migratedservices) {
		if (cSMViewComponents.hasDataValidation()) {
			try {
				ArrayList<String[]> filterTableData = new ArrayList<String[]>();
				// ADD HEADER
				filterTableData.add(tableData.get(0));
				
				for (String[] rowdata : tableData) 
				{

					String orderservice = (rowdata[4] == null || rowdata[4].isEmpty() || rowdata[4].length() ==0 ? rowdata[3] : rowdata[4]);
				
					if(orderservice != null && !orderservice.isEmpty() && orderservice.length()>0)
					{
						for(int x = 0 ;x< migratedservices.length; x++)
						{
						if (orderservice.equalsIgnoreCase(migratedservices[x])) 
							{
								filterTableData.add(rowdata);
							}
						}
					}
				}
				setQueryResultDecorator(filterTableData, tabName);
				
			} catch (Exception e) {
				e.printStackTrace();
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
				cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			}
		}
	}
	
	public void databaseSQLFilterListExecution(ArrayList<StringBuilder> stringBuilderQuery,ArrayList<String[]> csvFileData, String tabName, int csvIndex1,int csvIndex2,int queryIndex1,int queryIndex2) {
		if (cSMViewComponents.hasDataValidation()) {
			try {
				ArrayList<String[]> tableData = dataValidator.getQueryListResult(stringBuilderQuery);
				ArrayList<String[]> filterTableData = new ArrayList<String[]>();
				for (String[] rowdata : tableData) {

					String ICO = rowdata[queryIndex1];
					String siteCode = rowdata[queryIndex2];
					//siteCode = CSMFileValidator.refineData(siteCode);
					boolean isDataMatch = false;
					for (String[] csvRowData : csvFileData) 
					{
						String ICOFromFile = csvRowData[csvIndex1];
						String siteCodeFromFile = csvRowData[csvIndex2];
						
						if ((ICO + siteCode).equals((ICOFromFile + siteCodeFromFile))) 
						{
							isDataMatch = true;
							break;
						}
					}
					if (!isDataMatch) {
						filterTableData.add(rowdata);
					}
				}
				setQueryResultDecorator(filterTableData, tabName);
			} catch (Exception e) {
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
				cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			}
		}
	}
	
	
	public void databaseSQLFilterListExecutionWithData(ArrayList<String[]> tableData,ArrayList<String[]> csvFileData, String tabName, int csvIndex1,int csvIndex2,int csvIndex3,int queryIndex1,int queryIndex2,int queryIndex3) {
		
		
		if (cSMViewComponents.hasDataValidation()) {
			try {
				//ArrayList<String[]> tableData = dataValidator.getQueryListResult(stringBuilderQuery);
				ArrayList<String[]> filterTableData = new ArrayList<String[]>();
				for (String[] rowdata : tableData) {

					String COL1 = rowdata[queryIndex1];
					String COL2 = rowdata[queryIndex2];
					String COL3 = rowdata[queryIndex3];
					
					
					COL1 = ((null == COL1 || COL1.isEmpty() || COL1.length() == 0) ? "": COL1);
					COL2 = ((null == COL2 || COL2.isEmpty() || COL2.length() == 0) ? "": COL2);
					COL3 = ((null == COL3 || COL3.isEmpty() || COL3.length() == 0) ? "": COL3);
					
					//siteCode = CSMFileValidator.refineData(siteCode);
					boolean isDataMatch = false;
					for (String[] csvRowData : csvFileData) 
					{
						
						String COL1FromFile = csvRowData[csvIndex1];
						String COL2FromFile = csvRowData[csvIndex2];
						String COL3FromFile = csvRowData[csvIndex3];
						
						//System.out.println((COL1 + COL2 + COL3) +" IS EQUAL ?" +(COL1FromFile + COL2FromFile+ COL3FromFile));
						
						
						if ((COL1 + COL2 + COL3).equals((COL1FromFile + COL2FromFile+ COL3FromFile)) || (COL1 + COL2 +COL3)==((COL1FromFile + COL2FromFile +COL3FromFile))) 
						{
							isDataMatch = true;
							break;
						}
					}
					if (!isDataMatch) {
						filterTableData.add(rowdata);
					}
				}
				setQueryResultDecorator(filterTableData, tabName);
			} catch (Exception e) {
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
				cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			}
		}
	}

	public ArrayList<String[]> databaseSQLListExecution(ArrayList<StringBuilder> stringBuilderQuery, String tabName) {
			if (cSMViewComponents.hasDataValidation()) 
			{
				ArrayList<String[]> tableData= null;
			
				try {
					tableData = dataValidator.getQueryListResult(stringBuilderQuery);
							
					setQueryResultDecorator(tableData, tabName);
					
					} catch (Exception e) 
					{
					e.printStackTrace();
					cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
					cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
					cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
					}
					return tableData;
			}
			return null;
		}
	public ArrayList<String[]> databaseSQLListExecutionFromCSI(ArrayList<StringBuilder> stringBuilderQuery, String tabName) {
		if (cSMViewComponents.hasDataValidation()) 
		{
			ArrayList<String[]> tableData = null;
			try 
			{
				tableData = dataValidator.getQueryListResultFromCSI(stringBuilderQuery);
				setQueryResultDecorator(tableData, tabName);
			} catch (Exception e) 
			{
				e.printStackTrace();
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
				cSMViewComponents.getQueryResult().append("Error while executing above query .." + e.getMessage()+ "\n");
				cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			}
			return tableData;
		}
		return null;
	}
	
	
	public void setQueryResultDecorator(ArrayList<String[]> tableData,String tabName) throws IOException {
		if (cSMViewComponents.hasShowSQL()) 
		{
			// Header Info
			cSMViewComponents.getQueryResult().append("--------------------------------------------------\n");
			String[] tableHeader = tableData.get(0);
			for (int headerIndex = 0; headerIndex < tableHeader.length; headerIndex++) 
			{
				textArea.append(tableHeader[headerIndex] + "\t\t");
			}
			cSMViewComponents.getQueryResult().append("\n--------------------------------------------------");
			textArea.append("\n");
			ArrayList<String[]> tableDataRows = tableData;

			// Data Info
			for (int row = 1; row < tableDataRows.size(); row++) 
			{
				String rowData[] = tableDataRows.get(row);
				for (int rowIndex = 0; rowIndex < rowData.length; rowIndex++) 
				{
					textArea.append(rowData[rowIndex] + "\t\t");
				}
				textArea.append("\n");
			}
			cSMViewComponents.getQueryResult().append(
					"\n-------------------------------------\n");
		}

		JTable jtable =createTable(tabName, tableData);
		ExportToCSV export = new ExportToCSV(jtable,tabName,cSMViewComponents);
		
			export.export();
		
	}

	
}
