package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class MigrationRelocationOrdersFormat {

	Map<String,Integer> columnRepCount;
	ArrayList<String> columns;
	
	String inputFileName;
	
	CSVWriter MIGR_REL_UPDATED;
	String outputFileName;
	
	SiteFixedViewComponents siteFixViewComponents;
	Map<ArrayList<String>, ArrayList<ArrayList<String>>> tableData;
	public MigrationRelocationOrdersFormat(SiteFixedViewComponents siteFixViewComponents) throws Exception{
		
		this.siteFixViewComponents = siteFixViewComponents;
		columns  = new ArrayList<String>();
		tableData = new HashMap<ArrayList<String>, ArrayList<ArrayList<String>>>();
	
		inputFileName = siteFixViewComponents.getFileToValidate().getText();	
		int index = inputFileName.lastIndexOf(".");
		String sub = inputFileName.substring(0,index) ;
		outputFileName=sub+"_Updated.csv";
	
		columnRepCount = new HashMap<String, Integer>();
	
		siteFixViewComponents.getQueryResult().append("Data Reading Process Start..\n");
		initCSVReader();
		siteFixViewComponents.getQueryResult().append("Data Reading Process ends .\n");
		siteFixViewComponents.getQueryResult().append("Data Writing Process Start..\n");
		initCSVWriter();
		siteFixViewComponents.getQueryResult().append("Data Writing Process ends .\n");
		siteFixViewComponents.getQueryResult().append("Process completed successfully...\n");
		siteFixViewComponents.getQueryResult().append("New file created ..."+outputFileName+"\n");
		CommonUtils.createConsoleLogFile(siteFixViewComponents);
	}
	
	
	
	private void addMapData(ArrayList<String> key, ArrayList<String> data) {
		if (!tableData.containsKey(key)) 
		{
			ArrayList<ArrayList<String>> initData = new ArrayList<ArrayList<String>>();
			initData.add(data);
			tableData.put(key, initData);
		
		} else 
		{
			ArrayList<ArrayList<String>> existingData = tableData.get(key);
		    existingData.add(data);
			tableData.put(key, existingData);
		}
		key = null; 
		data = null;
	}
	
	
	
	
	private int getIndexOfColumn(String colName){
		int count =0;
		for(String col: columns)
		{
			if(col.equalsIgnoreCase(colName))
			{
				return count;
			}
			count++;
		}
		return count;
	}
	
	
	

	private void initCSVReader() throws Exception{
		CSVReader br = new CSVReader(new FileReader(inputFileName));
		String []cols = br.readNext();
		for(String col : cols)
		{
			columns.add(col);
		}
		
		while ((cols = br.readNext()) != null) 
		{
			String OrderNumber=cols[0];
			String OrderStatus=cols[1];
			String ChangeType=cols[2];
			String Product_ServiceFrom=cols[3];
			String Product_Service_To=cols[4];
			String OriginalSiteCORESiteID=cols[5];
			
			String OriginalSiteAddressID=cols[6];
			String NewSiteCORESiteID=cols[7];
			String NewSiteAddressID=cols[8];
			String SendtoCustforSignatureCmpltd=cols[9];
			String Internallabel=cols[10];
			String Existingconfiguration=cols[11];	
						
			ArrayList<String>  key = new ArrayList<String>();
			key.add(OrderNumber);
			key.add(OrderStatus);
			key.add(ChangeType);
			key.add(Product_ServiceFrom);
			
			
			key.add(Product_Service_To);
			key.add(OriginalSiteCORESiteID);
			key.add(OriginalSiteAddressID);
			key.add(NewSiteCORESiteID);
			key.add(NewSiteAddressID);
			key.add(SendtoCustforSignatureCmpltd);
			
			if(!columns.contains(Internallabel))
			{
			columns.add(Internallabel);
			}
			
			ArrayList<String> data = new ArrayList<String>();
			data.add(Internallabel);
			data.add(Existingconfiguration);
			data.add(""+getIndexOfColumn(Internallabel));
			addMapData(key,data);
		    
			
			
			
			}
		br.close();
	}
	
	
	private void initCSVWriter() throws Exception
	{
		Iterator<Map.Entry<ArrayList<String>, ArrayList<ArrayList<String>>>> entries = tableData.entrySet().iterator();
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(outputFileName),true);
		
		String[] columnNames = new String[columns.size()];
		columnNames = columns.toArray(columnNames);
		writer.writeNext(columnNames);
		int count = 0;
		while (entries.hasNext())
		{
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,(double)tableData.size());
			
			String[] csvRow = new String[columns.size()];
			Map.Entry<ArrayList<String>, ArrayList<ArrayList<String>>> entry = entries.next();
			ArrayList<String> key = entry.getKey();
			for(int x = 0 ;x<key.size();x++)
			{
				csvRow[x] = key.get(x);
			}
			ArrayList<ArrayList<String>> keyRows = entry.getValue();
			
			for(ArrayList<String> row : keyRows)
			{
				//String Internallabel = row.get(0);
				String Existingconfiguration= row.get(1);
				String InternallabelIndex = row.get(2);
				csvRow[Integer.parseInt(InternallabelIndex)] = Existingconfiguration;
				
			}
			writer.writeNext(csvRow);
			
		}
		writer.close();
	}
}