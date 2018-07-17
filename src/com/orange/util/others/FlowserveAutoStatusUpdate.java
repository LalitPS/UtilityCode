package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class FlowserveAutoStatusUpdate {
	
	public static void main(String ad[]) throws IOException{
		
		String statusFile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/MyStatusFile.csv";
		String baseFile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/MyBaseFile.csv";
		String updatedBaseFile = "C:/Lalit/Gold-Assignment/CSM/Flowserve/31May/MyBaseFileUpdated.csv";
		FlowserveAutoStatusUpdate fs;
	
			fs = new FlowserveAutoStatusUpdate(statusFile);
			fs.cacheMainFile(baseFile,updatedBaseFile);
			System.out.println("Completed");
		
	}
	
	LinkedHashMap<String, String> duplicate = new LinkedHashMap<String, String>();
	
	LinkedHashMap<String, String[]> status = new LinkedHashMap<String, String[]>();
	

	
	public FlowserveAutoStatusUpdate(String filterDataFile) throws IOException{
		
		CSVReader filterReader = new CSVReader(new FileReader(filterDataFile));
		String[] row = null;
		row = filterReader.readNext();
		boolean duplicate = false;
		while ((row = filterReader.readNext()) != null) {
			String arr[] = new String[2];
			arr[0] = row [1];
			arr[1] = row [2];
			if(status.containsKey(row[0])){
				System.out.println("Duplicate value found ... " +row[0]);
				duplicate = true;
				
			}
			status.put(row[0], arr);
			
			
		}
		filterReader.close();
		if(duplicate){
			System.exit(0);
		}
		System.out.println("Key created successfully...");
	}
	private void cacheMainFile(String mainFileReaderPath,String mainFileWriterPath) throws IOException{
		CSVReader fileReader = new CSVReader(new FileReader(mainFileReaderPath));
		CustomCSVWriter fileWriter = new CustomCSVWriter(new FileWriter(mainFileWriterPath),true);
		String[] row = null;
		row = fileReader.readNext();
		fileWriter.writeNext(row);
		boolean hasFound  = false; 
		
		while ((row = fileReader.readNext()) != null) {
		
			String order = row[0];
			String checkBY = row[10];
			if(duplicate.containsKey(order)){
			System.out.println("Warning ..... Duplicate Order found ! "+order);
			}
			duplicate.put(order, row[10]);
			
			
			Iterator<Map.Entry<String, String[]>> entries = status.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<String, String[]> entry = entries.next();
				String key = entry.getKey();
				
				if(key.equals(order)){
			
					String[] comments = status.get(key);
					row[12] = comments[0];
					
					if(checkBY == null || checkBY.isEmpty() || checkBY.length()==0)
					{
						String[] arr = status.get(key);
						row[10] = arr[1];
					}
					SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
					Date resultdate = new Date();
					row[13] = date_format.format(resultdate);
					
					fileWriter.writeNext(row);
					hasFound = true;
					break;
				}
			
				
			}
			
			if(!hasFound){
				fileWriter.writeNext(row);
				
			}
			hasFound = false;
		}
		fileReader.close();
		
		Iterator<Map.Entry<String, String[]>> entries = status.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, String[]> entry = entries.next();
			String key = entry.getKey();
			
			if(!duplicate.containsKey(key)){
				String arr[] = new String[14];
				String[] comments = status.get(key);
				arr[0] = key;
				arr[12] = comments[0];
				arr[10] = comments[1];
				
				for(int x = 1 ; x< 10 ; x++){
					arr[x] = "AUTO";
				}
				SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
				Date resultdate = new Date();
				arr[13] = date_format.format(resultdate);
				fileWriter.writeNext(arr);
			}
		}
		
		
		
		
		fileWriter.close();
	}
}
