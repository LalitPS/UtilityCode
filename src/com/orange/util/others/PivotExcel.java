package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class PivotExcel {

	public static void main(String ad[]){
		String readPath = "C:\\Lalit\\Gold-Assignment\\CSM\\Flowserve\\FlowserveSitesv0.3.csv";
		String writePath = "C:\\Lalit\\Gold-Assignment\\CSM\\Flowserve\\FlowserveSitesv0.3.1.X.csv";
		try {
			new PivotExcel(readPath,writePath);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,e);
		}
		System.out.println("Completed...");
	}
	LinkedHashMap<String, String[]> csvData = new LinkedHashMap<String, String[]>();
	CSVReader csvReader;
	
	CustomCSVWriter writer ;
	public PivotExcel(String readCsvFilePath,String writeCsvFilePath) throws IOException{
		csvReader = new CSVReader(new FileReader(readCsvFilePath));
		writer = new CustomCSVWriter(new FileWriter(writeCsvFilePath),true);
		readExcel(csvReader);
		writeExcel(writer);
		
	}
	
	private int getOrderNumber(String order){
		int ON = -1;
		try{
		ON = Integer.parseInt(order);
		}catch(Exception e){}
		return ON;
	}
	private void readExcel(CSVReader csvReader) throws IOException{
		String[] csvRow;
		writer.writeNext(csvReader.readNext());
			while ((csvRow = csvReader.readNext()) != null) {
				String key = csvRow[3];
				if(!csvData.containsKey(key))
				{
				csvData.put(key, csvRow);
				}
				else{
					String row [] = csvData.get(key);
					
					/*
					 * Get the biggest 
					 */
					int orderNumber = getOrderNumber(row[0]);
					int newOrderNumber = getOrderNumber(csvRow[0]);
					if(newOrderNumber  > orderNumber){
					//	row  = csvRow;
					//	csvData.put(key, csvRow);
					}
					
					/*
					 * Combined All..
					 * 
					 */
					if(newOrderNumber  > 0){
						String OldOrders = row[0];
						System.out.println("OldOrders" +OldOrders  +" newOrderNumber "+newOrderNumber);
						OldOrders+=  "/"+csvRow[0];
						row[0] = OldOrders;
						csvData.put(key, row);
					}
					
				}
			}
			csvReader.close();

	}
	
	private void writeExcel(CustomCSVWriter writer) throws IOException{
		for (String osr : csvData.keySet()) {
			writer.writeNext(csvData.get(osr));
		}
		writer.close();
	}
}
