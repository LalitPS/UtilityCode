package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class SitesLinkedtoMICOs {

	public static void main(String ad[]){
		String readFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\LinkedUpSites\\sites_linked_to_multiple_icos_7th_SEP.csv";
		String writeFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\LinkedUpSites\\sites_linked_to_multiple_icos_7th_SEP_COPY.csv";
		try {
			new SitesLinkedtoMICOs(readFilePath,writeFilePath);
		} catch (IOException e) {
		
			JOptionPane.showMessageDialog(null,e);
		}
		
	}
	CustomCSVWriter csvwriter ;
	Map<ArrayList<String>,ArrayList<ArrayList<String>>> fileData;
	
	public SitesLinkedtoMICOs(String firstfilePath,String writeFilePath) throws IOException{
		
		CSVReader first = new CSVReader(new FileReader(firstfilePath));
		csvwriter = new CustomCSVWriter(new FileWriter(writeFilePath),true);
		fileData = new HashMap<ArrayList<String>,ArrayList<ArrayList<String>>>();
		String row[] = null;
		csvwriter.writeNext(first.readNext());
		while ((row = first.readNext()) != null) 
		{
			
			String ordernumber =row[0];
			String status =row[1];
			String siteid =row[2];
			String address_id =row[3];
			String osr =row[4];
			String sitecode =row[5];
			String ico =row[6];
			String ico1 =row[7];
			
			ArrayList<String> key = new ArrayList<String>();
			key.add(siteid);
			key.add(address_id);
			key.add(osr);
			
			if(!fileData.containsKey(key))
			{
				
				ArrayList<ArrayList<String>> rowdata = new ArrayList<ArrayList<String>>();
				
				ArrayList<String> ordernumbers = new ArrayList<String>();
				ordernumbers.add(ordernumber+"|");
				
				ArrayList<String> statuses = new ArrayList<String>();
				statuses.add(status+"|");
				
				ArrayList<String> sitecodes = new ArrayList<String>();
				sitecodes.add(sitecode+"|");
				
				ArrayList<String> icos = new ArrayList<String>();
				icos.add(ico+"|");
				icos.add(ico1+"|");
				
				rowdata.add(ordernumbers);
				rowdata.add(statuses);
				rowdata.add(sitecodes);
				rowdata.add(icos);
				
				fileData.put(key,rowdata);
				
			}
			else{
				
				ArrayList<ArrayList<String>> rowdata = fileData.get(key);
				
				ArrayList<String> ordernumbers = rowdata.get(0);
				if(!ordernumbers.contains(ordernumber+"|")){
					ordernumbers.add(ordernumber+"|");
				}
				
				ArrayList<String> statuses = rowdata.get(1);
				
				if(!statuses.contains(status+"|")){
					
					statuses.add(status+"|");
				}
				
				ArrayList<String> sitecodes = rowdata.get(2);
				if(!sitecodes.contains(sitecode+"|")){
					sitecodes.add(sitecode+"|");
				}
				
				ArrayList<String> icos = rowdata.get(3);
				if(!icos.contains(ico+"|")){
					icos.add(ico+"|");
				}
				if(!icos.contains(ico1+"|")){
					icos.add(ico1+"|");
				}
				
				rowdata.add(ordernumbers);
				rowdata.add(statuses);
				rowdata.add(sitecodes);
				rowdata.add(icos);
				
				fileData.put(key,rowdata);
			}// end of else
			
			
		}// end of while
		first.close();
		writeCSV(writeFilePath);
	}// end of cons
	
	private String getArrayValue(ArrayList<String> icos){
		String ICO="";
		for(String ico :icos){
			ICO+=ico;
		}
		return ICO;
	}
	private void writeCSV(String writeFilePath) throws IOException{
		
		
		Set mapSet = (Set) fileData.entrySet();
		Iterator mapIterator = mapSet.iterator();
		
		
		while(mapIterator.hasNext()){
			//ArrayList<ArrayList<String>> row = mapIterator.next();
			
			Map.Entry mapEntry = (Map.Entry) mapIterator.next();
			ArrayList<String> keyValue = (ArrayList<String>) mapEntry.getKey();
			ArrayList<ArrayList<String>> value = (ArrayList<ArrayList<String>>) mapEntry.getValue();
	        
			String [] updatedrow = new String[7];
			
			updatedrow[2] =keyValue.get(0);
			updatedrow[3] =keyValue.get(1);
			updatedrow[4] =keyValue.get(2);
	        
			
			updatedrow[0] =getArrayValue(value.get(0));
			updatedrow[1] =getArrayValue(value.get(1));
			updatedrow[5] =getArrayValue(value.get(2));
			updatedrow[6] =getArrayValue(value.get(3));
	       
			csvwriter.writeNext(updatedrow);
		}
		csvwriter.close();
	}
}
