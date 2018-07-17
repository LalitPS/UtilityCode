package com.orange.util.others;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

public class CheckUnique {

	public static void main(String ad[]) {

		
		String firstFilePath = "C:/Lalit/Gold-Assignment/Order_GOLD_NOTIN_CSI/Book1.csv";
		String secondFilePath = "C:/Lalit/Gold-Assignment/Order_GOLD_NOTIN_CSI/Book2.csv";

		try {
			new CheckUnique(firstFilePath, secondFilePath);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,e);
		}
	}
	LinkedHashMap<String,String[]> firstHashSet = new LinkedHashMap<String,String[]>();

	LinkedHashMap<String,String> secondHashSet = new LinkedHashMap<String,String>();

	public CheckUnique(String firstfilePath, String secondfilePath)
			throws IOException {

		CSVReader first = new CSVReader(new FileReader(firstfilePath));
		CSVReader second = new CSVReader(new FileReader(secondfilePath));

		String row[] = null;

		while ((row = first.readNext()) != null) {
			String arr[] = new String[5];
			arr[0] = row[1];
			arr[1] = row[2];
			arr[2] = row[3];
			arr[3] = row[4];
			arr[4] = row[5];
			firstHashSet.put(row[0],arr);
		}
        first.close();
		while ((row = second.readNext()) != null) {
			secondHashSet.put(row[0],row[0]);
		}
		second.close();
		
		Iterator<Map.Entry<String, String>> entries2 = secondHashSet.entrySet().iterator();

		if (firstHashSet.size() >= secondHashSet.size()) {
			
			while (entries2.hasNext()) {
				Map.Entry<String, String> data = entries2.next();
				String key = data.getKey();
				if (!firstHashSet.containsKey(key)) {
					
					System.out.println("NA,"+key +",NA,NA,NA,NA,NA");
				}
				
				if (firstHashSet.containsKey(key)) {
					String[] value = firstHashSet.get(key);
					System.out.println("Available,"+key +","+value[0]+","+value[1]+","+value[2]+","+value[3]+","+value[4]);
				}
			}
		}

		

	}
}
