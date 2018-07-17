package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.DedupViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.CustomCSVWriter;

public class CreateReplaceReplacementFileForPrevDedup {

	private DedupViewComponents dedupViewComponents;
	private String fileName;
	private Map<String,ArrayList<String[]>> fileReaderMap;
	private String resultFileName ;
	
	public CreateReplaceReplacementFileForPrevDedup(DedupViewComponents dedupViewComponents) throws IOException 
	{
		
		this.fileName= dedupViewComponents.getFileToValidate().getText();
		fileReaderMap = new LinkedHashMap<String,ArrayList<String[]>>();
		this.dedupViewComponents = dedupViewComponents;
		String path = this.fileName;	
		
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		resultFileName=sub+"_NEW_SITEFIX.csv";
		//String logPath=sub+"_CONSOLE_LOGS.xml";
		readDedupCSV(this.fileName);
		writeSiteFixCSV(this.resultFileName);
		CommonUtils.createConsoleLogFile(dedupViewComponents);
		
	}
	
	private void addMapData(String key,String[] value)
	{
		if(fileReaderMap.containsKey(key))
		{
			ArrayList<String[]> existitng =fileReaderMap.get(key);
			existitng.add(value);
			fileReaderMap.put(key, existitng);
		}
		else
		{
			ArrayList<String[]> existitng =new ArrayList<String[]>();
			existitng.add(value);
			fileReaderMap.put(key, existitng);
		}
	}
	
	private void readDedupCSV(String filePath) throws IOException{
		
		CSVReader	csvReader = new CSVReader(new FileReader(filePath));
		String[] csvRow;
		while ((csvRow = csvReader.readNext()) != null) 
		{
			String GID = csvRow[0];
			
			String AM_RESPONSE = csvRow[1];
			String ICO = csvRow[2];
			String SITECODE = csvRow[22];
			String arr[] = new String[3];
			
			arr[0]=AM_RESPONSE;
			arr[1]=ICO;
			arr[2]=SITECODE;
			addMapData(GID,arr);
		}
	csvReader.close();
	}
	
	private void writeSiteFixCSV(String resultFileName) throws IOException
	{
		
		CustomCSVWriter siteFixWriter = new CustomCSVWriter(new FileWriter(resultFileName),true);
		
		ArrayList<String[]> writerData = new ArrayList<String[]>();

		String header[] = 	new String[]{"REPLACE_SITECODE","REPLACE_STATUS","REPLACEMENT_SITECODE","REPLACEMENT_STATUS","REPLACE_OSR","REPLACEMENT_OSR","MOVE_ORDERS"};
		siteFixWriter.writeNext(header);
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = fileReaderMap.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			ArrayList<String[]> value = entry.getValue();
			
			String TRUE_SITECODE="";
			
			for (int x = 0; x < value.size(); x++) 
			{
				String[] ARR = value.get(x);
				String amresponse=ARR[0].trim();
				String ico=ARR[1].trim();
				String sitecode=ARR[2].trim();
				
				if(amresponse.equalsIgnoreCase("TRUE"))
				{
					TRUE_SITECODE = sitecode;
				}
				
				if(ico.equalsIgnoreCase("683512") && amresponse.equalsIgnoreCase("FALSE"))
				{
				 String[] arr = new String[header.length];
				 arr[0]=sitecode;
				 arr[1]="1";
				 
				 arr[2]=TRUE_SITECODE;
				 arr[3]="0";
				 
				 arr[4]="";
				 arr[5]="";
				 arr[6]="ALL";
				
				 writerData.add(arr);
				 
				}
			}
		}
		siteFixWriter.writeAll(writerData);
		siteFixWriter.close();
		writerData = null;
		fileReaderMap = null;
		dedupViewComponents.getQueryResult().append("\n --------------------------------------------------------------------------------");
		dedupViewComponents.getQueryResult().append("\n SITEFIX FILE CREATED "+resultFileName);
		dedupViewComponents.getQueryResult().append("\n --------------------------------------------------------------------------------\n");
	}
}
