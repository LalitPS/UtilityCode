package com.orange.L3.MLAN;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class ServiceBuildFileFormater {

	private ArrayList<String> columns;
	
	private String inputFileName;
	
	/**********************************************************************************
	 * THIS INDEX VALUE NEEDS TO BE CHANGE IF ANY COLUM HAS BEEN ADDED IN ARRAY
	 * 
	 * 
		 * THIS INDEX CAN BE CHANGE AS PER THE REQUIREMENT. THIS IS THE INDEX OF LINEITEM COLUM IN THE FILE.
		 * OR THE COLUMN NUMBER THOSE VALUES SHOULD BE PUT VERTICALLY.
		 * FOT THIS CASE LINEITEM IS THE COLUMN.
	 **********************************************************************************/
	 
	private int lineitemindex = 26;
	
	private String outputFileName;
	
	private Map<ArrayList<String>, ArrayList<ArrayList<String>>> tableData;
	
	public ServiceBuildFileFormater(MLANViewComponents MLANViewComponents) throws Exception {
	
		
		columns  = new ArrayList<String>();
		tableData = new HashMap<ArrayList<String>, ArrayList<ArrayList<String>>>();
	
		inputFileName = MLANViewComponents.getFileToValidate().getText();	
		int index = inputFileName.lastIndexOf(".");
		String sub = inputFileName.substring(0,index) ;
		outputFileName=sub+"_Updated.csv";
	
	
	
		MLANViewComponents.getQueryResult().append("Data Reading Process Start..\n");
		initCSVReader();
		MLANViewComponents.getQueryResult().append("Data Reading Process ends .\n");
		MLANViewComponents.getQueryResult().append("Data Writing Process Start..\n");
		initCSVWriter();
		MLANViewComponents.getQueryResult().append("Data Writing Process ends .\n");
		MLANViewComponents.getQueryResult().append("Process completed successfully...\n");
		MLANViewComponents.getQueryResult().append("New file created ..."+outputFileName+"\n");
		CommonUtils.showOnTable(outputFileName);
		
	}
	
	private void addMapData(ArrayList<String> key, ArrayList<String> data) {
		/*System.out.println("KEY>>>");
		for(String S : key){
			System.out.print(S+">");
		}
		System.out.println("END KEY>>>");
		*/
		
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
	
	private String[] filterCols(String[] row){
		if(null != row && row.length>0)
		{
		String updatedCols[] = new String[row.length-3];
		int y =0;
		for(int x = 0 ; x< row.length; x++)
		{
			/**************************************************************************
			 * THESE ARE THE COLUMN WHICH WILL BE SKIP FROM THE FORMATED FILE
			 * VALUE,EXIST_CONFIG AND NEW_CONFIG AT THIS TIME ARE SKIP 
			 * 
			 **************************************************************************/
			
			if(x == (lineitemindex+1) || x ==(lineitemindex+2) || x == (lineitemindex+3) ){
				
			}
			else{
				updatedCols[y] = row[x];
				y++;
			}
		}
		return updatedCols;
		}
		
		else{
			return row;
		}
		
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
	
	
	

	private String getPrepareText(String one,String two,String three){
		String T="";
		if (null != one && one.length()!=0){
			T+=one;
		}
		if (null != two && two.length()!=0){
			T+="EXIST_CONFIG << "+two+" >>";
		}
		if (null != three && three.length()!=0){
			T+="NEW_CONFIG << "+three+" >>";
		}
		return T;
	}
	
	
	private void initCSVReader() throws Exception{
		
		
		CSVReader br = new CSVReader(new FileReader(inputFileName));
	
		int LINEITEMINDEX =lineitemindex;
		
		
		String []cols = br.readNext();
	
		cols = filterCols(cols);
	
		
		for(String col : cols)
		{
			columns.add(col);
		}
		while ((cols = filterCols(br.readNext())) != null) 
		{
			
			ArrayList<String>  key = new ArrayList<String>();
			/******************************************************************************************************
			 * PLEASE NOTE : >> 
			 * 
			 * THE LINEITEM AND CONSIDERABLE VALUE SHOULD NOT BE INCLUDED IN KEY. 
			 * OTHERWISE 2 ROW WILL BE CERATED 
			 * EX: ONCE FOR CPE USID AND ANOTHER FOR ROUTER USID.
			 * 
			 * HENCE IN BELOW FOR LOOP COL.LEMGTH-2 USED , TO SKIP LINEITEM AND CONSIDERABLE VALUE FROM KEY.
			 ********************************************************************************************************/
			for(int x = 0 ; x< cols.length-2;x++)
			{
				key.add(cols[x]);
			}
			
			if(!columns.contains(cols[LINEITEMINDEX]))
			{
				columns.add(cols[LINEITEMINDEX]);
			}
			
			ArrayList<String> data = new ArrayList<String>();
			
			for(int x=LINEITEMINDEX;x<cols.length;x++)	
			{
				data.add(cols[x]);	
			}
			data.add(""+getIndexOfColumn(cols[LINEITEMINDEX]));
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
				String Internallabel = row.get(0);
				String value= row.get(1);
				//String ExistConfig= row.get(2);
				//String NewConfig= row.get(3);
				
				String InternallabelIndex = row.get(2);
				
			
			
				csvRow[Integer.parseInt(InternallabelIndex)] = getPrepareText(value,null,null);
			
				
				
			}
			writer.writeNext(csvRow);
			
		}
		writer.close();
	}
}
