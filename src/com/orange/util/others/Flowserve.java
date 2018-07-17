package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class Flowserve {
	
	public static void main(String ad[]) throws IOException{
		
		String jagJitfile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/Jagjit.csv";
		String dataBaseFile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/Flowserv_DataBase.csv";
		String orderWithStatusFile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/FlowserveOdrersStatus.csv";
		String orderWithUpdatedStatusFile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/FlowserveOdrersUpdatedStatus.csv";
		Flowserve fs;
		
			fs = new Flowserve(jagJitfile);
			fs.getOrdersStatus(dataBaseFile,orderWithStatusFile);
			fs.updateOrdersStatusWithFixed(orderWithStatusFile,orderWithUpdatedStatusFile);
			System.out.println("Completed");
		
	}

	ArrayList<String[]> csvFileData = new ArrayList<String[]>(5000);
	
	ArrayList<String[]> filterOrder = new ArrayList<String[]>(5000);
	LinkedHashMap<String, ArrayList<String>> fixedstatus = new LinkedHashMap<String, ArrayList<String>>();
	ArrayList<String[]> koOrder = new ArrayList<String[]>(4000);
	ArrayList<String[]> okOrder = new ArrayList<String[]>(4000);
	
	LinkedHashMap<String, ArrayList<String>> status = new LinkedHashMap<String, ArrayList<String>>();
	
	public Flowserve( String filterDataFile) throws IOException{
		
		CSVReader filterReader = new CSVReader(new FileReader(filterDataFile));
		
		String[] row = null;
		row = filterReader.readNext();
		int x=1;
		while ((row = filterReader.readNext()) != null) {
			x++;
		   if(!row[9].contains("Fixed"))	
		   {   
			if(!status.containsKey(row[9])){
				ArrayList<String> list = new ArrayList<String>();
				list.add(row[0]);
				System.out.println(row[9]);
				status.put(row[9],list);
			}
			else if(status.containsKey(row[9])){
				ArrayList<String> list = status.get(row[9]);
				list.add(row[0]);
				status.put(row[9],list);
			}
		   }
		   
		   if(row[9].contains("Fixed"))	
		   {   
			if(!fixedstatus.containsKey(row[9])){
				ArrayList<String> list = new ArrayList<String>();
				list.add(row[0]);
				fixedstatus.put(row[9],list);
			}
			else if(fixedstatus.containsKey(row[9])){
				ArrayList<String> list = fixedstatus.get(row[9]);
				list.add(row[0]);
				fixedstatus.put(row[9],list);
			}
		   }
		   
		}
		filterReader.close();
		System.out.println("Jagjit file Total Count" +x);
	}
	
	
	public void getOrdersStatus(String dataBaseFile,String outputFile) throws IOException{
		CSVReader baseFileReader = new CSVReader(new FileReader(dataBaseFile));
		CustomCSVWriter statusFileWriter = new CustomCSVWriter(new FileWriter(outputFile),true);
		String[] row = null;
		row = baseFileReader.readNext();
		statusFileWriter.writeNext(row);
		boolean hasFound = false;
		int count =1;
		while ((row = baseFileReader.readNext()) != null) 
		{
			count++;
			
			Iterator<Map.Entry<String, ArrayList<String>>> entries = status	.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<String, ArrayList<String>> entry = entries.next();
				String key = entry.getKey();
				ArrayList<String> values = entry.getValue();
				for(String order : values)
				{
					if(row[0].equals(order))
					{
						String[] newrow = new String[row.length];
						for(int x = 0 ; x< row.length ; x++){
							newrow[x] = row[x];
						}
						newrow[row.length-1] = key;
						statusFileWriter.writeNext(newrow);
						hasFound = true;
						break;
					}
					
					
				}
			}
		if(!hasFound){
			String[] newrow = new String[row.length];
			for(int x = 0 ; x< row.length ; x++){
				newrow[x] = row[x];
			}
			newrow[row.length-1] = "Not Found";
			statusFileWriter.writeNext(newrow);
		}
		hasFound = false;	
		}
		baseFileReader.close();
		System.out.println("Database file Count " +count);
		statusFileWriter.close();
	}
	
	
	public void updateOrdersStatusWithFixed(String prevOutputFile,String outputFile) throws IOException{
		CSVReader baseFileReader = new CSVReader(new FileReader(prevOutputFile));
		CustomCSVWriter statusFileWriter = new CustomCSVWriter(new FileWriter(outputFile),true);
		String[] row = null;
		row = baseFileReader.readNext();
		statusFileWriter.writeNext(row);
		boolean hasFound = false;
		while ((row = baseFileReader.readNext()) != null) 
		{
			
			Iterator<Map.Entry<String, ArrayList<String>>> entries = fixedstatus	.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<String, ArrayList<String>> entry = entries.next();
				String key = entry.getKey();
				ArrayList<String> values = entry.getValue();
				for(String order : values)
				{
					if(row[0].equals(order))
					{
						String[] newrow = new String[row.length];
						for(int x = 0 ; x< row.length ; x++){
							newrow[x] = row[x];
						}
						newrow[row.length-1] = key+"::"+row[8];
						statusFileWriter.writeNext(newrow);
						hasFound = true;
						break;
					}
					
					
				}
			}
		if(!hasFound){
			String[] newrow = new String[row.length];
			for(int x = 0 ; x< row.length ; x++){
				newrow[x] = row[x];
			}
			newrow[row.length-1] = row[8];
			statusFileWriter.writeNext(newrow);
		}
		hasFound = false;	
		}
		baseFileReader.close();
		statusFileWriter.close();
	}
}
