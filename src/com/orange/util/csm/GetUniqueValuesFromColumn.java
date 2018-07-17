package com.orange.util.csm;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class GetUniqueValuesFromColumn {
	private CustomCSVWriter writer ;
	
	public static void main(String[] ar) throws IOException{
			String readerFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\Test\\Book1.csv";
			String writerFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\Test\\Book1Updated.csv";
			new GetUniqueValuesFromColumn(readerFilePath,writerFilePath);
	}
	public GetUniqueValuesFromColumn(String fn,String rp ) throws IOException{
		
		CSVReader reader = new CSVReader(new FileReader(fn));
		String row[] = reader.readNext();
		writer = new CustomCSVWriter(new FileWriter(rp),true);
		writer.writeNext(row);

		ArrayList<String> firstColList = new ArrayList<String>();
		ArrayList<String> secondColList = new ArrayList<String>();
		
		while ((row = reader.readNext()) != null) 
		{
			
			String firstCol = row[0];
			String secCol = row[1];
			firstColList.add(firstCol);
			secondColList.add(secCol);
			
		}
		reader.close();
		
		System.out.println("Reading completing");
		
		Set<String> firstColSet = new LinkedHashSet<String>(firstColList);
		Set<String> secondColSet = new LinkedHashSet<String>(secondColList);
		firstColList.clear();
		secondColList.clear();
		firstColList.addAll(firstColSet);
		secondColList.addAll(secondColSet);
		
		System.out.println("Remove Duplicates");
		
		System.out.println("Analyze Column");
		
		if(firstColList.size()>=secondColList.size())
		{
			
			for(int x = 0; x< firstColList.size(); x++)
			{
				String val = firstColList.get(x);
				
				String[] newrow = new String[3];
				newrow[0] = val;
				if(x<secondColList.size()){
				newrow[1] = secondColList.get(x);
				}
				else{
					newrow[1] = "";	
				}
				
				if(secondColList.contains(val))
				{
					
					newrow[2] = "Found in second col";
					
				}
				else
				{
					newrow[2] = "Not FOUND in second col";
				}
				
				writer.writeNext(newrow);
			}
			writer.close();
		}
		if(firstColList.size()<secondColList.size())
		{
			
			for(int x = 0; x< secondColList.size(); x++)
			{
				String val = secondColList.get(x);
				
				String[] newrow = new String[3];
				newrow[0] = val;
				if(x<firstColList.size()){
				newrow[1] = firstColList.get(x);
				}
				else{
					newrow[1] = "";
				}
				if(firstColList.contains(val))
				{
					
					newrow[2] = "Found in first col";
					
				}
				else
				{
					newrow[2] = "Not FOUND in first col";
				}
				
				writer.writeNext(newrow);
			}
			writer.close();
		}
		System.out.println("Analysis completed...");
	}
}
