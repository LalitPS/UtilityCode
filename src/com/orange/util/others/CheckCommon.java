package com.orange.util.others;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;

import au.com.bytecode.opencsv.CSVReader;

public class CheckCommon {

	public static void main(String ad[]) {

		String firstFilePath = "C:/Lalit/Gold-Assignment/Order_GOLD_NOTIN_CSI/Book1.csv";
		String secondFilePath = "C:/Lalit/Gold-Assignment/Order_GOLD_NOTIN_CSI/Book2.csv";
		
		
			try {
				new CheckCommon(firstFilePath, secondFilePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	LinkedHashSet<String> firstHashSet = new LinkedHashSet<String>();

	LinkedHashSet<String> secondHashSet = new LinkedHashSet<String>();

	public CheckCommon(String firstfilePath, String secondfilePath)
			throws IOException {

		CSVReader first = new CSVReader(new FileReader(firstfilePath));
		CSVReader second = new CSVReader(new FileReader(secondfilePath));

		String row[] = null;

		while ((row = first.readNext()) != null) {
			firstHashSet.add(row[0]);
		}
        first.close();
		while ((row = second.readNext()) != null) {
			secondHashSet.add(row[0]);
		}
		second.close();
		Iterator<String> fitr = firstHashSet.iterator();
		Iterator<String> sitr = secondHashSet.iterator();

		if (firstHashSet.size() >= secondHashSet.size()) {
		
			while (fitr.hasNext()) {
				String data = fitr.next();
				if (!secondHashSet.contains(data)) {
					System.out.println(data);
				}
			}
		}

		else if (firstHashSet.size() <= secondHashSet.size()) {
			System.out.println("Not Available in " + firstfilePath);
			while (sitr.hasNext()) {
				String data = sitr.next();
				if (!firstHashSet.contains(data)) {
					System.out.println(data);
				}
			}
		}

	}
}
